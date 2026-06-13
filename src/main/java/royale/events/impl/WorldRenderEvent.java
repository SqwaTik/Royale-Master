package royale.events.impl;

import net.minecraft.client.util.math.MatrixStack;
import royale.events.api.events.Event;

public class WorldRenderEvent
implements Event {
    private MatrixStack stack;
    private float partialTicks;

    public WorldRenderEvent(MatrixStack stack, float partialTicks) {
        this.stack = stack;
        this.partialTicks = partialTicks;
    }

    public MatrixStack getStack() {
        return this.stack;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }
}

