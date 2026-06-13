package royale.events.impl;
import net.minecraft.block.Block;
import royale.events.api.events.callables.EventCancellable;
public class PlayerCollisionEvent
extends EventCancellable {
private Block block;
public void setBlock(Block block) {
this.block = block;
} public PlayerCollisionEvent(Block block) {
this.block = block;
}
public Block getBlock() {
return this.block;
}
}


