package royale.screens.clickgui.impl.module.render;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import royale.modules.module.ModuleStructure;
import royale.screens.clickgui.impl.module.handler.ModuleAnimationHandler;
import royale.screens.clickgui.impl.module.handler.ModuleBindHandler;
import royale.screens.clickgui.impl.module.handler.ModuleScrollHandler;
import royale.screens.clickgui.impl.module.util.ModuleDisplayHelper;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.render.shader.Scissor;
public class ModuleListRenderer
{
private static final float MODULE_ITEM_HEIGHT = 22.0F;
private static final float MODULE_LIST_CORNER_RADIUS = 6.0F;
private static final float CORNER_INSET = 3.0F;
private static final float STATE_BALL_SIZE = 3.0F;
private static final float STATE_TEXT_OFFSET = 6.0F;
private static final float BIND_BOX_HEIGHT = 9.0F;
private static final float BIND_BOX_MIN_WIDTH = 18.0F;
private static final float BIND_BOX_PADDING = 6.0F;
private static final float BIND_WIDTH_ANIM_SPEED = 12.0F;
private final ModuleBindHandler bindHandler;
private final ModuleDisplayHelper displayHelper;
public ModuleListRenderer(ModuleBindHandler bindHandler, ModuleDisplayHelper displayHelper) {
this.bindHandler = bindHandler;
this.displayHelper = displayHelper;
}
public void render(DrawContext context, List<ModuleStructure> displayModules, ModuleStructure selectedModule, ModuleStructure bindingModule, float x, float y, float width, float height, float mouseX, float mouseY, int guiScale, float alphaMultiplier, ModuleAnimationHandler animHandler, ModuleScrollHandler scrollHandler) {
float newAlpha, newOffsetX, newScale;
int panelAlpha = (int)(15.0F * alphaMultiplier);
int outlineAlpha = (int)(215.0F * alphaMultiplier);
Render2D.rect(x, y, width, height, (new Color(64, 64, 64, panelAlpha)).getRGB(), 6.0F);
Render2D.outline(x, y, width, height, 0.5F, (new Color(55, 55, 55, outlineAlpha)).getRGB(), 6.0F);
float topInset = 3.0F;
float bottomInset = 3.0F;
float sideInset = 3.0F;
Scissor.enable(x + sideInset, y + topInset - 1.5F, width - sideInset * 2.0F, height - topInset - bottomInset + 3.0F, guiScale);
if (animHandler.isCategoryTransitioning() && !animHandler.getOldModules().isEmpty()) {
float oldAlpha = (1.0F - animHandler.getCategoryTransitionProgress()) * alphaMultiplier;
float oldOffsetX = animHandler.easeInCubic(animHandler.getCategoryTransitionProgress()) * -animHandler.getCategorySlideDistance();
float oldScale = 1.0F - animHandler.getCategoryTransitionProgress() * 0.1F;
renderModuleItems(context, animHandler.getOldModules(), animHandler.getOldModuleAnimations(), selectedModule, bindingModule, x, y, width, height, mouseX, mouseY, oldAlpha, oldOffsetX, oldScale, 
(float)animHandler.getOldModuleDisplayScroll(), false, topInset, bottomInset, animHandler);
} 
if (animHandler.isCategoryTransitioning()) {
float entryProgress = Math.max(0.0F, (animHandler.getCategoryTransitionProgress() - 0.2F) / 0.8F);
entryProgress = animHandler.easeOutQuart(entryProgress);
newAlpha = entryProgress * alphaMultiplier;
newOffsetX = (1.0F - entryProgress) * animHandler.getCategorySlideDistance();
newScale = 0.9F + entryProgress * 0.1F;
} else {
newAlpha = alphaMultiplier;
newOffsetX = 0.0F;
newScale = 1.0F;
} 
renderModuleItems(context, displayModules, animHandler.getModuleAnimations(), selectedModule, bindingModule, x, y, width, height, mouseX, mouseY, newAlpha, newOffsetX, newScale, 
(float)scrollHandler.getModuleDisplayScroll(), true, topInset, bottomInset, animHandler);
Scissor.disable();
renderScrollFade(x, y + topInset, width, height - topInset - bottomInset, scrollHandler
.getModuleScrollTopFade() * alphaMultiplier, scrollHandler
.getModuleScrollBottomFade() * alphaMultiplier, 80, 15);
}
private void renderModuleItems(DrawContext context, List<ModuleStructure> moduleList, Map<ModuleStructure, Float> animations, ModuleStructure selectedModule, ModuleStructure bindingModule, float x, float y, float width, float height, float mouseX, float mouseY, float alphaMultiplier, float offsetX, float scale, float scrollOffset, boolean interactive, float topInset, float bottomInset, ModuleAnimationHandler animHandler) {
if (alphaMultiplier <= 0.01F)
return; 
float startY = y + topInset + 2.0F + scrollOffset;
float centerY = y + height / 2.0F;
float visibleTop = y + topInset;
float visibleBottom = y + height - bottomInset;
for (int i = 0; i < moduleList.size(); i++) {
ModuleStructure module = moduleList.get(i);
float modY = startY + i * 24.0F;
if (modY + 22.0F >= visibleTop && modY <= visibleBottom) {
float itemProgress = ((Float)animations.getOrDefault(module, Float.valueOf(1.0F))).floatValue();
float posAnim = ((Float)animHandler.getPositionAnimations().getOrDefault(module, Float.valueOf(1.0F))).floatValue();
float alphaAnim = ((Float)animHandler.getModuleAlphaAnimations().getOrDefault(module, Float.valueOf(1.0F))).floatValue();
float combinedAlpha = itemProgress * alphaMultiplier * alphaAnim;
if (combinedAlpha > 0.01F) {
int bgColor;
float itemAnimOffset = (1.0F - itemProgress) * 20.0F;
float posAnimOffset = (1.0F - easeOutCubic(posAnim)) * 15.0F;
float scaledModY = centerY + (modY - centerY) * scale;
float scaledHeight = 22.0F * scale;
float animX = x + 3.0F + offsetX + itemAnimOffset + posAnimOffset;
boolean selected = (interactive && module == selectedModule);
boolean isHighlighted = (interactive && module == animHandler.getHighlightedModule() && animHandler.getHighlightAnimation() > 0.01F);
float hoverAnim = 0.0F;
float stateAnim = interactive ? ((Float)animHandler.getStateAnimations().getOrDefault(module, Float.valueOf(module.isState() ? 1.0F : 0.0F))).floatValue() : (module.isState() ? 1.0F : 0.0F);
float selectedIconAnim = interactive ? ((Float)animHandler.getSelectedIconAnimations().getOrDefault(module, Float.valueOf(0.0F))).floatValue() : 0.0F;
float favoriteAnim = interactive ? ((Float)animHandler.getFavoriteAnimations().getOrDefault(module, Float.valueOf(0.0F))).floatValue() : 0.0F;
boolean hasSettings = this.displayHelper.hasSettings(module);
int baseBgAlpha = 25;
int hoverBgAlpha = 45;
int selectedBgAlpha = 55;
if (selected) {
int bgAlpha = (int)((selectedBgAlpha + hoverAnim * 10.0F) * combinedAlpha);
bgColor = (new Color(71, 71, 71, bgAlpha)).getRGB();
} else {
int bgAlpha = (int)((baseBgAlpha + (hoverBgAlpha - baseBgAlpha) * hoverAnim) * combinedAlpha);
int gray = (int)(64.0F + 36.0F * hoverAnim);
bgColor = (new Color(gray, gray, gray, bgAlpha)).getRGB();
} 
float scaledWidth = (width - 6.0F) * scale;
Render2D.rect(animX, scaledModY, scaledWidth, scaledHeight, bgColor, 5.0F);
if (selected) {
float highlightBoost = isHighlighted ? (animHandler.getHighlightAnimation() * 0.5F) : 0.0F;
int outlineAlpha = (int)((90.0F + 70.0F * highlightBoost) * combinedAlpha);
int outlineGray = (int)(105.0F + 35.0F * highlightBoost);
Render2D.outline(animX, scaledModY, scaledWidth, scaledHeight, 0.5F, (new Color(outlineGray, outlineGray, outlineGray, outlineAlpha)).getRGB(), 5.0F);
} else if (hoverAnim > 0.01F) {
int outlineAlpha = (int)(60.0F * hoverAnim * combinedAlpha);
Render2D.outline(animX, scaledModY, scaledWidth, scaledHeight, 0.5F, (new Color(120, 120, 120, outlineAlpha))
.getRGB(), 5.0F);
} 
float stateTextOffset = stateAnim * 6.0F;
if (stateAnim > 0.01F) {
float ballAlpha = stateAnim * 200.0F * combinedAlpha;
float ballX = animX + 4.0F;
float ballY = scaledModY + (scaledHeight - 3.0F * scale) / 2.0F + 1.0F;
Render2D.rect(ballX, ballY, 3.0F * scale, 3.0F * scale, (new Color(255, 255, 255, (int)ballAlpha))
.getRGB(), 3.0F * scale / 2.0F);
} 
String name = module.getName();
int baseGray = 128;
int targetWhite = 255;
int textBrightness = (int)(baseGray + (targetWhite - baseGray) * stateAnim);
int textAlphaValue = (int)((180.0F + 75.0F * stateAnim) * combinedAlpha);
if (hoverAnim > 0.01F && stateAnim < 0.99F) {
textBrightness = (int)(textBrightness + 40.0F * hoverAnim * (1.0F - stateAnim));
textAlphaValue = (int)(textAlphaValue + 40.0F * hoverAnim * (1.0F - stateAnim));
} 
if (isHighlighted) {
textBrightness = (int)Math.min(255.0F, textBrightness + 30.0F * animHandler.getHighlightAnimation());
}
Color textColor = new Color(textBrightness, textBrightness, textBrightness, Math.min(255, textAlphaValue));
float textX = animX + 5.0F + stateTextOffset;
float textY = scaledModY + (scaledHeight - 6.0F * scale) / 2.0F;
Fonts.BOLD.draw(name, textX, textY, 6.0F * scale, textColor.getRGB());
if (interactive) {
float starX; renderBindBox(module, bindingModule, animX, scaledModY, scaledWidth, scaledHeight, scale, combinedAlpha, stateTextOffset, animHandler);
float iconBaseX = animX + scaledWidth - 14.0F;
float iconY = scaledModY + (scaledHeight - 8.0F * scale) / 2.0F;
if (hasSettings) {
starX = iconBaseX - 12.0F;
} else {
starX = iconBaseX;
} 
int starGray = 50;
int starR = (int)(starGray + (255 - starGray) * favoriteAnim);
int starG = (int)(starGray + (215 - starGray) * favoriteAnim);
int starB = (int)(starGray + (0 - starGray) * favoriteAnim);
float starAlpha = (80.0F + 120.0F * favoriteAnim + 55.0F * hoverAnim) * combinedAlpha;
Fonts.GUI_ICONS.draw("D", starX, iconY + 1.0F, 8.0F * scale, (new Color(starR, starG, starB, (int)starAlpha)).getRGB());
if (hasSettings) {
if (selectedIconAnim > 0.01F) {
float gearAlpha = (150.0F + 50.0F * (isHighlighted ? animHandler.getHighlightAnimation() : 0.0F)) * selectedIconAnim * combinedAlpha;
Fonts.GUI_ICONS.draw("B", iconBaseX, iconY + 1.0F, 8.0F * scale, (new Color(200, 200, 200, (int)gearAlpha)).getRGB());
} 
if (selectedIconAnim < 0.99F) {
float dotsAlpha = 120.0F * (1.0F - selectedIconAnim) * combinedAlpha;
Fonts.BOLD.draw("...", iconBaseX + 1.0F, iconY - 1.0F, 7.0F * scale, (new Color(150, 150, 150, (int)dotsAlpha)).getRGB());
} 
} 
} 
} 
} 
} 
}
private void renderBindBox(ModuleStructure module, ModuleStructure bindingModule, float moduleX, float moduleY, float moduleWidth, float moduleHeight, float scale, float combinedAlpha, float stateTextOffset, ModuleAnimationHandler animHandler) {
String bindText;
boolean isBinding = (module == bindingModule);
int key = module.getKey();
float bindAlpha = isBinding ? 1.0F : ((Float)animHandler.getBindBoxAlphaAnimations().getOrDefault(module, Float.valueOf(0.0F))).floatValue();
if (bindAlpha <= 0.01F && !isBinding && (key == -1 || key == -1)) {
return;
}
if (isBinding) {
bindText = "...";
} else {
bindText = this.bindHandler.getBindDisplayName(key);
} 
float textWidth = Fonts.BOLD.getWidth(bindText, 5.0F * scale);
float targetWidth = Math.max(18.0F, textWidth + 12.0F);
float currentWidth = ((Float)animHandler.getBindBoxWidthAnimations().getOrDefault(module, Float.valueOf(targetWidth))).floatValue();
float widthDiff = targetWidth - currentWidth;
if (Math.abs(widthDiff) > 0.1F) {
currentWidth += widthDiff * 12.0F * 0.016F;
animHandler.getBindBoxWidthAnimations().put(module, Float.valueOf(currentWidth));
} else {
currentWidth = targetWidth;
animHandler.getBindBoxWidthAnimations().put(module, Float.valueOf(currentWidth));
} 
float boxHeight = 9.0F * scale;
float boxWidth = currentWidth * scale * (isBinding ? 1.0F : bindAlpha);
float nameWidth = Fonts.BOLD.getWidth(module.getName(), 6.0F * scale);
float boxX = moduleX + 5.0F + stateTextOffset + nameWidth;
float boxY = moduleY + (moduleHeight - boxHeight) / 2.0F + 0.5F;
float finalAlpha = combinedAlpha * (isBinding ? 1.0F : bindAlpha);
int bgAlpha = (int)(30.0F * finalAlpha);
Color bgColor = new Color(50, 50, 55, bgAlpha);
Render2D.rect(boxX + 3.0F, boxY + 0.5F, boxWidth - 6.0F, boxHeight, bgColor.getRGB(), 3.0F * scale);
int outlineAlpha = (int)(60.0F * finalAlpha);
Color outlineColor = new Color(80, 80, 85, outlineAlpha);
Render2D.outline(boxX + 3.0F, boxY + 0.5F, boxWidth - 6.0F, boxHeight, 0.5F, outlineColor.getRGB(), 3.0F * scale);
if (isBinding || bindAlpha > 0.5F) {
int textAlpha = (int)(160.0F * finalAlpha);
Color textColor = new Color(140, 140, 145, textAlpha);
float textX = boxX + (boxWidth - textWidth) / 2.0F;
float textY = boxY + (boxHeight - 5.0F * scale) / 2.0F;
Fonts.BOLD.draw(bindText, textX, textY, 5.0F * scale, textColor.getRGB());
} 
}
private void renderScrollFade(float x, float y, float w, float h, float topFade, float bottomFade, int alpha, int size) {
if (topFade > 0.01F) {
for (int i = 0; i < size; i++) {
float fadeAlpha = alpha * topFade * (1.0F - i / size);
Render2D.rect(x, y + i, w, 1.0F, (new Color(20, 20, 20, (int)fadeAlpha)).getRGB(), 0.0F);
} 
}
if (bottomFade > 0.01F) {
for (int i = 0; i < size; i++) {
float fadeAlpha = alpha * bottomFade * i / size;
Render2D.rect(x, y + h - size + i, w, 1.0F, (new Color(20, 20, 20, (int)fadeAlpha)).getRGB(), 0.0F);
} 
}
}
public ModuleStructure getModuleAtPosition(List<ModuleStructure> displayModules, double mouseX, double mouseY, float listX, float listY, float listWidth, float listHeight, double scrollOffset, boolean isTransitioning) {
if (isTransitioning) return null; 
if (mouseX < listX || mouseX > (listX + listWidth) || mouseY < listY || mouseY > (listY + listHeight)) return null;
float startY = listY + 3.0F + 2.0F + (float)scrollOffset;
for (int i = 0; i < displayModules.size(); i++) {
float modY = startY + i * 24.0F;
if (mouseX >= (listX + 3.0F) && mouseX <= (listX + listWidth - 3.0F) && mouseY >= modY && mouseY <= (modY + 22.0F)) {
return displayModules.get(i);
}
} 
return null;
}
public boolean isStarClicked(List<ModuleStructure> displayModules, double mouseX, double mouseY, float listX, float listY, float listWidth, float listHeight, double scrollOffset, ModuleDisplayHelper displayHelper, boolean isTransitioning) {
if (isTransitioning) return false;
float startY = listY + 3.0F + 2.0F + (float)scrollOffset;
for (int i = 0; i < displayModules.size(); i++) {
ModuleStructure module = displayModules.get(i);
float modY = startY + i * 24.0F;
if (mouseY >= modY && mouseY <= (modY + 22.0F)) {
float starX, scaledWidth = listWidth - 6.0F;
float animX = listX + 3.0F;
boolean hasSettings = displayHelper.hasSettings(module);
if (hasSettings) {
starX = animX + scaledWidth - 14.0F - 12.0F;
} else {
starX = animX + scaledWidth - 14.0F;
} 
if (mouseX >= starX && mouseX <= (starX + 10.0F)) {
return true;
}
} 
} 
return false;
}
public ModuleStructure getModuleForStarClick(List<ModuleStructure> displayModules, double mouseX, double mouseY, float listX, float listY, float listWidth, float listHeight, double scrollOffset, ModuleDisplayHelper displayHelper, boolean isTransitioning) {
if (isTransitioning) return null;
float startY = listY + 3.0F + 2.0F + (float)scrollOffset;
for (int i = 0; i < displayModules.size(); i++) {
ModuleStructure module = displayModules.get(i);
float modY = startY + i * 24.0F;
if (mouseY >= modY && mouseY <= (modY + 22.0F)) {
float starX, scaledWidth = listWidth - 6.0F;
float animX = listX + 3.0F;
boolean hasSettings = displayHelper.hasSettings(module);
if (hasSettings) {
starX = animX + scaledWidth - 14.0F - 12.0F;
} else {
starX = animX + scaledWidth - 14.0F;
} 
if (mouseX >= starX && mouseX <= (starX + 10.0F)) {
return module;
}
} 
} 
return null;
}
private float easeOutCubic(float x) {
return 1.0F - (float)Math.pow((1.0F - x), 3.0D);
}
}


