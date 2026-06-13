package royale.events.api.types;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import royale.Initialization;
import royale.events.api.EventHandler;
import royale.events.impl.PacketEvent;
import royale.events.impl.TickEvent;
import royale.events.impl.UsingItemEvent;
public class EventListener
implements Listener {
public static boolean serverSprint;
public static int selectedSlot;
@EventHandler
public void onTick(TickEvent e) {
if (Initialization.getInstance().getManager().getHudManager() != null)
Initialization.getInstance().getManager().getHudManager().tick(); 
}
@EventHandler
public void onPacket(PacketEvent e) {
// Byte code:
//   0: aload_1
//   1: invokevirtual getPacket : ()Lnet/minecraft/Packet;
//   4: dup
//   5: invokestatic requireNonNull : (Ljava/lang/Object;)Ljava/lang/Object;
//   8: pop
//   9: astore_2
//   10: iconst_0
//   11: istore_3
//   12: aload_2
//   13: iload_3
//   14: <illegal opcode> typeSwitch : (Ljava/lang/Object;I)I
//   19: lookupswitch default -> 122, 0 -> 44, 1 -> 105
//   44: aload_2
//   45: checkcast net/minecraft/ClientCommandC2SPacket
//   48: astore #4
//   50: getstatic royale/events/api/types/EventListener$1.$SwitchMap$net$minecraft$network$packet$c2s$play$ClientCommandC2SPacket$Mode : [I
//   53: aload #4
//   55: invokevirtual getMode : ()Lnet/minecraft/ClientCommandC2SPacket$Mode;
//   58: invokevirtual ordinal : ()I
//   61: iaload
//   62: lookupswitch default -> 96, 1 -> 88, 2 -> 92
//   88: iconst_1
//   89: goto -> 99
//   92: iconst_0
//   93: goto -> 99
//   96: getstatic royale/events/api/types/EventListener.serverSprint : Z
//   99: putstatic royale/events/api/types/EventListener.serverSprint : Z
//   102: goto -> 122
//   105: aload_2
//   106: checkcast net/minecraft/UpdateSelectedSlotC2SPacket
//   109: astore #5
//   111: aload #5
//   113: invokevirtual getSelectedSlot : ()I
//   116: putstatic royale/events/api/types/EventListener.selectedSlot : I
//   119: goto -> 122
//   122: invokestatic getInstance : ()Lroyale/Initialization;
//   125: invokevirtual getManager : ()Lroyale/manager/Manager;
//   128: invokevirtual getHudManager : ()Lroyale/client/draggables/HudManager;
//   131: aload_1
//   132: invokevirtual onPacket : (Lroyale/events/impl/PacketEvent;)V
//   135: return
// Line number table:
//   Java source line number -> byte code offset
//   #24	-> 0
//   #25	-> 44
//   #26	-> 88
//   #27	-> 92
//   #28	-> 96
//   #25	-> 102
//   #30	-> 105
//   #34	-> 122
//   #35	-> 135
// Local variable table:
//   start	length	slot	name	descriptor
//   50	55	4	command	Lnet/minecraft/ClientCommandC2SPacket;
//   111	11	5	slot	Lnet/minecraft/UpdateSelectedSlotC2SPacket;
//   0	136	0	this	Lroyale/events/api/types/EventListener;
//   0	136	1	e	Lroyale/events/impl/PacketEvent;
}
@EventHandler
public void onUsingItemEvent(UsingItemEvent e) {}
}


