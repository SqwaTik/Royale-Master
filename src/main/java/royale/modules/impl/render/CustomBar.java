package royale.modules.impl.render;

import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.BooleanSetting;
import royale.modules.module.setting.implement.SelectSetting;
import royale.util.Instance;

public class CustomBar extends ModuleStructure {

    public final SelectSetting mode = (new SelectSetting(
            "Анимация",
            "Как двигается выделение слота"
    )).value(new String[]{"Плавно", "Резко"}).selected("Плавно");

    public final SelectSetting type = (new SelectSetting(
            "Форма",
            "Форма панели хотбара"
    )).value(new String[]{"Без скругления", "Скругленная"}).selected("Скругленная");

    public final BooleanSetting showHotbar = (new BooleanSetting(
            "Показывать хотбар",
            "Рисует кастомный хотбар"
    )).setValue(true);

    public CustomBar() {
        super("CustomBar", "Меняет внешний вид хотбара", ModuleCategory.RENDER);
        settings(new Setting[]{this.mode, this.type, this.showHotbar});
    }

    public static CustomBar getInstance() {
        return Instance.get(CustomBar.class);
    }
}
