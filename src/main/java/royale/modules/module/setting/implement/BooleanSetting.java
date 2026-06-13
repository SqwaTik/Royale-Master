package royale.modules.module.setting.implement;
import java.util.function.Supplier;
import royale.modules.module.setting.Setting;
public class BooleanSetting
extends Setting
{
private boolean value;
public BooleanSetting setValue(boolean value) {
this.value = value; return this; } public BooleanSetting setKey(int key) { this.key = key; return this; } public BooleanSetting setType(int type) { this.type = type; return this; }
public boolean isValue() {
return this.value;
} private int key = -1; public int getKey() { return this.key; }
private int type = 1; public int getType() { return this.type; }
public BooleanSetting(String name, String description) {
super(name, description);
}
public BooleanSetting visible(Supplier<Boolean> visible) {
setVisible(visible);
return this;
}
}


