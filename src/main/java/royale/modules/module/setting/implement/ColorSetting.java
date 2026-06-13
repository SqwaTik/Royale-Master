package royale.modules.module.setting.implement;
import java.awt.Color;
import java.util.function.Supplier;
import royale.modules.module.setting.Setting;
public class ColorSetting
extends Setting
{
public void setPresets(int[] presets) {
this.presets = presets;
}
private float hue = 0.0F; private float saturation = 1.0F; private float brightness = 1.0F; private float alpha = 1.0F; public float getHue() { return this.hue; }
public float getSaturation() { return this.saturation; }
public float getBrightness() { return this.brightness; } public float getAlpha() {
return this.alpha;
}
private int[] presets = new int[0]; public int[] getPresets() { return this.presets; }
public ColorSetting(String name, String description) {
super(name, description);
}
public ColorSetting value(int value) {
setColor(value);
return this;
}
public ColorSetting presets(int... presets) {
this.presets = presets;
return this;
}
public ColorSetting visible(Supplier<Boolean> visible) {
setVisible(visible);
return this;
}
public int getColor() {
int rgb = Color.HSBtoRGB(this.hue, this.saturation, this.brightness);
int alphaInt = Math.round(this.alpha * 255.0F);
return alphaInt << 24 | rgb & 0xFFFFFF;
}
public int getColorWithAlpha() {
return getColor();
}
public int getColorNoAlpha() {
return Color.HSBtoRGB(this.hue, this.saturation, this.brightness) | 0xFF000000;
}
public ColorSetting setColor(int color) {
int r = color >> 16 & 0xFF;
int g = color >> 8 & 0xFF;
int b = color & 0xFF;
int a = color >> 24 & 0xFF;
float[] hsb = Color.RGBtoHSB(r, g, b, null);
this.hue = hsb[0];
this.saturation = hsb[1];
this.brightness = hsb[2];
this.alpha = a / 255.0F;
return this;
}
public Color getAwtColor() {
int color = getColor();
return new Color(color, true);
}
public ColorSetting setHue(float hue) {
this.hue = Math.max(0.0F, Math.min(1.0F, hue));
return this;
}
public ColorSetting setSaturation(float saturation) {
this.saturation = Math.max(0.0F, Math.min(1.0F, saturation));
return this;
}
public ColorSetting setBrightness(float brightness) {
this.brightness = Math.max(0.0F, Math.min(1.0F, brightness));
return this;
}
public ColorSetting setAlpha(float alpha) {
this.alpha = Math.max(0.0F, Math.min(1.0F, alpha));
return this;
}
}


