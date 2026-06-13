package royale.events.impl;
import royale.events.api.events.Event;
public class RotationUpdateEvent implements Event {
byte type;
public RotationUpdateEvent(byte type) {
this.type = type;
} public byte getType() {
return this.type;
}
}


