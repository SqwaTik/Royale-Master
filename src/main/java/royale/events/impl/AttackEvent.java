package royale.events.impl;
import net.minecraft.entity.Entity;
import royale.events.api.events.Event;
public class AttackEvent implements Event {
private final Entity target;
public AttackEvent(Entity target) {
this.target = target;
} public Entity getTarget() {
return this.target;
}
}


