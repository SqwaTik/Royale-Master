package royale.util.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class CombatTargetPriority {
    private static final long OUTGOING_HOLD_MS = 3500L;
    private static final long INCOMING_HOLD_MS = 2500L;

    private static int lastCombatTargetId = Integer.MIN_VALUE;
    private static long lastCombatTargetUntilMs = 0L;

    private CombatTargetPriority() {
    }

    public static void recordOutgoingHit(Entity target) {
        if (!(target instanceof PlayerEntity player) || !isUsablePlayer(player)) {
            return;
        }

        lastCombatTargetId = player.getId();
        lastCombatTargetUntilMs = System.currentTimeMillis() + OUTGOING_HOLD_MS;
    }

    public static LivingEntity resolvePreferredTarget(double maxDistanceSq) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) {
            clear();
            return null;
        }

        long now = System.currentTimeMillis();

        PlayerEntity attacker = mc.player.getAttacker() instanceof PlayerEntity playerAttacker ? playerAttacker : null;
        if (isUsableForClient(mc, attacker, maxDistanceSq)) {
            lastCombatTargetId = attacker.getId();
            lastCombatTargetUntilMs = now + INCOMING_HOLD_MS;
            return attacker;
        }

        if (now > lastCombatTargetUntilMs || lastCombatTargetId == Integer.MIN_VALUE) {
            return null;
        }

        Entity tracked = mc.world.getEntityById(lastCombatTargetId);
        if (!(tracked instanceof PlayerEntity trackedPlayer) || !isUsableForClient(mc, trackedPlayer, maxDistanceSq)) {
            return null;
        }

        return trackedPlayer;
    }

    private static boolean isUsableForClient(MinecraftClient mc, PlayerEntity player, double maxDistanceSq) {
        if (!isUsablePlayer(player) || player == mc.player || mc.player == null) {
            return false;
        }

        return mc.player.squaredDistanceTo(player) <= maxDistanceSq;
    }

    private static boolean isUsablePlayer(PlayerEntity player) {
        return player != null && player.isAlive() && !player.isRemoved() && !player.isSpectator();
    }

    private static void clear() {
        lastCombatTargetId = Integer.MIN_VALUE;
        lastCombatTargetUntilMs = 0L;
    }
}
