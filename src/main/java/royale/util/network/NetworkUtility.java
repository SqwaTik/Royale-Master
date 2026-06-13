package royale.util.network;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PendingUpdateManager;
import royale.IMinecraft;
import royale.mixin.ClientConnectionAccessor;
import royale.mixin.IClientWorld;
import royale.util.angle.Angle;
import royale.util.timer.TimerUtil;

public final class NetworkUtility
implements IMinecraft {
    private static boolean shouldTriggerEvent = true;
    private static boolean serverSprinting = false;
    private static float tpsFactor = 0.0f;
    private static int received = 0;
    private static long lastReceive = 0L;
    private static TimerUtil tpsTimer = new TimerUtil();

    public static void pauseEvents() {
        shouldTriggerEvent = false;
    }

    public static void resumeEvents() {
        shouldTriggerEvent = true;
    }

    public static boolean shouldTriggerEvent() {
        return shouldTriggerEvent;
    }

    public static void updateServerSprint(boolean sprint) {
        serverSprinting = sprint;
    }

    public static boolean serverSprinting() {
        return serverSprinting;
    }

    public static void sendWithoutEvent(Runnable runnable) {
        NetworkUtility.pauseEvents();
        runnable.run();
        NetworkUtility.resumeEvents();
    }

    public static void sendWithoutEvent(Packet<?> packet) {
        NetworkUtility.pauseEvents();
        NetworkUtility.send(packet);
        NetworkUtility.resumeEvents();
    }

    public static void send(Packet<?> packet) {
        if (mc.getNetworkHandler() == null) {
            return;
        }
        if (packet instanceof ClickSlotC2SPacket) {
            ClickSlotC2SPacket click = (ClickSlotC2SPacket)packet;
            NetworkUtility.mc.interactionManager.clickSlot(click.syncId(), (int)click.slot(), (int)click.button(), click.actionType(), (PlayerEntity)NetworkUtility.mc.player);
        } else {
            mc.getNetworkHandler().sendPacket(packet);
        }
    }

    public static void sendInputPacket(boolean forward, boolean backward, boolean left, boolean right, boolean jump, boolean sneak, boolean sprint) {
        PlayerInput input = new PlayerInput(forward, backward, left, right, jump, sneak, sprint);
        mc.getNetworkHandler().sendPacket((Packet)new PlayerInputC2SPacket(input));
    }

    public static void sendOnlySneak(boolean sneak) {
        PlayerInput playerInput = NetworkUtility.mc.player.input.playerInput;
        NetworkUtility.sendInputPacket(playerInput.forward(), playerInput.backward(), playerInput.left(), playerInput.right(), playerInput.jump(), sneak, playerInput.sprint());
    }

    public static void sendUse(Hand hand) {
        NetworkUtility.sendUse(hand, new Angle(NetworkUtility.mc.player.getYaw(), NetworkUtility.mc.player.getPitch()));
    }

    public static void sendUse(Hand hand, Angle angle) {
        try (PendingUpdateManager pendingUpdateManager = ((IClientWorld)NetworkUtility.mc.world).client$pending().incrementSequence();){
            int i = pendingUpdateManager.getSequence();
            PlayerInteractItemC2SPacket packet = new PlayerInteractItemC2SPacket(hand, i, angle.getYaw(), angle.getPitch());
            NetworkUtility.send(packet);
        }
    }

    public static void sendUse(Hand hand, BlockHitResult hitResult) {
        try (PendingUpdateManager pendingUpdateManager = ((IClientWorld)NetworkUtility.mc.world).client$pending().incrementSequence();){
            int i = pendingUpdateManager.getSequence();
            PlayerInteractBlockC2SPacket packet = new PlayerInteractBlockC2SPacket(hand, hitResult, i);
            NetworkUtility.send(packet);
        }
    }

    public static boolean is(String server) {
        return mc.getNetworkHandler() != null && mc.getNetworkHandler().getServerInfo() != null && NetworkUtility.mc.getNetworkHandler().getServerInfo().address.contains(server);
    }

    public static void handleCPacket(Packet<?> packet) {
        if (packet instanceof PlayerMoveC2SPacket) {
            PlayerMoveC2SPacket e = (PlayerMoveC2SPacket)packet;
            PlayerState.lastGround = e.isOnGround();
            PlayerState.lastVertical = NetworkUtility.mc.player.verticalCollision;
        }
    }

    public static void handleSPacket(Packet<?> packet) {
        if (packet instanceof WorldTimeUpdateS2CPacket) {
            WorldTimeUpdateS2CPacket e = (WorldTimeUpdateS2CPacket)packet;
            lastReceive = System.currentTimeMillis();
        }
    }

    public static void handlePacket(Packet<?> packet) {
        ClientPlayNetworkHandler TrapezoidHeightProvider = mc.getNetworkHandler();
        if (!(TrapezoidHeightProvider instanceof ClientPlayNetworkHandler)) {
            return;
        }
        ClientPlayNetworkHandler net = TrapezoidHeightProvider;
        if (mc.isOnThread()) {
            ClientConnectionAccessor.handlePacket(packet, (PacketListener)net);
        } else {
            mc.execute(() -> ClientConnectionAccessor.handlePacket(packet, (PacketListener)net));
        }
    }

    public static UUID offlineUUID(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    private NetworkUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static float getTpsFactor() {
        return tpsFactor;
    }

    public static final class PlayerState {
        public static boolean lastGround = false;
        public static boolean lastVertical = false;
        public static int lastTp = 0;

        private PlayerState() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }
    }
}

