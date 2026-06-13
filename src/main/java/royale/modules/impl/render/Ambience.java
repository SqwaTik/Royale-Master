package royale.modules.impl.render;
import royale.events.api.EventHandler;
import royale.events.impl.PacketEvent;
import royale.events.impl.TickEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.util.Instance;
public class Ambience
extends ModuleStructure
{
public static Ambience getInstance() {
return (Ambience)Instance.get(Ambience.class);
}
public SliderSettings time = (new SliderSettings("Время", "Время суток (0-24000)"))
.range(0, 24000)
.setValue(1000.0F);
private double animatedTime = 1000.0D;
public Ambience() {
super("Ambience", "Изменяет время мира", ModuleCategory.RENDER);
settings(new Setting[] { (Setting)this.time });
}
public void activate() {
this.animatedTime = this.time.getValue();
}
@EventHandler
public void onTick(TickEvent event) {
double targetTime = this.time.getValue();
double speed = 0.15D;
double diff = targetTime - this.animatedTime;
this.animatedTime += diff * speed;
}
@EventHandler
public void onPacket(PacketEvent event) {
if (event.getPacket() instanceof net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket) {
event.cancel();
}
}
public long getCustomTime() {
return (long)this.animatedTime;
}
}


