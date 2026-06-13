package royale.util.angle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import royale.IMinecraft;
public final class MathAngle
implements IMinecraft
{
private MathAngle() {
throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
} public static Angle fromVec2f(Vec2f vector2f) {
return new Angle(vector2f.y, vector2f.x);
}
public static float computeAngleDifference(float a, float b) {
return MathHelper.wrapDegrees(a - b);
}
public static Angle fromVec3d(Vec3d vector) {
return new Angle((float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vector.z, vector.x)) - 90.0D), (float)MathHelper.wrapDegrees(Math.toDegrees(-Math.atan2(vector.y, Math.hypot(vector.x, vector.z)))));
}
public static Angle calculateDelta(Angle start, Angle end) {
float deltaYaw = MathHelper.wrapDegrees(end.getYaw() - start.getYaw());
float deltaPitch = MathHelper.wrapDegrees(end.getPitch() - start.getPitch());
return new Angle(deltaYaw, deltaPitch);
}
public static Angle calculateAngle(Vec3d to) {
return fromVec3d(to.subtract(mc.player.getEyePos()));
}
public static Angle pitch(float pitch) {
return new Angle(mc.player.getYaw(), pitch);
}
public static Angle cameraAngle() {
return new Angle(mc.player.getYaw(), mc.player.getPitch());
}
public static boolean rayTrace(float yaw, float pitch, float distance, float wallDistance, Entity entity) {
HitResult result = rayTrace(distance, yaw, pitch);
Vec3d startPoint = mc.player.getEntityPos().add(0.0D, mc.player.getEyeHeight(mc.player.getPose()), 0.0D);
double distancePow2 = Math.pow(distance, 2.0D);
if (result != null) distancePow2 = startPoint.squaredDistanceTo(result.getPos()); 
Vec3d rotationVector = getRotationVector(pitch, yaw).multiply(distance);
Vec3d endPoint = startPoint.add(rotationVector);
Box entityArea = mc.player.getBoundingBox().stretch(rotationVector).expand(1.0D, 1.0D, 1.0D);
double maxDistance = Math.max(distancePow2, Math.pow(wallDistance, 2.0D));
EntityHitResult ehr = ProjectileUtil.raycast((Entity)mc.player, startPoint, endPoint, entityArea, e -> (!e.isSpectator() && e.canHit() && e == entity), maxDistance);
if (ehr != null && 
startPoint.squaredDistanceTo(ehr.getPos()) <= Math.pow(distance, 2.0D)) {
double minY = entity.getY();
double targetHeight = entity.getHeight();
double minHitY = minY + targetHeight * 0.3D;
if ((ehr.getPos()).y >= minHitY) {
return (ehr.getEntity() == entity);
}
} 
return false;
}
public static HitResult rayTrace(double dst, float yaw, float pitch) {
Vec3d vec3d = mc.player.getCameraPosVec(1.0F);
Vec3d vec3d2 = getRotationVector(pitch, yaw);
Vec3d vec3d3 = vec3d.add(vec3d2.x * dst, vec3d2.y * dst, vec3d2.z * dst);
return (HitResult)mc.world.raycast(new RaycastContext(vec3d, vec3d3, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)mc.player));
}
@NotNull
public static Vec3d getRotationVector(float yaw, float pitch) {
return new Vec3d((MathHelper.sin((-pitch * 0.017453292F)) * MathHelper.cos((yaw * 0.017453292F))), -MathHelper.sin((yaw * 0.017453292F)), (MathHelper.cos((-pitch * 0.017453292F)) * MathHelper.cos((yaw * 0.017453292F))));
}
}


