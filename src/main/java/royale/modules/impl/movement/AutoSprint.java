package royale.modules.impl.movement;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import royale.events.api.EventHandler;
import royale.events.impl.PacketEvent;
import royale.events.impl.TickEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.util.Instance;
public class AutoSprint extends ModuleStructure {
public static AutoSprint getInstance() {
return (AutoSprint)Instance.get(AutoSprint.class);
}
private static volatile boolean serverSprintState = false;
public AutoSprint() {
super("AutoSprint", "Автоматически удерживает спринт при движении вперед", ModuleCategory.MOVEMENT);
}
@EventHandler
public void onPacket(PacketEvent event) {
ClientCommandC2SPacket packet;
if (event.getType() != PacketEvent.Type.SEND) {
return;
}
Packet Packet = event.getPacket(); if (Packet instanceof ClientCommandC2SPacket) { packet = (ClientCommandC2SPacket)Packet; }
else
{ return; }
if (packet.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING) {
if (serverSprintState) {
event.cancel();
return;
} 
serverSprintState = true;
} else if (packet.getMode() == ClientCommandC2SPacket.Mode.STOP_SPRINTING) {
if (!serverSprintState) {
event.cancel();
return;
} 
serverSprintState = false;
} 
}
public static boolean isServerSprinting() {
return serverSprintState;
}
public static void resetServerState() {
serverSprintState = false;
}
@EventHandler
public void onTick(TickEvent e) {
if (mc.player == null) {
return;
}
processSprint();
}
private void processSprint() {
boolean horizontal = (mc.player.horizontalCollision && !mc.player.collidedSoftly);
boolean sneaking = (mc.player.isSneaking() && !mc.player.isSwimming());
boolean canSprint = (!horizontal && mc.player.forwardSpeed > 0.0F);
if (sneaking) {
return;
}
if (canSprint && !mc.player.isSprinting()) {
mc.player.setSprinting(true);
}
}
public void deactivate() {
resetServerState();
}
}


