package royale.modules.impl.render;

import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.BooleanSetting;
import royale.modules.module.setting.implement.MultiSelectSetting;
import royale.util.Instance;

public class Hud extends ModuleStructure {

    public static Hud getInstance() {
        return Instance.get(Hud.class);
    }

    public final MultiSelectSetting interfaceSettings = (new MultiSelectSetting(
            "Элементы",
            "Выбор отображаемых HUD-элементов"
    )).value(new String[]{
            "Watermark", "DebugOverlay", "CustomBar", "HotKeys", "Potions", "Players", "TargetHud", "Info", "Notifications"
    }).selected(new String[]{
            "Watermark", "DebugOverlay", "CustomBar", "HotKeys", "Potions", "Players", "TargetHud", "Info", "Notifications"
    });

    public final BooleanSetting showBps = (new BooleanSetting(
            "Показывать BPS",
            "Показывает скорость в блоках в секунду в элементе Info"
    )).setValue(true).visible(() -> this.interfaceSettings.isSelected("Info"));

    public final BooleanSetting showTps = (new BooleanSetting(
            "Показывать TPS",
            "Показывает TPS сервера в элементе Watermark"
    )).setValue(true).visible(() -> this.interfaceSettings.isSelected("Watermark"));

    public Hud() {
        super("Hud", "Управляет отображением элементов интерфейса", ModuleCategory.RENDER);
        settings(new Setting[]{this.interfaceSettings, this.showBps, this.showTps});
    }
}
