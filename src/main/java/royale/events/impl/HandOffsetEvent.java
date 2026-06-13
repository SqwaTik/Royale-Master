package royale.events.impl;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import net.minecraft.client.util.math.MatrixStack;
import royale.events.api.events.callables.EventCancellable;
public class HandOffsetEvent extends EventCancellable {
private MatrixStack matrices;
private ItemStack stack;
private Hand hand;
private float scale;
public void setMatrices(MatrixStack matrices) { this.matrices = matrices; } public void setStack(ItemStack stack) { this.stack = stack; } public void setHand(Hand hand) { this.hand = hand; } public void setScale(float scale) { this.scale = scale; }
public MatrixStack getMatrices() { return this.matrices; }
public ItemStack getStack() { return this.stack; }
public Hand getHand() { return this.hand; } public float getScale() {
return this.scale;
}
public HandOffsetEvent(MatrixStack matrices, ItemStack stack, Hand hand) {
this.matrices = matrices;
this.stack = stack;
this.hand = hand;
this.scale = 1.0F;
}
}


