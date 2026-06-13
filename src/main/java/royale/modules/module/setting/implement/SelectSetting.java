package royale.modules.module.setting.implement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import royale.modules.module.setting.Setting;
import royale.util.localization.DescriptionLocalizer;
public class SelectSetting extends Setting {
private String selected;
private List<String> list;
public void setSelected(String selected) {
this.selected = resolveRawValue(selected);
} public String getSelected() { return this.selected; } public List<String> getList() {
return this.list;
}
public String getDisplaySelected() { return getDisplayValue(this.selected); }
public String getDisplayValue(String value) { return DescriptionLocalizer.sanitizeDisplay(value); }
public List<String> getDisplayList() {
if (this.list == null || this.list.isEmpty()) {
return Collections.emptyList();
}
List<String> display = new ArrayList<>(this.list.size());
for (String value : this.list) {
display.add(getDisplayValue(value));
}
return display;
}
public SelectSetting(String name, String description) {
super(name, description);
}
public SelectSetting value(String... values) {
this.list = Arrays.asList(values);
this.selected = this.list.isEmpty() ? "" : this.list.get(0);
return this;
}
public SelectSetting setValues(List<String> values) {
List<String> resolvedValues = values == null ? Collections.emptyList() : new ArrayList<>(values);
String current = this.selected;
this.list = resolvedValues;
if (resolvedValues.isEmpty()) {
this.selected = "";
return this;
}
String resolvedCurrent = resolveRawValue(current);
if (resolvedValues.contains(resolvedCurrent)) {
this.selected = resolvedCurrent;
return this;
}
this.selected = resolvedValues.get(0);
return this;
}
public SelectSetting setValues(String... values) {
return setValues(values == null ? Collections.emptyList() : Arrays.asList(values));
}
public SelectSetting visible(Supplier<Boolean> visible) {
setVisible(visible);
return this;
}
public SelectSetting selected(String string) {
String resolved = resolveRawValue(string);
if (this.list.contains(resolved)) {
this.selected = resolved;
}
return this;
}
public boolean isSelected(String name) {
return Objects.equals(this.selected, resolveRawValue(name));
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
