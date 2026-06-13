package royale.modules.module.setting;

import java.util.function.Supplier;
import royale.util.localization.DescriptionLocalizer;

public class Setting {
private final String name;
private final String description;
private Supplier<Boolean> visible;
public String getName() {
return DescriptionLocalizer.sanitizeDisplay(this.name);
}
public String getStorageName() {
return this.name;
}
public void setVisible(Supplier<Boolean> visible) { this.visible = visible; } public Supplier<Boolean> getVisible() {
return this.visible;
}
public Setting(String name) {
this(name, "");
}
public Setting(String name, String description) {
this.name = name;
this.description = description == null ? "" : description;
}
public String getDescription() {
return DescriptionLocalizer.localizeSetting(this.name, this.description);
}
public boolean isVisible() {
return (this.visible == null || ((Boolean)this.visible.get()).booleanValue());
}
}
