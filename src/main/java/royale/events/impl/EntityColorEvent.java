package royale.events.impl;
import royale.events.api.events.callables.EventCancellable;
public class EntityColorEvent
extends EventCancellable
{
private int color;
public void setColor(int color) {
this.color = color; } public EntityColorEvent(int color) {
this.color = color;
}
public int getColor() {
return this.color;
}
}


