package royale.events.impl;
import royale.events.api.events.callables.EventCancellable;
public class FovEvent
extends EventCancellable {
private float fov;
public void setFov(float fov) {
this.fov = fov;
}
public float getFov() {
return this.fov;
}
}


