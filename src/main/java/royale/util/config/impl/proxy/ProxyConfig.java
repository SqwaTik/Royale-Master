package royale.util.config.impl.proxy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import royale.util.config.impl.consolelogger.Logger;
import royale.util.proxy.Proxy;
public class ProxyConfig
{
private static ProxyConfig instance;
private final Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
private final Path configPath; private boolean proxyEnabled = false;
public boolean isProxyEnabled() { return this.proxyEnabled; } public void setProxyEnabled(boolean proxyEnabled) { this.proxyEnabled = proxyEnabled; }
private Proxy defaultProxy = new Proxy(); public Proxy getDefaultProxy() { return this.defaultProxy; } public void setDefaultProxy(Proxy defaultProxy) { this.defaultProxy = defaultProxy; }
private Proxy lastUsedProxy = new Proxy(); public Proxy getLastUsedProxy() { return this.lastUsedProxy; } public void setLastUsedProxy(Proxy lastUsedProxy) { this.lastUsedProxy = lastUsedProxy; }
private ProxyConfig() {
Path configDir = Paths.get("Royale", new String[] { "configs", "proxy" });
try {
Files.createDirectories(configDir, (FileAttribute<?>[])new FileAttribute[0]);
} catch (IOException iOException) {}
this.configPath = configDir.resolve("proxy.json");
}
public static ProxyConfig getInstance() {
if (instance == null) {
instance = new ProxyConfig();
}
return instance;
}
public void save() {
try {
JsonObject root = new JsonObject();
root.addProperty("proxyEnabled", Boolean.valueOf(this.proxyEnabled));
JsonObject defaultProxyJson = new JsonObject();
defaultProxyJson.addProperty("ipPort", this.defaultProxy.ipPort);
defaultProxyJson.addProperty("type", this.defaultProxy.type.name());
defaultProxyJson.addProperty("username", this.defaultProxy.username);
defaultProxyJson.addProperty("password", this.defaultProxy.password);
root.add("defaultProxy", (JsonElement)defaultProxyJson);
JsonObject lastUsedProxyJson = new JsonObject();
lastUsedProxyJson.addProperty("ipPort", this.lastUsedProxy.ipPort);
lastUsedProxyJson.addProperty("type", this.lastUsedProxy.type.name());
lastUsedProxyJson.addProperty("username", this.lastUsedProxy.username);
lastUsedProxyJson.addProperty("password", this.lastUsedProxy.password);
root.add("lastUsedProxy", (JsonElement)lastUsedProxyJson);
Files.writeString(this.configPath, this.gson.toJson((JsonElement)root), StandardCharsets.UTF_8, new java.nio.file.OpenOption[0]);
} catch (IOException e) {
Logger.error("ProxyConfig: Save failed! " + e.getMessage());
} 
}
public void load() {
try {
if (!Files.exists(this.configPath, new java.nio.file.LinkOption[0])) {
save();
return;
} 
String json = Files.readString(this.configPath, StandardCharsets.UTF_8);
if (json.isEmpty()) {
return;
}
JsonObject root = JsonParser.parseString(json).getAsJsonObject();
if (root.has("proxyEnabled")) {
this.proxyEnabled = root.get("proxyEnabled").getAsBoolean();
}
if (root.has("defaultProxy")) {
this.defaultProxy = parseProxy(root.getAsJsonObject("defaultProxy"));
}
if (root.has("lastUsedProxy")) {
this.lastUsedProxy = parseProxy(root.getAsJsonObject("lastUsedProxy"));
}
Logger.success("ProxyConfig: proxy.json loaded successfully!");
} catch (Exception e) {
Logger.error("ProxyConfig: Load failed! " + e.getMessage());
} 
}
private Proxy parseProxy(JsonObject json) {
Proxy proxy = new Proxy();
if (json.has("ipPort")) {
proxy.ipPort = json.get("ipPort").getAsString();
}
if (json.has("type")) {
try {
proxy.type = Proxy.ProxyType.valueOf(json.get("type").getAsString());
} catch (IllegalArgumentException illegalArgumentException) {}
}
if (json.has("username")) {
proxy.username = json.get("username").getAsString();
}
if (json.has("password")) {
proxy.password = json.get("password").getAsString();
}
return proxy;
}
public void setDefaultProxyAndSave(Proxy proxy) {
this.defaultProxy = proxy;
save();
}
public void setProxyEnabledAndSave(boolean enabled) {
this.proxyEnabled = enabled;
save();
}
public void setLastUsedProxyAndSave(Proxy proxy) {
this.lastUsedProxy = proxy;
save();
}
}


