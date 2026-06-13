package royale.screens.clickgui.impl.settingsrender;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.SelectSetting;
import royale.util.interfaces.AbstractSettingComponent;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.render.shader.Scissor;
public class SelectComponent extends AbstractSettingComponent {
private final SelectSetting selectSetting;
private boolean expanded = false;
private float expandAnimation = 0.0F;
private float hoverAnimation = 0.0F;
private float descScrollOffset = 0.0F;
private boolean descScrollingRight = true;
private long descScrollPauseTime = 0L;
private float arrowRotation = 0.0F;
private final Map<String, Float> optionHoverAnimations = new HashMap<>();
private final Map<String, Float> selectAnimations = new HashMap<>();
private String previousSelected = "";
private float selectedTextAlpha = 1.0F;
private float selectedTextSlide = 1.0F;
private float newSelectedTextAlpha = 0.0F;
private float newSelectedTextSlide = 0.0F;
private String animatingFromText = "";
private boolean isAnimatingSelection = false;
private long lastUpdateTime = System.currentTimeMillis();
private static final float ANIMATION_SPEED = 8.0F;
private static final float COLLAPSE_SPEED = 15.0F;
private static final float BOX_WIDTH = 65.0F;
private static final float OPTION_HEIGHT = 14.0F;
private static final long SCROLL_PAUSE_DURATION = 2000L;
private static final float SCROLL_PIXELS_PER_SECOND = 20.0F;
private static final float DESC_PADDING = 8.0F;
private static final float SELECTION_ANIMATION_SPEED = 10.0F;
public SelectComponent(SelectSetting setting) {
super((Setting)setting);
this.selectSetting = setting;
this.previousSelected = setting.getSelected();
for (String option : setting.getList()) {
this.optionHoverAnimations.put(option, Float.valueOf(0.0F));
this.selectAnimations.put(option, Float.valueOf(setting.isSelected(option) ? 1.0F : 0.0F));
} 
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
private void updateSelectionAnimation(float deltaTime) {
String currentSelected = this.selectSetting.getSelected();
if (!currentSelected.equals(this.previousSelected) && !this.isAnimatingSelection) {
this.animatingFromText = this.previousSelected;
this.isAnimatingSelection = true;
this.selectedTextAlpha = 1.0F;
this.selectedTextSlide = 1.0F;
this.newSelectedTextAlpha = 0.0F;
this.newSelectedTextSlide = 0.0F;
} 
if (this.isAnimatingSelection) {
this.selectedTextAlpha = lerp(this.selectedTextAlpha, 0.0F, deltaTime * 10.0F);
this.selectedTextSlide = lerp(this.selectedTextSlide, 0.0F, deltaTime * 10.0F);
if (this.selectedTextAlpha < 0.5F) {
this.newSelectedTextAlpha = lerp(this.newSelectedTextAlpha, 1.0F, deltaTime * 10.0F);
this.newSelectedTextSlide = lerp(this.newSelectedTextSlide, 1.0F, deltaTime * 10.0F);
} 
if (this.newSelectedTextAlpha > 0.99F && this.newSelectedTextSlide > 0.99F) {
this.isAnimatingSelection = false;
this.previousSelected = currentSelected;
this.selectedTextAlpha = 1.0F;
this.selectedTextSlide = 1.0F;
this.newSelectedTextAlpha = 1.0F;
this.newSelectedTextSlide = 1.0F;
} 
} else {
this.previousSelected = currentSelected;
} 
}
public void render(DrawContext context, int mouseX, int mouseY, float delta) {
float deltaTime = getDeltaTime();
updateSelectionAnimation(deltaTime);
boolean mainHovered = isMainHover(mouseX, mouseY);
this.hoverAnimation = lerp(this.hoverAnimation, mainHovered ? 1.0F : 0.0F, deltaTime * 8.0F);
float expandSpeed = this.expanded ? 8.0F : 15.0F;
this.expandAnimation = lerp(this.expandAnimation, this.expanded ? 1.0F : 0.0F, deltaTime * expandSpeed);
float targetRotation = this.expanded ? 90.0F : 0.0F;
this.arrowRotation = lerp(this.arrowRotation, targetRotation, deltaTime * 8.0F);
Fonts.GUI_ICONS.draw("J", this.x - 0.5F, this.y + this.height / 2.0F - 8.5F, 9.0F, applyAlpha(new Color(210, 210, 210, 200)).getRGB());
Fonts.BOLD.draw(this.selectSetting.getName(), this.x + 9.5F, this.y + this.height / 2.0F - 7.5F, 6.0F, applyAlpha(new Color(210, 210, 220, 200)).getRGB());
String description = this.selectSetting.getDescription();
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
renderAnimatedSelectedText(boxX, boxY, boxHeight);
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
private void renderAnimatedSelectedText(float boxX, float boxY, float boxHeight) {
float maxTextWidth = 51.0F;
float textY = boxY + boxHeight / 2.0F - 2.5F;
Scissor.enable(boxX + 2.0F, boxY, maxTextWidth + 2.0F, boxHeight, 2.0F);
if (this.isAnimatingSelection) {
if (this.selectedTextAlpha > 0.01F) {
String displayOld = truncateText(this.selectSetting.getDisplayValue(this.animatingFromText), maxTextWidth);
float slideOffset = (1.0F - this.selectedTextSlide) * -15.0F;
int alpha = (int)(200.0F * this.selectedTextAlpha * this.alphaMultiplier);
Fonts.BOLD.draw(displayOld, boxX + 4.0F + slideOffset, textY, 5.0F, (new Color(160, 160, 165, alpha)).getRGB());
} 
if (this.newSelectedTextAlpha > 0.01F) {
String selected = this.selectSetting.getDisplaySelected();
String displayNew = truncateText(selected, maxTextWidth);
float slideOffset = (1.0F - this.newSelectedTextSlide) * 20.0F;
int alpha = (int)(200.0F * this.newSelectedTextAlpha * this.alphaMultiplier);
Fonts.BOLD.draw(displayNew, boxX + 4.0F + slideOffset, textY, 5.0F, (new Color(160, 160, 165, alpha)).getRGB());
} 
} else {
String selected = this.selectSetting.getDisplaySelected();
String displaySelected = truncateText(selected, maxTextWidth);
Fonts.BOLD.draw(displaySelected, boxX + 4.0F, textY, 5.0F, applyAlpha(new Color(160, 160, 165, 200)).getRGB());
} 
Scissor.disable();
}
private String truncateText(String text, float maxWidth) {
if (Fonts.BOLD.getWidth(text, 5.0F) <= maxWidth) {
return text;
}
String truncated = text;
while (Fonts.BOLD.getWidth(truncated + "..", 5.0F) > maxWidth && truncated.length() > 1) {
truncated = truncated.substring(0, truncated.length() - 1);
}
return truncated + "..";
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
private void renderExpandedOptions(DrawContext context, int mouseX, int mouseY, float boxX, float startY, float deltaTime) {
List<String> options = this.selectSetting.getList();
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
boolean isSelected = this.selectSetting.isSelected(option);
float selectAnim = ((Float)this.selectAnimations.getOrDefault(option, Float.valueOf(0.0F))).floatValue();
selectAnim = lerp(selectAnim, isSelected ? 1.0F : 0.0F, deltaTime * 10.0F);
this.selectAnimations.put(option, Float.valueOf(selectAnim));
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
if (selectAnim > 0.01F) {
float innerSize = (checkSize - 2.0F) * selectAnim;
float innerX = checkX + (checkSize - innerSize) / 2.0F;
float innerY = checkY + (checkSize - innerSize) / 2.0F;
int innerAlpha = (int)(220.0F * selectAnim * panelAlpha);
Render2D.rect(innerX, innerY, innerSize, innerSize, (new Color(140, 180, 160, innerAlpha)).getRGB(), 1.5F);
} 
float textX = checkX + checkSize + 4.0F;
float textY = optionY + 7.0F - 2.5F;
float availableTextWidth = 65.0F - checkSize - 14.0F;
String displayOption = this.selectSetting.getDisplayValue(option);
float optionTextWidth = Fonts.BOLD.getWidth(displayOption, 5.0F);
if (optionTextWidth > availableTextWidth) {
while (Fonts.BOLD.getWidth(displayOption + "..", 5.0F) > availableTextWidth && displayOption.length() > 1) {
displayOption = displayOption.substring(0, displayOption.length() - 1);
}
displayOption = displayOption + "..";
} 
int textGray = (int)(140.0F + selectAnim * 40.0F + hoverAnim * 20.0F);
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
for (String option : this.selectSetting.getList()) {
if (mouseX >= boxX && mouseX <= (boxX + 65.0F) && mouseY >= optionY && mouseY <= (optionY + 14.0F)) {
this.selectSetting.setSelected(option);
this.expanded = false;
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
float expandedHeight = this.selectSetting.getList().size() * 14.0F * this.expandAnimation;
return baseHeight + expandedHeight;
}
}