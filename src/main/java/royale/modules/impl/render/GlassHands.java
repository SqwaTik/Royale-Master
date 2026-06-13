package royale.modules.impl.render;
import java.util.Objects;
import royale.events.api.EventHandler;
import royale.events.impl.GlassHandsRenderEvent;
import royale.events.impl.WorldChangeEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.BooleanSetting;
import royale.modules.module.setting.implement.ColorSetting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.util.render.shader.GlassHandsRenderer;
public class GlassHands
extends ModuleStructure
{
private static GlassHands instance;
public SliderSettings getBlurRadius() {
return this.blurRadius; } private final SliderSettings blurRadius = (new SliderSettings("Сила размытия", "Сила эффекта размытия стекла"))
.setValue(2.5F).range(1.0F, 5.0F);
public SliderSettings getBlurIterations() { return this.blurIterations; } private final SliderSettings blurIterations = (new SliderSettings("Качество", "Количество итераций размытия"))
.setValue(3.0F).range(1, 5);
public SliderSettings getSaturation() { return this.saturation; } private final SliderSettings saturation = (new SliderSettings("Насыщенность", "Насыщенность цвета"))
.setValue(0.0F).range(0.0F, 2.0F);
public BooleanSetting getEnableTint() { return this.enableTint; } private final BooleanSetting enableTint = (new BooleanSetting("Оттенок", "Включить цветной оттенок стекла"))
.setValue(false); private final SliderSettings tintIntensity; private final ColorSetting tintColor; private final BooleanSetting enableEdgeGlow; private final SliderSettings edgeGlowIntensity;
public SliderSettings getTintIntensity() {
return this.tintIntensity;
}
public ColorSetting getTintColor() {
return this.tintColor;
}
public BooleanSetting getEnableEdgeGlow() {
return this.enableEdgeGlow;
}
public SliderSettings getEdgeGlowIntensity() {
return this.edgeGlowIntensity;
}
public GlassHands() {
super("GlassHands", "Делает руки и предметы стеклянными", ModuleCategory.RENDER); Objects.requireNonNull(this.enableTint); this.tintIntensity = (new SliderSettings("Сила оттенка", "Интенсивность оттенка")).setValue(0.2F).range(0.0F, 0.5F).visible(this.enableTint::isValue); Objects.requireNonNull(this.enableTint); this.tintColor = (new ColorSetting("Цвет оттенка", "Цвет оттенка стекла")).value(-16711681).visible(this.enableTint::isValue); this.enableEdgeGlow = (new BooleanSetting("Свечение краёв", "Свечение по краям стекла")).setValue(true); Objects.requireNonNull(this.enableEdgeGlow); this.edgeGlowIntensity = (new SliderSettings("Сила свечения", "Интенсивность свечения краёв")).setValue(0.2F).range(0.0F, 1.0F).visible(this.enableEdgeGlow::isValue);
settings(new Setting[] { (Setting)this.blurRadius, (Setting)this.blurIterations, (Setting)this.saturation, (Setting)this.enableTint, (Setting)this.tintIntensity, (Setting)this.tintColor, (Setting)this.enableEdgeGlow, (Setting)this.edgeGlowIntensity });
instance = this;
}
public static GlassHands getInstance() {
return instance;
}
public void activate() {
GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();
if (renderer != null) {
renderer.invalidate();
renderer.setEnabled(true);
updateRendererSettings();
} 
}
public void deactivate() {
GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();
if (renderer != null) {
renderer.setEnabled(false);
}
}
@EventHandler
public void onWorldChange(WorldChangeEvent event) {
if (!isState())
return; 
GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();
if (renderer != null) {
renderer.invalidate();
renderer.setEnabled(true);
updateRendererSettings();
} 
}
@EventHandler
public void onGlassHandsRender(GlassHandsRenderEvent event) {
if (!isState())
return; 
GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();
if (renderer == null)
return; 
updateRendererSettings();
if (event.getPhase() == GlassHandsRenderEvent.Phase.PRE) {
renderer.captureSceneBeforeHands();
} else if (event.getPhase() == GlassHandsRenderEvent.Phase.POST) {
renderer.captureSceneAfterHands();
renderer.renderGlassEffect();
} 
}
private void updateRendererSettings() {
GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();
if (renderer == null)
return; 
renderer.setBlurRadius(this.blurRadius.getValue());
renderer.setBlurIterations(this.blurIterations.getInt());
renderer.setSaturation(this.saturation.getValue());
renderer.setReflect(true);
if (this.enableTint.isValue()) {
renderer.setTintColor(this.tintColor.getColor());
renderer.setTintIntensity(this.tintIntensity.getValue());
} else {
renderer.setTintColor(0);
renderer.setTintIntensity(0.0F);
} 
if (this.enableEdgeGlow.isValue()) {
renderer.setEdgeGlowIntensity(this.edgeGlowIntensity.getValue());
} else {
renderer.setEdgeGlowIntensity(0.0F);
} 
}
}


