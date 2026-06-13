package royale.screens.clickgui.impl.settingsrender;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.TextSetting;
import royale.util.interfaces.AbstractSettingComponent;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.render.shader.Scissor;
public class TextComponent extends AbstractSettingComponent {
public static boolean typing = false;
private final TextSetting textSetting;
private boolean focused = false;
private int cursorPosition = 0;
private int selectionStart = -1;
private int selectionEnd = -1;
private long lastClickTime = 0L;
private String text = "";
private float focusAnimation = 0.0F;
private float hoverAnimation = 0.0F;
private float textScrollOffset = 0.0F;
private float targetScrollOffset = 0.0F;
private float cursorBlinkAnimation = 0.0F;
private float selectionAnimation = 0.0F;
private long lastUpdateTime = System.currentTimeMillis();
private static final float ANIMATION_SPEED = 8.0F;
private static final float SCROLL_ANIMATION_SPEED = 10.0F;
private static final float INPUT_BOX_WIDTH = 65.0F;
private static final float INPUT_BOX_HEIGHT = 10.0F;
private static final float TEXT_PADDING = 4.0F;
public TextComponent(TextSetting setting) {
super((Setting)setting);
this.textSetting = setting;
this.text = (this.textSetting.getText() != null) ? this.textSetting.getText() : "";
this.cursorPosition = this.text.length();
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
public void render(DrawContext context, int mouseX, int mouseY, float delta) {
float deltaTime = getDeltaTime();
boolean hovered = isInputBoxHover(mouseX, mouseY);
this.hoverAnimation = lerp(this.hoverAnimation, hovered ? 1.0F : 0.0F, deltaTime * 8.0F);
this.focusAnimation = lerp(this.focusAnimation, this.focused ? 1.0F : 0.0F, deltaTime * 8.0F);
this.selectionAnimation = lerp(this.selectionAnimation, hasSelection() ? 1.0F : 0.0F, deltaTime * 8.0F);
if (this.focused) {
this.cursorBlinkAnimation += deltaTime * 2.0F;
if (this.cursorBlinkAnimation > 1.0F) this.cursorBlinkAnimation--; 
} else {
this.cursorBlinkAnimation = 0.0F;
} 
int iconAlpha = (int)(200.0F * this.alphaMultiplier);
Fonts.GUI_ICONS.draw("S", this.x + 0.5F, this.y + this.height / 2.0F - 10.25F, 11.0F, (new Color(210, 210, 220, iconAlpha)).getRGB());
Fonts.BOLD.draw(this.textSetting.getName(), this.x + 9.5F, this.y + this.height / 2.0F - 7.5F, 6.0F, applyAlpha(new Color(210, 210, 220, 200)).getRGB());
String description = this.textSetting.getDescription();
float boxX = this.x + this.width - 65.0F - 2.0F;
if (description != null && !description.isEmpty()) {
drawSettingDescription(description, this.x + 0.5F, this.y + this.height / 2.0F + 0.5F, boxX - this.x - 6.0F);
}
float boxY = this.y + this.height / 2.0F - 5.0F;
int bgAlpha = (int)(25.0F + this.focusAnimation * 15.0F + this.hoverAnimation * 10.0F);
Render2D.rect(boxX, boxY, 65.0F, 10.0F, applyAlpha(new Color(40, 40, 45, bgAlpha)).getRGB(), 3.0F);
float outlineAlpha = 60.0F + this.hoverAnimation * 40.0F + this.focusAnimation * 60.0F;
Color outlineColor = this.focused ? new Color(100, 140, 180, (int)(outlineAlpha * this.alphaMultiplier)) : new Color(155, 155, 155, (int)(outlineAlpha * this.alphaMultiplier));
Render2D.outline(boxX, boxY, 65.0F, 10.0F, 0.5F, outlineColor.getRGB(), 3.0F);
renderTextContent(boxX, boxY, deltaTime);
}
private void renderTextContent(float boxX, float boxY, float deltaTime) {
float textAreaX = boxX + 4.0F;
float textAreaWidth = 57.0F;
float textY = boxY + 5.0F - 2.5F;
String displayText = this.text;
float fullTextWidth = Fonts.BOLD.getWidth(displayText, 5.0F);
if (this.focused) {
String beforeCursor = this.text.substring(0, this.cursorPosition);
float cursorX = Fonts.BOLD.getWidth(beforeCursor, 5.0F);
if (cursorX - this.targetScrollOffset > textAreaWidth - 2.0F) {
this.targetScrollOffset = cursorX - textAreaWidth + 2.0F;
} else if (cursorX - this.targetScrollOffset < 0.0F) {
this.targetScrollOffset = cursorX;
} 
if (fullTextWidth <= textAreaWidth) {
this.targetScrollOffset = 0.0F;
}
this.targetScrollOffset = Math.max(0.0F, Math.min(this.targetScrollOffset, Math.max(0.0F, fullTextWidth - textAreaWidth)));
} else {
this.targetScrollOffset = 0.0F;
} 
this.textScrollOffset = lerp(this.textScrollOffset, this.targetScrollOffset, deltaTime * 10.0F);
Scissor.enable(boxX + 2.0F, boxY, 61.0F, 10.0F, 2.0F);
if (this.text.isEmpty() && !this.focused) {
String placeholder = this.textSetting.getPlaceholder();
if (placeholder == null || placeholder.isBlank()) {
placeholder = "Enter text...";
}
Fonts.BOLD.draw(placeholder, textAreaX, textY, 5.0F, applyAlpha(new Color(100, 100, 105, 100)).getRGB());
} else {
if (this.focused && hasSelection() && this.selectionAnimation > 0.01F) {
int start = getStartOfSelection();
int end = getEndOfSelection();
String beforeSelection = this.text.substring(0, start);
String selection = this.text.substring(start, end);
float selectionX = textAreaX + Fonts.BOLD.getWidth(beforeSelection, 5.0F) - this.textScrollOffset;
float selectionWidth = Fonts.BOLD.getWidth(selection, 5.0F);
int selAlpha = (int)(100.0F * this.selectionAnimation * this.alphaMultiplier);
Render2D.rect(selectionX, boxY + 2.0F, selectionWidth, 6.0F, (new Color(100, 140, 180, selAlpha))
.getRGB(), 2.0F);
} 
int textAlpha = (int)((160.0F + this.focusAnimation * 60.0F) * this.alphaMultiplier);
Fonts.BOLD.draw(displayText, textAreaX - this.textScrollOffset, textY, 5.0F, (new Color(210, 210, 220, textAlpha))
.getRGB());
if (this.focused && !hasSelection()) {
float cursorAlpha = (float)(Math.sin(this.cursorBlinkAnimation * Math.PI * 2.0D) * 0.5D + 0.5D);
if (cursorAlpha > 0.3F) {
String beforeCursor = this.text.substring(0, this.cursorPosition);
float cursorXPos = textAreaX + Fonts.BOLD.getWidth(beforeCursor, 5.0F) - this.textScrollOffset;
int cursorAlphaInt = (int)(255.0F * cursorAlpha * this.focusAnimation * this.alphaMultiplier);
Render2D.rect(cursorXPos, boxY + 2.0F, 0.5F, 6.0F, (new Color(180, 180, 185, cursorAlphaInt))
.getRGB(), 0.0F);
} 
} 
} 
Scissor.disable();
}
private boolean isInputBoxHover(double mouseX, double mouseY) {
float boxX = this.x + this.width - 65.0F - 2.0F;
float boxY = this.y + this.height / 2.0F - 5.0F;
return (mouseX >= boxX && mouseX <= (boxX + 65.0F) && mouseY >= boxY && mouseY <= (boxY + 10.0F));
}
public boolean mouseClicked(double mouseX, double mouseY, int button) {
boolean wasInside = isInputBoxHover(mouseX, mouseY);
if (wasInside && button == 0) {
long currentTime = System.currentTimeMillis();
if (currentTime - this.lastClickTime < 250L && this.focused) {
selectAllText();
} else {
this.focused = true;
typing = true;
this.cursorPosition = getCursorIndexAt(mouseX);
this.selectionStart = this.cursorPosition;
this.selectionEnd = this.cursorPosition;
} 
this.lastClickTime = currentTime;
return true;
}  if (!wasInside && this.focused) {
applyText();
this.focused = false;
typing = false;
clearSelection();
} 
return false;
}
public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
if (this.focused && button == 0) {
this.cursorPosition = getCursorIndexAt(mouseX);
this.selectionEnd = this.cursorPosition;
return true;
} 
return false;
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
public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
if (!this.focused) return false;
if (isControlDown()) {
switch (keyCode) {
case 65:
selectAllText();
return true;
case 86:
pasteFromClipboard();
return true;
case 67:
copyToClipboard();
return true;
case 88:
if (hasSelection()) {
copyToClipboard();
deleteSelectedText();
} 
return true;
} 
} else {
switch (keyCode) {
case 259:
handleBackspace();
return true;
case 261:
handleDelete();
return true;
case 263:
moveCursor(-1);
return true;
case 262:
moveCursor(1);
return true;
case 268:
this.cursorPosition = 0;
updateSelectionAfterCursorMove();
return true;
case 269:
this.cursorPosition = this.text.length();
updateSelectionAfterCursorMove();
return true;
case 257:
applyText();
this.focused = false;
typing = false;
return true;
case 256:
this.text = (this.textSetting.getText() != null) ? this.textSetting.getText() : "";
this.cursorPosition = this.text.length();
this.focused = false;
typing = false;
return true;
} 
} 
return false;
}
public boolean charTyped(char chr, int modifiers) {
if (!this.focused) return false;
if (Character.isISOControl(chr)) {
return false;
}
int maxLength = (this.textSetting.getMax() > 0) ? this.textSetting.getMax() : Integer.MAX_VALUE;
if (this.text.length() < maxLength || hasSelection()) {
deleteSelectedText();
        this.text = this.text.substring(0, this.cursorPosition) + chr + this.text.substring(this.cursorPosition);
this.cursorPosition++;
clearSelection();
return true;
} 
return false;
}
public void tick() {}
private void applyText() {
if (this.textSetting.isValidText(this.text)) {
this.textSetting.setText(this.text);
}
this.text = (this.textSetting.getText() != null) ? this.textSetting.getText() : "";
this.cursorPosition = this.text.length();
} 
private void handleBackspace() {
if (hasSelection()) {
replaceText(getStartOfSelection(), getEndOfSelection(), "");
} else if (this.cursorPosition > 0) {
replaceText(this.cursorPosition - 1, this.cursorPosition, "");
} 
}
private void handleDelete() {
if (hasSelection()) {
replaceText(getStartOfSelection(), getEndOfSelection(), "");
} else if (this.cursorPosition < this.text.length()) {
      this.text = this.text.substring(0, this.cursorPosition) + this.text.substring(this.cursorPosition + 1);
} 
}
private void moveCursor(int direction) {
if (hasSelection() && !isShiftDown()) {
if (direction < 0) {
this.cursorPosition = getStartOfSelection();
} else {
this.cursorPosition = getEndOfSelection();
} 
clearSelection();
} else {
if (direction < 0 && this.cursorPosition > 0) {
this.cursorPosition--;
} else if (direction > 0 && this.cursorPosition < this.text.length()) {
this.cursorPosition++;
} 
updateSelectionAfterCursorMove();
} 
}
private void updateSelectionAfterCursorMove() {
if (isShiftDown()) {
if (this.selectionStart == -1) {
this.selectionStart = (this.selectionEnd != -1) ? this.selectionEnd : this.cursorPosition;
}
this.selectionEnd = this.cursorPosition;
} else {
clearSelection();
} 
}
private void pasteFromClipboard() {
String clipboardText = GLFW.glfwGetClipboardString(mc.getWindow().getHandle());
if (clipboardText != null && !clipboardText.isEmpty()) {
clipboardText = clipboardText.replaceAll("[\n\r\t]", "");
if (hasSelection()) {
deleteSelectedText();
}
int maxLength = (this.textSetting.getMax() > 0) ? this.textSetting.getMax() : Integer.MAX_VALUE;
int remainingSpace = maxLength - this.text.length();
if (clipboardText.length() > remainingSpace) {
clipboardText = clipboardText.substring(0, remainingSpace);
}
if (!clipboardText.isEmpty()) {
        this.text = this.text.substring(0, this.cursorPosition) + clipboardText + this.text.substring(this.cursorPosition);
this.cursorPosition += clipboardText.length();
} 
} 
}
private void copyToClipboard() {
if (hasSelection()) {
GLFW.glfwSetClipboardString(mc.getWindow().getHandle(), getSelectedText());
}
}
private void selectAllText() {
this.selectionStart = 0;
this.selectionEnd = this.text.length();
this.cursorPosition = this.text.length();
}
private void replaceText(int start, int end, String replacement) {
if (start < 0) start = 0; 
if (end > this.text.length()) end = this.text.length(); 
if (start > end) {
int temp = start;
start = end;
end = temp;
} 
    this.text = this.text.substring(0, start) + replacement + this.text.substring(end);
this.cursorPosition = start + replacement.length();
clearSelection();
}
private void deleteSelectedText() {
if (hasSelection()) {
replaceText(getStartOfSelection(), getEndOfSelection(), "");
}
}
private boolean hasSelection() {
return (this.selectionStart != -1 && this.selectionEnd != -1 && this.selectionStart != this.selectionEnd);
}
private String getSelectedText() {
if (!hasSelection()) return ""; 
return this.text.substring(getStartOfSelection(), getEndOfSelection());
}
private int getStartOfSelection() {
return Math.min(this.selectionStart, this.selectionEnd);
}
private int getEndOfSelection() {
return Math.max(this.selectionStart, this.selectionEnd);
}
private void clearSelection() {
this.selectionStart = -1;
this.selectionEnd = -1;
}
private int getCursorIndexAt(double mouseX) {
float boxX = this.x + this.width - 65.0F - 2.0F;
float textAreaX = boxX + 4.0F;
float relativeX = (float)(mouseX - textAreaX + this.textScrollOffset);
if (relativeX <= 0.0F) return 0;
int position = 0;
float lastWidth = 0.0F;
while (position < this.text.length()) {
float currentWidth = Fonts.BOLD.getWidth(this.text.substring(0, position + 1), 5.0F);
float midPoint = (lastWidth + currentWidth) / 2.0F;
if (relativeX < midPoint) {
return position;
}
lastWidth = currentWidth;
position++;
} 
return this.text.length();
}
public boolean isHover(double mouseX, double mouseY) {
return (mouseX >= this.x && mouseX <= (this.x + this.width) && mouseY >= this.y && mouseY <= (this.y + this.height));
}
public boolean isFocused() {
return this.focused;
}
}


