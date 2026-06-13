package royale.events.impl;
import royale.events.api.events.Event;
public class DeathScreenEvent
implements Event {
private int ticksSinceDeath;
public DeathScreenEvent(int ticksSinceDeath) {
this.ticksSinceDeath = ticksSinceDeath;
}
public int getTicksSinceDeath() {
return this.ticksSinceDeath;
}
}


