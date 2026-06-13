package royale.events.impl;
import net.minecraft.util.Hand;
import net.minecraft.client.util.math.MatrixStack;
import royale.events.api.events.callables.EventCancellable;
public class HandAnimationEvent extends EventCancellable {
private MatrixStack matrices;
private Hand hand;
private float swingProgress;
public HandAnimationEvent(MatrixStack matrices, Hand hand, float swingProgress) {
this.matrices = matrices; this.hand = hand; this.swingProgress = swingProgress;
}
public void setMatrices(MatrixStack matrices) { this.matrices = matrices; } public void setHand(Hand hand) { this.hand = hand; } public void setSwingProgress(float swingProgress) { this.swingProgress = swingProgress; }
public MatrixStack getMatrices() { return this.matrices; }
public Hand getHand() { return this.hand; } public float getSwingProgress() {
return this.swingProgress;
}
}


