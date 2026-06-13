package royale.util.rpc;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import de.jcm.discordgamesdk.ActivityManager;
import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.DiscordEventAdapter;
import de.jcm.discordgamesdk.LogLevel;
import de.jcm.discordgamesdk.activity.Activity;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DiscordRpcService {
    private static final Logger LOGGER = LoggerFactory.getLogger("royale/DiscordRpc");
    private static final DiscordRpcService INSTANCE = new DiscordRpcService();

    private static final int OP_HANDSHAKE = 0;
    private static final int OP_FRAME = 1;
    private static final int OP_CLOSE = 2;

    private static final long RECONNECT_DELAY_MS = 2500L;
    private static final long AFK_TIMEOUT_MS = 60000L;
    private static final long PVP_TIMEOUT_MS = 30000L;
    private static final long RESEND_INTERVAL_MS = 5000L;
    private static final long GAMESDK_UPDATE_TIMEOUT_MS = 6000L;
    private static final long IPC_CLEAR_DELAY_MS = 30L;
    private static final int MAX_DISCORD_IPC_PIPES = 10;
    private static final Pattern DISCORD_APP_ID_PATTERN = Pattern.compile("(?<!\\d)\\d{17,22}(?!\\d)");
    private static final Pattern IPV4_PATTERN = Pattern.compile("^(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}$");
    private static final Pattern IPV6_PATTERN = Pattern.compile("^[0-9a-fA-F:]+$");
    private static final Pattern PORT_SUFFIX_PATTERN = Pattern.compile(":(\\d{1,5})$");

    private static final String DEFAULT_APPLICATION_ID = "1475962037493563452";
    private static final String LEGACY_APPLICATION_ID = "1462442165392380175";
    private static final String DEFAULT_LARGE_IMAGE_KEY = "logo";
    private static final String DEFAULT_LARGE_IMAGE_TEXT = "";
    private static final String DEFAULT_SMALL_IMAGE_KEY = "minecraft";
    private static final String RPC_USE_GAMESDK_PROPERTY = "royalemaster.rpc.useGameSdk";
    private static final String RPC_USE_GAMESDK_ENV = "ROYALEMASTER_RPC_USE_GAMESDK";

    private final Object lock = new Object();
    private final ExecutorService clearExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "royale-master-rpc-clear");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicBoolean clearTaskQueued = new AtomicBoolean(false);
    private final AtomicBoolean stopInProgress = new AtomicBoolean(false);

    private RandomAccessFile pipe;
    private boolean connected;
    private boolean running;
    private long nextReconnectAt;
    private long sessionStartEpochSec;
    private long combatUntilMs;
    private long lastActivityMs;
    private long lastSentAt;
    private String lastSignature = "";
    private String applicationId = "";
    private String largeImageKey = DEFAULT_LARGE_IMAGE_KEY;
    private String largeImageText = DEFAULT_LARGE_IMAGE_TEXT;
    private boolean shutdownHookRegistered;
    private Core gameSdkCore;
    private CreateParams gameSdkParams;
    private ActivityManager gameSdkActivityManager;
    private boolean gameSdkMode;
    private int gameSdkFailureCount;
    private boolean gameSdkUpdateInFlight;
    private long gameSdkUpdateStartedAt;
    private String gameSdkPendingSignature = "";
    private String pendingServerAddress = "";

    private DiscordRpcService() {
    }

    public static DiscordRpcService getInstance() {
        return INSTANCE;
    }

    public void start() {
        boolean mustReset;
        synchronized (this.lock) {
            mustReset = this.running || this.connected || this.pipe != null;
        }
        if (mustReset) {
            stop();
        }

        RpcAutoConfig autoConfig = resolveAutoConfig();
        String appId = safe(autoConfig.appId());
        String imageKey = safe(autoConfig.imageKey());
        String imageText = safe(autoConfig.imageText());

        // Clear stale rich presence from previous sessions before reconnecting.
        quickClearPreviousPresence(appId);

        if (shouldUseGameSdk() && tryStartGameSdk(appId, imageKey, imageText)) {
            LOGGER.info("RPC started in GameSDK mode");
            return;
        }

        synchronized (this.lock) {
            registerShutdownHookIfNeeded();
            this.applicationId = appId;
            this.largeImageKey = imageKey;
            this.largeImageText = imageText;
            this.running = true;
            this.connected = false;
            this.nextReconnectAt = 0L;
            this.sessionStartEpochSec = Instant.now().getEpochSecond();
            this.combatUntilMs = 0L;
            this.lastActivityMs = System.currentTimeMillis();
            this.lastSentAt = 0L;
            this.lastSignature = "";
            this.pendingServerAddress = "";
            this.gameSdkUpdateInFlight = false;
            this.gameSdkUpdateStartedAt = 0L;
            this.gameSdkPendingSignature = "";
            this.gameSdkMode = false;
            this.gameSdkFailureCount = 0;
        }
        LOGGER.info("RPC started in IPC mode");
    }

    public void restart() {
        stop();
        start();
    }

    public boolean isRunning() {
        synchronized (this.lock) {
            return this.running;
        }
    }

    public void stop() {
        if (!this.stopInProgress.compareAndSet(false, true)) {
            return;
        }

        try {
            if (stopGameSdkIfActive()) {
                return;
            }

            String appIdSnapshot;
            RandomAccessFile localPipe;
            boolean hadActiveState;
            List<String> clearClientIds;

            synchronized (this.lock) {
                hadActiveState = this.running || this.connected || this.pipe != null;
                appIdSnapshot = this.applicationId;
                this.running = false;
                this.connected = false;
                this.combatUntilMs = 0L;
                this.nextReconnectAt = 0L;
                this.lastSentAt = 0L;
                this.lastSignature = "";
                this.sessionStartEpochSec = 0L;
                this.lastActivityMs = 0L;
                this.pendingServerAddress = "";
                this.gameSdkUpdateInFlight = false;
                this.gameSdkUpdateStartedAt = 0L;
                this.gameSdkPendingSignature = "";
                this.applicationId = DEFAULT_APPLICATION_ID;
                this.largeImageKey = DEFAULT_LARGE_IMAGE_KEY;
                this.largeImageText = DEFAULT_LARGE_IMAGE_TEXT;
                localPipe = this.pipe;
                this.pipe = null;
                clearClientIds = hadActiveState ? collectClearClientIds(appIdSnapshot) : List.of();
            }

            if (localPipe != null) {
                try {
                    clearPresenceOnCurrentPipe(localPipe);
                } catch (Exception ignored) {
                }
                try {
                    closePipeQuietly(localPipe);
                } catch (Exception ignored) {
                }
            }
            if (hadActiveState) {
                try {
                    clearPresenceSynchronously(clearClientIds, false);
                } catch (Exception ignored) {
                }
                this.clearTaskQueued.set(false);
                scheduleAsyncPresenceClear(clearClientIds);
            }
        } finally {
            this.stopInProgress.set(false);
        }
        LOGGER.info("RPC stopped");
    }

    public void markCombat() {
        synchronized (this.lock) {
            this.combatUntilMs = System.currentTimeMillis() + PVP_TIMEOUT_MS;
        }
    }

    public void markActivity() {
        synchronized (this.lock) {
            this.lastActivityMs = System.currentTimeMillis();
        }
    }

    public void forceRefresh() {
        synchronized (this.lock) {
            this.lastSignature = "";
            this.lastSentAt = 0L;
            this.gameSdkPendingSignature = "";
            this.lastActivityMs = System.currentTimeMillis();
        }
    }

    public void setPendingServerAddress(String rawAddress) {
        synchronized (this.lock) {
            this.pendingServerAddress = sanitizeServerAddress(rawAddress);
            this.lastSignature = "";
            this.lastSentAt = 0L;
        }
    }

    public void clearPendingServerAddress() {
        synchronized (this.lock) {
            this.pendingServerAddress = "";
            this.lastSignature = "";
            this.lastSentAt = 0L;
        }
    }

    public void tick() {
        if (this.gameSdkMode) {
            tickGameSdk();
            return;
        }

        synchronized (this.lock) {
            if (!this.running) {
                return;
            }

            if (!isValidApplicationId(this.applicationId)) {
                this.applicationId = DEFAULT_APPLICATION_ID;
                if (this.connected) {
                    closePipe();
                    this.connected = false;
                }
            }

            long now = System.currentTimeMillis();
            if (!this.connected) {
                if (now < this.nextReconnectAt) {
                    return;
                }

                if (!connect()) {
                    this.nextReconnectAt = now + RECONNECT_DELAY_MS;
                    return;
                }
            }

            PresenceContext context = resolvePresenceContext(now);
            String details = context.details();
            String state = buildState(context);
            String signature = details + "|" + state + "|" + this.largeImageKey + "|" + this.largeImageText + "|" + context.status();
            if (signature.equals(this.lastSignature) && now - this.lastSentAt < RESEND_INTERVAL_MS) {
                return;
            }

            JsonObject activity = new JsonObject();
            activity.addProperty("details", limit(details, 128));
            activity.addProperty("state", limit(state, 128));

            JsonObject timestamps = new JsonObject();
            timestamps.addProperty("start", this.sessionStartEpochSec);
            activity.add("timestamps", timestamps);

            JsonObject assets = new JsonObject();
            if (!this.largeImageKey.isBlank()) {
                assets.addProperty("large_image", limit(this.largeImageKey, 32));
            }
            if (!this.largeImageText.isBlank()) {
                assets.addProperty("large_text", limit(this.largeImageText, 128));
            }
            assets.addProperty("small_image", DEFAULT_SMALL_IMAGE_KEY);
            assets.addProperty("small_text", limit("Minecraft | " + context.status(), 128));
            activity.add("assets", assets);

            if (!sendFrame(buildSetActivityPayload(activity))) {
                closePipe();
                this.connected = false;
                this.nextReconnectAt = now + RECONNECT_DELAY_MS;
                return;
            }

            this.lastSignature = signature;
            this.lastSentAt = now;
            LOGGER.info("RPC IPC update: {}", limit(details, 120));
        }
    }

    private RpcAutoConfig resolveAutoConfig() {
        String appId = normalizeApplicationId(firstNonBlank(
                System.getProperty("royalemaster.rpc.appId"),
                System.getenv("ROYALEMASTER_RPC_APP_ID"),
                DEFAULT_APPLICATION_ID
        ));

        String imageKey = normalizeImageKey(firstNonBlank(
                System.getProperty("royalemaster.rpc.imageKey"),
                System.getenv("ROYALEMASTER_RPC_IMAGE_KEY"),
                DEFAULT_LARGE_IMAGE_KEY
        ));

        String imageText = normalizeImageText(firstNonBlank(
                System.getProperty("royalemaster.rpc.imageText"),
                System.getenv("ROYALEMASTER_RPC_IMAGE_TEXT"),
                DEFAULT_LARGE_IMAGE_TEXT
        ));

        return new RpcAutoConfig(appId, imageKey, imageText);
    }
    private String normalizeApplicationId(String candidate) {
        String normalized = safe(candidate);
        if (isValidApplicationId(normalized)) {
            return normalized;
        }
        return DEFAULT_APPLICATION_ID;
    }

    private Path getAutoConfigDirectory() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.runDirectory != null) {
            return mc.runDirectory.toPath().resolve("Royale").resolve("configs").resolve("autocfg");
        }
        return null;
    }

    private boolean connect() {
        List<String> candidateClientIds = collectConnectClientIds(this.applicationId);
        for (int i = 0; i < MAX_DISCORD_IPC_PIPES; i++) {
            String pipeName = "\\\\.\\pipe\\discord-ipc-" + i;
            for (String candidateClientId : candidateClientIds) {
                if (!isValidApplicationId(candidateClientId)) {
                    continue;
                }

                RandomAccessFile freshPipe = null;
                try {
                    freshPipe = new RandomAccessFile(pipeName, "rw");
                    writeHandshake(freshPipe, candidateClientId);
                    sleepSilently(IPC_CLEAR_DELAY_MS);
                    writePacketTo(freshPipe, OP_FRAME, buildSetActivityPayload(JsonNull.INSTANCE));

                    this.pipe = freshPipe;
                    this.applicationId = candidateClientId;
                    this.connected = true;
                    return true;
                } catch (Exception ignored) {
                    closePipeQuietly(freshPipe);
                }
            }
        }

        LOGGER.debug("Discord IPC pipe not found. Waiting for Discord...");
        return false;
    }

    private List<String> collectConnectClientIds(String preferredAppId) {
        LinkedHashSet<String> ids = new LinkedHashSet<>();

        if (isValidApplicationId(preferredAppId)) {
            ids.add(preferredAppId);
        }
        if (isValidApplicationId(DEFAULT_APPLICATION_ID)) {
            ids.add(DEFAULT_APPLICATION_ID);
        }
        if (isValidApplicationId(LEGACY_APPLICATION_ID)) {
            ids.add(LEGACY_APPLICATION_ID);
        }

        return new ArrayList<>(ids);
    }

    private boolean tryStartGameSdk(String appId, String imageKey, String imageText) {
        synchronized (this.lock) {
            registerShutdownHookIfNeeded();
            this.applicationId = normalizeApplicationId(appId);
            this.largeImageKey = normalizeImageKey(imageKey);
            this.largeImageText = normalizeImageText(imageText);
            this.running = true;
            this.connected = false;
            this.nextReconnectAt = 0L;
            this.sessionStartEpochSec = Instant.now().getEpochSecond();
            this.combatUntilMs = 0L;
            this.lastActivityMs = System.currentTimeMillis();
            this.lastSentAt = 0L;
            this.lastSignature = "";
            this.gameSdkMode = true;
            this.gameSdkFailureCount = 0;
            this.gameSdkUpdateInFlight = false;
            this.gameSdkUpdateStartedAt = 0L;
            this.gameSdkPendingSignature = "";
        }

        boolean connected = false;
        try {
            connected = connectGameSdk();
        } catch (Exception ignored) {
        }
        if (connected) {
            return true;
        }

        synchronized (this.lock) {
            closeGameSdkHandles(this.gameSdkCore, this.gameSdkParams);
            this.gameSdkCore = null;
            this.gameSdkParams = null;
            this.gameSdkActivityManager = null;
            this.gameSdkMode = false;
            this.connected = false;
            this.gameSdkFailureCount = 0;
            this.gameSdkUpdateInFlight = false;
            this.gameSdkUpdateStartedAt = 0L;
            this.gameSdkPendingSignature = "";
        }
        return false;
    }

    private boolean connectGameSdk() {
        String appId;
        synchronized (this.lock) {
            if (!this.running || !this.gameSdkMode) {
                return false;
            }
            if (this.gameSdkCore != null && this.gameSdkActivityManager != null) {
                this.connected = true;
                return true;
            }
            appId = this.applicationId;
        }

        long clientId;
        try {
            clientId = Long.parseLong(normalizeApplicationId(appId));
        } catch (Exception parseError) {
            synchronized (this.lock) {
                this.connected = false;
                this.nextReconnectAt = System.currentTimeMillis() + RECONNECT_DELAY_MS;
            }
            return false;
        }

        CreateParams params = null;
        Core core = null;
        ActivityManager manager = null;
        try {
            params = new CreateParams();
            params.setClientID(clientId);
            params.setFlags(CreateParams.Flags.NO_REQUIRE_DISCORD, CreateParams.Flags.SUPPRESS_EXCEPTIONS);
            params.registerEventHandler(new DiscordEventAdapter() {
            });

            core = new Core(params);
            core.setLogHook(LogLevel.WARN, (level, message) -> LOGGER.debug("Discord GameSDK {}: {}", level, message));
            if (!core.isDiscordRunning()) {
                closeGameSdkHandles(core, params);
                synchronized (this.lock) {
                    this.connected = false;
                    this.nextReconnectAt = System.currentTimeMillis() + RECONNECT_DELAY_MS;
                }
                return false;
            }

            manager = core.activityManager();
            try {
                manager.clearActivity();
            } catch (Exception ignored) {
            }
            core.runCallbacks();

            synchronized (this.lock) {
                if (!this.running || !this.gameSdkMode) {
                    closeGameSdkHandles(core, params);
                    return false;
                }
                this.gameSdkCore = core;
                this.gameSdkParams = params;
                this.gameSdkActivityManager = manager;
                this.connected = true;
                this.nextReconnectAt = 0L;
                this.gameSdkUpdateInFlight = false;
                this.gameSdkUpdateStartedAt = 0L;
                this.gameSdkPendingSignature = "";
                return true;
            }
        } catch (Exception connectError) {
            closeGameSdkHandles(core, params);
            synchronized (this.lock) {
                this.connected = false;
                this.nextReconnectAt = System.currentTimeMillis() + RECONNECT_DELAY_MS;
                this.gameSdkUpdateInFlight = false;
                this.gameSdkUpdateStartedAt = 0L;
                this.gameSdkPendingSignature = "";
            }
            LOGGER.debug("Discord GameSDK connect failed: {}", connectError.getMessage());
            return false;
        }
    }

    private boolean stopGameSdkIfActive() {
        Core core;
        CreateParams params;
        ActivityManager manager;
        boolean hadState;
        String appIdSnapshot;
        List<String> clearClientIds;

        synchronized (this.lock) {
            if (!this.gameSdkMode) {
                return false;
            }

            hadState = this.running || this.connected || this.gameSdkCore != null;
            appIdSnapshot = this.applicationId;
            this.running = false;
            this.connected = false;
            this.combatUntilMs = 0L;
            this.nextReconnectAt = 0L;
            this.lastSentAt = 0L;
            this.lastSignature = "";
            this.sessionStartEpochSec = 0L;
            this.lastActivityMs = 0L;
            this.pendingServerAddress = "";
            this.gameSdkUpdateInFlight = false;
            this.gameSdkUpdateStartedAt = 0L;
            this.gameSdkPendingSignature = "";
            this.applicationId = DEFAULT_APPLICATION_ID;
            this.largeImageKey = DEFAULT_LARGE_IMAGE_KEY;
            this.largeImageText = DEFAULT_LARGE_IMAGE_TEXT;
            clearClientIds = hadState ? collectClearClientIds(appIdSnapshot) : List.of();

            core = this.gameSdkCore;
            params = this.gameSdkParams;
            manager = this.gameSdkActivityManager;
            this.gameSdkCore = null;
            this.gameSdkParams = null;
            this.gameSdkActivityManager = null;
            this.gameSdkMode = false;
        }

        if (hadState && manager != null) {
            clearGameSdkPresence(manager, core);
        }

        if (core != null) {
            try {
                core.runCallbacks();
            } catch (Exception ignored) {
            }
        }
        sleepSilently(25L);
        closeGameSdkHandles(core, params);
        if (hadState) {
            try {
                clearPresenceSynchronously(clearClientIds, false);
            } catch (Exception ignored) {
            }
            this.clearTaskQueued.set(false);
            scheduleAsyncPresenceClear(clearClientIds);
        }
        return true;
    }

    private void tickGameSdk() {
        long now = System.currentTimeMillis();
        Core core;
        ActivityManager manager;
        long sessionStart;
        String imageKey;
        String imageText;

        synchronized (this.lock) {
            if (!this.running || !this.gameSdkMode) {
                return;
            }
            if (this.gameSdkCore == null || this.gameSdkActivityManager == null) {
                if (now < this.nextReconnectAt) {
                    return;
                }
            }
        }

        if (!connectGameSdk()) {
            return;
        }

        synchronized (this.lock) {
            core = this.gameSdkCore;
            manager = this.gameSdkActivityManager;
            sessionStart = this.sessionStartEpochSec;
            imageKey = this.largeImageKey;
            imageText = this.largeImageText;
        }
        if (core == null || manager == null) {
            return;
        }

        try {
            core.runCallbacks();
        } catch (Exception callbacksError) {
            synchronized (this.lock) {
                closeGameSdkHandles(this.gameSdkCore, this.gameSdkParams);
                this.gameSdkCore = null;
                this.gameSdkParams = null;
                this.gameSdkActivityManager = null;
                this.connected = false;
                this.nextReconnectAt = now + RECONNECT_DELAY_MS;
                this.gameSdkFailureCount++;
                this.gameSdkUpdateInFlight = false;
                this.gameSdkUpdateStartedAt = 0L;
                this.gameSdkPendingSignature = "";
                if (this.gameSdkFailureCount >= 3) {
                    this.gameSdkMode = false;
                    this.gameSdkFailureCount = 0;
                    LOGGER.warn("Discord GameSDK callbacks failed repeatedly, switched to IPC fallback");
                }
            }
            return;
        }

        PresenceContext context = resolvePresenceContext(now);
        String details = context.details();
        String state = buildState(context);
        String signature = details + "|" + state + "|" + imageKey + "|" + imageText + "|" + context.status();

        synchronized (this.lock) {
            if (this.gameSdkUpdateInFlight) {
                if (now - this.gameSdkUpdateStartedAt > GAMESDK_UPDATE_TIMEOUT_MS) {
                    LOGGER.warn("Discord GameSDK update callback timed out, reconnecting bridge");
                    closeGameSdkHandles(this.gameSdkCore, this.gameSdkParams);
                    this.gameSdkCore = null;
                    this.gameSdkParams = null;
                    this.gameSdkActivityManager = null;
                    this.connected = false;
                    this.nextReconnectAt = now + RECONNECT_DELAY_MS;
                    this.gameSdkFailureCount++;
                    this.gameSdkUpdateInFlight = false;
                    this.gameSdkUpdateStartedAt = 0L;
                    this.gameSdkPendingSignature = "";
                    if (this.gameSdkFailureCount >= 2) {
                        this.gameSdkMode = false;
                        this.gameSdkFailureCount = 0;
                        LOGGER.warn("Discord GameSDK is unstable, switched to IPC fallback");
                    }
                }
                return;
            }
            if (signature.equals(this.lastSignature) && now - this.lastSentAt < RESEND_INTERVAL_MS) {
                return;
            }
            this.gameSdkUpdateInFlight = true;
            this.gameSdkUpdateStartedAt = now;
            this.gameSdkPendingSignature = signature;
        }

        try (Activity activity = new Activity()) {
            activity.setDetails(limit(details, 128));
            activity.setState(limit(state, 128));
            activity.timestamps().setStart(Instant.ofEpochSecond(sessionStart));
            if (!imageKey.isBlank()) {
                activity.assets().setLargeImage(limit(imageKey, 32));
            }
            if (!imageText.isBlank()) {
                activity.assets().setLargeText(limit(imageText, 128));
            }
            activity.assets().setSmallImage(DEFAULT_SMALL_IMAGE_KEY);
            activity.assets().setSmallText(limit("Minecraft | " + context.status(), 128));

            final String finalSignature = signature;
            final long finalNow = now;
            manager.updateActivity(activity, result -> {
                synchronized (this.lock) {
                    this.gameSdkUpdateInFlight = false;
                    this.gameSdkUpdateStartedAt = 0L;
                    this.gameSdkPendingSignature = "";
                    if (!this.running || !this.gameSdkMode) {
                        return;
                    }

                    if (result == null || result == de.jcm.discordgamesdk.Result.OK) {
                        this.lastSignature = finalSignature;
                        this.lastSentAt = finalNow;
                        this.gameSdkFailureCount = 0;
                        LOGGER.info("RPC GameSDK update: {}", limit(details, 120));
                        return;
                    }

                    LOGGER.debug("Discord GameSDK updateActivity result: {}", result);
                    this.connected = false;
                    this.nextReconnectAt = System.currentTimeMillis() + RECONNECT_DELAY_MS;
                    this.gameSdkFailureCount++;
                    closeGameSdkHandles(this.gameSdkCore, this.gameSdkParams);
                    this.gameSdkCore = null;
                    this.gameSdkParams = null;
                    this.gameSdkActivityManager = null;
                    if (this.gameSdkFailureCount >= 3) {
                        this.gameSdkMode = false;
                        this.gameSdkFailureCount = 0;
                        LOGGER.warn("Discord GameSDK is unstable, switched to IPC fallback");
                    }
                }
            });
        } catch (Exception updateError) {
            synchronized (this.lock) {
                closeGameSdkHandles(this.gameSdkCore, this.gameSdkParams);
                this.gameSdkCore = null;
                this.gameSdkParams = null;
                this.gameSdkActivityManager = null;
                this.connected = false;
                this.nextReconnectAt = now + RECONNECT_DELAY_MS;
                this.gameSdkFailureCount++;
                this.gameSdkUpdateInFlight = false;
                this.gameSdkUpdateStartedAt = 0L;
                this.gameSdkPendingSignature = "";
                if (this.gameSdkFailureCount >= 3) {
                    this.gameSdkMode = false;
                    this.gameSdkFailureCount = 0;
                    LOGGER.warn("Discord GameSDK threw repeatedly, switched to IPC fallback");
                }
            }
        }
    }

    private void closeGameSdkHandles(Core core, CreateParams params) {
        if (core != null) {
            try {
                core.close();
            } catch (Exception ignored) {
            }
        }
        if (params != null) {
            try {
                params.close();
            } catch (Exception ignored) {
            }
        }
    }

    private void clearGameSdkPresence(ActivityManager manager, Core core) {
        if (manager == null) {
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        try {
            manager.clearActivity(result -> latch.countDown());
        } catch (Exception ignored) {
            latch.countDown();
        }

        drainGameSdkCallbacks(core, latch, 220L);

        try {
            manager.clearActivity();
        } catch (Exception ignored) {
        }

        if (core != null) {
            try {
                core.runCallbacks();
            } catch (Exception ignored) {
            }
        }
        sleepSilently(35L);
    }

    private void drainGameSdkCallbacks(Core core, CountDownLatch latch, long timeoutMs) {
        if (latch == null) {
            return;
        }

        long deadline = System.currentTimeMillis() + Math.max(32L, timeoutMs);
        while (latch.getCount() > 0L && System.currentTimeMillis() < deadline) {
            if (core != null) {
                try {
                    core.runCallbacks();
                } catch (Exception ignored) {
                }
            }
            sleepSilently(12L);
        }
    }

    private String buildSetActivityPayload(JsonElement activityElement) {
        JsonObject root = new JsonObject();
        root.addProperty("cmd", "SET_ACTIVITY");
        root.addProperty("nonce", UUID.randomUUID().toString());

        JsonObject args = new JsonObject();
        args.addProperty("pid", ProcessHandle.current().pid());
        args.add("activity", activityElement);

        root.add("args", args);
        return root.toString();
    }

    private boolean sendFrame(String payload) {
        try {
            writePacket(OP_FRAME, payload);
            return true;
        } catch (Exception e) {
            LOGGER.debug("Failed to send Discord RPC frame: {}", e.getMessage());
            return false;
        }
    }

    private void writePacket(int op, String payload) throws IOException {
        if (this.pipe == null) {
            throw new IOException("Discord pipe is not connected");
        }
        writePacketTo(this.pipe, op, payload);
    }

    private void writePacketTo(RandomAccessFile targetPipe, int op, String payload) throws IOException {
        if (targetPipe == null) {
            throw new IOException("Discord pipe is not connected");
        }

        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        ByteBuffer header = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        header.putInt(op);
        header.putInt(bytes.length);

        targetPipe.write(header.array());
        targetPipe.write(bytes);
    }

    private void writeHandshake(RandomAccessFile targetPipe, String clientId) throws IOException {
        JsonObject payload = new JsonObject();
        payload.addProperty("v", 1);
        payload.addProperty("client_id", normalizeApplicationId(clientId));
        writePacketTo(targetPipe, OP_HANDSHAKE, payload.toString());
    }

    private void sendCloseFrame(RandomAccessFile targetPipe) {
        if (targetPipe == null) {
            return;
        }

        try {
            JsonObject close = new JsonObject();
            close.addProperty("code", 1000);
            close.addProperty("message", "close");
            writePacketTo(targetPipe, OP_CLOSE, close.toString());
        } catch (Exception ignored) {
        }
    }

    private PresenceContext resolvePresenceContext(long nowMs) {
        MinecraftClient mc = MinecraftClient.getInstance();
        String nickname = resolveNickname();
        String serverAddress = resolveServerAddress();
        String status = resolveStatus(nowMs);

        String details;
        if (isInWorld(mc)) {
            if (isMultiplayerSession(mc)) {
                details = serverAddress.isBlank()
                        ? nickname + " - \u0421\u0435\u0440\u0432\u0435\u0440"
                        : nickname + " - " + serverAddress;
            } else {
                details = nickname + " - \u041E\u0434\u0438\u043D\u043E\u0447\u043D\u0430\u044F \u0438\u0433\u0440\u0430";
            }
        } else {
            details = nickname;
        }

        return new PresenceContext(details, status);
    }

    private String buildState(PresenceContext context) {
        return "Status: " + context.status();
    }

    private String resolveNickname() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) {
            return "Player";
        }

        if (mc.player != null && mc.player.getName() != null) {
            String playerName = safe(mc.player.getName().getString());
            if (!playerName.isBlank()) {
                return playerName;
            }
        }

        if (mc.getSession() != null) {
            String sessionName = safe(mc.getSession().getUsername());
            if (!sessionName.isBlank()) {
                return sessionName;
            }
        }

        return "Player";
    }

    private String resolveServerAddress() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!isMultiplayerSession(mc)) {
            this.pendingServerAddress = "";
            return "";
        }

        if (mc.getNetworkHandler() != null
                && mc.getNetworkHandler().getConnection() != null
                && mc.getNetworkHandler().getConnection().getAddress() != null) {
            SocketAddress socketAddress = mc.getNetworkHandler().getConnection().getAddress();
            String liveAddress = sanitizeSocketAddress(socketAddress);
            if (!liveAddress.isBlank()) {
                this.pendingServerAddress = liveAddress;
                return liveAddress;
            }
        }

        if (mc.getNetworkHandler() != null && mc.getNetworkHandler().getServerInfo() != null) {
            ServerInfo info = mc.getNetworkHandler().getServerInfo();
            String serverAddress = sanitizeServerAddress(info.address);
            if (!serverAddress.isBlank()) {
                this.pendingServerAddress = serverAddress;
                return serverAddress;
            }
        }

        ServerInfo info = mc.getCurrentServerEntry();
        if (info != null) {
            String serverAddress = sanitizeServerAddress(info.address);
            if (!serverAddress.isBlank()) {
                this.pendingServerAddress = serverAddress;
                return serverAddress;
            }
        }

        return this.pendingServerAddress;
    }

    private String sanitizeServerAddress(String rawAddress) {
        String host = extractHost(rawAddress);
        if (host.isBlank() || looksNumericAddress(host)) {
            return DEFAULT_LARGE_IMAGE_TEXT;
        }
        return limit(host, 64);
    }

    private String sanitizeSocketAddress(SocketAddress socketAddress) {
        if (socketAddress == null) {
            return DEFAULT_LARGE_IMAGE_TEXT;
        }

        if (socketAddress instanceof InetSocketAddress inet) {
            String hostString = safe(inet.getHostString());
            if (!hostString.isBlank() && !looksNumericAddress(hostString)) {
                return limit(hostString, 64);
            }
        }

        return sanitizeServerAddress(socketAddress.toString());
    }

    private String extractHost(String rawAddress) {
        String value = safe(rawAddress);
        if (value.isBlank()) {
            return DEFAULT_LARGE_IMAGE_TEXT;
        }

        int slashIndex = value.indexOf('/');
        if (slashIndex >= 0) {
            String left = safe(value.substring(0, slashIndex));
            String right = safe(value.substring(slashIndex + 1));
            value = !left.isBlank() ? left : right;
        }

        if (value.startsWith("/")) {
            value = safe(value.substring(1));
        }

        if (value.startsWith("[")) {
            int endBracket = value.indexOf(']');
            if (endBracket > 1) {
                return safe(value.substring(1, endBracket));
            }
        }

        if (value.indexOf(':') == value.lastIndexOf(':')) {
            Matcher portMatcher = PORT_SUFFIX_PATTERN.matcher(value);
            if (portMatcher.find()) {
                value = value.substring(0, portMatcher.start());
            }
        }

        return safe(value);
    }

    private boolean looksNumericAddress(String value) {
        String normalized = safe(value);
        if (normalized.isBlank()) {
            return true;
        }

        if (IPV4_PATTERN.matcher(normalized).matches()) {
            return true;
        }

        return normalized.contains(":") && IPV6_PATTERN.matcher(normalized).matches();
    }

    private String resolveStatus(long nowMs) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) {
            return "\u0417\u0430\u043f\u0443\u0441\u043a";
        }
        if (isConnectingScreen(mc.currentScreen)) {
            return "\u041f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0435";
        }
        if (!isInWorld(mc)) {
            return mc.currentScreen != null
                    ? "\u0412 \u043c\u0435\u043d\u044e"
                    : "\u041e\u0436\u0438\u0434\u0430\u043d\u0438\u0435";
        }
        if (nowMs <= this.combatUntilMs) {
            return "\u0412 PvP";
        }
        if (mc.player != null && !mc.player.isAlive()) {
            return "\u0421\u043c\u0435\u0440\u0442\u044c";
        }
        if (mc.isPaused()) {
            return "\u041f\u0430\u0443\u0437\u0430";
        }
        if (nowMs - this.lastActivityMs >= AFK_TIMEOUT_MS) {
            return "\u0410\u0424\u041a";
        }
        return "\u0412 \u0438\u0433\u0440\u0435";
    }

    private boolean isConnectingScreen(Screen screen) {
        if (screen == null) {
            return false;
        }

        String simple = safe(screen.getClass().getSimpleName()).toLowerCase(Locale.ROOT);
        String full = safe(screen.getClass().getName()).toLowerCase(Locale.ROOT);
        return simple.contains("connect")
                || simple.contains("download")
                || simple.contains("progress")
                || simple.contains("loading")
                || full.contains("connect")
                || full.contains("download")
                || full.contains("loading");
    }

    private boolean isInWorld(MinecraftClient mc) {
        return mc != null && mc.player != null && mc.world != null && mc.getNetworkHandler() != null;
    }

    private boolean isMultiplayerSession(MinecraftClient mc) {
        if (!isInWorld(mc)) {
            return false;
        }

        if (mc.getNetworkHandler() != null && mc.getNetworkHandler().getServerInfo() != null) {
            return true;
        }

        return mc.getCurrentServerEntry() != null;
    }

    private boolean isValidApplicationId(String value) {
        if (value == null) {
            return false;
        }
        if (value.length() < 17 || value.length() > 22) {
            return false;
        }

        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private String safe(String value) {
        return value == null ? DEFAULT_LARGE_IMAGE_TEXT : value.trim();
    }

    private String normalizeImageKey(String value) {
        String key = safe(value);
        return key.isBlank() ? DEFAULT_LARGE_IMAGE_KEY : key;
    }

    private String normalizeImageText(String value) {
        return safe(value);
    }

    private String limit(String value, int maxLength) {
        if (value == null) {
            return DEFAULT_LARGE_IMAGE_TEXT;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private void closePipe() {
        RandomAccessFile localPipe = this.pipe;
        this.pipe = null;
        closePipeQuietly(localPipe);
    }

    private void closePipeQuietly(RandomAccessFile targetPipe) {
        if (targetPipe == null) {
            return;
        }

        try {
            sendCloseFrame(targetPipe);
            targetPipe.close();
        } catch (IOException ignored) {
        }
    }

    private void clearPresenceOnCurrentPipe(RandomAccessFile targetPipe) {
        if (targetPipe == null) {
            return;
        }

        String clearPayload = buildSetActivityPayload(JsonNull.INSTANCE);
        try {
            writePacketTo(targetPipe, OP_FRAME, clearPayload);
        } catch (Exception ignored) {
        }
    }

    private void quickClearPreviousPresence(String appId) {
        List<String> clientIds = collectClearClientIds(appId);
        if (clientIds.isEmpty()) {
            return;
        }

        String clearPayload = buildSetActivityPayload(JsonNull.INSTANCE);
        for (String clientId : clientIds) {
            if (!isValidApplicationId(clientId)) {
                continue;
            }
            for (int i = 0; i < MAX_DISCORD_IPC_PIPES; i++) {
                if (clearPresenceOnPipe(i, clientId, clearPayload, true)) {
                    break;
                }
            }
        }
    }

    private void clearPresenceSynchronously(List<String> clientIds, boolean quick) {
        if (clientIds == null || clientIds.isEmpty()) {
            return;
        }

        String clearPayload = buildSetActivityPayload(JsonNull.INSTANCE);
        for (String clientId : clientIds) {
            if (!isValidApplicationId(clientId)) {
                continue;
            }

            for (int i = 0; i < MAX_DISCORD_IPC_PIPES; i++) {
                clearPresenceOnPipe(i, clientId, clearPayload, quick);
            }
        }
    }

    private void scheduleAsyncPresenceClear(List<String> clientIds) {
        if (clientIds == null || clientIds.isEmpty()) {
            return;
        }
        if (!this.clearTaskQueued.compareAndSet(false, true)) {
            return;
        }

        this.clearExecutor.execute(() -> {
            try {
                clearPresenceSynchronously(clientIds, true);
            } catch (Exception ignored) {
            } finally {
                this.clearTaskQueued.set(false);
            }
        });
    }

    private boolean clearPresenceOnPipe(int pipeIndex, String clientId, String clearPayload, boolean quick) {
        String pipeName = "\\\\.\\pipe\\discord-ipc-" + pipeIndex;
        RandomAccessFile freshPipe = null;
        try {
            freshPipe = new RandomAccessFile(pipeName, "rw");

            writeHandshake(freshPipe, clientId);

            if (!quick) {
                sleepSilently(IPC_CLEAR_DELAY_MS);
            }

            writePacketTo(freshPipe, OP_FRAME, clearPayload);
            if (!quick) {
                sleepSilently(IPC_CLEAR_DELAY_MS);
                writePacketTo(freshPipe, OP_FRAME, clearPayload);
            }

            sendCloseFrame(freshPipe);
            return true;
        } catch (Exception ignored) {
            return false;
        } finally {
            if (freshPipe != null) {
                try {
                    freshPipe.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private List<String> collectClearClientIds(String preferredAppId) {
        LinkedHashSet<String> ids = new LinkedHashSet<>();

        if (isValidApplicationId(preferredAppId)) {
            ids.add(preferredAppId);
        }
        if (isValidApplicationId(this.applicationId)) {
            ids.add(this.applicationId);
        }
        if (isValidApplicationId(DEFAULT_APPLICATION_ID)) {
            ids.add(DEFAULT_APPLICATION_ID);
        }
        if (isValidApplicationId(LEGACY_APPLICATION_ID)) {
            ids.add(LEGACY_APPLICATION_ID);
        }

        for (String discoveredId : discoverClientIdsFromConfig()) {
            if (isValidApplicationId(discoveredId)) {
                ids.add(discoveredId);
            }
        }

        return new ArrayList<>(ids);
    }

    private List<String> discoverClientIdsFromConfig() {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        Path autoCfgDir = getAutoConfigDirectory();
        if (autoCfgDir == null || !Files.exists(autoCfgDir) || !Files.isDirectory(autoCfgDir)) {
            return new ArrayList<>(ids);
        }

        try (Stream<Path> stream = Files.list(autoCfgDir)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                        return name.endsWith(".cfg") || name.endsWith(".json") || name.endsWith(".txt");
                    })
                    .forEach(path -> {
                        try {
                            String raw = Files.readString(path, StandardCharsets.UTF_8);
                            if (raw == null || raw.isBlank()) {
                                return;
                            }

                            Matcher matcher = DISCORD_APP_ID_PATTERN.matcher(raw);
                            while (matcher.find()) {
                                String id = matcher.group();
                                if (isValidApplicationId(id)) {
                                    ids.add(id);
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    });
        } catch (Exception ignored) {
        }

        return new ArrayList<>(ids);
    }

    private void registerShutdownHookIfNeeded() {
        if (this.shutdownHookRegistered) {
            return;
        }

        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    DiscordRpcService.getInstance().stop();
                } catch (Exception ignored) {
                }
            }, "royale-master-rpc-shutdown"));
            this.shutdownHookRegistered = true;
        } catch (IllegalStateException ignored) {
        }
    }

    private void sleepSilently(long millis) {
        if (millis <= 0L) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return DEFAULT_LARGE_IMAGE_TEXT;
        }

        for (String value : values) {
            String normalized = safe(value);
            if (!normalized.isBlank()) {
                return normalized;
            }
        }

        return DEFAULT_LARGE_IMAGE_TEXT;
    }

    private boolean shouldUseGameSdk() {
        String value = firstNonBlank(
                System.getProperty(RPC_USE_GAMESDK_PROPERTY),
                System.getenv(RPC_USE_GAMESDK_ENV)
        );
        if (value.isBlank()) {
            return false;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return "1".equals(normalized)
                || "true".equals(normalized)
                || "yes".equals(normalized)
                || "on".equals(normalized);
    }

    private record RpcAutoConfig(String appId, String imageKey, String imageText) {
    }

    private record PresenceContext(String details, String status) {
    }
}











