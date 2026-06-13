package royale.mixin;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.WriteBufferWaterMark;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.handler.PacketSizeLogger;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import royale.events.api.EventManager;
import royale.events.api.events.Event;
import royale.events.impl.PacketEvent;
import royale.util.network.ViaFabricPlusBridge;
@Mixin({ClientConnection.class})
public class ClientConnectionMixin {
@Unique
private boolean royale$skipPacketEvent;
@Inject(method = {"handlePacket"}, at = {@At("HEAD")}, cancellable = true)
private static <T extends PacketListener> void handlePacketPre(Packet<T> packet, PacketListener listener, CallbackInfo info) {
PacketEvent packetEvent = new PacketEvent(packet, PacketEvent.Type.RECEIVE);
EventManager.callEvent((Event)packetEvent);
if (packetEvent.isCancelled()) {
info.cancel();
}
}
@Inject(method = {"sendInternal"}, at = {@At("HEAD")}, cancellable = true)
private void sendPre(Packet<?> packet, ChannelFutureListener callbacks, boolean flush, CallbackInfo info) {
if (this.royale$skipPacketEvent) {
return;
}
PacketEvent packetEvent = new PacketEvent(packet, PacketEvent.Type.SEND);
EventManager.callEvent((Event)packetEvent);
if (packetEvent.isCancelled()) {
info.cancel();
return;
}
Packet<?> modifiedPacket = packetEvent.getPacket();
if (modifiedPacket != packet) {
resendModifiedPacket(modifiedPacket, info);
}
}
@Inject(method = {"addHandlers"}, at = {@At("RETURN")})
private static void addHandlersHook(ChannelPipeline pipeline, NetworkSide side, boolean local, PacketSizeLogger packetSizeLogger, CallbackInfo ci) {
if (!local) {
applyLowLatencyChannelOptions(pipeline);
ViaFabricPlusBridge.injectPreviousVersionReset(pipeline.channel());
}
}
private static void applyLowLatencyChannelOptions(ChannelPipeline pipeline) {
if (pipeline == null || pipeline.channel() == null) {
return;
}
try {
pipeline.channel().config().setOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
} catch (Throwable throwable) {}
try {
pipeline.channel().config().setOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);
} catch (Throwable throwable) {}
try {
pipeline.channel().config().setOption(ChannelOption.SO_RCVBUF, Integer.valueOf(262144));
} catch (Throwable throwable) {}
try {
pipeline.channel().config().setOption(ChannelOption.SO_SNDBUF, Integer.valueOf(131072));
} catch (Throwable throwable) {}
try {
pipeline.channel().config().setOption(ChannelOption.AUTO_READ, Boolean.TRUE);
} catch (Throwable throwable) {}
try {
pipeline.channel().config().setOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
} catch (Throwable throwable) {}
try {
pipeline.channel().config().setOption(ChannelOption.IP_TOS, Integer.valueOf(16));
} catch (Throwable throwable) {}
try {
pipeline.channel().config().setWriteBufferWaterMark(new WriteBufferWaterMark(32768, 262144));
} catch (Throwable throwable) {}
}
@Unique
private void resendModifiedPacket(Packet<?> packet, CallbackInfo info) {
this.royale$skipPacketEvent = true;
try {
((ClientConnection)(Object)this).send(packet);
} finally {
this.royale$skipPacketEvent = false;
}
info.cancel();
}
}
