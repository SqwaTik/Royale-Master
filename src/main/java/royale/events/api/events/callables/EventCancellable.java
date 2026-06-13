package royale.events.api.events.callables;
import royale.events.api.events.Cancellable;
import royale.events.api.events.Event;
public abstract class EventCancellable
implements Event, Cancellable {
public void setCancelled(boolean cancelled) {
this.cancelled = cancelled;
}
private boolean cancelled;
public boolean isCancelled() {
return this.cancelled;
}
public void cancel() {
this.cancelled = true;
}
}


