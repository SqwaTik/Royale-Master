package royale.util.tps;
import net.minecraft.util.math.MathHelper;
import royale.Initialization;
import royale.events.api.EventHandler;
import royale.events.api.EventManager;
import royale.events.impl.PacketEvent;
public class TPSCalculate
{
private static TPSCalculate instance;
private float TPS = 20.0F; public float getTPS() { return this.TPS; }
private long timestamp; private float adjustTicks = 0.0F; public float getAdjustTicks() { return this.adjustTicks; } public long getTimestamp() {
return this.timestamp;
}
public TPSCalculate() {
instance = this;
Initialization.getInstance().getManager().getEventManager(); EventManager.register(this);
}
public static TPSCalculate getInstance() {
return instance;
}
@EventHandler
private void onPacket(PacketEvent e) {
if (e.getPacket() instanceof net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket) {
updateTPS();
}
}
private void updateTPS() {
long delay = System.nanoTime() - this.timestamp;
float maxTPS = 20.0F;
float rawTPS = maxTPS * 1.0E9F / (float)delay;
float boundedTPS = MathHelper.clamp(rawTPS, 0.0F, maxTPS);
this.TPS = (float)round(boundedTPS);
this.adjustTicks = boundedTPS - maxTPS;
this.timestamp = System.nanoTime();
}
public double round(double input) {
return Math.round(input * 100.0D) / 100.0D;
}
public float getTpsRounded() {
return (float)(Math.round(this.TPS * 2.0F) / 2.0D);
}
}


