package royale.modules.module.setting.implement;
import java.util.function.Supplier;
import royale.modules.module.setting.Setting;
public class ButtonSetting extends Setting {
private Runnable runnable;
private String buttonName;
public ButtonSetting setRunnable(Runnable runnable) {
this.runnable = runnable; return this; } public ButtonSetting setButtonName(String buttonName) { this.buttonName = buttonName; return this; }
public Runnable getRunnable() { return this.runnable; } public String getButtonName() {
return this.buttonName;
}
public ButtonSetting(String name, String description) {
super(name, description);
}
public ButtonSetting visible(Supplier<Boolean> visible) {
setVisible(visible);
return this;
}
}


