package royale.modules.impl.render;

import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.SelectSetting;
import royale.util.Instance;

public class FullBright extends ModuleStructure {
    public final SelectSetting mode = (new SelectSetting(
            "Режим",
            "Как усиливать освещение"
    )).value(new String[]{"Обычный", "Динамичный"}).selected("Обычный");

    public FullBright() {
        super("FullBright", "Повышает яркость в темных зонах", ModuleCategory.RENDER);
        settings(new Setting[]{this.mode});
    }

    public static FullBright getInstance() {
        return Instance.get(FullBright.class);
    }

    public boolean isDynamicMode() {
        return this.mode.isSelected("Динамичный")
                || this.mode.isSelected("Умный");
    }
}
