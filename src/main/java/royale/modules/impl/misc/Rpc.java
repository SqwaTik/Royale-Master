package royale.modules.impl.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import royale.events.api.EventHandler;
import royale.events.impl.AttackEvent;
import royale.events.impl.TickEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.util.rpc.DiscordRpcService;

public class Rpc extends ModuleStructure {
    private static final long HIT_CONFIRM_TIMEOUT_MS = 1500L;

    private int pendingTargetId = -1;
    private long pendingTargetUntilMs = 0L;
    private int previousSelfHurtTime = 0;

    private boolean hasPositionSnapshot = false;
    private double lastX;
    private double lastY;
    private double lastZ;
    private float lastYaw;
    private float lastPitch;

    public Rpc() {
        super("RPC", "Показывает статус клиента в Discord", ModuleCategory.MISC);
    }

    @Override
    public void setState(boolean state) {
        boolean previous = isState();
        super.setState(state);

        if (previous == state) {
            return;
        }

        DiscordRpcService service = DiscordRpcService.getInstance();
        if (state) {
            resetRuntimeState();
            try {
                service.start();
                service.markActivity();
                service.tick();
            } catch (Exception ignored) {
            }
        } else {
            resetRuntimeState();
            safeStop(service);
        }
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        if (!isState()) {
            return;
        }

        Entity targetEntity = event.getTarget();
        if (!(targetEntity instanceof PlayerEntity target)) {
            return;
        }

        if (mc.player == null || mc.world == null || !isValidPvpPlayer(target)) {
            return;
        }

        this.pendingTargetId = target.getId();
        this.pendingTargetUntilMs = System.currentTimeMillis() + HIT_CONFIRM_TIMEOUT_MS;
        DiscordRpcService.getInstance().markActivity();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        DiscordRpcService service = DiscordRpcService.getInstance();

        if (isClientClosing()) {
            return;
        }

        if (!isState()) {
            resetRuntimeState();
            return;
        }

        if (mc.player == null || mc.world == null) {
            resetRuntimeState();
            return;
        }

        updateActivity(service);
        updateCombatState(service);
    }

    private void updateActivity(DiscordRpcService service) {
        if (mc.player == null) {
            return;
        }

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();

        if (!this.hasPositionSnapshot) {
            this.hasPositionSnapshot = true;
            this.lastX = x;
            this.lastY = y;
            this.lastZ = z;
            this.lastYaw = yaw;
            this.lastPitch = pitch;
            service.markActivity();
            return;
        }

        double dx = x - this.lastX;
        double dy = y - this.lastY;
        double dz = z - this.lastZ;
        double distanceSq = dx * dx + dy * dy + dz * dz;

        float yawDiff = Math.abs(MathHelper.wrapDegrees(yaw - this.lastYaw));
        float pitchDiff = Math.abs(MathHelper.wrapDegrees(pitch - this.lastPitch));

        boolean moved = distanceSq > 8.0E-4D;
        boolean rotated = yawDiff > 0.9F || pitchDiff > 0.9F;
        boolean pressedKeys = mc.options.forwardKey.isPressed()
                || mc.options.backKey.isPressed()
                || mc.options.leftKey.isPressed()
                || mc.options.rightKey.isPressed()
                || mc.options.jumpKey.isPressed()
                || mc.options.attackKey.isPressed()
                || mc.options.useKey.isPressed();

        if (moved || rotated || pressedKeys || mc.player.handSwinging) {
            service.markActivity();
        }

        this.lastX = x;
        this.lastY = y;
        this.lastZ = z;
        this.lastYaw = yaw;
        this.lastPitch = pitch;
    }

    private void updateCombatState(DiscordRpcService service) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        int hurtTime = mc.player.hurtTime;
        if (hurtTime > 0 && this.previousSelfHurtTime <= 0 && isAttackedByValidPlayer()) {
            service.markCombat();
            service.markActivity();
        }
        this.previousSelfHurtTime = hurtTime;

        if (this.pendingTargetId == -1) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now > this.pendingTargetUntilMs) {
            this.pendingTargetId = -1;
            this.pendingTargetUntilMs = 0L;
            return;
        }

        Entity entity = mc.world.getEntityById(this.pendingTargetId);
        if (entity instanceof PlayerEntity target && isValidPvpPlayer(target) && target.hurtTime > 0) {
            service.markCombat();
            service.markActivity();
            this.pendingTargetId = -1;
            this.pendingTargetUntilMs = 0L;
        }
    }

    private boolean isAttackedByValidPlayer() {
        if (mc.player == null) {
            return false;
        }

        LivingEntity attacker = mc.player.getAttacker();
        if (!(attacker instanceof PlayerEntity playerAttacker)) {
            return false;
        }

        return isValidPvpPlayer(playerAttacker);
    }

    private boolean isValidPvpPlayer(PlayerEntity player) {
        if (player == null || mc.player == null || mc.world == null) {
            return false;
        }
        if (player == mc.player || player.isRemoved() || !player.isAlive() || player.isSpectator()) {
            return false;
        }

        String name = player.getName() != null ? player.getName().getString() : "";
        if (name.isBlank()) {
            return false;
        }

        String upper = name.toUpperCase();
        if (upper.contains("NPC") || upper.startsWith("[ZNPC]") || upper.startsWith("CIT-")) {
            return false;
        }

        return mc.getNetworkHandler() == null || mc.getNetworkHandler().getPlayerListEntry(player.getUuid()) != null;
    }

    private void resetRuntimeState() {
        this.pendingTargetId = -1;
        this.pendingTargetUntilMs = 0L;
        this.previousSelfHurtTime = 0;
        this.hasPositionSnapshot = false;
    }

    private boolean isClientClosing() {
        if (mc == null || mc.getWindow() == null) {
            return false;
        }
        long handle = mc.getWindow().getHandle();
        return handle != 0L && GLFW.glfwWindowShouldClose(handle);
    }

    private static void safeStop(DiscordRpcService service) {
        try {
            service.stop();
        } catch (Exception ignored) {
        }
    }
}
