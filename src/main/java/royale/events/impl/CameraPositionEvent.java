package royale.events.impl;
import net.minecraft.util.math.Vec3d;
import royale.events.api.events.Event;
public class CameraPositionEvent
implements Event
{
private Vec3d pos;
public void setPos(Vec3d pos) {
this.pos = pos; } public CameraPositionEvent(Vec3d pos) {
this.pos = pos;
}
public Vec3d getPos() {
return this.pos;
}
}


