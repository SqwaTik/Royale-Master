package royale.events.impl;
import net.minecraft.util.math.Vec3d;
import royale.events.api.events.Event;
public class FireworkEvent
implements Event {
public Vec3d vector;
public FireworkEvent(Vec3d vector) {
this.vector = vector;
} public void setVector(Vec3d vector) {
this.vector = vector;
}
public Vec3d getVector() {
return this.vector;
}
}


