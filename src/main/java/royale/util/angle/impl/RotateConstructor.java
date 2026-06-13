package royale.util.angle.impl;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import royale.IMinecraft;
import royale.util.angle.Angle;
public abstract class RotateConstructor
implements IMinecraft {
private final String name;
public RotateConstructor(String name) {
this.name = name;
}
public String getName() {
return this.name;
}
public Angle limitAngleChange(Angle currentAngle, Angle targetAngle) {
return limitAngleChange(currentAngle, targetAngle, null, null);
}
public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3d vec3d) {
return limitAngleChange(currentAngle, targetAngle, vec3d, null);
}
public abstract Angle limitAngleChange(Angle paramAngle1, Angle paramAngle2, Vec3d paramclass_243, Entity paramclass_1297);
public abstract Vec3d randomValue();
}


