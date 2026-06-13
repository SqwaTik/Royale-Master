package royale.events.impl;
import royale.events.api.events.callables.EventCancellable;
public class SwingDurationEvent
extends EventCancellable {
private float animation;
public void setAnimation(float animation) {
this.animation = animation;
}
public float getAnimation() {
return this.animation;
}
}


