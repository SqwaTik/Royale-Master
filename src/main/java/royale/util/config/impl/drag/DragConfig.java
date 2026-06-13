package royale.util.config.impl.drag;
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
import royale.Initialization;
import royale.client.draggables.HudElement;
import royale.client.draggables.HudManager;
import royale.util.config.impl.consolelogger.Logger;
public class DragConfig {
private final Gson gson = (new GsonBuilder()).setPrettyPrinting().create(); private static DragConfig instance;
private final Path configPath;
private DragConfig() {
Path configDir = Paths.get("Royale", new String[] { "configs" });
try {
Files.createDirectories(configDir, (FileAttribute<?>[])new FileAttribute[0]);
} catch (IOException iOException) {}
this.configPath = configDir.resolve("draggables.json");
}
public static DragConfig getInstance() {
if (instance == null) {
instance = new DragConfig();
}
return instance;
}
public void save() {
try {
HudManager hudManager = getHudManager();
if (hudManager == null || !hudManager.isInitialized())
return; 
JsonObject root = new JsonObject();
for (HudElement element : hudManager.getElements()) {
JsonObject elementJson = new JsonObject();
elementJson.addProperty("x", Integer.valueOf(element.getX()));
elementJson.addProperty("y", Integer.valueOf(element.getY()));
elementJson.addProperty("width", Integer.valueOf(element.getWidth()));
elementJson.addProperty("height", Integer.valueOf(element.getHeight()));
root.add(element.getName(), (JsonElement)elementJson);
} 
Files.writeString(this.configPath, this.gson.toJson((JsonElement)root), StandardCharsets.UTF_8, new java.nio.file.OpenOption[0]);
Logger.success("DragConfig: draggables.json saved successfully!");
} catch (IOException e) {
Logger.error("DragConfig: Save failed! " + e.getMessage());
} 
}
public void load() {
try {
if (!Files.exists(this.configPath, new java.nio.file.LinkOption[0])) {
Logger.info("DragConfig: No config file found, using defaults.");
return;
} 
HudManager hudManager = getHudManager();
if (hudManager == null) {
Logger.error("DragConfig: HudManager is null, cannot load.");
return;
} 
String json = Files.readString(this.configPath, StandardCharsets.UTF_8);
if (json == null || json.trim().isEmpty()) {
Logger.error("DragConfig: Config file is empty.");
return;
} 
JsonObject root = JsonParser.parseString(json).getAsJsonObject();
for (HudElement element : hudManager.getElements()) {
if (root.has(element.getName())) {
JsonObject elementJson = root.getAsJsonObject(element.getName());
if (elementJson.has("x")) {
element.setX(elementJson.get("x").getAsInt());
}
if (elementJson.has("y")) {
element.setY(elementJson.get("y").getAsInt());
}
if (elementJson.has("width")) {
element.setWidth(elementJson.get("width").getAsInt());
}
if (elementJson.has("height")) {
element.setHeight(elementJson.get("height").getAsInt());
}
} 
} 
Logger.success("DragConfig: draggables.json loaded successfully!");
} catch (Exception e) {
Logger.error("DragConfig: Load failed! " + e.getMessage());
} 
}
private HudManager getHudManager() {
if (Initialization.getInstance() == null) return null; 
if (Initialization.getInstance().getManager() == null) return null; 
return Initialization.getInstance().getManager().getHudManager();
}
}


