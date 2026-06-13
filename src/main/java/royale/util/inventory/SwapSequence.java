package royale.util.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class SwapSequence {
    private final List<SwapStep> steps = new ArrayList<SwapStep>();
    private int currentIndex;
    private int tickCounter;
    private boolean running;

    public SwapSequence step(int delayTicks, Runnable action) {
        return this.step(delayTicks, action, () -> true);
    }

    public SwapSequence step(int delayTicks, Runnable action, BooleanSupplier condition) {
        this.steps.add(new SwapStep(delayTicks, action, condition));
        return this;
    }

    public SwapSequence start() {
        this.currentIndex = 0;
        this.tickCounter = 0;
        this.running = true;
        return this;
    }

    public void tick() {
        if (!this.running || this.currentIndex >= this.steps.size()) {
            this.running = false;
            return;
        }
        SwapStep current = this.steps.get(this.currentIndex);
        if (!current.condition.getAsBoolean()) {
            return;
        }
        if (this.tickCounter >= current.delayTicks) {
            current.action.run();
            ++this.currentIndex;
            this.tickCounter = 0;
        } else {
            ++this.tickCounter;
        }
    }

    public boolean isFinished() {
        return !this.running || this.currentIndex >= this.steps.size();
    }

    public void reset() {
        this.steps.clear();
        this.currentIndex = 0;
        this.tickCounter = 0;
        this.running = false;
    }

    public void cancel() {
        this.running = false;
    }

    private record SwapStep(int delayTicks, Runnable action, BooleanSupplier condition) {
    }
}

