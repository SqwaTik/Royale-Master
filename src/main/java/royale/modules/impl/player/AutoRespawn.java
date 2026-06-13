package royale.modules.impl.player;
import royale.events.api.EventHandler;
import royale.events.impl.DeathScreenEvent;
import royale.events.impl.PacketEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.SelectSetting;
public class AutoRespawn
extends ModuleStructure
{
private final SelectSetting modeSetting = (new SelectSetting("Режим", "Выберите, что будет использоваться")).value(new String[] { "Default" });
public AutoRespawn() {
super("AutoRespawn", "Auto Respawn", ModuleCategory.PLAYER);
settings(new Setting[] { (Setting)this.modeSetting });
}
@EventHandler
public void onPacket(PacketEvent e) {}
@EventHandler
public void onDeathScreen(DeathScreenEvent e) {
if (this.modeSetting.isSelected("Default")) {
mc.player.requestRespawn();
mc.setScreen(null);
} 
}
}


