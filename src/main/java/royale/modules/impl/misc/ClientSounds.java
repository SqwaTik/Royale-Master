package royale.modules.impl.misc;
import royale.events.api.EventHandler;
import royale.events.impl.ModuleToggleEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.SelectSetting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.util.Instance;
import royale.util.sounds.SoundManager;
public class ClientSounds extends ModuleStructure {
public static ClientSounds getInstance() {
return (ClientSounds)Instance.get(ClientSounds.class);
}
private final SelectSetting soundType = (new SelectSetting("Тип звука", "Выбор набора звуков переключения"))
.value(new String[] { "New", "Old"
}).selected("New");
private final SliderSettings volume = (new SliderSettings("Громкость", "Громкость звуков переключения"))
.range(0.1F, 2.0F)
.setValue(1.0F);
public ClientSounds() {
super("ClientSounds", "Проигрывает звуки при включении и выключении модулей", ModuleCategory.MISC);
settings(new Setting[] { (Setting)this.soundType, (Setting)this.volume });
}
@EventHandler
public void onModuleToggle(ModuleToggleEvent event) {
if (mc.player == null || mc.world == null)
return;  if (event.getModule() == this)
return; 
playToggleSound(event.isEnabled());
}
private void playToggleSound(boolean enabled) {
float vol = this.volume.getValue();
if (enabled) {
if (this.soundType.isSelected("New")) {
SoundManager.playSound(SoundManager.MODULE_ENABLE, vol, 1.0F);
} else {
SoundManager.playSound(SoundManager.ON, vol, 1.0F);
}
} else if (this.soundType.isSelected("New")) {
SoundManager.playSound(SoundManager.MODULE_DISABLE, vol, 1.0F);
} else {
SoundManager.playSound(SoundManager.OFF, vol, 1.0F);
} 
}
}


