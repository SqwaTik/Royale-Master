package royale.util.angle;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import royale.util.angle.impl.LinearConstructor;
import royale.util.angle.impl.RotateConstructor;
public class AngleConfig
{
public static final AngleConfig DEFAULT = new AngleConfig((RotateConstructor)new LinearConstructor(), true, true);
private final boolean moveCorrection;
private final boolean freeCorrection;
private final RotateConstructor angleSmooth;
private final int resetThreshold = 1;
public AngleConfig(boolean moveCorrection, boolean freeCorrection) {
this((RotateConstructor)new LinearConstructor(), moveCorrection, freeCorrection);
}
public AngleConfig(boolean moveCorrection) {
this((RotateConstructor)new LinearConstructor(), moveCorrection, true);
}
public AngleConfig(RotateConstructor angleSmooth, boolean moveCorrection, boolean freeCorrection) {
this.angleSmooth = angleSmooth;
this.moveCorrection = moveCorrection;
this.freeCorrection = freeCorrection;
}
public AngleConstructor createRotationPlan(Angle angle, Vec3d vec, Entity entity, int reset) {
return new AngleConstructor(angle, vec, entity, this.angleSmooth, reset, 1.0F, this.moveCorrection, this.freeCorrection);
}
public AngleConstructor createRotationPlan(Angle angle, Vec3d vec, Entity entity, boolean moveCorrection, boolean freeCorrection) {
return new AngleConstructor(angle, vec, entity, this.angleSmooth, 1, 1.0F, moveCorrection, freeCorrection);
}
}


