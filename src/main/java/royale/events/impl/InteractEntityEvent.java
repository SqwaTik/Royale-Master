package royale.events.impl;
import net.minecraft.entity.Entity;
import royale.events.api.events.callables.EventCancellable;
public class InteractEntityEvent
extends EventCancellable {
private Entity entity;
public InteractEntityEvent(Entity entity) {
this.entity = entity;
} public void setEntity(Entity entity) {
this.entity = entity;
}
public Entity getEntity() {
return this.entity;
}
}


