package royale.modules.module.setting.implement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import royale.modules.module.setting.Setting;
import royale.util.localization.DescriptionLocalizer;
public class MultiSelectSetting extends Setting {
private List<String> list;
public void setList(List<String> list) {
this.list = list; } public void setSelected(List<String> selected) { this.selected = new ArrayList<>(); if (selected == null) return; for (String value : selected) { this.selected.add(resolveRawValue(value)); } }
private List<String> selected = new ArrayList<>(); public List<String> getList() { return this.list; } public List<String> getSelected() { return this.selected; }
public List<String> getDisplayList() { if (this.list == null || this.list.isEmpty()) return Collections.emptyList(); List<String> display = new ArrayList<>(this.list.size()); for (String value : this.list) { display.add(getDisplayValue(value)); } return display; }
public String getDisplayValue(String value) { return DescriptionLocalizer.sanitizeDisplay(value); }
public MultiSelectSetting(String name, String description) {
super(name, description);
}
public MultiSelectSetting value(String... settings) {
this.list = Arrays.asList(settings);
return this;
}
public MultiSelectSetting selected(String... settings) {
setSelected(Arrays.asList(settings));
return this;
}
public MultiSelectSetting visible(Supplier<Boolean> visible) {
setVisible(visible);
return this;
}
public boolean isSelected(String name) {
String raw = resolveRawValue(name);
return this.selected.stream().anyMatch(value -> value.equals(raw));
}
private String resolveRawValue(String value) {
if (value == null) {
return "";
}
if (this.list == null || this.list.isEmpty()) {
return value;
}
String displayValue = DescriptionLocalizer.sanitizeDisplay(value);
for (String option : this.list) {
if (option.equals(value) || DescriptionLocalizer.sanitizeDisplay(option).equalsIgnoreCase(displayValue)) {
return option;
}
}
return value;
}
}
