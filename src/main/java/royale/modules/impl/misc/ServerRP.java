package royale.modules.impl.misc;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import royale.Initialization;
import royale.events.api.EventHandler;
import royale.events.impl.PacketEvent;
import royale.events.impl.TickEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.BooleanSetting;
import royale.util.config.impl.consolelogger.Logger;
import royale.util.timer.TimerUtil;

/**
 * ServerRP accepts server resource packs without letting vanilla download/apply
 * them on the render thread. Packs are cached in the background and repeated
 * joins answer from cache without another download or resource reload.
 */
public class ServerRP extends ModuleStructure {

    public final BooleanSetting cacheResourcePack =
        (new BooleanSetting("Сохранять ресурспак", "Сохранять серверный ресурспак локально"))
            .setValue(true);

    private enum PackAction { IDLE, SEND_ACCEPTED, SEND_DOWNLOADED, SEND_SUCCESS }

    private final TimerUtil counter = TimerUtil.create();
    private final Set<Path> activeDownloads = ConcurrentHashMap.newKeySet();

    private PackAction pendingAction = PackAction.IDLE;
    private UUID pendingPackId = null;
    private volatile Path pendingTargetFile = null;
    private volatile boolean pendingDownloadComplete = true;

    public ServerRP() {
        super("ServerRP", "Автоматически принимает и сохраняет серверные ресурспаки без фризов", ModuleCategory.MISC);
        settings(new Setting[]{ cacheResourcePack });
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (event.getType() != PacketEvent.Type.RECEIVE) return;
        Packet<?> packetRaw = event.getPacket();
        if (!(packetRaw instanceof ResourcePackSendS2CPacket packet)) return;
        if (isSpooferEnabled()) return;
        if (!cacheResourcePack.isValue()) return;

        String url = packet.url();
        String hash = packet.hash();
        if (url == null || url.isBlank()) return;

        String serverName = resolveServerName();
        if (serverName.isBlank()) serverName = "unknown";

        Path targetDir = getResourcePackRoot().resolve(serverName);
        Path targetFile = targetDir.resolve(resolveFileName(url, hash));
        boolean cached = Files.isRegularFile(targetFile);

        event.cancel();
        pendingPackId = packet.id();
        pendingTargetFile = targetFile;
        pendingDownloadComplete = cached;
        pendingAction = PackAction.SEND_ACCEPTED;
        counter.resetCounter();

        if (cached) {
            Logger.info("ServerRP: serving from cache -> " + targetFile.getFileName());
            return;
        }

        if (activeDownloads.add(targetFile)) {
            CompletableFuture.runAsync(() -> downloadPack(url, targetDir, targetFile))
                .whenComplete((ignored, throwable) -> {
                    activeDownloads.remove(targetFile);
                    if (targetFile.equals(pendingTargetFile)) {
                        pendingDownloadComplete = true;
                    }
                    if (throwable != null) {
                        Logger.error("ServerRP: async download crashed - " + throwable.getMessage());
                    }
                });
        } else {
            Logger.info("ServerRP: download already in progress -> " + targetFile.getFileName());
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (pendingAction == PackAction.IDLE || pendingPackId == null) return;
        if (mc.getNetworkHandler() == null) {
            reset();
            return;
        }
        processStatusMachine();
    }

    private void processStatusMachine() {
        switch (pendingAction) {
            case SEND_ACCEPTED -> {
                mc.getNetworkHandler().sendPacket(
                    (Packet<?>) new ResourcePackStatusC2SPacket(pendingPackId, ResourcePackStatusC2SPacket.Status.ACCEPTED));
                pendingAction = PackAction.SEND_DOWNLOADED;
                counter.resetCounter();
            }
            case SEND_DOWNLOADED -> {
                if (!pendingDownloadComplete || !counter.isReached(25L)) return;
                mc.getNetworkHandler().sendPacket(
                    (Packet<?>) new ResourcePackStatusC2SPacket(pendingPackId, ResourcePackStatusC2SPacket.Status.DOWNLOADED));
                pendingAction = PackAction.SEND_SUCCESS;
                counter.resetCounter();
            }
            case SEND_SUCCESS -> {
                if (!counter.isReached(25L)) return;
                mc.getNetworkHandler().sendPacket(
                    (Packet<?>) new ResourcePackStatusC2SPacket(pendingPackId, ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED));
                reset();
            }
            default -> {}
        }
    }

    @Override
    public void deactivate() {
        reset();
        super.deactivate();
    }

    private void reset() {
        pendingAction = PackAction.IDLE;
        pendingPackId = null;
        pendingTargetFile = null;
        pendingDownloadComplete = true;
    }

    private boolean isSpooferEnabled() {
        if (Initialization.getInstance() == null ||
            Initialization.getInstance().getManager() == null ||
            Initialization.getInstance().getManager().getModuleProvider() == null) return false;
        ServerRPSpoofer spoofer = Initialization.getInstance()
            .getManager().getModuleProvider().get(ServerRPSpoofer.class);
        return spoofer != null && spoofer.isState();
    }

    private void downloadPack(String url, Path targetDir, Path targetFile) {
        try {
            Files.createDirectories(targetDir, (FileAttribute<?>[]) new FileAttribute[0]);
            URI uri = URI.create(url);
            if (uri.getScheme() == null ||
                (!uri.getScheme().equalsIgnoreCase("http") && !uri.getScheme().equalsIgnoreCase("https"))) {
                Logger.error("ServerRP: unsupported url scheme");
                return;
            }

            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("User-Agent", "Royale-ServerRP");
            conn.setUseCaches(true);
            conn.setInstanceFollowRedirects(true);
            try (InputStream stream = conn.getInputStream()) {
                Files.copy(stream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                conn.disconnect();
            }
            Logger.info("ServerRP: saved pack -> " + targetFile.getFileName());
        } catch (Exception ex) {
            Logger.error("ServerRP: failed to download pack - " + ex.getMessage());
        }
    }

    private Path getResourcePackRoot() {
        Path root = (mc.runDirectory != null)
            ? mc.runDirectory.toPath()
            : java.nio.file.Paths.get("").toAbsolutePath();
        return root.resolve("Royale").resolve("resourcepacks");
    }

    private String resolveServerName() {
        if (mc.getNetworkHandler() == null || mc.getNetworkHandler().getServerInfo() == null)
            return "singleplayer";
        String raw = mc.getNetworkHandler().getServerInfo().address;
        if (raw == null || raw.isBlank()) return "unknown";
        return raw.toLowerCase(Locale.ROOT).replace(":", "_").replaceAll("[^a-z0-9._-]", "_");
    }

    private String resolveFileName(String url, String hash) {
        if (hash != null && !hash.isBlank()) return hash + ".zip";
        String raw = url;
        int q = raw.indexOf("?");
        if (q >= 0) raw = raw.substring(0, q);
        int s = raw.lastIndexOf("/");
        String name = (s >= 0) ? raw.substring(s + 1) : "server-pack";
        if (name.isBlank()) name = "server-pack";
        if (!name.toLowerCase(Locale.ROOT).endsWith(".zip")) name += ".zip";
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
