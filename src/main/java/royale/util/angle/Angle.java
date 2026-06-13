package royale.util.angle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import royale.util.math.MathUtils;
public class Angle
{
public void setYaw(float yaw) {
this.yaw = yaw; } public void setPitch(float pitch) { this.pitch = pitch; }
public String toString() { return "Angle(yaw=" + getYaw() + ", pitch=" + getPitch() + ")"; } public Angle(float yaw, float pitch) {
this.yaw = yaw; this.pitch = pitch;
}
public static Angle DEFAULT = new Angle(0.0F, 0.0F); private float yaw; private float pitch;
public float getYaw() { return this.yaw; } public float getPitch() { return this.pitch; }
public static Angle fromTargetHead(Vec3d playerPos, Vec3d targetPos, double targetHeight) {
double headY = targetPos.y + targetHeight * 0.9D;
double deltaX = targetPos.x - playerPos.x;
double deltaY = headY - playerPos.y + 1.5D;
double deltaZ = targetPos.z - playerPos.z;
float yaw = (float)Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F;
yaw = MathHelper.wrapDegrees(yaw);
double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
float pitch = (float)Math.toDegrees(-Math.atan2(deltaY, horizontalDistance));
pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);
return new Angle(yaw, pitch);
}
public Angle adjustSensitivity() {
double gcd = MathUtils.computeGcd();
Angle previousAngle = AngleConnection.INSTANCE.getServerAngle();
float adjustedYaw = adjustAxis(this.yaw, previousAngle.yaw, gcd);
float adjustedPitch = adjustAxis(this.pitch, previousAngle.pitch, gcd);
return new Angle(adjustedYaw, MathHelper.clamp(adjustedPitch, -90.0F, 90.0F));
}
public Angle random(float f) {
return new Angle(this.yaw + MathUtils.getRandom(-f, f), this.pitch + MathUtils.getRandom(-f, f));
}
private float adjustAxis(float axisValue, float previousValue, double gcd) {
float delta = axisValue - previousValue;
return previousValue + (float)Math.round(delta / gcd) * (float)gcd;
}
public final Vec3d toVector() {
float f = this.pitch * 0.017453292F;
float g = -this.yaw * 0.017453292F;
float h = MathHelper.cos(g);
float i = MathHelper.sin(g);
float j = MathHelper.cos(f);
float k = MathHelper.sin(f);
return new Vec3d((i * j), -k, (h * j));
}
public Angle addYaw(float yaw) {
return new Angle(this.yaw + yaw, this.pitch);
}
public Angle addPitch(float pitch) {
this.pitch = MathHelper.clamp(this.pitch + pitch, -90.0F, 90.0F);
return this;
}
public Angle of(Angle angle) {
return new Angle(angle.getYaw(), angle.getPitch());
} public static class VecRotation { private final Angle angle;
public String toString() {
return "Angle.VecRotation(angle=" + String.valueOf(getAngle()) + ", vec=" + String.valueOf(getVec()) + ")";
} private final Vec3d vec; public VecRotation(Angle angle, Vec3d vec) {
this.angle = angle; this.vec = vec;
}
public Angle getAngle() { return this.angle; } public Vec3d getVec() {
return this.vec;
} }
}


