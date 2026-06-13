package royale.modules.impl.render;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.ColorSetting;
import royale.util.ColorUtil;
public class ChinaHat
extends ModuleStructure
{
private static ChinaHat instance;
public static ChinaHat getInstance() {
return instance;
}
public final ColorSetting color1 = (new ColorSetting("Цвет 1", "Первый цвет градиента"))
.value(ColorUtil.getColor(255, 50, 100, 255));
public final ColorSetting color2 = (new ColorSetting("Цвет 2", "Второй цвет градиента"))
.value(ColorUtil.getColor(100, 50, 255, 255));
public ChinaHat() {
super("ChinaHat", "Рисует декоративную шляпу над игроком", ModuleCategory.RENDER);
instance = this;
settings(new Setting[] { (Setting)this.color1, (Setting)this.color2 });
}
}


