package royale.util.inventory;

import net.minecraft.client.MinecraftClient;
import royale.util.inventory.InventoryUtils;
import royale.util.inventory.MovementController;
import royale.util.inventory.SwapSettings;

public class SwapExecutor {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private Phase phase = Phase.IDLE;
    private final MovementController movement = new MovementController();
    private SwapSettings settings = SwapSettings.defaults();
    private Runnable swapAction;
    private Runnable onComplete;
    private long phaseStartTime;
    private int currentDelay;

    public void execute(Runnable swapAction, SwapSettings settings) {
        this.execute(swapAction, settings, null);
    }

    public void execute(Runnable swapAction, SwapSettings settings, Runnable onComplete) {
        if (this.phase != Phase.IDLE) {
            return;
        }
        this.swapAction = swapAction;
        this.settings = settings != null ? settings : SwapSettings.defaults();
        this.onComplete = onComplete;
        if (this.settings.shouldStopMovement()) {
            this.movement.saveState();
            this.movement.block();
            if (this.settings.shouldStopSprint()) {
                this.movement.stopSprint();
            }
            this.startPhase(Phase.PRE_STOP, this.settings.randomPreStopDelay());
        } else {
            this.startPhase(Phase.SWAPPING, 0);
        }
    }

    public void tick() {
        if (this.phase == Phase.IDLE || this.phase == Phase.FINISHED) {
            return;
        }
        if (SwapExecutor.mc.player == null) {
            this.reset();
            return;
        }
        if (this.settings.shouldStopMovement() && this.phase != Phase.RESUMING && this.phase != Phase.FINISHED) {
            this.movement.block();
            if (this.settings.shouldStopSprint()) {
                this.movement.stopSprint();
            }
        }
        boolean continueProcessing = true;
        int maxIterations = 10;
        for (int iterations = 0; continueProcessing && iterations < maxIterations; ++iterations) {
            continueProcessing = this.processPhase();
        }
    }

    private boolean processPhase() {
        long elapsed = System.currentTimeMillis() - this.phaseStartTime;
        switch (this.phase.ordinal()) {
            case 1: {
                this.movement.block();
                if (this.settings.shouldStopSprint()) {
                    this.movement.stopSprint();
                }
                if (elapsed < (long)this.currentDelay) break;
                this.startPhase(Phase.STOPPING, 0);
                return true;
            }
            case 2: {
                this.movement.block();
                if (this.settings.shouldStopSprint()) {
                    this.movement.stopSprint();
                }
                this.startPhase(Phase.WAIT_STOP, this.settings.randomWaitStopDelay());
                return this.currentDelay == 0;
            }
            case 3: {
                boolean timeout;
                this.movement.block();
                if (this.settings.shouldStopSprint()) {
                    this.movement.stopSprint();
                }
                boolean stopped = this.movement.isPlayerStopped(this.settings.getVelocityThreshold());
                boolean bl = timeout = elapsed >= (long)this.currentDelay;
                if (!stopped && !timeout) break;
                this.startPhase(Phase.PRE_SWAP, this.settings.randomPreSwapDelay());
                return this.currentDelay == 0;
            }
            case 4: {
                this.movement.block();
                if (this.settings.shouldStopSprint()) {
                    this.movement.stopSprint();
                }
                if (elapsed < (long)this.currentDelay) break;
                this.startPhase(Phase.SWAPPING, 0);
                return true;
            }
            case 5: {
                this.movement.block();
                if (this.settings.shouldStopSprint()) {
                    this.movement.stopSprint();
                }
                if (this.swapAction != null) {
                    this.swapAction.run();
                }
                this.startPhase(Phase.POST_SWAP, this.settings.randomPostSwapDelay());
                return this.currentDelay == 0;
            }
            case 6: {
                this.movement.block();
                if (elapsed < (long)this.currentDelay) break;
                if (this.settings.shouldCloseInventory()) {
                    InventoryUtils.closeScreen();
                }
                this.startPhase(Phase.RESUMING, this.settings.randomResumeDelay());
                return this.currentDelay == 0;
            }
            case 7: {
                if (elapsed < (long)this.currentDelay) break;
                if (this.settings.shouldStopMovement()) {
                    this.movement.restoreFromCurrent();
                }
                this.phase = Phase.FINISHED;
                if (this.onComplete != null) {
                    this.onComplete.run();
                }
                this.reset();
                return false;
            }
        }
        return false;
    }

    private void startPhase(Phase newPhase, int delay) {
        this.phase = newPhase;
        this.phaseStartTime = System.currentTimeMillis();
        this.currentDelay = delay;
    }

    public void cancel() {
        if (this.movement.isBlocked()) {
            this.movement.restoreFromCurrent();
        }
        this.reset();
    }

    public void reset() {
        this.phase = Phase.IDLE;
        this.swapAction = null;
        this.onComplete = null;
        this.movement.reset();
    }

    public boolean isRunning() {
        return this.phase != Phase.IDLE && this.phase != Phase.FINISHED;
    }

    public boolean isBlocking() {
        return this.movement.isBlocked() || this.isRunning() && this.settings.shouldStopMovement();
    }

    public Phase getPhase() {
        return this.phase;
    }

    public static enum Phase {
        IDLE,
        PRE_STOP,
        STOPPING,
        WAIT_STOP,
        PRE_SWAP,
        SWAPPING,
        POST_SWAP,
        RESUMING,
        FINISHED;

    }
}

