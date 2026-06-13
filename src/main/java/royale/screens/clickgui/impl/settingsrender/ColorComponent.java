package royale.screens.clickgui.impl.settingsrender;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.ColorSetting;
import royale.util.interfaces.AbstractSettingComponent;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.render.shader.Scissor;
public class ColorComponent extends AbstractSettingComponent {
private final ColorSetting colorSetting;
private boolean expanded = false;
private float expandAnimation = 0.0F;
private float hoverAnimation = 0.0F;
private float previewHoverAnimation = 0.0F;
private float contentAlpha = 0.0F;
private boolean draggingPalette = false;
private boolean draggingHue = false;
private boolean draggingAlpha = false;
private float paletteHandleAnimation = 0.0F;
private float hueHandleAnimation = 0.0F;
private float alphaHandleAnimation = 0.0F;
private boolean hexInputActive = false;
private String hexInputText = "";
private int hexCursorPosition = 0;
private int hexSelectionStart = -1;
private int hexSelectionEnd = -1;
private float hexInputAnimation = 0.0F;
private float hexSelectionAnimation = 0.0F;
private float hexCursorBlinkAnimation = 0.0F;
private float displayHue;
private float displaySaturation;
private float displayBrightness;
private float displayAlpha;
private boolean colorInitialized = false;
private long lastUpdateTime = System.currentTimeMillis();
private static final float ANIMATION_SPEED = 8.0F;
private static final float FAST_ANIMATION_SPEED = 15.0F;
private static final float COLOR_TRANSITION_SPEED = 6.0F;
private static final float CONTENT_FADE_SPEED = 15.0F;
private static final float PALETTE_SIZE = 70.0F;
private static final float SLIDER_WIDTH = 8.0F;
private static final float SPACING = 4.0F;
private static final float PREVIEW_SIZE = 12.0F;
public ColorComponent(ColorSetting setting) {
super((Setting)setting);
this.colorSetting = setting;
updateHexFromColor();
this.displayHue = setting.getHue();
this.displaySaturation = setting.getSaturation();
this.displayBrightness = setting.getBrightness();
this.displayAlpha = setting.getAlpha();
this.colorInitialized = true;
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
private float lerpHue(float current, float target, float speed) {
float diff = target - current;
if (diff > 0.5F) {
diff--;
} else if (diff < -0.5F) {
diff++;
} 
if (Math.abs(diff) < 0.001F) {
return target;
}
float result = current + diff * Math.min(speed, 1.0F);
if (result < 0.0F) result++; 
if (result > 1.0F) result--;
return result;
}
private int clamp(int value) {
return Math.max(0, Math.min(255, value));
}
private void updateDisplayColors(float deltaTime) {
if (!this.colorInitialized) {
this.displayHue = this.colorSetting.getHue();
this.displaySaturation = this.colorSetting.getSaturation();
this.displayBrightness = this.colorSetting.getBrightness();
this.displayAlpha = this.colorSetting.getAlpha();
this.colorInitialized = true;
return;
} 
float speed = deltaTime * 6.0F;
if (this.draggingPalette || this.draggingHue || this.draggingAlpha) {
this.displayHue = this.colorSetting.getHue();
this.displaySaturation = this.colorSetting.getSaturation();
this.displayBrightness = this.colorSetting.getBrightness();
this.displayAlpha = this.colorSetting.getAlpha();
} else {
this.displayHue = lerpHue(this.displayHue, this.colorSetting.getHue(), speed);
this.displaySaturation = lerp(this.displaySaturation, this.colorSetting.getSaturation(), speed);
this.displayBrightness = lerp(this.displayBrightness, this.colorSetting.getBrightness(), speed);
this.displayAlpha = lerp(this.displayAlpha, this.colorSetting.getAlpha(), speed);
} 
}
private int getDisplayColor() {
int rgb = Color.HSBtoRGB(this.displayHue, this.displaySaturation, this.displayBrightness);
int alphaInt = Math.round(this.displayAlpha * 255.0F);
return alphaInt << 24 | rgb & 0xFFFFFF;
}
private int getDisplayColorNoAlpha() {
return Color.HSBtoRGB(this.displayHue, this.displaySaturation, this.displayBrightness) | 0xFF000000;
}
private Color applyContentAlpha(Color color) {
int newAlpha = Math.max(0, Math.min(255, (int)(color.getAlpha() * this.alphaMultiplier * this.contentAlpha)));
return new Color(color.getRed(), color.getGreen(), color.getBlue(), newAlpha);
}
private boolean isControlDown() {
long window = mc.getWindow().getHandle();
return (GLFW.glfwGetKey(window, 341) == 1 || 
GLFW.glfwGetKey(window, 345) == 1);
}
private boolean isShiftDown() {
long window = mc.getWindow().getHandle();
return (GLFW.glfwGetKey(window, 340) == 1 || 
GLFW.glfwGetKey(window, 344) == 1);
}
private boolean hasHexSelection() {
return (this.hexSelectionStart != -1 && this.hexSelectionEnd != -1 && this.hexSelectionStart != this.hexSelectionEnd);
}
private int getHexSelectionStart() {
return Math.min(this.hexSelectionStart, this.hexSelectionEnd);
}
private int getHexSelectionEnd() {
return Math.max(this.hexSelectionStart, this.hexSelectionEnd);
}
private String getHexSelectedText() {
if (!hasHexSelection()) return ""; 
return this.hexInputText.substring(getHexSelectionStart(), getHexSelectionEnd());
}
private void clearHexSelection() {
this.hexSelectionStart = -1;
this.hexSelectionEnd = -1;
}
private void selectAllHexText() {
this.hexSelectionStart = 0;
this.hexSelectionEnd = this.hexInputText.length();
this.hexCursorPosition = this.hexInputText.length();
}
private void deleteHexSelectedText() {
if (hasHexSelection()) {
      int start = getHexSelectionStart();
      int end = getHexSelectionEnd();
      this.hexInputText = this.hexInputText.substring(0, start) + this.hexInputText.substring(end);
      this.hexCursorPosition = start;
      clearHexSelection();
    } 
}
private void pasteHexFromClipboard() {
String clipboardText = GLFW.glfwGetClipboardString(mc.getWindow().getHandle());
if (clipboardText != null && !clipboardText.isEmpty()) {
clipboardText = clipboardText.replace("#", "").replaceAll("[^0-9A-Fa-f]", "").toUpperCase();
if (hasHexSelection()) {
deleteHexSelectedText();
}
int remainingSpace = 8 - this.hexInputText.length();
      if (clipboardText.length() > remainingSpace) {
        clipboardText = clipboardText.substring(0, remainingSpace);
      }
      if (!clipboardText.isEmpty()) {
        this.hexInputText = this.hexInputText.substring(0, this.hexCursorPosition) + clipboardText + this.hexInputText.substring(this.hexCursorPosition);
        this.hexCursorPosition += clipboardText.length();
      } 
    } 
}
private void copyHexToClipboard() {
if (hasHexSelection()) {
GLFW.glfwSetClipboardString(mc.getWindow().getHandle(), "#" + getHexSelectedText());
} else if (!this.hexInputText.isEmpty()) {
GLFW.glfwSetClipboardString(mc.getWindow().getHandle(), "#" + this.hexInputText);
} 
}
private void moveHexCursor(int direction) {
if (hasHexSelection() && !isShiftDown()) {
if (direction < 0) {
this.hexCursorPosition = getHexSelectionStart();
} else {
this.hexCursorPosition = getHexSelectionEnd();
} 
clearHexSelection();
} else {
if (direction < 0 && this.hexCursorPosition > 0) {
this.hexCursorPosition--;
} else if (direction > 0 && this.hexCursorPosition < this.hexInputText.length()) {
this.hexCursorPosition++;
} 
updateHexSelectionAfterCursorMove();
} 
}
private void updateHexSelectionAfterCursorMove() {
if (isShiftDown()) {
if (this.hexSelectionStart == -1) {
this.hexSelectionStart = (this.hexSelectionEnd != -1) ? this.hexSelectionEnd : this.hexCursorPosition;
}
this.hexSelectionEnd = this.hexCursorPosition;
} else {
clearHexSelection();
} 
}
public void render(DrawContext context, int mouseX, int mouseY, float delta) {
float deltaTime = getDeltaTime();
updateDisplayColors(deltaTime);
if (this.draggingPalette) {
updatePalette(mouseX, mouseY);
}
if (this.draggingHue) {
updateHue(mouseY);
}
if (this.draggingAlpha) {
updateAlpha(mouseY);
}
boolean hovered = isHover(mouseX, mouseY);
boolean previewHovered = isPreviewHover(mouseX, mouseY);
this.hoverAnimation = lerp(this.hoverAnimation, hovered ? 1.0F : 0.0F, deltaTime * 8.0F);
this.previewHoverAnimation = lerp(this.previewHoverAnimation, previewHovered ? 1.0F : 0.0F, deltaTime * 8.0F);
this.expandAnimation = lerp(this.expandAnimation, this.expanded ? 1.0F : 0.0F, deltaTime * 8.0F);
this.hexInputAnimation = lerp(this.hexInputAnimation, this.hexInputActive ? 1.0F : 0.0F, deltaTime * 15.0F);
this.hexSelectionAnimation = lerp(this.hexSelectionAnimation, hasHexSelection() ? 1.0F : 0.0F, deltaTime * 8.0F);
if (this.hexInputActive) {
this.hexCursorBlinkAnimation += deltaTime * 2.0F;
if (this.hexCursorBlinkAnimation > 1.0F) this.hexCursorBlinkAnimation--; 
} else {
this.hexCursorBlinkAnimation = 0.0F;
} 
float contentAlphaTarget = this.expanded ? 1.0F : 0.0F;
float contentAlphaSpeed = this.expanded ? 15.0F : 22.5F;
this.contentAlpha = lerp(this.contentAlpha, contentAlphaTarget, deltaTime * contentAlphaSpeed);
this.paletteHandleAnimation = lerp(this.paletteHandleAnimation, this.draggingPalette ? 1.0F : 0.0F, deltaTime * 15.0F);
this.hueHandleAnimation = lerp(this.hueHandleAnimation, this.draggingHue ? 1.0F : 0.0F, deltaTime * 15.0F);
this.alphaHandleAnimation = lerp(this.alphaHandleAnimation, this.draggingAlpha ? 1.0F : 0.0F, deltaTime * 15.0F);
int iconAlpha = (int)(200.0F * this.alphaMultiplier);
Fonts.GUI_ICONS.draw("R", this.x + 0.5F, this.y + this.height / 2.0F - 11.5F, 16.0F, (new Color(210, 210, 210, iconAlpha)).getRGB());
Fonts.BOLD.draw(this.colorSetting.getName(), this.x + 11.5F, this.y + this.height / 2.0F - 6.5F, 6.0F, applyAlpha(new Color(210, 210, 220, 200)).getRGB());
String description = this.colorSetting.getDescription();
float previewX = this.x + this.width - 14.0F;
if (description != null && !description.isEmpty()) {
drawSettingDescription(description, this.x + 8.5F, this.y + this.height / 2.0F + 0.5F, previewX - this.x - 12.0F);
}
renderColorPreview(mouseX, mouseY);
if (this.expandAnimation > 0.01F) {
renderColorPicker(context, mouseX, mouseY, deltaTime);
}
}
private void renderColorPreview(int mouseX, int mouseY) {
float previewX = this.x + this.width - 14.0F;
float previewY = this.y + this.height / 2.0F / 2.0F;
float scale = 1.0F + this.previewHoverAnimation * 0.1F;
float scaledX = previewX - scale / 2.0F + 1.0F;
float scaledY = previewY - scale / 2.0F;
int colorValue = getDisplayColor();
Color previewColor = new Color(colorValue, true);
Render2D.rect(scaledX + 0.5F, scaledY + 0.5F, 9.0F, 9.0F, applyAlpha(previewColor).getRGB(), 15.0F);
int outlineAlpha = clamp((int)((255.0F + this.previewHoverAnimation * 60.0F) * this.alphaMultiplier));
Render2D.outline(scaledX, scaledY, 10.0F, 10.0F, 1.0F, (new Color(125, 125, 125, outlineAlpha)).getRGB(), 15.0F);
}
private void renderColorPicker(DrawContext context, int mouseX, int mouseY, float deltaTime) {
float pickerX = this.x;
float pickerY = this.y + this.height + 4.0F;
float pickerWidth = this.width;
float totalExpandedHeight = 96.0F;
float visibleHeight = totalExpandedHeight * this.expandAnimation;
int outlineAlpha = clamp((int)(60.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
Render2D.outline(pickerX, pickerY, pickerWidth, visibleHeight + 2.0F, 0.5F, (new Color(80, 80, 85, outlineAlpha))
.getRGB(), 4.0F);
if (this.expandAnimation < 0.3F || this.contentAlpha < 0.01F)
return; 
Scissor.enable(pickerX, pickerY, pickerWidth, visibleHeight, 2.0F);
float contentX = pickerX + 4.0F;
float contentY = pickerY + 4.0F;
float contentWidth = pickerWidth - 8.0F;
float slidersWidth = 20.0F;
float paletteWidth = contentWidth - slidersWidth - 4.0F;
renderHueSlider(contentX, contentY, 8.0F, 70.0F, mouseX, mouseY);
renderAlphaSlider(contentX + 8.0F + 4.0F, contentY, 8.0F, 70.0F, mouseX, mouseY);
renderSaturationBrightnessPalette(contentX + slidersWidth + 4.0F, contentY, paletteWidth, 70.0F, mouseX, mouseY);
contentY += 74.0F;
renderHexInput(contentX, contentY, contentWidth, 16.0F, mouseX, mouseY);
Scissor.disable();
}
private void renderSaturationBrightnessPalette(float paletteX, float paletteY, float paletteWidth, float paletteHeight, int mouseX, int mouseY) {
int pureColor = Color.HSBtoRGB(this.displayHue, 1.0F, 1.0F);
Color pure = new Color(pureColor);
int[] gradientColors = { applyContentAlpha(Color.WHITE).getRGB(), applyContentAlpha(pure).getRGB(), applyContentAlpha(pure).getRGB(), applyContentAlpha(Color.WHITE).getRGB() };
Render2D.gradientRect(paletteX, paletteY, paletteWidth, paletteHeight - 0.5F, gradientColors, 5.0F);
int[] blackGradient = { (new Color(0, 0, 0, 0)).getRGB(), (new Color(0, 0, 0, 0)).getRGB(), applyContentAlpha(Color.BLACK).getRGB(), applyContentAlpha(Color.BLACK).getRGB() };
Render2D.gradientRect(paletteX, paletteY, paletteWidth, paletteHeight, blackGradient, 3.0F);
float handleX = paletteX + this.displaySaturation * paletteWidth;
float handleY = paletteY + (1.0F - this.displayBrightness) * paletteHeight;
float handleSize = 6.0F + this.paletteHandleAnimation * 2.0F;
int handleOutlineAlpha = clamp((int)(255.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
Render2D.rect(handleX - handleSize / 2.0F, handleY - handleSize / 2.0F, handleSize, handleSize, (new Color(255, 255, 255, handleOutlineAlpha))
.getRGB(), handleSize / 2.0F);
int currentColor = Color.HSBtoRGB(this.displayHue, this.displaySaturation, this.displayBrightness);
Color handleColor = new Color(currentColor);
Render2D.rect(handleX - handleSize / 2.0F + 1.0F, handleY - handleSize / 2.0F + 1.0F, handleSize - 2.0F, handleSize - 2.0F, 
applyContentAlpha(handleColor).getRGB(), (handleSize - 2.0F) / 2.0F);
}
private void renderHueSlider(float sliderX, float sliderY, float sliderWidth, float sliderHeight, int mouseX, int mouseY) {
int[] hueColors = { Color.HSBtoRGB(0.0F, 1.0F, 1.0F), Color.HSBtoRGB(0.16666667F, 1.0F, 1.0F), Color.HSBtoRGB(0.33333334F, 1.0F, 1.0F), Color.HSBtoRGB(0.5F, 1.0F, 1.0F), Color.HSBtoRGB(0.6666667F, 1.0F, 1.0F), Color.HSBtoRGB(0.8333333F, 1.0F, 1.0F), Color.HSBtoRGB(1.0F, 1.0F, 1.0F) };
float segmentHeight = sliderHeight / 6.0F;
int[] colorsTop = { applyContentAlpha(new Color(hueColors[0])).getRGB(), applyContentAlpha(new Color(hueColors[0])).getRGB(), applyContentAlpha(new Color(hueColors[1])).getRGB(), applyContentAlpha(new Color(hueColors[1])).getRGB() };
Render2D.gradientRect(sliderX, sliderY, sliderWidth, segmentHeight, colorsTop, 2.0F, 2.0F, 0.0F, 0.0F);
for (int i = 1; i < 5; i++) {
float segY = sliderY + i * segmentHeight;
int[] colors = { applyContentAlpha(new Color(hueColors[i])).getRGB(), applyContentAlpha(new Color(hueColors[i])).getRGB(), applyContentAlpha(new Color(hueColors[i + 1])).getRGB(), applyContentAlpha(new Color(hueColors[i + 1])).getRGB() };
Render2D.gradientRect(sliderX, segY - 0.5F, sliderWidth, segmentHeight + 0.5F, colors, 0.0F);
} 
int[] colorsBottom = { applyContentAlpha(new Color(hueColors[5])).getRGB(), applyContentAlpha(new Color(hueColors[5])).getRGB(), applyContentAlpha(new Color(hueColors[6])).getRGB(), applyContentAlpha(new Color(hueColors[6])).getRGB() };
Render2D.gradientRect(sliderX, sliderY + 5.0F * segmentHeight - 0.5F, sliderWidth, segmentHeight, colorsBottom, 0.0F, 0.0F, 2.0F, 2.0F);
int hueOutlineAlpha = clamp((int)(80.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
Render2D.outline(sliderX, sliderY, sliderWidth, sliderHeight, 0.5F, (new Color(100, 100, 105, hueOutlineAlpha))
.getRGB(), 3.0F);
float handleY = sliderY + this.displayHue * sliderHeight;
float handleHeight = 3.0F + this.hueHandleAnimation * 1.0F;
float handleWidth = sliderWidth + 2.0F;
int handleAlpha = clamp((int)(255.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
Render2D.rect(sliderX - 1.0F, handleY - handleHeight / 2.0F, handleWidth, handleHeight, (new Color(255, 255, 255, handleAlpha))
.getRGB(), 1.5F);
int handleShadowAlpha = clamp((int)(100.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
Render2D.outline(sliderX - 1.0F, handleY - handleHeight / 2.0F, handleWidth, handleHeight, 0.5F, (new Color(0, 0, 0, handleShadowAlpha))
.getRGB(), 1.5F);
}
private void renderAlphaSlider(float sliderX, float sliderY, float sliderWidth, float sliderHeight, int mouseX, int mouseY) {
int checkAlpha = clamp((int)(150.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
Render2D.rect(sliderX, sliderY, sliderWidth, sliderHeight, (new Color(180, 180, 180, checkAlpha)).getRGB(), 2.0F);
int baseColor = getDisplayColorNoAlpha() & 0xFFFFFF;
int transparentColor = baseColor;
int opaqueColor = baseColor | 0xFF000000;
int[] alphaGradient = { applyContentAlpha(new Color(transparentColor, true), 0.0F).getRGB(), applyContentAlpha(new Color(transparentColor, true), 0.0F).getRGB(), applyContentAlpha(new Color(opaqueColor, true)).getRGB(), applyContentAlpha(new Color(opaqueColor, true)).getRGB() };
Render2D.gradientRect(sliderX, sliderY, sliderWidth, sliderHeight, alphaGradient, 2.0F);
int alphaOutlineAlpha = clamp((int)(80.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
Render2D.outline(sliderX, sliderY, sliderWidth, sliderHeight, 0.5F, (new Color(100, 100, 105, alphaOutlineAlpha))
.getRGB(), 3.0F);
float handleY = sliderY + this.displayAlpha * sliderHeight;
float handleHeight = 3.0F + this.alphaHandleAnimation * 1.0F;
float handleWidth = sliderWidth + 2.0F;
int handleAlpha = clamp((int)(255.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
Render2D.rect(sliderX - 1.0F, handleY - handleHeight / 2.0F, handleWidth, handleHeight, (new Color(255, 255, 255, handleAlpha))
.getRGB(), 1.5F);
int handleShadowAlpha = clamp((int)(100.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
Render2D.outline(sliderX - 1.0F, handleY - handleHeight / 2.0F, handleWidth, handleHeight, 0.5F, (new Color(0, 0, 0, handleShadowAlpha))
.getRGB(), 1.5F);
}
private Color applyContentAlpha(Color color, float extraAlpha) {
int newAlpha = Math.max(0, Math.min(255, (int)(color.getAlpha() * this.alphaMultiplier * this.contentAlpha * extraAlpha)));
return new Color(color.getRed(), color.getGreen(), color.getBlue(), newAlpha);
}
private void renderHexInput(float inputX, float inputY, float inputWidth, float inputHeight, int mouseX, int mouseY) {
boolean inputHovered = (mouseX >= inputX && mouseX <= inputX + inputWidth && mouseY >= inputY && mouseY <= inputY + inputHeight);
int bgAlpha = clamp((int)((40.0F + this.hexInputAnimation * 20.0F + (inputHovered ? 10 : 0)) * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
Render2D.rect(inputX, inputY, inputWidth, inputHeight, (new Color(35, 35, 40, bgAlpha)).getRGB(), 3.0F);
int hexOutlineAlpha = clamp((int)((60.0F + this.hexInputAnimation * 80.0F + (inputHovered ? 20 : 0)) * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
Color outlineColor = this.hexInputActive ? new Color(100, 140, 180, hexOutlineAlpha) : new Color(80, 80, 85, hexOutlineAlpha);
Render2D.outline(inputX, inputY, inputWidth, inputHeight, 0.5F, outlineColor.getRGB(), 3.0F);
int iconAlpha = clamp((int)(200.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
Fonts.GUI_ICONS.draw("V", inputX + 4.0F, inputY + inputHeight / 2.0F - 7.5F, 12.0F, (new Color(210, 210, 210, iconAlpha)).getRGB());
String label = "HEX: ";
float iconOffset = 10.0F;
float labelWidth = Fonts.BOLD.getWidth(label, 5.0F);
int labelAlpha = clamp((int)(150.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
Fonts.BOLD.draw(label, inputX + 4.0F + iconOffset, inputY + inputHeight / 2.0F - 2.5F, 5.0F, (new Color(140, 140, 150, labelAlpha))
.getRGB());
String displayText = this.hexInputActive ? this.hexInputText : getDisplayHexString();
float textStartX = inputX + 4.0F + iconOffset + labelWidth;
float textY = inputY + inputHeight / 2.0F - 2.5F;
if (this.hexInputActive && hasHexSelection() && this.hexSelectionAnimation > 0.01F) {
int start = getHexSelectionStart();
int end = getHexSelectionEnd();
String beforeSelection = "#" + this.hexInputText.substring(0, start);
String selection = this.hexInputText.substring(start, end);
float selectionX = textStartX + Fonts.BOLD.getWidth(beforeSelection, 5.0F);
float selectionWidth = Fonts.BOLD.getWidth(selection, 5.0F);
int selAlpha = clamp((int)(100.0F * this.hexSelectionAnimation * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
Render2D.rect(selectionX - 1.0F, inputY + 4.25F, selectionWidth + 2.0F, inputHeight - 8.0F, (new Color(100, 140, 180, selAlpha))
.getRGB(), 2.0F);
} 
int textAlpha = clamp((int)((180.0F + this.hexInputAnimation * 40.0F) * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
Fonts.BOLD.draw("#" + displayText, textStartX, textY, 5.0F, (new Color(210, 210, 220, textAlpha))
.getRGB());
if (this.hexInputActive && !hasHexSelection()) {
float cursorAlpha = (float)(Math.sin(this.hexCursorBlinkAnimation * Math.PI * 2.0D) * 0.5D + 0.5D);
if (cursorAlpha > 0.3F) {
String beforeCursor = "#" + this.hexInputText.substring(0, this.hexCursorPosition);
float cursorX = textStartX + Fonts.BOLD.getWidth(beforeCursor, 5.0F);
int cursorAlphaInt = clamp((int)(255.0F * cursorAlpha * this.hexInputAnimation * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
Render2D.rect(cursorX, inputY + 3.0F, 0.5F, inputHeight - 6.0F, (new Color(180, 180, 185, cursorAlphaInt))
.getRGB(), 0.0F);
} 
} 
float miniPreviewX = inputX + inputWidth - 15.0F;
float miniPreviewY = inputY + 3.0F;
float miniPreviewSize = inputHeight - 6.0F;
int miniCheckAlpha = clamp((int)(120.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
Render2D.rect(miniPreviewX, miniPreviewY, miniPreviewSize, miniPreviewSize, (new Color(150, 150, 150, miniCheckAlpha))
.getRGB(), 3.0F);
Render2D.rect(miniPreviewX, miniPreviewY, miniPreviewSize, miniPreviewSize, 
applyContentAlpha(new Color(getDisplayColor(), true)).getRGB(), 3.0F);
int miniOutlineAlpha = clamp((int)(80.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
Render2D.outline(miniPreviewX, miniPreviewY, miniPreviewSize, miniPreviewSize, 0.5F, (new Color(80, 80, 85, miniOutlineAlpha))
.getRGB(), 3.0F);
}
private String getDisplayHexString() {
int color = getDisplayColor();
int a = color >> 24 & 0xFF;
int r = color >> 16 & 0xFF;
int g = color >> 8 & 0xFF;
int b = color & 0xFF;
return String.format("%02X%02X%02X%02X", new Object[] { Integer.valueOf(r), Integer.valueOf(g), Integer.valueOf(b), Integer.valueOf(a) });
}
private boolean isPreviewHover(double mouseX, double mouseY) {
float previewX = this.x + this.width - 12.0F - 4.0F;
float previewY = this.y + this.height / 2.0F - 6.0F;
return (mouseX >= previewX && mouseX <= (previewX + 12.0F) && mouseY >= previewY && mouseY <= (previewY + 12.0F));
}
private boolean isPaletteHover(double mouseX, double mouseY) {
float pickerX = this.x;
float pickerY = this.y + this.height + 4.0F;
float contentX = pickerX + 4.0F;
float contentY = pickerY + 4.0F;
float contentWidth = this.width - 8.0F;
float slidersWidth = 20.0F;
float paletteWidth = contentWidth - slidersWidth - 4.0F;
float paletteX = contentX + slidersWidth + 4.0F;
return (mouseX >= paletteX && mouseX <= (paletteX + paletteWidth) && mouseY >= contentY && mouseY <= (contentY + 70.0F));
}
private boolean isHueSliderHover(double mouseX, double mouseY) {
float pickerX = this.x;
float pickerY = this.y + this.height + 4.0F;
float contentX = pickerX + 4.0F;
float contentY = pickerY + 4.0F;
return (mouseX >= contentX && mouseX <= (contentX + 8.0F) && mouseY >= contentY && mouseY <= (contentY + 70.0F));
}
private boolean isAlphaSliderHover(double mouseX, double mouseY) {
float pickerX = this.x;
float pickerY = this.y + this.height + 4.0F;
float contentX = pickerX + 4.0F;
float contentY = pickerY + 4.0F;
float alphaSliderX = contentX + 8.0F + 4.0F;
return (mouseX >= alphaSliderX && mouseX <= (alphaSliderX + 8.0F) && mouseY >= contentY && mouseY <= (contentY + 70.0F));
}
private boolean isHexInputHover(double mouseX, double mouseY) {
float pickerX = this.x;
float pickerY = this.y + this.height + 4.0F;
float contentX = pickerX + 4.0F;
float contentY = pickerY + 4.0F + 70.0F + 4.0F;
float contentWidth = this.width - 8.0F;
return (mouseX >= contentX && mouseX <= (contentX + contentWidth) && mouseY >= contentY && mouseY <= (contentY + 16.0F));
}
public boolean mouseClicked(double mouseX, double mouseY, int button) {
if (button == 1 && isPreviewHover(mouseX, mouseY)) {
this.expanded = !this.expanded;
if (!this.expanded) {
this.hexInputActive = false;
this.draggingPalette = false;
this.draggingHue = false;
this.draggingAlpha = false;
clearHexSelection();
} 
return true;
} 
if (button == 0 && 
this.expanded && this.expandAnimation > 0.8F && this.contentAlpha > 0.5F) {
if (isPaletteHover(mouseX, mouseY)) {
this.draggingPalette = true;
updatePalette(mouseX, mouseY);
this.hexInputActive = false;
clearHexSelection();
return true;
} 
if (isHueSliderHover(mouseX, mouseY)) {
this.draggingHue = true;
updateHue(mouseY);
this.hexInputActive = false;
clearHexSelection();
return true;
} 
if (isAlphaSliderHover(mouseX, mouseY)) {
this.draggingAlpha = true;
updateAlpha(mouseY);
this.hexInputActive = false;
clearHexSelection();
return true;
} 
if (isHexInputHover(mouseX, mouseY)) {
this.hexInputActive = true;
this.hexInputText = getHexString();
this.hexCursorPosition = this.hexInputText.length();
this.hexSelectionStart = 0;
this.hexSelectionEnd = this.hexInputText.length();
return true;
}  if (this.hexInputActive) {
applyHexInput();
this.hexInputActive = false;
clearHexSelection();
} 
} 
return false;
}
public boolean mouseReleased(double mouseX, double mouseY, int button) {
if (button == 0) {
boolean wasDragging = (this.draggingPalette || this.draggingHue || this.draggingAlpha);
this.draggingPalette = false;
this.draggingHue = false;
this.draggingAlpha = false;
if (wasDragging) {
updateHexFromColor();
return true;
} 
} 
return false;
}
public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
if (button == 0) {
if (this.draggingPalette) {
updatePalette(mouseX, mouseY);
return true;
} 
if (this.draggingHue) {
updateHue(mouseY);
return true;
} 
if (this.draggingAlpha) {
updateAlpha(mouseY);
return true;
} 
} 
return false;
}
public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
if (!this.hexInputActive) return false;
if (isControlDown()) {
switch (keyCode) {
case 65:
selectAllHexText();
return true;
case 86:
pasteHexFromClipboard();
return true;
case 67:
copyHexToClipboard();
return true;
case 88:
if (hasHexSelection()) {
copyHexToClipboard();
deleteHexSelectedText();
} 
return true;
} 
}
switch (keyCode) { case 257:
case 335:
applyHexInput();
this.hexInputActive = false;
clearHexSelection();
return true;
case 256:
this.hexInputActive = false;
clearHexSelection();
return true;
      case 259:
        if (hasHexSelection()) {
          deleteHexSelectedText();
        } else if (this.hexCursorPosition > 0) {
          this.hexInputText = this.hexInputText.substring(0, this.hexCursorPosition - 1) + this.hexInputText.substring(this.hexCursorPosition);
          this.hexCursorPosition--;
        } 
        return true;
      case 261:
        if (hasHexSelection()) {
          deleteHexSelectedText();
        } else if (this.hexCursorPosition < this.hexInputText.length()) {
          this.hexInputText = this.hexInputText.substring(0, this.hexCursorPosition) + this.hexInputText.substring(this.hexCursorPosition + 1);
        } 
        return true;
case 263:
moveHexCursor(-1);
return true;
case 262:
moveHexCursor(1);
return true;
case 268:
this.hexCursorPosition = 0;
updateHexSelectionAfterCursorMove();
return true;
case 269:
this.hexCursorPosition = this.hexInputText.length();
updateHexSelectionAfterCursorMove();
return true; }
return false;
}
public boolean charTyped(char chr, int modifiers) {
if (!this.hexInputActive) return false;
if (isHexChar(chr)) {
      if (hasHexSelection()) {
        deleteHexSelectedText();
      }
      if (this.hexInputText.length() < 8) {
        this.hexInputText = this.hexInputText.substring(0, this.hexCursorPosition) + Character.toUpperCase(chr) + this.hexInputText.substring(this.hexCursorPosition);
        this.hexCursorPosition++;
      } 
      return true;
} 
return false;
}
private boolean isHexChar(char c) {
return ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f'));
}
private void updatePalette(double mouseX, double mouseY) {
float pickerX = this.x;
float pickerY = this.y + this.height + 4.0F;
float contentX = pickerX + 4.0F;
float contentY = pickerY + 4.0F;
float contentWidth = this.width - 8.0F;
float slidersWidth = 20.0F;
float paletteWidth = contentWidth - slidersWidth - 4.0F;
float paletteX = contentX + slidersWidth + 4.0F;
float saturation = (float)((mouseX - paletteX) / paletteWidth);
float brightness = 1.0F - (float)((mouseY - contentY) / 70.0D);
this.colorSetting.setSaturation(saturation);
this.colorSetting.setBrightness(brightness);
}
private void updateHue(double mouseY) {
float pickerY = this.y + this.height + 4.0F;
float contentY = pickerY + 4.0F;
float hue = (float)((mouseY - contentY) / 70.0D);
this.colorSetting.setHue(hue);
}
private void updateAlpha(double mouseY) {
float pickerY = this.y + this.height + 4.0F;
float contentY = pickerY + 4.0F;
float alpha = (float)((mouseY - contentY) / 70.0D);
this.colorSetting.setAlpha(alpha);
}
private String getHexString() {
int color = this.colorSetting.getColor();
int a = color >> 24 & 0xFF;
int r = color >> 16 & 0xFF;
int g = color >> 8 & 0xFF;
int b = color & 0xFF;
return String.format("%02X%02X%02X%02X", new Object[] { Integer.valueOf(r), Integer.valueOf(g), Integer.valueOf(b), Integer.valueOf(a) });
}
private void updateHexFromColor() {
this.hexInputText = getHexString();
this.hexCursorPosition = this.hexInputText.length();
}
private void applyHexInput() {
String hex = this.hexInputText.toUpperCase();
try {
int r, g, b, a = 255;
if (hex.length() == 6) {
r = Integer.parseInt(hex.substring(0, 2), 16);
g = Integer.parseInt(hex.substring(2, 4), 16);
b = Integer.parseInt(hex.substring(4, 6), 16);
} else if (hex.length() == 8) {
r = Integer.parseInt(hex.substring(0, 2), 16);
g = Integer.parseInt(hex.substring(2, 4), 16);
b = Integer.parseInt(hex.substring(4, 6), 16);
a = Integer.parseInt(hex.substring(6, 8), 16);
} else if (hex.length() == 3) {
r = Integer.parseInt(hex.substring(0, 1) + hex.substring(0, 1), 16);
g = Integer.parseInt(hex.substring(1, 2) + hex.substring(1, 2), 16);
b = Integer.parseInt(hex.substring(2, 3) + hex.substring(2, 3), 16);
} else {
updateHexFromColor();
return;
} 
float[] hsb = Color.RGBtoHSB(r, g, b, null);
this.colorSetting.setHue(hsb[0]);
this.colorSetting.setSaturation(hsb[1]);
this.colorSetting.setBrightness(hsb[2]);
this.colorSetting.setAlpha(a / 255.0F);
    } catch (NumberFormatException e) {
      updateHexFromColor();
    } 
  }
public void tick() {}
public boolean isHover(double mouseX, double mouseY) {
return (mouseX >= this.x && mouseX <= (this.x + this.width) && mouseY >= this.y && mouseY <= (this.y + this.height));
}
public float getTotalHeight() {
float totalExpandedHeight = 104.0F;
float expandedHeight = totalExpandedHeight * this.expandAnimation;
return this.height + expandedHeight;
}
public boolean isExpanded() {
return this.expanded;
}
public boolean isHexInputActive() {
return this.hexInputActive;
}
public boolean isDragging() {
return (this.draggingPalette || this.draggingHue || this.draggingAlpha);
}
}


