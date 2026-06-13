package royale.modules.impl.render;
import royale.events.api.EventHandler;
import royale.events.impl.TickEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.implement.SelectSetting;
import royale.util.Instance;
public class ItemPhysic
extends ModuleStructure
{
public static ItemPhysic getInstance() {
return (ItemPhysic)Instance.get(ItemPhysic.class);
}
public final SelectSetting mode = (new SelectSetting("Физика", "Режим физики предметов")).value(new String[] { "Обычная" }).selected("Обычная");
public ItemPhysic() {
super("ItemPhysic", "Изменяет анимацию и поведение предметов на земле", ModuleCategory.RENDER);
}
@EventHandler
public void onTick(TickEvent e) {}
}


