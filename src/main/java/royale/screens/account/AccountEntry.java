package royale.screens.account;
import net.minecraft.util.Identifier;
public class AccountEntry
{
private String name;
private String date;
private boolean pinned;
private int originalIndex;
public AccountEntry(String name, String date) {
this.name = name;
this.date = date;
this.pinned = false;
this.originalIndex = -1;
}
public AccountEntry(String name, String date, Identifier skin) {
this.name = name;
this.date = date;
this.pinned = false;
this.originalIndex = -1;
}
public AccountEntry(String name, String date, Identifier skin, boolean pinned) {
this.name = name;
this.date = date;
this.pinned = pinned;
this.originalIndex = -1;
}
public AccountEntry(String name, String date, Identifier skin, boolean pinned, int originalIndex) {
this.name = name;
this.date = date;
this.pinned = pinned;
this.originalIndex = originalIndex;
}
public String getName() {
return this.name;
}
public void setName(String name) {
this.name = name;
}
public String getDate() {
return this.date;
}
public void setDate(String date) {
this.date = date;
}
public Identifier getSkin() {
return SkinManager.getSkin(this.name);
}
public void setSkin(Identifier skin) {}
public boolean isPinned() {
return this.pinned;
}
public void setPinned(boolean pinned) {
this.pinned = pinned;
}
public void togglePinned() {
this.pinned = !this.pinned;
}
public int getOriginalIndex() {
return this.originalIndex;
}
public void setOriginalIndex(int originalIndex) {
this.originalIndex = originalIndex;
}
public void reloadSkin() {
SkinManager.reloadSkin(this.name);
}
}


