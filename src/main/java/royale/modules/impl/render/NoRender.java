package royale.modules.impl.render;
import royale.events.api.EventHandler;
import royale.events.impl.TickEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.MultiSelectSetting;
import royale.util.Instance;
public class NoRender
extends ModuleStructure
{
public static NoRender getInstance() {
return (NoRender)Instance.get(NoRender.class);
}
public final MultiSelectSetting modeSetting = (new MultiSelectSetting("Элементы", "Выберите элементы для игнорирования"))
.value(new String[] { "Fire", "Bad Effects", "Darkness", "Damage", "Nausea", "Scoreboard", "BossBar", "Fov"
}).selected(new String[] { "Fire", "Bad Effects", "Darkness", "Damage", "Nausea" });
private boolean fovOverrideApplied = false;
private double previousFovEffectScale = 1.0D;
public NoRender() {
super("NoRender", "No Render", ModuleCategory.RENDER);
settings(new Setting[] { (Setting)this.modeSetting });
}
@EventHandler
public void onTick(TickEvent event) {
if (this.modeSetting.isSelected("Fov")) {
applyFovEffectScaleOverride();
return;
} 
restoreFovEffectScale();
}
public void deactivate() {
restoreFovEffectScale();
super.deactivate();
}
private void applyFovEffectScaleOverride() {
if (mc == null || mc.options == null) {
return;
}
Object value = mc.options.getFovEffectScale().getValue();
double current = value instanceof Number ? ((Number)value).doubleValue() : 1.0D;
if (!this.fovOverrideApplied) {
this.previousFovEffectScale = current;
this.fovOverrideApplied = true;
} 
if (current != 0.0D) {
mc.options.getFovEffectScale().setValue(Double.valueOf(0.0D));
}
}
private void restoreFovEffectScale() {
if (!this.fovOverrideApplied || mc == null || mc.options == null) {
return;
}
mc.options.getFovEffectScale().setValue(Double.valueOf(this.previousFovEffectScale));
this.fovOverrideApplied = false;
}
}


