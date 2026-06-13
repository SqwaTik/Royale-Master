package royale.screens.clickgui.impl.configs.render;
import java.awt.Color;
import royale.screens.clickgui.impl.configs.handler.ConfigDataHandler;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.theme.ClientTheme;
public class ConfigCreateBoxRenderer
{
private final ConfigDataHandler dataHandler;
private final ConfigNotificationRenderer notificationRenderer;
private float createBoxAnimation = 0.0F;
private float cursorBlink = 0.0F;
private long lastUpdateTime = System.currentTimeMillis();
public ConfigCreateBoxRenderer(ConfigDataHandler dataHandler, ConfigNotificationRenderer notificationRenderer) {
this.dataHandler = dataHandler;
this.notificationRenderer = notificationRenderer;
}
public void render(float x, float y, float alpha) {
updateAnimations();
if (this.createBoxAnimation < 0.01F) {
return;
}
float boxY = y + 204.0F - 40.0F;
float boxAlpha = this.createBoxAnimation * alpha;
renderBackground(x, boxY, boxAlpha);
renderInput(x, boxY, boxAlpha);
renderSaveButton(x, boxY, boxAlpha);
}
private void updateAnimations() {
long currentTime = System.currentTimeMillis();
float deltaTime = Math.min((float)(currentTime - this.lastUpdateTime) / 1000.0F, 0.1F);
this.lastUpdateTime = currentTime;
float targetCreate = this.dataHandler.isCreating() ? 1.0F : 0.0F;
this.createBoxAnimation += (targetCreate - this.createBoxAnimation) * 14.0F * deltaTime;
this.cursorBlink += deltaTime * 2.0F;
if (this.cursorBlink > 1.0F) {
this.cursorBlink--;
}
}
private void renderBackground(float x, float boxY, float alpha) {
Render2D.rect(x + 8.0F, boxY, 282.0F, 32.0F, (new Color(50, 50, 55, (int)(30.0F * alpha)))
.getRGB(), 5.0F);
Render2D.outline(x + 8.0F, boxY, 282.0F, 32.0F, 0.5F, (new Color(80, 80, 85, (int)(100.0F * alpha)))
.getRGB(), 5.0F);
}
private void renderInput(float x, float boxY, float alpha) {
float inputX = x + 15.0F;
float inputY = boxY + 8.0F;
float inputW = 198.0F;
float inputH = 16.0F;
Render2D.rect(inputX, inputY, inputW, inputH, (new Color(40, 40, 45, (int)(40.0F * alpha)))
.getRGB(), 4.0F);
Render2D.outline(inputX, inputY, inputW, inputH, 0.5F, (new Color(70, 70, 75, (int)(80.0F * alpha)))
.getRGB(), 4.0F);
String text = this.dataHandler.getNewConfigName();
if (text.isEmpty()) {
String placeholder = this.dataHandler.isRenaming() ? "Введите новое имя..." : "Введите имя конфига...";
Fonts.BOLD.draw(placeholder, inputX + 5.0F, inputY + 5.0F, 5.0F, (new Color(100, 100, 105, (int)(150.0F * alpha)))
.getRGB());
} else {
Fonts.BOLD.draw(text, inputX + 5.0F, inputY + 5.0F, 5.0F, (new Color(210, 210, 220, (int)(255.0F * alpha)))
.getRGB());
} 
if (this.dataHandler.isCreating()) {
renderCursor(inputX, inputY, inputH, text, alpha);
}
}
private void renderCursor(float inputX, float inputY, float inputH, String text, float alpha) {
float cursorAlpha = (float)(Math.sin(this.cursorBlink * Math.PI * 2.0D) * 0.5D + 0.5D);
if (cursorAlpha > 0.3F) {
float cursorX = inputX + 5.0F + Fonts.BOLD.getWidth(text, 5.0F);
Render2D.rect(cursorX, inputY + 3.0F, 0.5F, inputH - 6.0F, 
ClientTheme.accentWithAlpha((int)(255.0F * cursorAlpha * alpha)), 0.0F);
} 
}
private void renderSaveButton(float x, float boxY, float alpha) {
float saveX = x + 298.0F - 75.0F;
float saveY = boxY + 6.0F;
float saveW = 60.0F;
float saveH = 20.0F;
Render2D.rect(saveX, saveY, saveW, saveH, (new Color(45, 45, 50, (int)(45.0F * alpha))).getRGB(), 4.0F);
Render2D.outline(saveX, saveY, saveW, saveH, 0.5F, 
ClientTheme.accentWithAlpha((int)(115.0F * alpha)), 4.0F);
String buttonText = this.dataHandler.isRenaming() ? "Переименовать" : "Сохранить";
float textWidth = Fonts.BOLD.getWidth(buttonText, 5.0F);
Fonts.BOLD.draw(buttonText, saveX + (saveW - textWidth) / 2.0F, saveY + 7.0F, 5.0F, 
ClientTheme.accentWithAlpha((int)(240.0F * alpha)));
}
public boolean mouseClicked(double mouseX, double mouseY, int button, float panelX, float panelY) {
if (!this.dataHandler.isCreating() || this.createBoxAnimation < 0.5F) {
return false;
}
float saveX = panelX + 298.0F - 75.0F;
float saveY = panelY + 204.0F - 34.0F;
if (mouseX >= saveX && mouseX <= (saveX + 60.0F) && mouseY >= saveY && mouseY <= (saveY + 20.0F) && button == 0) {
applyChanges();
return true;
} 
return false;
}
public boolean keyPressed(int keyCode) {
if (!this.dataHandler.isCreating()) {
return false;
}
if (keyCode == 256) {
this.dataHandler.cancelEditing();
return true;
} 
if (keyCode == 259) {
this.dataHandler.removeLastChar();
return true;
} 
if (keyCode == 257 || keyCode == 335) {
applyChanges();
return true;
} 
return false;
}
public boolean charTyped(char chr) {
if (!this.dataHandler.isCreating()) {
return false;
}
this.dataHandler.appendChar(chr);
return true;
}
private void applyChanges() {
String name = this.dataHandler.getNewConfigName();
if (name.isBlank()) {
this.notificationRenderer.show("Введите имя конфига", ConfigNotificationRenderer.NotificationType.ERROR);
return;
} 
if (this.dataHandler.isRenaming()) {
String oldName = this.dataHandler.getRenamingConfig();
if (this.dataHandler.renameConfig(oldName, name)) {
this.notificationRenderer.show("Конфиг переименован: " + oldName + " -> " + name, ConfigNotificationRenderer.NotificationType.SUCCESS);
this.dataHandler.cancelEditing();
} else {
this.notificationRenderer.show("Не удалось переименовать конфиг", ConfigNotificationRenderer.NotificationType.ERROR);
} 
return;
} 
if (name.equalsIgnoreCase("autoconfig")) {
this.notificationRenderer.show("Это имя зарезервировано", ConfigNotificationRenderer.NotificationType.ERROR);
return;
} 
if (this.dataHandler.saveConfig(name)) {
this.notificationRenderer.show("Конфиг сохранен: " + name, ConfigNotificationRenderer.NotificationType.SUCCESS);
this.dataHandler.cancelEditing();
} else {
this.notificationRenderer.show("Конфиг уже существует или имя некорректно", ConfigNotificationRenderer.NotificationType.ERROR);
} 
}
}


