package royale.modules.impl.render;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.SliderSettings;
public class ChunkAnimator extends ModuleStructure {
private static ChunkAnimator instance;
private final SliderSettings speed = (new SliderSettings("Скорость", "")).range(1, 20).setValue(10.0F);
public ChunkAnimator() {
super("Chunk Animator", "Анимирует появляющиеся чанки", ModuleCategory.RENDER);
instance = this;
settings(new Setting[] { (Setting)this.speed });
}
public static ChunkAnimator getInstance() {
return instance;
}
public float getSpeed() {
return this.speed.getValue();
}
}


