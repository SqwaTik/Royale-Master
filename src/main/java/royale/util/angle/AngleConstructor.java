package royale.util.angle;
import java.util.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import royale.IMinecraft;
import royale.util.angle.impl.RotateConstructor;
public class AngleConstructor
implements IMinecraft
{
private final Angle angle;
private final Vec3d vec3d;
private final Entity entity;
private final RotateConstructor angleSmooth;
private final int ticksUntilReset;
private final float resetThreshold;
public final boolean moveCorrection;
public final boolean freeCorrection;
public final boolean changeLook = false;
public AngleConstructor(Angle angle, Vec3d vec3d, Entity entity, RotateConstructor angleSmooth, int ticksUntilReset, float resetThreshold, boolean moveCorrection, boolean freeCorrection) {
this.angle = angle; this.vec3d = vec3d; this.entity = entity; this.angleSmooth = angleSmooth; this.ticksUntilReset = ticksUntilReset; this.resetThreshold = resetThreshold; this.moveCorrection = moveCorrection; this.freeCorrection = freeCorrection; } public Angle getAngle() { return this.angle; } public Vec3d getVec3d() { return this.vec3d; } public Entity getEntity() { return this.entity; } public RotateConstructor getAngleSmooth() { return this.angleSmooth; } public boolean isChangeLook() { Objects.requireNonNull(this); return false; }
public int getTicksUntilReset() { return this.ticksUntilReset; }
public float getResetThreshold() { return this.resetThreshold; }
public boolean isMoveCorrection() { return this.moveCorrection; }
public boolean isFreeCorrection() { return this.freeCorrection; } public Angle nextRotation(Angle fromAngle, boolean isResetting) { if (isResetting) {
return this.angleSmooth.limitAngleChange(fromAngle, MathAngle.fromVec2f(mc.player.getRotationClient()));
}
return this.angleSmooth.limitAngleChange(fromAngle, this.angle, this.vec3d, this.entity); }
}


