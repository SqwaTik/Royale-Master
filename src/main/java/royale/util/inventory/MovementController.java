package royale.util.inventory;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
public class MovementController {
private static final MinecraftClient mc = MinecraftClient.getInstance();
private boolean forward;
private boolean back;
private boolean left;
private boolean right;
public void saveState() {
if (mc.player == null)
return;  this.forward = isKeyPressed(mc.options.forwardKey);
this.back = isKeyPressed(mc.options.backKey);
this.left = isKeyPressed(mc.options.leftKey);
this.right = isKeyPressed(mc.options.rightKey);
this.jump = isKeyPressed(mc.options.jumpKey);
this.sprint = mc.player.isSprinting();
this.saved = true;
}
private boolean jump; private boolean sprint; private boolean saved = false; private boolean blocked = false;
public void block() {
if (mc.player == null)
return;  mc.options.forwardKey.setPressed(false);
mc.options.backKey.setPressed(false);
mc.options.leftKey.setPressed(false);
mc.options.rightKey.setPressed(false);
mc.options.jumpKey.setPressed(false);
mc.options.sprintKey.setPressed(false);
this.blocked = true;
}
public void stopSprint() {
if (mc.player != null) {
mc.player.setSprinting(false);
mc.options.sprintKey.setPressed(false);
} 
}
public void restore() {
if (!this.saved)
return;  mc.options.forwardKey.setPressed((this.forward && isCurrentlyPressed(mc.options.forwardKey)));
mc.options.backKey.setPressed((this.back && isCurrentlyPressed(mc.options.backKey)));
mc.options.leftKey.setPressed((this.left && isCurrentlyPressed(mc.options.leftKey)));
mc.options.rightKey.setPressed((this.right && isCurrentlyPressed(mc.options.rightKey)));
mc.options.jumpKey.setPressed((this.jump && isCurrentlyPressed(mc.options.jumpKey)));
this.blocked = false;
this.saved = false;
}
public void restoreFromCurrent() {
mc.options.forwardKey.setPressed(isCurrentlyPressed(mc.options.forwardKey));
mc.options.backKey.setPressed(isCurrentlyPressed(mc.options.backKey));
mc.options.leftKey.setPressed(isCurrentlyPressed(mc.options.leftKey));
mc.options.rightKey.setPressed(isCurrentlyPressed(mc.options.rightKey));
mc.options.jumpKey.setPressed(isCurrentlyPressed(mc.options.jumpKey));
mc.options.sprintKey.setPressed(isCurrentlyPressed(mc.options.sprintKey));
this.blocked = false;
}
public boolean isPlayerStopped(double threshold) {
if (mc.player == null) return true; 
double vx = Math.abs((mc.player.getVelocity()).x);
double vz = Math.abs((mc.player.getVelocity()).z);
return (vx < threshold && vz < threshold);
}
public boolean isBlocked() {
return this.blocked;
}
public void reset() {
this.saved = false;
this.blocked = false;
}
private boolean isKeyPressed(KeyBinding key) {
return key.isPressed();
}
private boolean isCurrentlyPressed(KeyBinding key) {
return InputUtil.isKeyPressed(mc
.getWindow(), 
InputUtil.fromTranslationKey(key.getBoundKeyTranslationKey()).getCode());
}
}


