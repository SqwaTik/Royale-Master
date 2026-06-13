package royale.modules.impl.player;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.option.Perspective;
import royale.events.api.EventHandler;
import royale.events.impl.CameraEvent;
import royale.events.impl.FovEvent;
import royale.events.impl.KeyEvent;
import royale.events.impl.MouseRotationEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.BindSetting;
import royale.util.angle.Angle;
import royale.util.angle.MathAngle;
import royale.util.string.PlayerInteractionHelper;
public class FreeLook
extends ModuleStructure
{
private Perspective perspective;
private Angle angle;
public static BindSetting freeLookSetting = new BindSetting("Свободный обзор", "Клавиша для свободного осмотра камерой");
public FreeLook() {
super("FreeLook", "Позволяет осматриваться без поворота персонажа", ModuleCategory.RENDER);
settings(new Setting[] { (Setting)freeLookSetting });
this.angle = null;
}
@EventHandler
public void onKey(KeyEvent e) {
if (e.isKeyDown(freeLookSetting.getKey())) {
this.perspective = mc.options.getPerspective();
if (this.angle == null) {
this.angle = MathAngle.cameraAngle();
}
} 
}
@EventHandler
public void onFov(FovEvent e) {
if (PlayerInteractionHelper.isKey(freeLookSetting)) {
handleFreeLookActivation();
} else if (this.perspective != null) {
handleFreeLookDeactivation();
} 
}
private void handleFreeLookActivation() {
if (mc.options.getPerspective().isFirstPerson()) mc.options.setPerspective(Perspective.THIRD_PERSON_BACK); 
if (this.angle == null) {
this.angle = MathAngle.cameraAngle();
}
}
private void handleFreeLookDeactivation() {
mc.options.setPerspective(this.perspective);
this.perspective = null;
this.angle = null;
}
@EventHandler
public void onMouseRotation(MouseRotationEvent e) {
if (PlayerInteractionHelper.isKey(freeLookSetting)) {
if (this.angle == null) {
this.angle = MathAngle.cameraAngle();
}
this.angle.setYaw(this.angle.getYaw() + e.getCursorDeltaX() * 0.15F);
this.angle.setPitch(MathHelper.clamp(this.angle.getPitch() + e.getCursorDeltaY() * 0.15F, -90.0F, 90.0F));
e.cancel();
} else {
this.angle = null;
} 
}
@EventHandler
public void onCamera(CameraEvent e) {
if (PlayerInteractionHelper.isKey(freeLookSetting) && this.angle != null) {
e.setAngle(this.angle);
e.cancel();
} 
}
}


