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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import royale.Initialization;
import royale.events.api.EventHandler;
import royale.events.impl.PacketEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.BooleanSetting;
import royale.util.config.impl.consolelogger.Logger;

/**
 * Safe server resource-pack helper. Vanilla still receives the packet and
 * applies textures normally; this module only saves a background cache copy.
 */
public class ServerRP extends ModuleStructure {

    public final BooleanSetting cacheResourcePack =
        (new BooleanSetting("Сохранять ресурспак", "Сохранять серверный ресурспак локально в фоне"))
            .setValue(true);

    public final BooleanSetting safeApply =
        (new BooleanSetting("Безопасная загрузка", "Не подменять статусы ресурспака, чтобы текстуры не становились черными"))
            .setValue(true);

    private final Set<Path> activeDownloads = ConcurrentHashMap.newKeySet();

    public ServerRP() {
        super("ServerRP", "Сохраняет серверные ресурспаки в фоне и не ломает загрузку текстур", ModuleCategory.MISC);
        settings(new Setting[]{ cacheResourcePack, safeApply });
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (event.getType() != PacketEvent.Type.RECEIVE) return;
        Packet<?> packetRaw = event.getPacket();
        if (!(packetRaw instanceof ResourcePackSendS2CPacket packet)) return;
        if (isSpooferEnabled()) return;

        String url = packet.url();
        String hash = packet.hash();
        if (url == null || url.isBlank()) return;

        if (cacheResourcePack.isValue()) {
            cacheAsync(url, hash);
        }

        if (safeApply.isValue()) {
            return;
        }

        Logger.info("ServerRP: unsafe status spoof is disabled to prevent black textures");
    }

    private void cacheAsync(String url, String hash) {
        String serverName = resolveServerName();
        if (serverName.isBlank()) serverName = "unknown";

        Path targetDir = getResourcePackRoot().resolve(serverName);
        Path targetFile = targetDir.resolve(resolveFileName(url, hash));
        if (Files.isRegularFile(targetFile)) {
            Logger.info("ServerRP: cache already exists -> " + targetFile.getFileName());
            return;
        }

        if (!activeDownloads.add(targetFile)) {
            Logger.info("ServerRP: download already in progress -> " + targetFile.getFileName());
            return;
        }

        CompletableFuture.runAsync(() -> downloadPack(url, targetDir, targetFile))
            .whenComplete((ignored, throwable) -> {
                activeDownloads.remove(targetFile);
                if (throwable != null) {
                    Logger.error("ServerRP: async download crashed - " + throwable.getMessage());
                }
            });
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
        Path tempFile = targetFile.resolveSibling(targetFile.getFileName() + ".tmp");
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
            conn.setReadTimeout(12000);
            conn.setRequestProperty("User-Agent", "Royale-ServerRP");
            conn.setUseCaches(true);
            conn.setInstanceFollowRedirects(true);
            try (InputStream stream = conn.getInputStream()) {
                Files.copy(stream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                conn.disconnect();
            }
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            Logger.info("ServerRP: saved pack -> " + targetFile.getFileName());
        } catch (Exception ex) {
            Logger.error("ServerRP: failed to download pack - " + ex.getMessage());
            try {
                Files.deleteIfExists(tempFile);
            } catch (Exception ignored) {
            }
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