package royale.events.impl;
import net.minecraft.entity.player.PlayerEntity;
import royale.events.api.events.callables.EventCancellable;
public class JumpEvent
extends EventCancellable {
private PlayerEntity player;
public JumpEvent(PlayerEntity player) {
this.player = player;
}
public PlayerEntity getPlayer() {
return this.player;
}
}


