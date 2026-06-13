package royale.screens.clickgui.impl.configs.render;
import java.awt.Color;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import royale.screens.clickgui.impl.configs.handler.ConfigAnimationHandler;
import royale.screens.clickgui.impl.configs.handler.ConfigDataHandler;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.render.shader.Scissor;
import royale.util.theme.ClientTheme;
public class ConfigListRenderer
{
private static final float CONFIG_ITEM_HEIGHT = 24.0F;
private static final float CONFIG_ITEM_SPACING = 3.0F;
private static final float HOVER_SPEED = 0.15F;
private final ConfigAnimationHandler animationHandler;
private final ConfigDataHandler dataHandler;
private final ConfigNotificationRenderer notificationRenderer;
private String lastClickedConfig = null;
private long lastClickTime = 0L;
private static final long RENAME_DOUBLE_CLICK_MS = 350L;
public ConfigListRenderer(ConfigAnimationHandler animationHandler, ConfigDataHandler dataHandler, ConfigNotificationRenderer notificationRenderer) {
this.animationHandler = animationHandler;
this.dataHandler = dataHandler;
this.notificationRenderer = notificationRenderer;
}
public void render(DrawContext context, float x, float y, float mouseX, float mouseY, int guiScale, float alpha) {
float listX = x + 8.0F;
float listY = y + 37.0F;
float listW = 282.0F;
float listH = 159.0F;
if (this.dataHandler.isCreating()) {
listH -= 40.0F * this.animationHandler.getCreateBoxAnimation();
}
this.dataHandler.updateScroll(0.016F);
this.dataHandler.updateScrollFades(listH);
Scissor.enable(listX, listY - 8.0F, listW, listH + 15.0F, guiScale);
float itemY = listY + (float)this.dataHandler.getScrollOffset();
for (String config : this.dataHandler.getConfigs()) {
float itemAlpha = this.animationHandler.getItemAppearAnimation(config);
if (itemAlpha < 0.01F) {
itemY += 27.0F;
continue;
} 
if (itemY + 24.0F >= listY && itemY <= listY + listH) {
float itemSlide = (1.0F - itemAlpha) * 15.0F;
renderConfigItem(config, listX + itemSlide, itemY, listW, mouseX, mouseY, alpha * itemAlpha);
} 
itemY += 27.0F;
} 
if (this.dataHandler.getConfigs().isEmpty()) {
renderEmptyMessage(x, y, alpha);
}
renderScrollFade(listX, listY - 1.0F, listW, listH + 2.0F, this.dataHandler.getScrollTopFade(), this.dataHandler.getScrollBottomFade());
Scissor.disable();
}
private void renderConfigItem(String config, float x, float y, float width, float mouseX, float mouseY, float alpha) {
boolean isHovered = (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 24.0F);
boolean isSelected = this.dataHandler.isSelectedConfig(config);
float hoverAnim = this.animationHandler.getHoverAnimation(config);
float target = isHovered ? 1.0F : 0.0F;
hoverAnim += (target - hoverAnim) * 0.15F;
this.animationHandler.setHoverAnimation(config, hoverAnim);
renderItemBackground(x, y, width, isSelected, hoverAnim, alpha);
renderItemName(config, x, y - 0.5F, alpha);
renderActionButtons(config, x, y - 0.5F, width, mouseX, mouseY, alpha);
}
private void renderItemBackground(float x, float y, float width, boolean isSelected, float hoverAnim, float alpha) {
int bgAlpha = (int)((20.0F + 15.0F * hoverAnim + (isSelected ? 10 : 0)) * alpha);
int gray = (int)(60.0F + 20.0F * hoverAnim);
Render2D.rect(x, y, width, 24.0F, (new Color(gray, gray, gray, bgAlpha)).getRGB(), 5.0F);
if (isSelected || hoverAnim > 0.01F) {
int outlineAlpha = (int)((40.0F + 40.0F * hoverAnim + (isSelected ? 55 : 0)) * alpha);
int outlineColor = isSelected ? ClientTheme.accentWithAlpha(outlineAlpha) : (new Color(100, 100, 100, outlineAlpha)).getRGB();
Render2D.outline(x, y, width, 24.0F, 0.5F, outlineColor, 5.0F);
} 
}
private void renderItemName(String config, float x, float y, float alpha) {
Fonts.GUI_ICONS.draw("B", x + 4.0F, y + 4.5F, 16.0F, (new Color(220, 220, 220, (int)(25.0F * alpha))).getRGB());
String displayName = (config.length() > 18) ? (config.substring(0, 18) + "...") : config;
Fonts.BOLD.draw(displayName, x + 10.0F, y + 8.0F, 6.0F, (new Color(220, 220, 220, (int)(255.0F * alpha))).getRGB());
}
private void renderActionButtons(String config, float x, float y, float width, float mouseX, float mouseY, float alpha) {
float buttonSize = 18.0F;
float buttonY = y + (24.0F - buttonSize) / 2.0F + 1.0F;
float deleteButtonX = x + width - buttonSize - 8.0F;
float refreshButtonX = deleteButtonX - buttonSize - 5.0F;
float loadButtonX = refreshButtonX - buttonSize - 5.0F;
Color accent = new Color(ClientTheme.accent(), true);
renderActionButton(loadButtonX, buttonY, buttonSize, "P", 15.0F, 4.0F, 2.0F, mouseX, mouseY, this.animationHandler
.getLoadHoverAnimations(), config, accent, alpha);
renderActionButton(refreshButtonX, buttonY, buttonSize, "N", 10.0F, 5.0F, 4.0F, mouseX, mouseY, this.animationHandler
.getRefreshHoverAnimations(), config, new Color(80, 140, 200), alpha);
renderActionButton(deleteButtonX, buttonY, buttonSize, "O", 13.0F, 4.5F, 2.5F, mouseX, mouseY, this.animationHandler
.getDeleteHoverAnimations(), config, new Color(180, 80, 80), alpha);
}
private void renderActionButton(float x, float y, float size, String icon, float iconSize, float iconOffsetX, float iconOffsetY, float mouseX, float mouseY, Map<String, Float> animations, String config, Color hoverColor, float alpha) {
boolean hovered = (mouseX >= x && mouseX <= x + size && mouseY >= y && mouseY <= y + size);
float anim = ((Float)animations.getOrDefault(config, Float.valueOf(0.0F))).floatValue();
float target = hovered ? 1.0F : 0.0F;
anim += (target - anim) * 0.15F;
animations.put(config, Float.valueOf(anim));
int bgAlpha = (int)((25.0F + 20.0F * anim) * alpha);
int r = (int)(60.0F + (hoverColor.getRed() - 60) * anim);
int g = (int)(60.0F + (hoverColor.getGreen() - 60) * anim);
int b = (int)(60.0F + (hoverColor.getBlue() - 60) * anim);
Render2D.rect(x, y, size, size, (new Color(r, g, b, bgAlpha)).getRGB(), 4.0F);
int iconAlpha = (int)((150.0F + 105.0F * anim) * alpha);
Fonts.GUI_ICONS.draw(icon, x + iconOffsetX, y + iconOffsetY, iconSize, (new Color(200, 200, 200, iconAlpha))
.getRGB());
}
private void renderEmptyMessage(float x, float y, float alpha) {
String text = "Конфиги не найдены";
float textWidth = Fonts.BOLD.getWidth(text, 6.0F);
Fonts.BOLD.draw(text, x + (298.0F - textWidth) / 2.0F, y + 102.0F, 6.0F, (new Color(100, 100, 100, (int)(150.0F * alpha)))
.getRGB());
}
private void renderScrollFade(float x, float y, float w, float h, float topFade, float bottomFade) {
int size = 15;
if (topFade > 0.01F) {
for (int i = 0; i < size; i++) {
float fadeAlpha = 80.0F * topFade * (1.0F - i / size);
Render2D.rect(x, y + i, w, 1.0F, (new Color(20, 20, 20, (int)fadeAlpha)).getRGB(), 0.0F);
} 
}
if (bottomFade > 0.01F) {
for (int i = 0; i < size; i++) {
float fadeAlpha = 80.0F * bottomFade * i / size;
Render2D.rect(x, y + h - size + i, w, 1.0F, (new Color(20, 20, 20, (int)fadeAlpha)).getRGB(), 0.0F);
} 
}
}
public boolean mouseClicked(double mouseX, double mouseY, int button, boolean doubled, float panelX, float panelY) {
float listX = panelX + 8.0F;
float listY = panelY + 37.0F;
float listW = 282.0F;
float listH = 159.0F;
if (this.dataHandler.isCreating()) {
listH -= 40.0F * this.animationHandler.getCreateBoxAnimation();
}
if (mouseX >= listX && mouseX <= (listX + listW) && mouseY >= listY && mouseY <= (listY + listH)) {
float itemY = listY + (float)this.dataHandler.getScrollOffset();
for (String config : this.dataHandler.getConfigs()) {
float itemAlpha = this.animationHandler.getItemAppearAnimation(config);
if (itemAlpha < 0.5F) {
itemY += 27.0F;
continue;
} 
if (mouseY >= itemY && mouseY <= (itemY + 24.0F)) {
return handleItemClick(config, mouseX, mouseY, button, doubled, listX, listW, itemY);
}
itemY += 27.0F;
} 
} 
return false;
}
private boolean handleItemClick(String config, double mouseX, double mouseY, int button, boolean doubled, float listX, float listW, float itemY) {
float buttonSize = 18.0F;
float buttonYPos = itemY + (24.0F - buttonSize) / 2.0F + 1.0F;
float deleteButtonX = listX + listW - buttonSize - 8.0F;
float refreshButtonX = deleteButtonX - buttonSize - 5.0F;
float loadButtonX = refreshButtonX - buttonSize - 5.0F;
if (mouseX >= loadButtonX && mouseX <= (loadButtonX + buttonSize) && mouseY >= buttonYPos && mouseY <= (buttonYPos + buttonSize) && button == 0) {
this.lastClickedConfig = null;
if (this.dataHandler.loadConfig(config)) {
this.notificationRenderer.show("Конфиг загружен: " + config, ConfigNotificationRenderer.NotificationType.SUCCESS);
} else {
this.notificationRenderer.show("Конфиг не найден", ConfigNotificationRenderer.NotificationType.ERROR);
} 
return true;
} 
if (mouseX >= refreshButtonX && mouseX <= (refreshButtonX + buttonSize) && mouseY >= buttonYPos && mouseY <= (buttonYPos + buttonSize) && button == 0) {
this.lastClickedConfig = null;
if (this.dataHandler.refreshConfig(config)) {
this.notificationRenderer.show("Конфиг обновлен: " + config, ConfigNotificationRenderer.NotificationType.INFO);
} else {
this.notificationRenderer.show("Ошибка обновления конфига", ConfigNotificationRenderer.NotificationType.ERROR);
} 
return true;
} 
if (mouseX >= deleteButtonX && mouseX <= (deleteButtonX + buttonSize) && mouseY >= buttonYPos && mouseY <= (buttonYPos + buttonSize) && button == 0) {
this.lastClickedConfig = null;
if (this.dataHandler.deleteConfig(config)) {
this.notificationRenderer.show("Конфиг удален: " + config, ConfigNotificationRenderer.NotificationType.SUCCESS);
} else {
this.notificationRenderer.show("Ошибка удаления конфига", ConfigNotificationRenderer.NotificationType.ERROR);
} 
return true;
} 
if (button == 0) {
long now = System.currentTimeMillis();
boolean sameConfig = config.equals(this.lastClickedConfig);
boolean insideRenameWindow = (now - this.lastClickTime <= 350L);
boolean sameConfigDoubleClick = (sameConfig && insideRenameWindow && doubled);
this.lastClickedConfig = config;
this.lastClickTime = now;
if (sameConfigDoubleClick) {
this.dataHandler.startRenaming(config);
this.notificationRenderer.show("Режим переименования: " + config, ConfigNotificationRenderer.NotificationType.INFO);
return true;
} 
return true;
} 
return false;
}
public boolean mouseScrolled(double mouseX, double mouseY, double vertical, float panelX, float panelY) {
if (mouseX >= panelX && mouseX <= (panelX + 298.0F) && mouseY >= panelY && mouseY <= (panelY + 204.0F)) {
float visibleHeight = 159.0F;
if (this.dataHandler.isCreating()) {
visibleHeight -= 40.0F;
}
this.dataHandler.handleScroll(vertical, visibleHeight);
return true;
} 
return false;
}
}
