package royale.events.impl;

import net.minecraft.network.packet.Packet;
import royale.events.api.events.callables.EventCancellable;

public class PacketEvent
extends EventCancellable {
    private Packet<?> packet;
    private Type type;

    public boolean isSend() {
        return this.type.equals((Object)Type.SEND);
    }

    public Packet<?> getPacket() {
        return this.packet;
    }

    public Type getType() {
        return this.type;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public PacketEvent(Packet<?> packet, Type type) {
        this.packet = packet;
        this.type = type;
    }

    public static enum Type {
        SEND,
        RECEIVE;

    }
}

