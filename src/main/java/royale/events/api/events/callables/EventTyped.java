package royale.events.api.events.callables;
import royale.events.api.events.Event;
import royale.events.api.events.Typed;
public abstract class EventTyped
implements Event, Typed {
private final byte type;
protected EventTyped(byte eventType) {
this.type = eventType;
}
public byte getType() {
return this.type;
}
}


