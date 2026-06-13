package royale.screens.clickgui.impl.settingsrender;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.gui.DrawContext;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.MultiSelectSetting;
import royale.util.interfaces.AbstractSettingComponent;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.render.shader.Scissor;
public class MultiSelectComponent extends AbstractSettingComponent {
private final MultiSelectSetting multiSelectSetting;
private boolean expanded = false;
private float expandAnimation = 0.0F;
private float hoverAnimation = 0.0F;
private float scrollOffset = 0.0F;
private float scrollOffsetAnimated = 0.0F;
private boolean scrollingRight = true;
private long scrollPauseTime = 0L;
private float descScrollOffset = 0.0F;
private boolean descScrollingRight = true;
private long descScrollPauseTime = 0L;
private float arrowRotation = 0.0F;
private final Map<String, Float> optionHoverAnimations = new HashMap<>();
private final Map<String, Float> checkAnimations = new HashMap<>();
private final Map<String, Float> itemAlphaAnimations = new HashMap<>();
private final Map<String, Float> itemXPositions = new HashMap<>();
private final Map<String, Float> itemTargetPositions = new HashMap<>();
private final Set<String> previousSelected = new HashSet<>();
private float noneAlphaAnimation = 0.0F;
private long lastUpdateTime = System.currentTimeMillis();
private static final float ANIMATION_SPEED = 8.0F;
private static final float COLLAPSE_SPEED = 15.0F;
private static final long SCROLL_PAUSE_DURATION = 2000L;
private static final float BOX_WIDTH = 65.0F;
private static final float OPTION_HEIGHT = 14.0F;
private static final float SCROLL_PIXELS_PER_SECOND = 20.0F;
private static final float DESC_PADDING = 8.0F;
private static final float ITEM_ANIMATION_SPEED = 10.0F;
private static final float POSITION_ANIMATION_SPEED = 8.0F;
public MultiSelectComponent(MultiSelectSetting setting) {
super((Setting)setting);
this.multiSelectSetting = setting;
for (String option : setting.getList()) {
this.checkAnimations.put(option, Float.valueOf(setting.isSelected(option) ? 1.0F : 0.0F));
this.optionHoverAnimations.put(option, Float.valueOf(0.0F));
} 
this.previousSelected.addAll(setting.getSelected());
float initX = 0.0F;
for (String item : setting.getList()) {
if (setting.isSelected(item)) {
this.itemAlphaAnimations.put(item, Float.valueOf(1.0F));
this.itemXPositions.put(item, Float.valueOf(initX));
this.itemTargetPositions.put(item, Float.valueOf(initX));
String displayText = item + ", ";
initX += Fonts.BOLD.getWidth(this.multiSelectSetting.getDisplayValue(displayText), 5.0F);
} 
} 
this.noneAlphaAnimation = setting.getSelected().isEmpty() ? 1.0F : 0.0F;
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
private void updateItemAnimations(float deltaTime) {
Set<String> currentSelected = new HashSet<>(this.multiSelectSetting.getSelected());
for (String item : currentSelected) {
if (!this.itemAlphaAnimations.containsKey(item)) {
this.itemAlphaAnimations.put(item, Float.valueOf(0.0F));
float lastPos = 0.0F;
for (String existingItem : this.multiSelectSetting.getList()) {
if (this.itemXPositions.containsKey(existingItem)) {
float pos = ((Float)this.itemXPositions.get(existingItem)).floatValue();
String text = existingItem + ", ";
float endPos = pos + Fonts.BOLD.getWidth(this.multiSelectSetting.getDisplayValue(text), 5.0F);
if (endPos > lastPos) {
lastPos = endPos;
}
} 
} 
this.itemXPositions.put(item, Float.valueOf(lastPos));
this.itemTargetPositions.put(item, Float.valueOf(lastPos));
} 
} 
for (String item : this.itemAlphaAnimations.keySet()) {
boolean isSelected = currentSelected.contains(item);
float currentAlpha = ((Float)this.itemAlphaAnimations.get(item)).floatValue();
float targetAlpha = isSelected ? 1.0F : 0.0F;
float newAlpha = lerp(currentAlpha, targetAlpha, deltaTime * 10.0F);
this.itemAlphaAnimations.put(item, Float.valueOf(newAlpha));
} 
List<String> allItems = this.multiSelectSetting.getList();
List<String> visibleItems = new ArrayList<>();
for (String item : allItems) {
if (this.itemAlphaAnimations.containsKey(item) && ((Float)this.itemAlphaAnimations.get(item)).floatValue() > 0.01F) {
visibleItems.add(item);
}
} 
float currentTargetX = 0.0F;
for (int i = 0; i < visibleItems.size(); i++) {
String item = visibleItems.get(i);
float itemAlpha = ((Float)this.itemAlphaAnimations.getOrDefault(item, Float.valueOf(0.0F))).floatValue();
this.itemTargetPositions.put(item, Float.valueOf(currentTargetX));
String displayText = item;
if (i < visibleItems.size() - 1) {
displayText = displayText + ", ";
}
float textWidth = Fonts.BOLD.getWidth(this.multiSelectSetting.getDisplayValue(displayText), 5.0F);
currentTargetX += textWidth * itemAlpha;
} 
for (String item : visibleItems) {
float targetX = ((Float)this.itemTargetPositions.getOrDefault(item, Float.valueOf(0.0F))).floatValue();
float currentX = ((Float)this.itemXPositions.getOrDefault(item, Float.valueOf(targetX))).floatValue();
currentX = lerp(currentX, targetX, deltaTime * 8.0F);
this.itemXPositions.put(item, Float.valueOf(currentX));
} 
List<String> toRemove = new ArrayList<>();
for (String item : this.itemAlphaAnimations.keySet()) {
boolean isSelected = currentSelected.contains(item);
float alpha = ((Float)this.itemAlphaAnimations.get(item)).floatValue();
if (!isSelected && alpha < 0.01F) {
toRemove.add(item);
}
} 
for (String item : toRemove) {
this.itemAlphaAnimations.remove(item);
this.itemXPositions.remove(item);
this.itemTargetPositions.remove(item);
} 
boolean hasVisibleItems = false;
for (Float alpha : this.itemAlphaAnimations.values()) {
if (alpha.floatValue() > 0.01F) {
hasVisibleItems = true;
break;
} 
} 
float noneTarget = (!hasVisibleItems && currentSelected.isEmpty()) ? 1.0F : 0.0F;
this.noneAlphaAnimation = lerp(this.noneAlphaAnimation, noneTarget, deltaTime * 10.0F);
this.previousSelected.clear();
this.previousSelected.addAll(currentSelected);
}
public void render(DrawContext context, int mouseX, int mouseY, float delta) {
float deltaTime = getDeltaTime();
updateItemAnimations(deltaTime);
boolean mainHovered = isMainHover(mouseX, mouseY);
this.hoverAnimation = lerp(this.hoverAnimation, mainHovered ? 1.0F : 0.0F, deltaTime * 8.0F);
float expandSpeed = this.expanded ? 8.0F : 15.0F;
this.expandAnimation = lerp(this.expandAnimation, this.expanded ? 1.0F : 0.0F, deltaTime * expandSpeed);
float targetRotation = this.expanded ? 90.0F : 0.0F;
this.arrowRotation = lerp(this.arrowRotation, targetRotation, deltaTime * 8.0F);
Fonts.GUI_ICONS.draw("I", this.x - 0.5F, this.y + this.height / 2.0F - 8.5F, 9.0F, applyAlpha(new Color(210, 210, 220, 200)).getRGB());
Fonts.BOLD.draw(this.multiSelectSetting.getName(), this.x + 9.5F, this.y + this.height / 2.0F - 7.5F, 6.0F, applyAlpha(new Color(210, 210, 220, 200)).getRGB());
String description = this.multiSelectSetting.getDescription();
if (description != null && !description.isEmpty()) {
renderScrollingDescription(description, deltaTime);
}
float boxX = this.x + this.width - 65.0F - 2.0F;
float boxY = this.y + this.height / 2.0F - 5.0F;
float boxHeight = 10.0F;
int bgAlpha = 25 + (int)(this.hoverAnimation * 15.0F);
Render2D.rect(boxX, boxY, 65.0F, boxHeight, applyAlpha(new Color(55, 55, 55, bgAlpha)).getRGB(), 3.0F);
int outlineAlpha = 60 + (int)(this.hoverAnimation * 40.0F);
Render2D.outline(boxX, boxY, 65.0F, boxHeight, 0.5F, applyAlpha(new Color(155, 155, 155, outlineAlpha)).getRGB(), 3.0F);
renderSelectedText(boxX, boxY, 65.0F, boxHeight, deltaTime);
renderArrowIcon(boxX + 65.0F - 4.5F, boxY + boxHeight / 2.0F);
if (this.expandAnimation > 0.01F) {
renderExpandedOptions(context, mouseX, mouseY, boxX, boxY + boxHeight + 2.0F, deltaTime);
}
}
private void renderArrowIcon(float centerX, float centerY) {
int arrowAlpha = 120 + (int)(this.hoverAnimation * 60.0F);
float iconSize = 7.8F;
String glyph = "W";
float glyphHeight = Fonts.GUI_ICONS.getHeight(7.8F);
float drawY = centerY - glyphHeight / 2.0F;
Fonts.GUI_ICONS.drawCentered("W", centerX, drawY, 7.8F, 
applyAlpha(new Color(180, 180, 185, arrowAlpha)).getRGB(), this.arrowRotation);
}
private void renderScrollingDescription(String description, float deltaTime) {
float descY = this.y + this.height / 2.0F + 0.5F;
float boxX = this.x + this.width - 65.0F - 2.0F;
drawSettingDescription(description, this.x + 0.5F, descY, boxX - this.x - 8.0F);
}
private void updateDescScrollAnimation(float deltaTime, float textWidth, float availableWidth) {
long currentTime = System.currentTimeMillis();
if (this.descScrollPauseTime > 0L) {
if (currentTime - this.descScrollPauseTime < 2000L) {
return;
}
this.descScrollPauseTime = 0L;
} 
float scrollDistance = textWidth - availableWidth + 5.0F;
if (scrollDistance <= 0.0F) {
this.descScrollOffset = 0.0F;
return;
} 
float scrollSpeed = 20.0F / scrollDistance;
if (this.descScrollingRight) {
this.descScrollOffset += deltaTime * scrollSpeed;
if (this.descScrollOffset >= 1.0F) {
this.descScrollOffset = 1.0F;
this.descScrollingRight = false;
this.descScrollPauseTime = currentTime;
} 
} else {
this.descScrollOffset -= deltaTime * scrollSpeed;
if (this.descScrollOffset <= 0.0F) {
this.descScrollOffset = 0.0F;
this.descScrollingRight = true;
this.descScrollPauseTime = currentTime;
} 
} 
}
private void renderSelectedText(float boxX, float boxY, float boxWidth, float boxHeight, float deltaTime) {
float textY = boxY + boxHeight / 2.0F - 2.5F;
float availableWidth = boxWidth - 4.0F;
float baseX = boxX + 4.0F;
Scissor.enable(boxX + 1.0F, boxY, availableWidth + 2.0F, boxHeight, 2.0F);
if (this.noneAlphaAnimation > 0.01F) {
int noneAlpha = (int)(200.0F * this.noneAlphaAnimation * this.alphaMultiplier);
Fonts.BOLD.draw("None", baseX, textY, 5.0F, (new Color(160, 160, 165, noneAlpha)).getRGB());
} 
List<String> allItems = this.multiSelectSetting.getList();
List<String> visibleItems = new ArrayList<>();
for (String item : allItems) {
if (this.itemAlphaAnimations.containsKey(item) && ((Float)this.itemAlphaAnimations.get(item)).floatValue() > 0.01F) {
visibleItems.add(item);
}
} 
if (visibleItems.isEmpty()) {
Scissor.disable();
return;
} 
float totalWidth = 0.0F;
for (int i = 0; i < visibleItems.size(); i++) {
String item = visibleItems.get(i);
float itemAlpha = ((Float)this.itemAlphaAnimations.getOrDefault(item, Float.valueOf(0.0F))).floatValue();
String displayText = item;
if (i < visibleItems.size() - 1) {
displayText = displayText + ", ";
}
totalWidth += Fonts.BOLD.getWidth(this.multiSelectSetting.getDisplayValue(displayText), 5.0F) * itemAlpha;
} 
if (totalWidth <= availableWidth) {
this.scrollOffset = 0.0F;
this.scrollOffsetAnimated = lerp(this.scrollOffsetAnimated, 0.0F, deltaTime * 8.0F);
} else {
updateScrollAnimation(deltaTime, totalWidth, availableWidth);
this.scrollOffsetAnimated = lerp(this.scrollOffsetAnimated, this.scrollOffset, deltaTime * 8.0F);
} 
float maxScroll = Math.max(0.0F, totalWidth - availableWidth + 5.0F);
float currentScroll = this.scrollOffsetAnimated * maxScroll;
for (int j = 0; j < visibleItems.size(); j++) {
String item = visibleItems.get(j);
float itemAlpha = ((Float)this.itemAlphaAnimations.getOrDefault(item, Float.valueOf(0.0F))).floatValue();
float itemX = ((Float)this.itemXPositions.getOrDefault(item, Float.valueOf(0.0F))).floatValue();
String displayText = item;
if (j < visibleItems.size() - 1) {
displayText = displayText + ", ";
}
float renderX = baseX + itemX - currentScroll;
int alpha = (int)(200.0F * itemAlpha * this.alphaMultiplier);
if (alpha > 0) {
Fonts.BOLD.draw(this.multiSelectSetting.getDisplayValue(displayText), renderX, textY, 5.0F, (new Color(160, 160, 165, alpha)).getRGB());
}
} 
Scissor.disable();
}
private void updateScrollAnimation(float deltaTime, float textWidth, float availableWidth) {
long currentTime = System.currentTimeMillis();
if (this.scrollPauseTime > 0L) {
if (currentTime - this.scrollPauseTime < 2000L) {
return;
}
this.scrollPauseTime = 0L;
} 
float scrollDistance = textWidth - availableWidth + 5.0F;
if (scrollDistance <= 0.0F) {
this.scrollOffset = 0.0F;
return;
} 
float scrollSpeed = 20.0F / scrollDistance;
if (this.scrollingRight) {
this.scrollOffset += deltaTime * scrollSpeed;
if (this.scrollOffset >= 1.0F) {
this.scrollOffset = 1.0F;
this.scrollingRight = false;
this.scrollPauseTime = currentTime;
} 
} else {
this.scrollOffset -= deltaTime * scrollSpeed;
if (this.scrollOffset <= 0.0F) {
this.scrollOffset = 0.0F;
this.scrollingRight = true;
this.scrollPauseTime = currentTime;
} 
} 
}
private void renderExpandedOptions(DrawContext context, int mouseX, int mouseY, float boxX, float startY, float deltaTime) {
List<String> options = this.multiSelectSetting.getList();
float fullPanelHeight = options.size() * 14.0F;
float visibleHeight = fullPanelHeight * this.expandAnimation;
float panelAlpha = this.expandAnimation * this.alphaMultiplier;
int panelBgAlpha = (int)(200.0F * panelAlpha);
Render2D.rect(boxX, startY, 65.0F, visibleHeight, (new Color(30, 30, 30, panelBgAlpha)).getRGB(), 3.0F);
int panelOutlineAlpha = (int)(100.0F * panelAlpha);
Render2D.outline(boxX, startY, 65.0F, visibleHeight, 0.5F, (new Color(80, 80, 85, panelOutlineAlpha)).getRGB(), 3.0F);
if (visibleHeight < 1.0F)
return; 
Scissor.enable(boxX, startY, 65.0F, visibleHeight, 2.0F);
float optionY = startY;
for (int i = 0; i < options.size(); i++) {
String option = options.get(i);
boolean optionHovered = (mouseX >= boxX && mouseX <= boxX + 65.0F && mouseY >= optionY && mouseY <= optionY + 14.0F && this.expandAnimation > 0.8F);
float hoverAnim = ((Float)this.optionHoverAnimations.getOrDefault(option, Float.valueOf(0.0F))).floatValue();
hoverAnim = lerp(hoverAnim, optionHovered ? 1.0F : 0.0F, deltaTime * 8.0F);
this.optionHoverAnimations.put(option, Float.valueOf(hoverAnim));
boolean isSelected = this.multiSelectSetting.isSelected(option);
float checkAnim = ((Float)this.checkAnimations.getOrDefault(option, Float.valueOf(0.0F))).floatValue();
checkAnim = lerp(checkAnim, isSelected ? 1.0F : 0.0F, deltaTime * 10.0F);
this.checkAnimations.put(option, Float.valueOf(checkAnim));
if (hoverAnim > 0.01F) {
int hoverBgAlpha = (int)(30.0F * hoverAnim * panelAlpha);
Render2D.rect(boxX + 2.0F, optionY + 1.0F, 61.0F, 12.0F, (new Color(100, 100, 105, hoverBgAlpha))
.getRGB(), 2.0F);
} 
float checkSize = 6.0F;
float checkX = boxX + 5.0F;
float checkY = optionY + 7.0F - checkSize / 2.0F;
int checkBgAlpha = (int)((40.0F + hoverAnim * 20.0F) * panelAlpha);
Render2D.rect(checkX, checkY, checkSize, checkSize, (new Color(55, 55, 60, checkBgAlpha)).getRGB(), 2.0F);
int checkOutlineAlpha = (int)((80.0F + hoverAnim * 40.0F) * panelAlpha);
Render2D.outline(checkX, checkY, checkSize, checkSize, 0.5F, (new Color(120, 120, 125, checkOutlineAlpha)).getRGB(), 2.0F);
if (checkAnim > 0.01F) {
float innerSize = (checkSize - 2.0F) * checkAnim;
float innerX = checkX + (checkSize - innerSize) / 2.0F;
float innerY = checkY + (checkSize - innerSize) / 2.0F;
int innerAlpha = (int)(220.0F * checkAnim * panelAlpha);
Render2D.rect(innerX, innerY, innerSize, innerSize, (new Color(140, 180, 160, innerAlpha)).getRGB(), 1.5F);
} 
float textX = checkX + checkSize + 4.0F;
float textY = optionY + 7.0F - 2.5F;
float availableTextWidth = 65.0F - checkSize - 14.0F;
String displayOption = this.multiSelectSetting.getDisplayValue(option);
float optionTextWidth = Fonts.BOLD.getWidth(displayOption, 5.0F);
if (optionTextWidth > availableTextWidth) {
while (Fonts.BOLD.getWidth(displayOption + "..", 5.0F) > availableTextWidth && displayOption.length() > 1) {
displayOption = displayOption.substring(0, displayOption.length() - 1);
}
displayOption = displayOption + "..";
} 
int textGray = (int)(140.0F + checkAnim * 40.0F + hoverAnim * 20.0F);
int textAlpha = (int)(200.0F * panelAlpha);
Fonts.BOLD.draw(displayOption, textX, textY, 5.0F, (new Color(textGray, textGray, textGray + 5, textAlpha)).getRGB());
optionY += 14.0F;
} 
Scissor.disable();
}
private boolean isMainHover(double mouseX, double mouseY) {
float boxX = this.x + this.width - 65.0F - 2.0F;
float boxY = this.y + this.height / 2.0F - 5.0F;
float boxHeight = 10.0F;
return (mouseX >= boxX && mouseX <= (boxX + 65.0F) && mouseY >= boxY && mouseY <= (boxY + boxHeight));
}
public boolean mouseClicked(double mouseX, double mouseY, int button) {
if (button == 1 && isMainHover(mouseX, mouseY)) {
this.expanded = !this.expanded;
return true;
} 
if (button == 0 && 
this.expanded && this.expandAnimation > 0.8F) {
float boxX = this.x + this.width - 65.0F - 2.0F;
float boxY = this.y + this.height / 2.0F - 5.0F;
float startY = boxY + 10.0F + 2.0F;
float optionY = startY;
for (String option : this.multiSelectSetting.getList()) {
if (mouseX >= boxX && mouseX <= (boxX + 65.0F) && mouseY >= optionY && mouseY <= (optionY + 14.0F)) {
if (this.multiSelectSetting.isSelected(option)) {
this.multiSelectSetting.getSelected().remove(option);
} else {
this.multiSelectSetting.getSelected().add(option);
} 
return true;
} 
optionY += 14.0F;
} 
} 
return false;
}
public void tick() {}
public boolean isHover(double mouseX, double mouseY) {
return (mouseX >= this.x && mouseX <= (this.x + this.width) && mouseY >= this.y && mouseY <= (this.y + this.height));
}
public float getTotalHeight() {
float baseHeight = this.height;
float expandedHeight = this.multiSelectSetting.getList().size() * 14.0F * this.expandAnimation;
return baseHeight + expandedHeight;
}
}


