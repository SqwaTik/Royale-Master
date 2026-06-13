package royale.util.config.impl.prefix;
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
import royale.command.CommandManager;
import royale.util.config.impl.consolelogger.Logger;
public class PrefixConfig {
private static PrefixConfig instance;
private final Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
private final Path configPath;
private String prefix = "."; public String getPrefix() { return this.prefix; }
private PrefixConfig() {
Path configDir = Paths.get("Royale", new String[] { "configs" });
try {
Files.createDirectories(configDir, (FileAttribute<?>[])new FileAttribute[0]);
} catch (IOException iOException) {}
this.configPath = configDir.resolve("prefix.json");
}
public static PrefixConfig getInstance() {
if (instance == null) {
instance = new PrefixConfig();
}
return instance;
}
public void setPrefix(String prefix) {
this.prefix = prefix;
if (CommandManager.getInstance() != null) {
CommandManager.getInstance().setPrefix(prefix);
}
}
public void setPrefixAndSave(String prefix) {
setPrefix(prefix);
save();
}
public void save() {
try {
JsonObject obj = new JsonObject();
obj.addProperty("prefix", this.prefix);
Files.writeString(this.configPath, this.gson.toJson((JsonElement)obj), StandardCharsets.UTF_8, new java.nio.file.OpenOption[0]);
} catch (IOException e) {
Logger.error("PrefixConfig: Save failed! " + e.getMessage());
} 
}
public void load() {
try {
if (!Files.exists(this.configPath, new java.nio.file.LinkOption[0])) {
return;
}
String json = Files.readString(this.configPath, StandardCharsets.UTF_8);
JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
if (obj.has("prefix")) {
String loadedPrefix = obj.get("prefix").getAsString();
if (!loadedPrefix.isEmpty()) {
this.prefix = loadedPrefix;
}
} 
Logger.success("PrefixConfig: prefix.json loaded successfully!");
} catch (Exception e) {
Logger.error("PrefixConfig: Load failed! " + e.getMessage());
} 
}
}


