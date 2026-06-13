package royale.util.config.impl.bind;
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
public class BindConfig
{
private static BindConfig instance;
private final Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
private final Path configPath;
private int BindKey = 344; public int getBindKey() { return this.BindKey; }
private BindConfig() {
Path configDir = Paths.get("Royale", new String[] { "configs" });
try {
Files.createDirectories(configDir, (FileAttribute<?>[])new FileAttribute[0]);
} catch (IOException iOException) {}
this.configPath = configDir.resolve("Bind.json");
load();
}
public static BindConfig getInstance() {
if (instance == null) {
instance = new BindConfig();
}
return instance;
}
public void setKey(int key) {
this.BindKey = key;
}
public void setKeyAndSave(int key) {
setKey(key);
save();
}
public void save() {
try {
JsonObject obj = new JsonObject();
obj.addProperty("BindKey", Integer.valueOf(this.BindKey));
Files.writeString(this.configPath, this.gson.toJson((JsonElement)obj), StandardCharsets.UTF_8, new java.nio.file.OpenOption[0]);
} catch (IOException e) {
Logger.error("BindConfig: Save failed! " + e.getMessage());
} 
}
public void load() {
try {
if (!Files.exists(this.configPath, new java.nio.file.LinkOption[0])) {
return;
}
String json = Files.readString(this.configPath, StandardCharsets.UTF_8);
JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
if (obj.has("BindKey")) {
this.BindKey = obj.get("BindKey").getAsInt();
}
Logger.success("BindConfig: Bind.json loaded successfully!");
} catch (Exception e) {
Logger.error("BindConfig: Load failed! " + e.getMessage());
} 
}
}


