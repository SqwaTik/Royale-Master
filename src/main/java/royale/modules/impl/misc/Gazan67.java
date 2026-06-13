package royale.modules.impl.misc;

import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.util.Instance;
import royale.util.sounds.SoundManager;

public class Gazan67 extends ModuleStructure {
    private final SliderSettings volume = (new SliderSettings("Громкость", "Громкость звука 67"))
        .range(0.1F, 2.0F)
        .setValue(1.0F);

    public Gazan67() {
        super("67", "Проигрывает звук gazan67", ModuleCategory.MISC);
        settings(new Setting[]{this.volume});
    }

    public static Gazan67 getInstance() {
        return Instance.get(Gazan67.class);
    }

    @Override
    public void activate() {
        SoundManager.playSoundDirect(SoundManager.GAZAN67, this.volume.getValue(), 1.0F);
    }
}