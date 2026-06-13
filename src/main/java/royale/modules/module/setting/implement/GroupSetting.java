package royale.modules.module.setting.implement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import royale.modules.module.setting.Setting;
public class GroupSetting
extends Setting {
private boolean value;
public GroupSetting setValue(boolean value) {
this.value = value; return this; } public GroupSetting setSubSettings(List<Setting> subSettings) { this.subSettings = subSettings; return this; }
public boolean isValue() {
return this.value;
} private List<Setting> subSettings = new ArrayList<>(); public List<Setting> getSubSettings() { return this.subSettings; }
public GroupSetting(String name, String description) {
super(name, description);
}
public GroupSetting settings(Setting... setting) {
this.subSettings.addAll(Arrays.asList(setting));
return this;
}
public GroupSetting visible(Supplier<Boolean> visible) {
setVisible(visible);
return this;
}
public Setting getSubSetting(String name) {
return this.subSettings.stream()
.filter(setting -> setting.getName().equalsIgnoreCase(name))
.findFirst()
.orElse(null);
}
}


