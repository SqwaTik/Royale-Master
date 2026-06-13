package royale.modules.impl.render;
import net.minecraft.util.Hand;
import net.minecraft.client.util.math.MatrixStack;
import royale.events.api.EventHandler;
import royale.events.impl.HandOffsetEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.SliderSettings;
public class ViewModel
extends ModuleStructure
{
private final SliderSettings mainHandXSetting = (new SliderSettings("Основная рука X", "Смещение основной руки по оси X"))
.setValue(0.0F).range(-1.0F, 1.0F);
private final SliderSettings mainHandYSetting = (new SliderSettings("Основная рука Y", "Смещение основной руки по оси Y"))
.setValue(0.0F).range(-1.0F, 1.0F);
private final SliderSettings mainHandZSetting = (new SliderSettings("Основная рука Z", "Смещение основной руки по оси Z"))
.setValue(0.0F).range(-2.5F, 2.5F);
private final SliderSettings offHandXSetting = (new SliderSettings("Вторая рука X", "Смещение второй руки по оси X"))
.setValue(0.0F).range(-1.0F, 1.0F);
private final SliderSettings offHandYSetting = (new SliderSettings("Вторая рука Y", "Смещение второй руки по оси Y"))
.setValue(0.0F).range(-1.0F, 1.0F);
private final SliderSettings offHandZSetting = (new SliderSettings("Вторая рука Z", "Смещение второй руки по оси Z"))
.setValue(0.0F).range(-2.5F, 2.5F);
public ViewModel() {
super("ViewModel", "Настраивает положение предметов в руках", ModuleCategory.RENDER);
settings(new Setting[] { (Setting)this.mainHandXSetting, (Setting)this.mainHandYSetting, (Setting)this.mainHandZSetting, (Setting)this.offHandXSetting, (Setting)this.offHandYSetting, (Setting)this.offHandZSetting });
}
@EventHandler
public void onHandOffset(HandOffsetEvent e) {
Hand hand = e.getHand();
if (hand.equals(Hand.MAIN_HAND) && e.getStack().getItem() instanceof net.minecraft.item.CrossbowItem)
return; 
MatrixStack matrix = e.getMatrices();
if (hand.equals(Hand.MAIN_HAND)) {
matrix.translate(this.mainHandXSetting.getValue(), this.mainHandYSetting.getValue(), this.mainHandZSetting.getValue());
} else {
matrix.translate(this.offHandXSetting.getValue(), this.offHandYSetting.getValue(), this.offHandZSetting.getValue());
} 
}
}


