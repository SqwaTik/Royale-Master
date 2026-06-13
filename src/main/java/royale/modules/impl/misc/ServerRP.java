package royale.modules.impl.misc;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
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
public class ServerRP extends ModuleStructure {
public final BooleanSetting cacheResourcePack = (new BooleanSetting("Сохранять ресурспак", "Сохранять серверный ресурспак локально"))
.setValue(true);
public ServerRP() {
super("ServerRP", "Автоматически принимает и сохраняет серверные ресурспаки", ModuleCategory.MISC);
settings(new Setting[] { (Setting)this.cacheResourcePack });
}
@EventHandler
public void onPacket(PacketEvent event) {
ResourcePackSendS2CPacket packet;
if (event.getType() != PacketEvent.Type.RECEIVE) {
return;
}
Packet Packet = event.getPacket(); if (Packet instanceof ResourcePackSendS2CPacket) { packet = (ResourcePackSendS2CPacket)Packet; }
else
{ return; }
if (isSpooferEnabled()) {
return;
}
if (!this.cacheResourcePack.isValue()) {
return;
}
String serverName = resolveServerName();
if (serverName.isBlank()) {
serverName = "unknown";
}
cacheIfNeeded(packet, serverName);
}
private boolean isSpooferEnabled() {
if (Initialization.getInstance() == null || 
Initialization.getInstance().getManager() == null || 
Initialization.getInstance().getManager().getModuleProvider() == null) {
return false;
}
ServerRPSpoofer spoofer = (ServerRPSpoofer)Initialization.getInstance().getManager().getModuleProvider().get(ServerRPSpoofer.class);
return (spoofer != null && spoofer.isState());
}
private void cacheIfNeeded(ResourcePackSendS2CPacket packet, String serverName) {
String url = packet.url();
if (url == null || url.isBlank()) {
return;
}
String hash = packet.hash();
Path targetDir = getResourcePackRoot().resolve(serverName);
String fileName = resolveFileName(url, hash);
Path targetFile = targetDir.resolve(fileName);
if (Files.exists(targetFile, new java.nio.file.LinkOption[0])) {
return;
}
CompletableFuture.runAsync(() -> downloadPack(url, targetDir, targetFile));
}
private void downloadPack(String url, Path targetDir, Path targetFile) {
try {
Files.createDirectories(targetDir, (FileAttribute<?>[])new FileAttribute[0]);
URI uri = URI.create(url);
if (uri.getScheme() == null || (!uri.getScheme().equalsIgnoreCase("http") && !uri.getScheme().equalsIgnoreCase("https"))) {
Logger.error("ServerRP: unsupported url scheme");
return;
} 
HttpURLConnection connection = (HttpURLConnection)uri.toURL().openConnection();
connection.setConnectTimeout(3000);
connection.setReadTimeout(8000);
connection.setRequestProperty("User-Agent", "Royale-ServerRP");
connection.setUseCaches(false);
connection.setInstanceFollowRedirects(true);
InputStream stream = connection.getInputStream(); 
try { Files.copy(stream, targetFile, new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
if (stream != null) stream.close();  } catch (Throwable throwable) { if (stream != null)
try { stream.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }   throw throwable; }
Logger.info("ServerRP: saved pack -> " + String.valueOf(targetFile.getFileName()));
} catch (Exception ex) {
Logger.error("ServerRP: failed to download server pack");
} 
}
private Path getResourcePackRoot() {
Path rootDir = (mc.runDirectory != null) ? mc.runDirectory.toPath() : Paths.get("", new String[0]).toAbsolutePath();
return rootDir.resolve("Royale").resolve("resourcepacks");
}
private String resolveServerName() {
if (mc.getNetworkHandler() == null || mc.getNetworkHandler().getServerInfo() == null) {
return "singleplayer";
}
String raw = (mc.getNetworkHandler().getServerInfo()).address;
if (raw == null || raw.isBlank()) {
return "unknown";
}
String cleaned = raw.toLowerCase(Locale.ROOT).replace(':', '_');
return cleaned.replaceAll("[^a-z0-9._-]", "_");
}
private String resolveFileName(String url, String hash) {
if (hash != null && !hash.isBlank()) {
return hash + ".zip";
}
String raw = url;
int queryIndex = raw.indexOf('?');
if (queryIndex >= 0) {
raw = raw.substring(0, queryIndex);
}
int slashIndex = raw.lastIndexOf('/');
String baseName = (slashIndex >= 0) ? raw.substring(slashIndex + 1) : "server-pack";
if (baseName.isBlank()) {
baseName = "server-pack";
}
if (!baseName.toLowerCase(Locale.ROOT).endsWith(".zip")) {
baseName = baseName + ".zip";
}
return baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
}
}



