package royale.modules.impl.misc;

import java.util.UUID;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import royale.events.api.EventHandler;
import royale.events.impl.PacketEvent;
import royale.events.impl.TickEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.util.timer.TimerUtil;

public class ServerRPSpoofer
extends ModuleStructure {
    private ResourcePackAction currentAction = ResourcePackAction.IDLE;
    private final TimerUtil counter = TimerUtil.create();
    private UUID currentPackId = null;

    public ServerRPSpoofer() {
        super("SRPOff", "\u041f\u0440\u043e\u043f\u0443\u0441\u043a\u0430\u0435\u0442 \u0441\u0435\u0440\u0432\u0435\u0440\u043d\u044b\u0439 \u0440\u0435\u0441\u0443\u0440\u0441\u043f\u0430\u043a \u0431\u0435\u0437 \u0441\u043a\u0430\u0447\u0438\u0432\u0430\u043d\u0438\u044f", ModuleCategory.MISC);
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (event.getType() != PacketEvent.Type.RECEIVE) {
            return;
        }
        Packet<?> class_25962 = event.getPacket();
        if (class_25962 instanceof ResourcePackSendS2CPacket) {
            ResourcePackSendS2CPacket packet = (ResourcePackSendS2CPacket)class_25962;
            this.currentPackId = packet.id();
            this.currentAction = ResourcePackAction.SEND_ACCEPTED;
            this.counter.resetCounter();
            event.cancel();
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        ClientPlayNetworkHandler networkHandler = mc.getNetworkHandler();
        if (networkHandler == null || this.currentPackId == null) {
            return;
        }
        this.processResourcePackAction(networkHandler);
    }

    private void processResourcePackAction(ClientPlayNetworkHandler networkHandler) {
        if (this.currentAction == ResourcePackAction.SEND_ACCEPTED) {
            networkHandler.sendPacket((Packet)new ResourcePackStatusC2SPacket(this.currentPackId, ResourcePackStatusC2SPacket.Status.ACCEPTED));
            this.currentAction = ResourcePackAction.SEND_DOWNLOADED;
            this.counter.resetCounter();
            return;
        }
        if (this.currentAction == ResourcePackAction.SEND_DOWNLOADED && this.counter.isReached(25L)) {
            networkHandler.sendPacket((Packet)new ResourcePackStatusC2SPacket(this.currentPackId, ResourcePackStatusC2SPacket.Status.DOWNLOADED));
            this.currentAction = ResourcePackAction.SEND_SUCCESS;
            this.counter.resetCounter();
            return;
        }
        if (this.currentAction == ResourcePackAction.SEND_SUCCESS && this.counter.isReached(25L)) {
            networkHandler.sendPacket((Packet)new ResourcePackStatusC2SPacket(this.currentPackId, ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED));
            this.currentAction = ResourcePackAction.IDLE;
            this.currentPackId = null;
        }
    }

    @Override
    public void deactivate() {
        this.currentAction = ResourcePackAction.IDLE;
        this.currentPackId = null;
        super.deactivate();
    }

    private static enum ResourcePackAction {
        SEND_ACCEPTED,
        SEND_DOWNLOADED,
        SEND_SUCCESS,
        IDLE;

    }
}


