package royale.events.impl;

import net.minecraft.client.util.math.MatrixStack;
import royale.events.api.events.callables.EventCancellable;

public class GlassHandsRenderEvent
extends EventCancellable {
    private Phase phase;
    private MatrixStack matrices;
    private float tickDelta;

    public GlassHandsRenderEvent(Phase phase, MatrixStack matrices, float tickDelta) {
        this.phase = phase;
        this.matrices = matrices;
        this.tickDelta = tickDelta;
    }

    public Phase getPhase() {
        return this.phase;
    }

    public MatrixStack getMatrices() {
        return this.matrices;
    }

    public float getTickDelta() {
        return this.tickDelta;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public void setMatrices(MatrixStack matrices) {
        this.matrices = matrices;
    }

    public void setTickDelta(float tickDelta) {
        this.tickDelta = tickDelta;
    }

    public static enum Phase {
        PRE,
        POST;

    }
}

