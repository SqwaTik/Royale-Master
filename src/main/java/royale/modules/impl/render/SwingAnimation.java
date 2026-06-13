package royale.modules.impl.render;
import net.minecraft.util.Hand;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionfc;
import royale.events.api.EventHandler;
import royale.events.impl.HandAnimationEvent;
import royale.events.impl.SwingDurationEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.BooleanSetting;
import royale.modules.module.setting.implement.SelectSetting;
import royale.modules.module.setting.implement.SliderSettings;
public class SwingAnimation
extends ModuleStructure
{
private final SelectSetting swingType = (new SelectSetting("Тип взмаха", "Выберите стиль анимации удара"))
.value(new String[] { "Chop", "Swipe", "Down", "Smooth", "Smooth 2", "Power", "Feast", "Twist", "Default" });
private final SliderSettings hitStrengthSetting = (new SliderSettings("Сила взмаха", "Насколько сильно меняется анимация удара"))
.range(0.5F, 3.0F)
.setValue(1.0F);
private float spinAngle;
private final SliderSettings swingSpeedSetting = (new SliderSettings("Длительность взмаха", "Скорость анимации удара"))
.range(0.5F, 4.0F)
.setValue(1.0F); private float spinBackTimer;
private boolean wasSwinging;
private final BooleanSetting onlySwing = (new BooleanSetting("Только при взмахе", "Показывать анимацию только в момент удара"))
.setValue(false);
public SwingAnimation() {
super("SwingAnimation", "Меняет анимацию удара рукой", ModuleCategory.RENDER);
this.spinAngle = 0.0F;
this.spinBackTimer = 0.0F;
this.wasSwinging = false;
settings(new Setting[] { (Setting)this.swingType, (Setting)this.hitStrengthSetting, (Setting)this.swingSpeedSetting, (Setting)this.onlySwing });
}
@EventHandler
public void onHandAnimation(HandAnimationEvent e) {
boolean isMainHand = e.getHand().equals(Hand.MAIN_HAND);
if (isMainHand) {
MatrixStack matrix = e.getMatrices();
float swingProgress = e.getSwingProgress();
int i = mc.player.getMainArm().equals(Arm.RIGHT) ? 1 : -1;
float sin1 = MathHelper.sin((swingProgress * swingProgress * 3.1415927F));
float sin2 = MathHelper.sin((MathHelper.sqrt(swingProgress) * 3.1415927F));
float sinSmooth = (float)(Math.sin(swingProgress * Math.PI) * 0.5D);
float strength = this.hitStrengthSetting.getValue();
if (!this.onlySwing.isValue() || mc.player.handSwingTicks != 0) {
float f2; switch (this.swingType.getSelected()) {
case "Chop":
matrix.translate(0.56F * i, -0.44F, -0.72F);
matrix.translate(0.0F, -0.19800001F, 0.0F);
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees(45.0F * i));
f2 = MathHelper.sin((MathHelper.sqrt(swingProgress) * 3.1415927F));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Z.rotationDegrees(f2 * -20.0F * i * strength));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(f2 * -80.0F * strength));
matrix.translate(0.4F, 0.2F, 0.2F);
matrix.translate(-0.5F, 0.08F, 0.0F);
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees(20.0F));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees(20.0F));
break;
case "Twist":
matrix.translate(i * 0.56F, -0.36F, -0.72F);
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees((80 * i)));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -90.0F * strength));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Z.rotationDegrees((sin1 - sin2) * 60.0F * i * strength));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(-30.0F));
matrix.translate(0.0F, -0.1F, 0.05F);
break;
case "Swipe":
matrix.translate(0.56F * i, -0.32F, -0.72F);
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees((70 * i)));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Z.rotationDegrees((-20 * i)));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * sin1 * -5.0F * strength));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(sin2 * sin1 * -120.0F * strength));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(-70.0F));
break;
case "Default":
matrix.translate(i * 0.56F, -0.52F - sin2 * 0.5F * strength, -0.72F);
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees((45 * i)));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees((-45 * i)));
break;
case "Down":
matrix.translate(i * 0.56F, -0.32F, -0.72F);
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees((76 * i)));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * -5.0F * strength));
matrix.multiply((Quaternionfc)RotationAxis.NEGATIVE_X.rotationDegrees(sin2 * -100.0F * strength));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -155.0F * strength));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(-100.0F));
break;
case "Smooth":
matrix.translate(i * 0.56F, -0.42F, -0.72F);
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees(i * (45.0F + sin1 * -20.0F * strength)));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Z.rotationDegrees(i * sin2 * -20.0F * strength));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -80.0F * strength));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees(i * -45.0F));
matrix.translate(0.0D, -0.1D, 0.0D);
break;
case "Smooth 2":
matrix.translate(i * 0.56F, -0.42F, -0.72F);
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -80.0F * strength));
matrix.translate(0.0D, -0.1D, 0.0D);
break;
case "Power":
matrix.translate(i * 0.56F, -0.32F, -0.72F);
matrix.translate(-sinSmooth * sinSmooth * sin1 * i * strength, 0.0F, 0.0F);
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees((61 * i)));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Z.rotationDegrees(sin2 * strength));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * sin1 * -5.0F * strength));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(sin2 * sin1 * -30.0F * strength));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(-60.0F));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(sinSmooth * -60.0F * strength));
break;
case "Feast":
matrix.translate(i * 0.56F, -0.32F, -0.72F);
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees((30 * i)));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * 75.0F * i * strength));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -45.0F * strength));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees((30 * i)));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
matrix.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees((35 * i)));
break;
} 
} else {
matrix.translate(i * 0.56F, -0.52F, -0.72F);
} 
e.cancel();
} 
}
@EventHandler
public void onSwingDuration(SwingDurationEvent e) {
e.setAnimation(this.swingSpeedSetting.getValue());
e.cancel();
}
}


