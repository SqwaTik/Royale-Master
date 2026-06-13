package royale.modules.module.setting.implement;
import java.util.function.Supplier;
import royale.modules.module.setting.Setting;
public class BindSetting
extends Setting
{
public BindSetting setKey(int key) {
this.key = key; return this; } public BindSetting setType(int type) { this.type = type; return this; }
private int key = -1; public int getKey() { return this.key; }
private int type = 1; public int getType() { return this.type; }
public BindSetting(String name, String description) {
super(name, description);
}
public BindSetting visible(Supplier<Boolean> visible) {
setVisible(visible);
return this;
}
}


