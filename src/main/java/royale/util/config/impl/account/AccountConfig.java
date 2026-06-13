package royale.util.config.impl.account;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Identifier;
import royale.screens.account.AccountEntry;
import royale.util.config.impl.consolelogger.Logger;
import royale.util.session.SessionChanger;
public class AccountConfig {
private static AccountConfig instance;
private final Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
private final Path configPath;
private final List<AccountEntry> accounts = new ArrayList<>();
private String activeAccountName = "";
private String activeAccountDate = "";
private String activeAccountSkin = "";
private AccountConfig() {
Path configDir = Paths.get("Royale", new String[] { "configs" });
try {
Files.createDirectories(configDir, (FileAttribute<?>[])new FileAttribute[0]);
} catch (IOException iOException) {}
this.configPath = configDir.resolve("accounts.json");
}
public static AccountConfig getInstance() {
if (instance == null) {
instance = new AccountConfig();
}
return instance;
}
public void save() {
try {
JsonObject root = new JsonObject();
JsonArray accountsArray = new JsonArray();
for (AccountEntry entry : this.accounts) {
JsonObject accountObj = new JsonObject();
accountObj.addProperty("name", entry.getName());
accountObj.addProperty("date", entry.getDate());
accountObj.addProperty("skin", (entry.getSkin() != null) ? entry.getSkin().toString() : "");
accountObj.addProperty("pinned", Boolean.valueOf(entry.isPinned()));
accountObj.addProperty("originalIndex", Integer.valueOf(entry.getOriginalIndex()));
accountsArray.add((JsonElement)accountObj);
} 
root.add("accounts", (JsonElement)accountsArray);
JsonObject activeObj = new JsonObject();
activeObj.addProperty("name", this.activeAccountName);
activeObj.addProperty("date", this.activeAccountDate);
activeObj.addProperty("skin", this.activeAccountSkin);
root.add("active", (JsonElement)activeObj);
Files.writeString(this.configPath, this.gson.toJson((JsonElement)root), StandardCharsets.UTF_8, new java.nio.file.OpenOption[0]);
Logger.success("AccountConfig: accounts.json saved successfully!");
} catch (IOException e) {
Logger.error("AccountConfig: Save failed! " + e.getMessage());
} 
}
public void load() {
try {
if (!Files.exists(this.configPath, new java.nio.file.LinkOption[0])) {
Logger.info("AccountConfig: No config file found, using defaults.");
return;
} 
String json = Files.readString(this.configPath, StandardCharsets.UTF_8);
if (json == null || json.trim().isEmpty()) {
Logger.error("AccountConfig: Config file is empty.");
return;
} 
JsonObject root = JsonParser.parseString(json).getAsJsonObject();
this.accounts.clear();
if (root.has("accounts")) {
JsonArray accountsArray = root.getAsJsonArray("accounts");
for (int i = 0; i < accountsArray.size(); i++) {
JsonObject accountObj = accountsArray.get(i).getAsJsonObject();
String name = accountObj.has("name") ? accountObj.get("name").getAsString() : "";
String date = accountObj.has("date") ? accountObj.get("date").getAsString() : "";
String skinStr = accountObj.has("skin") ? accountObj.get("skin").getAsString() : "";
boolean pinned = (accountObj.has("pinned") && accountObj.get("pinned").getAsBoolean());
int originalIndex = accountObj.has("originalIndex") ? accountObj.get("originalIndex").getAsInt() : i;
Identifier skin = null;
if (!skinStr.isEmpty()) {
try {
skin = Identifier.of(skinStr);
} catch (Exception exception) {}
}
AccountEntry entry = new AccountEntry(name, date, skin, pinned, originalIndex);
this.accounts.add(entry);
} 
} 
if (root.has("active")) {
JsonObject activeObj = root.getAsJsonObject("active");
this.activeAccountName = activeObj.has("name") ? activeObj.get("name").getAsString() : "";
this.activeAccountDate = activeObj.has("date") ? activeObj.get("date").getAsString() : "";
this.activeAccountSkin = activeObj.has("skin") ? activeObj.get("skin").getAsString() : "";
} 
if (!this.activeAccountName.isEmpty()) {
SessionChanger.changeUsername(this.activeAccountName);
}
Logger.success("AccountConfig: accounts.json loaded successfully!");
} catch (Exception e) {
Logger.error("AccountConfig: Load failed! " + e.getMessage());
} 
}
public List<AccountEntry> getAccounts() {
return this.accounts;
}
public List<AccountEntry> getSortedAccounts() {
List<AccountEntry> sorted = new ArrayList<>(this.accounts);
sorted.sort((a, b) -> 
(a.isPinned() && !b.isPinned()) ? -1 : (
(!a.isPinned() && b.isPinned()) ? 1 : Integer.compare(a.getOriginalIndex(), b.getOriginalIndex())));
return sorted;
}
public void addAccount(AccountEntry entry) {
entry.setOriginalIndex(this.accounts.size());
this.accounts.add(entry);
save();
}
public void removeAccount(AccountEntry entry) {
this.accounts.remove(entry);
updateOriginalIndices();
save();
}
public void removeAccountByIndex(int sortedIndex) {
List<AccountEntry> sorted = getSortedAccounts();
if (sortedIndex >= 0 && sortedIndex < sorted.size()) {
AccountEntry toRemove = sorted.get(sortedIndex);
this.accounts.remove(toRemove);
updateOriginalIndices();
save();
} 
}
public void clearAllAccounts() {
this.accounts.clear();
this.activeAccountName = "";
this.activeAccountDate = "";
this.activeAccountSkin = "";
save();
}
public AccountEntry getAccountBySortedIndex(int sortedIndex) {
List<AccountEntry> sorted = getSortedAccounts();
if (sortedIndex >= 0 && sortedIndex < sorted.size()) {
return sorted.get(sortedIndex);
}
return null;
}
private void updateOriginalIndices() {
List<AccountEntry> unpinned = new ArrayList<>();
for (AccountEntry entry : this.accounts) {
if (!entry.isPinned()) {
unpinned.add(entry);
}
} 
for (int i = 0; i < unpinned.size(); i++) {
((AccountEntry)unpinned.get(i)).setOriginalIndex(i);
}
}
public void togglePin(int sortedIndex) {
List<AccountEntry> sorted = getSortedAccounts();
if (sortedIndex >= 0 && sortedIndex < sorted.size()) {
AccountEntry entry = sorted.get(sortedIndex);
entry.togglePinned();
save();
} 
}
public String getActiveAccountName() {
return this.activeAccountName;
}
public String getActiveAccountDate() {
return this.activeAccountDate;
}
public Identifier getActiveAccountSkin() {
if (this.activeAccountSkin.isEmpty()) {
return null;
}
try {
return Identifier.of(this.activeAccountSkin);
} catch (Exception e) {
return null;
} 
}
public void setActiveAccount(String name, String date, Identifier skin) {
this.activeAccountName = name;
this.activeAccountDate = date;
this.activeAccountSkin = (skin != null) ? skin.toString() : "";
SessionChanger.changeUsername(name);
save();
}
}


