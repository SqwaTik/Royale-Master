package royale.screens.clickgui.impl.settingsrender;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.util.interfaces.AbstractSettingComponent;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
public class SliderComponent
extends AbstractSettingComponent {
private final SliderSettings sliderSettings;
private boolean dragging = false;
private float animatedPercentage = 0.0F;
private float knobAnimation = 0.0F;
private boolean inputMode = false;
private String inputText = "";
private int cursorPosition = 0;
private float inputAnimation = 0.0F;
private float hoverAnimation = 0.0F;
private float unitsAlpha = 1.0F;
private float valueOffsetX = 0.0F;
private float backgroundAlpha = 0.0F;
private long lastUpdateTime = System.currentTimeMillis();
private static final float ANIMATION_SPEED = 8.0F;
private static final float FAST_ANIMATION_SPEED = 12.0F;
public SliderComponent(SliderSettings setting) {
super((Setting)setting);
this.sliderSettings = setting;
float range = this.sliderSettings.getMax() - this.sliderSettings.getMin();
if (range > 0.0F) {
this.animatedPercentage = (this.sliderSettings.getValue() - this.sliderSettings.getMin()) / range;
}
}
private int clampAlpha(float alpha) {
return Math.max(0, Math.min(255, (int)alpha));
}
public void render(DrawContext context, int mouseX, int mouseY, float delta) {
if (this.dragging) {
updateValue(mouseX);
}
float deltaTime = getDeltaTime();
updateAnimations(mouseX, mouseY, deltaTime);
float range = this.sliderSettings.getMax() - this.sliderSettings.getMin();
float targetPercentage = (range > 0.0F) ? ((this.sliderSettings.getValue() - this.sliderSettings.getMin()) / range) : 0.0F;
this.animatedPercentage += (targetPercentage - this.animatedPercentage) * 0.25F;
float knobTarget = this.dragging ? 1.0F : 0.0F;
this.knobAnimation += (knobTarget - this.knobAnimation) * 0.25F;
this.knobAnimation = Math.max(0.0F, Math.min(1.0F, this.knobAnimation));
int iconAlpha = (int)(200.0F * this.alphaMultiplier);
Fonts.GUI_ICONS.draw("H", this.x - 0.5F, this.y + 0.5F, 9.0F, (new Color(210, 210, 210, iconAlpha)).getRGB());
Fonts.BOLD.draw(this.sliderSettings.getName(), this.x + 9.5F, this.y + 1.0F, 6.0F, applyAlpha(new Color(210, 210, 220, 200)).getRGB());
renderValueInput(mouseX, mouseY);
renderSlider();
}
private float getDeltaTime() {
long currentTime = System.currentTimeMillis();
float deltaTime = Math.min((float)(currentTime - this.lastUpdateTime) / 1000.0F, 0.1F);
this.lastUpdateTime = currentTime;
return deltaTime;
}
private float lerp(float current, float target, float speed) {
float diff = target - current;
if (Math.abs(diff) < 0.001F) {
return target;
}
return current + diff * Math.min(speed, 1.0F);
}
private void updateAnimations(int mouseX, int mouseY, float deltaTime) {
float inputTarget = this.inputMode ? 1.0F : 0.0F;
this.inputAnimation = lerp(this.inputAnimation, inputTarget, deltaTime * 12.0F);
boolean isHovered = (isValueHover(mouseX, mouseY) && !this.inputMode);
float hoverTarget = isHovered ? 1.0F : 0.0F;
this.hoverAnimation = lerp(this.hoverAnimation, hoverTarget, deltaTime * 8.0F);
float unitsTarget = this.inputMode ? 0.0F : 1.0F;
this.unitsAlpha = lerp(this.unitsAlpha, unitsTarget, deltaTime * 8.0F);
float offsetTarget = this.inputMode ? 1.0F : 0.0F;
this.valueOffsetX = lerp(this.valueOffsetX, offsetTarget, deltaTime * 8.0F);
float bgTarget = this.inputMode ? 1.0F : 0.0F;
this.backgroundAlpha = lerp(this.backgroundAlpha, bgTarget, deltaTime * 8.0F);
}
private void renderValueInput(int mouseX, int mouseY) {
String valueText = this.sliderSettings.isInteger() ? String.valueOf((int)this.sliderSettings.getValue()) : String.format("%.1f", new Object[] { Float.valueOf(this.sliderSettings.getValue()) });
String unitsText = " units";
String fullText = valueText + valueText;
float fullTextWidth = Fonts.BOLD.getWidth(fullText, 5.0F);
float valueTextWidth = Fonts.BOLD.getWidth(valueText, 5.0F);
float unitsTextWidth = Fonts.BOLD.getWidth(unitsText, 5.0F);
float baseX = this.x + this.width - fullTextWidth - 4.0F;
float textY = this.y + 2.0F;
float centerOffset = unitsTextWidth / 2.0F * this.valueOffsetX;
float currentValueX = baseX + centerOffset;
float inputBoxX = baseX - 3.0F;
float inputBoxY = textY - 1.0F;
float inputBoxWidth = fullTextWidth + 6.0F;
float inputBoxHeight = 8.0F;
if (this.backgroundAlpha > 0.01F) {
int bgAlpha = clampAlpha(200.0F * this.backgroundAlpha * this.alphaMultiplier);
Render2D.rect(inputBoxX, inputBoxY, inputBoxWidth, inputBoxHeight, (new Color(40, 40, 45, bgAlpha))
.getRGB(), 2.0F);
} 
float combinedOutlineAlpha = Math.max(this.hoverAnimation * 0.4F, this.inputAnimation);
if (combinedOutlineAlpha > 0.01F) {
int outlineAlpha = clampAlpha(180.0F * combinedOutlineAlpha * this.alphaMultiplier);
Render2D.outline(inputBoxX, inputBoxY, inputBoxWidth, inputBoxHeight, 0.1F, (new Color(180, 180, 180, outlineAlpha))
.getRGB(), 2.0F);
} 
if (this.inputMode && this.inputAnimation > 0.5F) {
String displayText = this.inputText;
float displayTextWidth = Fonts.BOLD.getWidth(displayText, 5.0F);
float centeredX = inputBoxX + (inputBoxWidth - displayTextWidth) / 2.0F;
int textAlpha = clampAlpha(220.0F * Math.min(1.0F, (this.inputAnimation - 0.5F) * 2.0F) * this.alphaMultiplier);
Fonts.BOLD.draw(displayText, centeredX, textY, 5.0F, (new Color(230, 230, 235, textAlpha))
.getRGB());
long currentTime = System.currentTimeMillis();
if (currentTime % 1000L < 500L) {
String beforeCursor = this.inputText.substring(0, this.cursorPosition);
float cursorX = centeredX + Fonts.BOLD.getWidth(beforeCursor, 5.0F);
int cursorAlpha = clampAlpha(255.0F * this.inputAnimation * this.alphaMultiplier);
Render2D.rect(cursorX, inputBoxY + 2.0F, 0.5F, inputBoxHeight - 4.0F, (new Color(180, 180, 180, cursorAlpha))
.getRGB(), 0.0F);
} 
} else {
float valueAlpha = 1.0F - this.inputAnimation * 0.5F;
int valueAlphaInt = clampAlpha(160.0F * valueAlpha * this.alphaMultiplier);
if (valueAlphaInt > 0) {
Fonts.BOLD.draw(valueText, currentValueX, textY, 5.0F, (new Color(100, 100, 105, valueAlphaInt))
.getRGB());
}
if (this.unitsAlpha > 0.01F) {
int unitsAlphaInt = clampAlpha(160.0F * this.unitsAlpha * this.alphaMultiplier);
if (unitsAlphaInt > 0) {
Fonts.BOLD.draw(unitsText, currentValueX + valueTextWidth, textY, 5.0F, (new Color(100, 100, 105, unitsAlphaInt))
.getRGB());
}
} 
} 
}
private void renderSlider() {
float sliderY = this.y + 11.0F;
float sliderHeight = 2.5F;
float sliderPadding = 1.0F;
float sliderTrackWidth = this.width - 2.0F;
Render2D.rect(this.x + sliderPadding, sliderY, sliderTrackWidth, sliderHeight, 
applyAlpha(new Color(60, 60, 65, 220)).getRGB(), 2.0F);
float filledWidth = sliderTrackWidth * this.animatedPercentage;
if (filledWidth > 0.0F) {
Render2D.rect(this.x + sliderPadding, sliderY, filledWidth, sliderHeight, 
applyAlpha(new Color(130, 130, 135, 230)).getRGB(), 2.0F);
}
float knobBaseSize = 5.0F;
float knobSize = knobBaseSize + this.knobAnimation * 1.0F;
float knobX = this.x + sliderPadding + sliderTrackWidth * this.animatedPercentage - knobSize / 2.0F;
float knobY = sliderY + sliderHeight / 2.0F - knobSize / 2.0F;
knobX = Math.max(this.x + sliderPadding - knobSize / 2.0F, 
Math.min(knobX, this.x + sliderPadding + sliderTrackWidth - knobSize / 2.0F));
Render2D.rect(knobX, knobY, knobSize, knobSize, 
applyAlpha(new Color(180, 180, 185, 255)).getRGB(), knobSize / 2.0F);
}
private boolean isValueHover(double mouseX, double mouseY) {
String valueText = this.sliderSettings.isInteger() ? String.valueOf((int)this.sliderSettings.getValue()) : String.format("%.1f", new Object[] { Float.valueOf(this.sliderSettings.getValue()) });
String fullText = valueText + " units";
float fullTextWidth = Fonts.BOLD.getWidth(fullText, 5.0F);
float boxX = this.x + this.width - fullTextWidth - 7.0F;
float boxY = this.y;
return (mouseX >= boxX && mouseX <= (boxX + fullTextWidth + 10.0F) && mouseY >= boxY && mouseY <= (boxY + 10.0F));
}
public boolean mouseClicked(double mouseX, double mouseY, int button) {
if (button == 0) {
if (isValueHover(mouseX, mouseY) && !this.inputMode) {
this.inputMode = true;
String currentValue = this.sliderSettings.isInteger() ? String.valueOf((int)this.sliderSettings.getValue()) : String.format("%.1f", new Object[] { Float.valueOf(this.sliderSettings.getValue()) });
this.inputText = currentValue;
this.cursorPosition = this.inputText.length();
return true;
} 
if (this.inputMode && !isValueHover(mouseX, mouseY)) {
applyInputValue();
this.inputMode = false;
this.inputText = "";
return true;
} 
if (isSliderHover(mouseX, mouseY) && !this.inputMode) {
this.dragging = true;
updateValue(mouseX);
return true;
} 
} 
return false;
}
private boolean isSliderHover(double mouseX, double mouseY) {
float sliderY = this.y + 6.0F;
float sliderHeight = 12.0F;
return (mouseX >= this.x && mouseX <= (this.x + this.width) && mouseY >= sliderY && mouseY <= (sliderY + sliderHeight));
}
public boolean mouseReleased(double mouseX, double mouseY, int button) {
if (button == 0 && this.dragging) {
this.dragging = false;
return true;
} 
return false;
}
public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
if (this.dragging && button == 0) {
updateValue(mouseX);
return true;
} 
return false;
}
public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
if (!this.inputMode) return false;
switch (keyCode) { case 257:
case 335:
applyInputValue();
this.inputMode = false;
this.inputText = "";
return true;
case 256:
this.inputMode = false;
this.inputText = "";
return true;
      case 259:
        if (this.cursorPosition > 0) {
          this.inputText = this.inputText.substring(0, this.cursorPosition - 1) + this.inputText.substring(this.cursorPosition);
          this.cursorPosition--;
        } 
        return true;
      case 261:
        if (this.cursorPosition < this.inputText.length()) {
          this.inputText = this.inputText.substring(0, this.cursorPosition) + this.inputText.substring(this.cursorPosition + 1);
        }
        return true;
case 263:
if (this.cursorPosition > 0) this.cursorPosition--; 
return true;
case 262:
if (this.cursorPosition < this.inputText.length()) this.cursorPosition++; 
return true;
case 268:
this.cursorPosition = 0;
return true;
case 269:
this.cursorPosition = this.inputText.length();
return true; }
return false;
}
public boolean charTyped(char chr, int modifiers) {
    if (!this.inputMode) return false;
    if (isValidInputChar(chr)) {
      String newText = this.inputText.substring(0, this.cursorPosition) + chr + this.inputText.substring(this.cursorPosition);
      if (isValidInputFormat(newText)) {
        this.inputText = newText;
        this.cursorPosition++;
} 
return true;
} 
return false;
}
private boolean isValidInputChar(char chr) {
return (Character.isDigit(chr) || chr == '.' || chr == '-');
}
private boolean isValidInputFormat(String text) {
if (text.isEmpty() || text.equals("-") || text.equals(".") || text.equals("-.")) {
return true;
}
int dotCount = 0;
int minusCount = 0;
int digitsAfterDot = 0;
boolean foundDot = false;
for (int i = 0; i < text.length(); i++) {
char c = text.charAt(i);
if (c == '-') {
if (i != 0) return false; 
minusCount++;
if (minusCount > 1) return false; 
} else if (c == '.') {
if (this.sliderSettings.isInteger()) return false; 
dotCount++;
if (dotCount > 1) return false; 
foundDot = true;
      } else if (Character.isDigit(c)) {
        if (foundDot) {
          digitsAfterDot++;
          if (digitsAfterDot > 1) return false;
        } 
      } else {
        return false;
      } 
} 
return true;
}
private void applyInputValue() {
if (this.inputText.isEmpty() || this.inputText.equals("-") || this.inputText.equals(".") || this.inputText.equals("-.")) {
return;
}
try {
float value;
if (this.sliderSettings.isInteger()) {
value = Integer.parseInt(this.inputText);
} else {
value = Float.parseFloat(this.inputText);
} 
value = Math.max(this.sliderSettings.getMin(), Math.min(this.sliderSettings.getMax(), value));
if (this.sliderSettings.isInteger()) {
value = Math.round(value);
}
this.sliderSettings.setValue(value);
} catch (NumberFormatException numberFormatException) {}
}
private void updateValue(double mouseX) {
float sliderPadding = 1.0F;
float sliderTrackWidth = this.width - 2.0F;
float percentage = (float)((mouseX - this.x - sliderPadding) / sliderTrackWidth);
percentage = Math.max(0.0F, Math.min(1.0F, percentage));
float range = this.sliderSettings.getMax() - this.sliderSettings.getMin();
float newValue = this.sliderSettings.getMin() + range * percentage;
if (this.sliderSettings.isInteger()) {
newValue = Math.round(newValue);
}
this.sliderSettings.setValue(newValue);
}
public void tick() {}
public boolean isHover(double mouseX, double mouseY) {
return (mouseX >= this.x && mouseX <= (this.x + this.width) && mouseY >= this.y && mouseY <= (this.y + this.height));
}
public boolean isInputMode() {
return this.inputMode;
}
}