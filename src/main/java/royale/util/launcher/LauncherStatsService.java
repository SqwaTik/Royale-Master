package royale.util.launcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import royale.events.api.EventHandler;
import royale.events.api.EventManager;
import royale.events.impl.AttackEvent;
import royale.util.network.Network;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class LauncherStatsService {
    private static final Logger LOGGER = LoggerFactory.getLogger("royale/LauncherStats");
    private static final LauncherStatsService INSTANCE = new LauncherStatsService();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final long AFK_TIMEOUT_MS = 60_000L;
    private static final long SAVE_INTERVAL_MS = 4_000L;
    private static final long MAX_TICK_DELTA_MS = 2_000L;
    private static final long PVP_TIMEOUT_MS = 10_000L;
    private static final long HIT_CONFIRM_TIMEOUT_MS = 1_500L;

    private static final String STATUS_PLAYING = "playing";
    private static final String STATUS_PVP = "pvp";
    private static final String STATUS_AFK = "afk";
    private static final String STATUS_PAUSE = "pause";
    private static final String STATUS_DEATH = "death";
    private static final String STATUS_MENU = "menu";
    private static final String STATUS_CONNECTING = "connecting";

    private final Object lock = new Object();

    private boolean registered;
    private boolean loaded;
    private boolean dirty;

    private Path statsPath;
    private Path backupStatsPath;
    private long firstSeenAtMs;
    private long lastUpdatedAtMs;
    private long lastSaveAtMs;
    private long lastTickAtMs;
    private long sessionStartedAtMs;
    private long lastActivityAtMs;

    private boolean hasMovementSnapshot;
    private double lastX;
    private double lastY;
    private double lastZ;
    private float lastYaw;
    private float lastPitch;
    private boolean wasInPvp;
    private int previousSelfHurtTime;
    private int pendingTargetId = -1;
    private long pendingTargetUntilMs;
    private long combatUntilMs;

    private long totalRuntimeMs;
    private long totalPlaytimeMs;
    private long totalActiveMs;
    private long totalAfkMs;
    private long totalPvpMs;
    private long totalPvpAfkMs;
    private int totalSessions;
    private int totalCombatEntries;

    private long sessionRuntimeMs;
    private long sessionPlaytimeMs;
    private long sessionActiveMs;
    private long sessionAfkMs;
    private long sessionPvpMs;
    private long sessionPvpAfkMs;
    private int sessionCombatEntries;

    private final Map<String, Long> totalStatusDurationsMs = createStatusMap();
    private final Map<String, Long> sessionStatusDurationsMs = createStatusMap();

    private String currentStatus = STATUS_MENU;
    private String currentStatusLabel = "В меню";
    private boolean currentInWorld;
    private boolean currentInPvp;
    private boolean currentAfk;
    private String serverName = "";
    private String serverAddress = "";
    private String worldType = "";

    private LauncherStatsService() {
    }

    public static LauncherStatsService getInstance() {
        return INSTANCE;
    }

    public void register() {
        synchronized (this.lock) {
            if (this.registered) {
                return;
            }
            this.registered = true;
        }

        EventManager.register(this);
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> shutdown());
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }

        Entity targetEntity = event.getTarget();
        if (!(targetEntity instanceof PlayerEntity target) || !isValidPvpPlayer(client, target)) {
            return;
        }

        synchronized (this.lock) {
            this.pendingTargetId = target.getId();
            this.pendingTargetUntilMs = System.currentTimeMillis() + HIT_CONFIRM_TIMEOUT_MS;
        }
    }

    private void tick(MinecraftClient client) {
        if (client == null) {
            return;
        }

        synchronized (this.lock) {
            ensureLoaded(client);

            long now = System.currentTimeMillis();
            if (this.lastTickAtMs <= 0L) {
                this.lastTickAtMs = now;
                this.lastActivityAtMs = now;
                updateRuntimeSnapshot(client, now);
                markDirty(now);
                return;
            }

            detectActivity(client, now);
            updateCombatState(client, now);

            long delta = Math.max(0L, Math.min(MAX_TICK_DELTA_MS, now - this.lastTickAtMs));
            this.lastTickAtMs = now;

            boolean inWorld = isInWorld(client);
            boolean inPvp = inWorld && (Network.isPvp() || now <= this.combatUntilMs);
            boolean isAfk = inWorld && (now - this.lastActivityAtMs >= AFK_TIMEOUT_MS);

            String nextStatus = resolveStatusKey(client, now, inWorld, inPvp, isAfk);
            String nextStatusLabel = getStatusLabel(nextStatus);

            this.totalRuntimeMs += delta;
            this.sessionRuntimeMs += delta;

            if (inWorld) {
                this.totalPlaytimeMs += delta;
                this.sessionPlaytimeMs += delta;

                if (isAfk) {
                    this.totalAfkMs += delta;
                    this.sessionAfkMs += delta;
                } else {
                    this.totalActiveMs += delta;
                    this.sessionActiveMs += delta;
                }

                if (inPvp) {
                    this.totalPvpMs += delta;
                    this.sessionPvpMs += delta;
                    if (isAfk) {
                        this.totalPvpAfkMs += delta;
                        this.sessionPvpAfkMs += delta;
                    }
                }
            }

            if (inPvp && !this.wasInPvp) {
                this.totalCombatEntries += 1;
                this.sessionCombatEntries += 1;
            }
            this.wasInPvp = inPvp;

            incrementDuration(this.totalStatusDurationsMs, nextStatus, delta);
            incrementDuration(this.sessionStatusDurationsMs, nextStatus, delta);

            this.currentStatus = nextStatus;
            this.currentStatusLabel = nextStatusLabel;
            this.currentInWorld = inWorld;
            this.currentInPvp = inPvp;
            this.currentAfk = isAfk;
            updateRuntimeSnapshot(client, now);
            markDirty(now);

            if (shouldSave(now)) {
                saveNow();
            }
        }
    }

    public void shutdown() {
        synchronized (this.lock) {
            if (!this.loaded) {
                return;
            }
            saveNow();
        }
        EventManager.unregister(this);
    }

    private void ensureLoaded(MinecraftClient client) {
        if (this.loaded) {
            return;
        }

        this.statsPath = resolveStatsPath(client);
        this.backupStatsPath = resolveBackupStatsPath(client);
        loadFromDisk();

        long now = System.currentTimeMillis();
        if (this.firstSeenAtMs <= 0L) {
            this.firstSeenAtMs = now;
        }

        this.loaded = true;
        this.dirty = true;
        this.lastSaveAtMs = 0L;
        this.lastTickAtMs = 0L;
        this.lastUpdatedAtMs = now;
        this.lastActivityAtMs = now;
        this.totalSessions += 1;
        resetSession(now);
    }

    private void resetSession(long now) {
        this.sessionStartedAtMs = now;
        this.sessionRuntimeMs = 0L;
        this.sessionPlaytimeMs = 0L;
        this.sessionActiveMs = 0L;
        this.sessionAfkMs = 0L;
        this.sessionPvpMs = 0L;
        this.sessionPvpAfkMs = 0L;
        this.sessionCombatEntries = 0;
        clearDurations(this.sessionStatusDurationsMs);
        this.hasMovementSnapshot = false;
        this.wasInPvp = false;
        this.previousSelfHurtTime = 0;
        this.pendingTargetId = -1;
        this.pendingTargetUntilMs = 0L;
        this.combatUntilMs = 0L;
        this.currentStatus = STATUS_MENU;
        this.currentStatusLabel = getStatusLabel(STATUS_MENU);
        this.currentInWorld = false;
        this.currentInPvp = false;
        this.currentAfk = false;
    }

    private void loadFromDisk() {
        Path sourcePath = selectReadableStatsPath();
        if (sourcePath == null) {
            return;
        }

        try {
            Path parent = sourcePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(sourcePath)) {
                return;
            }

            try (Reader reader = Files.newBufferedReader(sourcePath, StandardCharsets.UTF_8)) {
                JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
                this.firstSeenAtMs = getLong(root, "firstSeenAtMs");
                this.totalRuntimeMs = readDuration(root, "totals", "runtimeMs");
                this.totalPlaytimeMs = readDuration(root, "totals", "playtimeMs");
                this.totalActiveMs = readDuration(root, "totals", "activeMs");
                this.totalAfkMs = readDuration(root, "totals", "afkMs");
                this.totalPvpMs = readDuration(root, "totals", "pvpMs");
                this.totalPvpAfkMs = readDuration(root, "totals", "pvpAfkMs");
                this.totalSessions = (int) readDuration(root, "totals", "sessions");
                this.totalCombatEntries = (int) readDuration(root, "totals", "combatEntries");
                readStatusDurations(root.getAsJsonObject("statusTotals"), this.totalStatusDurationsMs);

                JsonObject runtime = root.getAsJsonObject("runtime");
                if (runtime != null) {
                    this.serverName = getString(runtime, "serverName");
                    this.serverAddress = getString(runtime, "serverAddress");
                    this.worldType = getString(runtime, "worldType");
                    this.currentStatus = getString(runtime, "status");
                    this.currentStatusLabel = getString(runtime, "statusLabel");
                }
            }
        } catch (Exception error) {
            LOGGER.warn("Failed to load launcher gameplay stats: {}", error.getMessage());
        }
    }

    private void saveNow() {
        if (this.statsPath == null) {
            return;
        }

        try {
            JsonObject root = new JsonObject();
            root.addProperty("formatVersion", 1);
            root.addProperty("firstSeenAt", toIso(this.firstSeenAtMs));
            root.addProperty("firstSeenAtMs", this.firstSeenAtMs);
            root.addProperty("updatedAt", toIso(this.lastUpdatedAtMs));
            root.addProperty("updatedAtMs", this.lastUpdatedAtMs);
            root.addProperty("sessionStartedAt", toIso(this.sessionStartedAtMs));
            root.addProperty("sessionStartedAtMs", this.sessionStartedAtMs);

            JsonObject totals = new JsonObject();
            totals.addProperty("sessions", this.totalSessions);
            totals.addProperty("combatEntries", this.totalCombatEntries);
            totals.addProperty("runtimeMs", this.totalRuntimeMs);
            totals.addProperty("playtimeMs", this.totalPlaytimeMs);
            totals.addProperty("activeMs", this.totalActiveMs);
            totals.addProperty("afkMs", this.totalAfkMs);
            totals.addProperty("pvpMs", this.totalPvpMs);
            totals.addProperty("pvpAfkMs", this.totalPvpAfkMs);
            root.add("totals", totals);

            JsonObject session = new JsonObject();
            session.addProperty("combatEntries", this.sessionCombatEntries);
            session.addProperty("runtimeMs", this.sessionRuntimeMs);
            session.addProperty("playtimeMs", this.sessionPlaytimeMs);
            session.addProperty("activeMs", this.sessionActiveMs);
            session.addProperty("afkMs", this.sessionAfkMs);
            session.addProperty("pvpMs", this.sessionPvpMs);
            session.addProperty("pvpAfkMs", this.sessionPvpAfkMs);
            root.add("currentSession", session);

            root.add("statusTotals", toJsonDurations(this.totalStatusDurationsMs));
            root.add("sessionStatusTotals", toJsonDurations(this.sessionStatusDurationsMs));

            JsonObject runtime = new JsonObject();
            runtime.addProperty("status", this.currentStatus);
            runtime.addProperty("statusLabel", this.currentStatusLabel);
            runtime.addProperty("serverName", this.serverName);
            runtime.addProperty("serverAddress", this.serverAddress);
            runtime.addProperty("worldType", this.worldType);
            runtime.addProperty("isInWorld", this.currentInWorld);
            runtime.addProperty("isInPvp", this.currentInPvp);
            runtime.addProperty("isAfk", this.currentAfk);
            root.add("runtime", runtime);

            writeStatsRoot(this.statsPath, root);
            writeStatsRoot(this.backupStatsPath, root);

            this.lastSaveAtMs = System.currentTimeMillis();
            this.dirty = false;
        } catch (IOException error) {
            LOGGER.warn("Failed to write launcher gameplay stats: {}", error.getMessage());
        }
    }

    private Path selectReadableStatsPath() {
        JsonObject primaryRoot = readStatsRoot(this.statsPath);
        JsonObject backupRoot = readStatsRoot(this.backupStatsPath);

        if (primaryRoot == null && backupRoot == null) {
            return null;
        }
        if (primaryRoot == null) {
            return this.backupStatsPath;
        }
        if (backupRoot == null) {
            return this.statsPath;
        }

        long primaryUpdatedAt = getLong(primaryRoot, "updatedAtMs");
        long backupUpdatedAt = getLong(backupRoot, "updatedAtMs");
        return backupUpdatedAt > primaryUpdatedAt ? this.backupStatsPath : this.statsPath;
    }

    private JsonObject readStatsRoot(Path path) {
        if (path == null) {
            return null;
        }

        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(path)) {
                return null;
            }

            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                return new JsonParser().parse(reader).getAsJsonObject();
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    private void writeStatsRoot(Path path, JsonObject root) throws IOException {
        if (path == null) {
            return;
        }

        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, new OpenOption[0])) {
            GSON.toJson((JsonElement) root, writer);
        }
    }

    private void detectActivity(MinecraftClient client, long now) {
        if (client.player == null) {
            return;
        }

        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();
        float yaw = client.player.getYaw();
        float pitch = client.player.getPitch();

        if (!this.hasMovementSnapshot) {
            this.hasMovementSnapshot = true;
            this.lastX = x;
            this.lastY = y;
            this.lastZ = z;
            this.lastYaw = yaw;
            this.lastPitch = pitch;
            this.lastActivityAtMs = now;
            return;
        }

        double dx = x - this.lastX;
        double dy = y - this.lastY;
        double dz = z - this.lastZ;
        double distanceSq = dx * dx + dy * dy + dz * dz;

        float yawDiff = Math.abs(yaw - this.lastYaw);
        float pitchDiff = Math.abs(pitch - this.lastPitch);

        boolean moved = distanceSq > 8.0E-4D;
        boolean rotated = yawDiff > 0.9F || pitchDiff > 0.9F;
        boolean pressedKeys = client.options.forwardKey.isPressed()
                || client.options.backKey.isPressed()
                || client.options.leftKey.isPressed()
                || client.options.rightKey.isPressed()
                || client.options.jumpKey.isPressed()
                || client.options.attackKey.isPressed()
                || client.options.useKey.isPressed();

        if (moved || rotated || pressedKeys || client.player.handSwinging) {
            this.lastActivityAtMs = now;
        }

        this.lastX = x;
        this.lastY = y;
        this.lastZ = z;
        this.lastYaw = yaw;
        this.lastPitch = pitch;
    }

    private void updateRuntimeSnapshot(MinecraftClient client, long now) {
        this.lastUpdatedAtMs = now;
        if (!isInWorld(client)) {
            this.worldType = "";
            this.serverAddress = "";
            this.serverName = "";
            return;
        }

        this.worldType = normalizeWorldType(Network.getWorldType());

        ServerInfo serverInfo = client.getCurrentServerEntry();
        if (client.getNetworkHandler() != null && client.getNetworkHandler().getServerInfo() != null) {
            serverInfo = client.getNetworkHandler().getServerInfo();
        }

        if (!isMultiplayerSession(client)) {
            this.serverAddress = "";
            this.serverName = "";
        } else if (client.getNetworkHandler() != null
                && client.getNetworkHandler().getConnection() != null
                && client.getNetworkHandler().getConnection().getAddress() != null) {
            this.serverAddress = sanitizeSocketAddress(client.getNetworkHandler().getConnection().getAddress());
            this.serverName = serverInfo != null
                    ? normalizeServerName(serverInfo.name)
                    : "";
            if (this.serverName.isBlank()) {
                this.serverName = normalizeServerName(this.serverAddress);
            }
        } else if (serverInfo != null) {
            this.serverAddress = safe(serverInfo.address);
            this.serverName = normalizeServerName(serverInfo.name);
            if (this.serverName.isBlank()) {
                this.serverName = normalizeServerName(this.serverAddress);
            }
        } else {
            this.serverAddress = "";
            this.serverName = normalizeServerName(Network.getServer());
        }

        if (this.serverName.isBlank() && isMultiplayerSession(client)) {
            this.serverName = normalizeServerName(Network.getServer());
        }
    }

    private String normalizeServerName(String value) {
        String sanitized = safe(value);
        if (sanitized.isBlank()) {
            return "";
        }

        String normalized = sanitized.toLowerCase(Locale.ROOT);
        if ("vanilla".equals(normalized) || "localhost".equals(normalized)) {
            return "";
        }

        return sanitized;
    }

    private String normalizeWorldType(String value) {
        String sanitized = safe(value);
        if (sanitized.isBlank()) {
            return "";
        }

        String normalized = sanitized.toLowerCase(Locale.ROOT);
        if ("vanilla".equals(normalized)) {
            return "";
        }
        if ("overworld".equals(normalized)) {
            return "overworld";
        }
        if ("the_nether".equals(normalized) || "nether".equals(normalized)) {
            return "the_nether";
        }
        if ("the_end".equals(normalized) || "end".equals(normalized)) {
            return "the_end";
        }

        return sanitized;
    }

    private String sanitizeSocketAddress(SocketAddress socketAddress) {
        if (socketAddress == null) {
            return "";
        }

        if (socketAddress instanceof InetSocketAddress inet) {
            String host = safe(inet.getHostString());
            if (!host.isBlank()) {
                return host;
            }
        }

        String raw = safe(socketAddress.toString());
        if (raw.startsWith("/")) {
            raw = safe(raw.substring(1));
        }
        int slashIndex = raw.indexOf('/');
        if (slashIndex >= 0) {
            String left = safe(raw.substring(0, slashIndex));
            String right = safe(raw.substring(slashIndex + 1));
            raw = !left.isBlank() ? left : right;
        }
        int colonIndex = raw.lastIndexOf(':');
        if (colonIndex > 0) {
            raw = safe(raw.substring(0, colonIndex));
        }
        return raw;
    }

    private void updateCombatState(MinecraftClient client, long now) {
        if (client.player == null || client.world == null) {
            this.previousSelfHurtTime = 0;
            this.pendingTargetId = -1;
            this.pendingTargetUntilMs = 0L;
            this.combatUntilMs = 0L;
            return;
        }

        int selfHurtTime = client.player.hurtTime;
        if (selfHurtTime > 0 && this.previousSelfHurtTime <= 0 && isAttackedByValidPlayer(client)) {
            this.combatUntilMs = Math.max(this.combatUntilMs, now + PVP_TIMEOUT_MS);
        }
        this.previousSelfHurtTime = selfHurtTime;

        if (this.pendingTargetId == -1) {
            return;
        }

        if (now > this.pendingTargetUntilMs) {
            this.pendingTargetId = -1;
            this.pendingTargetUntilMs = 0L;
            return;
        }

        Entity entity = client.world.getEntityById(this.pendingTargetId);
        if (entity instanceof PlayerEntity target && isValidPvpPlayer(client, target) && target.hurtTime > 0) {
            this.combatUntilMs = Math.max(this.combatUntilMs, now + PVP_TIMEOUT_MS);
            this.pendingTargetId = -1;
            this.pendingTargetUntilMs = 0L;
        }
    }

    private boolean shouldSave(long now) {
        return this.dirty && (this.lastSaveAtMs <= 0L || now - this.lastSaveAtMs >= SAVE_INTERVAL_MS);
    }

    private void markDirty(long now) {
        this.dirty = true;
        this.lastUpdatedAtMs = now;
    }

    private String resolveStatusKey(MinecraftClient client, long now, boolean inWorld, boolean inPvp, boolean afk) {
        if (isConnectingScreen(client.currentScreen)) {
            return STATUS_CONNECTING;
        }
        if (!inWorld) {
            return STATUS_MENU;
        }
        if (inPvp) {
            return STATUS_PVP;
        }
        if (client.player != null && !client.player.isAlive()) {
            return STATUS_DEATH;
        }
        if (client.isPaused()) {
            return STATUS_PAUSE;
        }
        if (afk || now - this.lastActivityAtMs >= AFK_TIMEOUT_MS) {
            return STATUS_AFK;
        }
        return STATUS_PLAYING;
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

    private boolean isInWorld(MinecraftClient client) {
        return client != null && client.player != null && client.world != null && client.getNetworkHandler() != null;
    }

    private boolean isMultiplayerSession(MinecraftClient client) {
        if (!isInWorld(client)) {
            return false;
        }

        if (client.getNetworkHandler() != null && client.getNetworkHandler().getServerInfo() != null) {
            return true;
        }

        return client.getCurrentServerEntry() != null;
    }

    private boolean isAttackedByValidPlayer(MinecraftClient client) {
        if (client.player == null) {
            return false;
        }

        if (!(client.player.getAttacker() instanceof PlayerEntity playerAttacker)) {
            return false;
        }

        return isValidPvpPlayer(client, playerAttacker);
    }

    private boolean isValidPvpPlayer(MinecraftClient client, PlayerEntity player) {
        if (player == null || client.player == null || client.world == null) {
            return false;
        }
        if (player == client.player || player.isRemoved() || !player.isAlive() || player.isSpectator()) {
            return false;
        }

        String name = player.getName() != null ? player.getName().getString() : "";
        if (name.isBlank()) {
            return false;
        }

        String upper = name.toUpperCase(Locale.ROOT);
        if (upper.contains("NPC") || upper.startsWith("[ZNPC]") || upper.startsWith("CIT-")) {
            return false;
        }

        return client.getNetworkHandler() == null || client.getNetworkHandler().getPlayerListEntry(player.getUuid()) != null;
    }

    private Path resolveStatsPath(MinecraftClient client) {
        Path runDirectory = client.runDirectory != null
                ? client.runDirectory.toPath()
                : Paths.get("").toAbsolutePath();
        return runDirectory.resolve("Royale").resolve("stats").resolve("launcher-game-stats.json");
    }

    private Path resolveBackupStatsPath(MinecraftClient client) {
        String appData = safe(System.getenv("APPDATA"));
        Path baseDirectory = appData.isBlank()
                ? Paths.get(System.getProperty("user.home", ".")).resolve(".royale-launcher")
                : Paths.get(appData).resolve("royale-launcher");
        return baseDirectory.resolve("gameplay-stats").resolve(resolveStatsFileName(client));
    }

    private String resolveStatsFileName(MinecraftClient client) {
        Path runDirectory = client.runDirectory != null
                ? client.runDirectory.toPath()
                : Paths.get("").toAbsolutePath();
        Path fileName = runDirectory.getFileName();
        String rawName = fileName != null ? fileName.toString() : "default";
        String safeName = rawName.replaceAll("[^A-Za-z0-9._-]", "_");
        return (safeName.isBlank() ? "default" : safeName) + ".json";
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String toIso(long value) {
        return value > 0L ? Instant.ofEpochMilli(value).toString() : "";
    }

    private static Map<String, Long> createStatusMap() {
        Map<String, Long> values = new LinkedHashMap<>();
        values.put(STATUS_MENU, 0L);
        values.put(STATUS_CONNECTING, 0L);
        values.put(STATUS_PLAYING, 0L);
        values.put(STATUS_PVP, 0L);
        values.put(STATUS_AFK, 0L);
        values.put(STATUS_PAUSE, 0L);
        values.put(STATUS_DEATH, 0L);
        return values;
    }

    private static void clearDurations(Map<String, Long> values) {
        values.replaceAll((_key, _value) -> 0L);
    }

    private static void incrementDuration(Map<String, Long> values, String key, long delta) {
        values.put(key, values.getOrDefault(key, 0L) + Math.max(0L, delta));
    }

    private static JsonObject toJsonDurations(Map<String, Long> values) {
        JsonObject result = new JsonObject();
        for (Map.Entry<String, Long> entry : values.entrySet()) {
            result.addProperty(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private static void readStatusDurations(JsonObject source, Map<String, Long> target) {
        if (source == null) {
            return;
        }
        for (String key : target.keySet()) {
            target.put(key, getLong(source, key));
        }
    }

    private static long readDuration(JsonObject root, String group, String key) {
        JsonObject nested = root.getAsJsonObject(group);
        return nested == null ? 0L : getLong(nested, key);
    }

    private static long getLong(JsonObject object, String key) {
        if (object == null || !object.has(key)) {
            return 0L;
        }
        try {
            return object.get(key).getAsLong();
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private static String getString(JsonObject object, String key) {
        if (object == null || !object.has(key)) {
            return "";
        }
        try {
            return safe(object.get(key).getAsString());
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String getStatusLabel(String statusKey) {
        if (STATUS_CONNECTING.equals(statusKey)) return "Подключение";
        if (STATUS_PLAYING.equals(statusKey)) return "В игре";
        if (STATUS_PVP.equals(statusKey)) return "В PvP";
        if (STATUS_AFK.equals(statusKey)) return "АФК";
        if (STATUS_PAUSE.equals(statusKey)) return "Пауза";
        if (STATUS_DEATH.equals(statusKey)) return "Смерть";
        return "В меню";
    }
}
