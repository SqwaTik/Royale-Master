package royale.modules.impl.render;

import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.BooleanSetting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.util.Instance;

public class CropTimer extends ModuleStructure {

    public final BooleanSetting showTitle = (new BooleanSetting(
            "Название", "Показывать название культуры"
    )).setValue(true);

    public final BooleanSetting showStage = (new BooleanSetting(
            "Стадия", "Показывать стадию роста"
    )).setValue(true);

    public final BooleanSetting showTime = (new BooleanSetting(
            "Время", "Показывать примерное время до созревания"
    )).setValue(true);

    public final SliderSettings updateInterval = (new SliderSettings(
            "Обновление", "Задержка обновления (мс)"
    )).setValue(500.0F).range(100, 2000);

    public CropTimer() {
        super("CropTimer", "Отслеживает время роста культур на экране", ModuleCategory.RENDER);
        settings(new Setting[]{ showTitle, showStage, showTime, updateInterval });
    }

    public static CropTimer getInstance() {
        return Instance.get(CropTimer.class);
    }
}