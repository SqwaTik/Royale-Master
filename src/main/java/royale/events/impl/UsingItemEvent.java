package royale.events.impl;
import royale.events.api.events.callables.EventCancellable;
public class UsingItemEvent extends EventCancellable {
byte type;
public void setType(byte type) {
this.type = type; } public UsingItemEvent(byte type) {
this.type = type;
} public byte getType() {
return this.type;
}
}


