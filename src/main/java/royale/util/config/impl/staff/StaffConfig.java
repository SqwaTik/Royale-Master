package royale.util.config.impl.staff;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import royale.util.config.impl.consolelogger.Logger;
import royale.util.repository.staff.StaffUtils;
public class StaffConfig {
private final Gson gson = (new GsonBuilder()).setPrettyPrinting().create(); private static StaffConfig instance;
private final Path configPath;
private StaffConfig() {
Path configDir = Paths.get("Royale", new String[] { "configs" });
try {
Files.createDirectories(configDir, (FileAttribute<?>[])new FileAttribute[0]);
} catch (IOException iOException) {}
this.configPath = configDir.resolve("staff.json");
}
public static StaffConfig getInstance() {
if (instance == null) {
instance = new StaffConfig();
}
return instance;
}
public void save() {
try {
JsonArray array = new JsonArray();
for (String name : StaffUtils.getStaffNames()) {
array.add(name);
}
Files.writeString(this.configPath, this.gson.toJson((JsonElement)array), StandardCharsets.UTF_8, new java.nio.file.OpenOption[0]);
} catch (IOException e) {
Logger.error("StaffConfig: Save failed! " + e.getMessage());
} 
}
public void load() {
try {
if (!Files.exists(this.configPath, new java.nio.file.LinkOption[0])) {
return;
}
String json = Files.readString(this.configPath, StandardCharsets.UTF_8);
JsonArray array = JsonParser.parseString(json).getAsJsonArray();
List<String> names = new ArrayList<>();
array.forEach(element -> names.add(element.getAsString()));
StaffUtils.setStaff(names);
Logger.success("StaffConfig: staff.json loaded successfully!");
} catch (Exception e) {
Logger.error("StaffConfig: Load failed! " + e.getMessage());
} 
}
}


