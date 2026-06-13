package royale.events.impl;
import royale.events.api.events.callables.EventCancellable;
public class ChatEvent
extends EventCancellable {
private String message;
public ChatEvent(String message) {
this.message = message;
} public void setMessage(String message) {
this.message = message;
}
public String getMessage() {
return this.message;
}
}


