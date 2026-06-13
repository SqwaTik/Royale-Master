package royale.modules.impl.combat;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.LivingEntity;
import royale.events.api.EventHandler;
import royale.events.impl.AttackEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.implement.SelectSetting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.util.Instance;
import royale.util.sounds.SoundManager;

public class HitSound
extends ModuleStructure {
    private final SelectSetting soundType = new SelectSetting("\u0422\u0438\u043f \u0437\u0432\u0443\u043a\u0430", "Select sound type").value("Moan", "Metallic", "Crime").selected("Moan");
    private final SliderSettings volume = new SliderSettings("\u0413\u0440\u043e\u043c\u043a\u043e\u0441\u0442\u044c", "Set volume").range(0.1f, 2.0f).setValue(1.0f);

    public static HitSound getInstance() {
        return Instance.get(HitSound.class);
    }

    public HitSound() {
        super("HitSound", ModuleCategory.COMBAT);
        this.settings(this.soundType, this.volume);
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        if (HitSound.mc.player == null || HitSound.mc.world == null) {
            return;
        }
        if (!(event.getTarget() instanceof LivingEntity)) {
            return;
        }
        this.playSelectedSound();
    }
    private void playSelectedSound() {
        float vol = this.volume.getValue();
        if (this.soundType.isSelected("Moan")) {
            this.playRandomMoan(vol, 1.0f);
        }
        if (this.soundType.isSelected("Metallic")) {
            SoundManager.playSound(SoundManager.METALLIC, vol, 1.0f);
        }
        if (this.soundType.isSelected("Crime")) {
            SoundManager.playSound(SoundManager.CRIME, vol, 1.0f);
        }
    }

    private void playRandomMoan(float volume, float pitch) {
        int random = ThreadLocalRandom.current().nextInt(4);
        switch (random) {
            case 0: {
                SoundManager.playSound(SoundManager.MOAN1, volume, pitch);
                break;
            }
            case 1: {
                SoundManager.playSound(SoundManager.MOAN2, volume, pitch);
                break;
            }
            case 2: {
                SoundManager.playSound(SoundManager.MOAN3, volume, pitch);
                break;
            }
            case 3: {
                SoundManager.playSound(SoundManager.MOAN4, volume, pitch);
            }
        }
    }
}

