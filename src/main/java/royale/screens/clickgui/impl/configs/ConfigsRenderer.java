package royale.screens.clickgui.impl.configs;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import royale.modules.module.category.ModuleCategory;
import royale.screens.clickgui.impl.configs.handler.ConfigAnimationHandler;
import royale.screens.clickgui.impl.configs.handler.ConfigDataHandler;
import royale.screens.clickgui.impl.configs.render.ConfigCreateBoxRenderer;
import royale.screens.clickgui.impl.configs.render.ConfigHeaderRenderer;
import royale.screens.clickgui.impl.configs.render.ConfigListRenderer;
import royale.screens.clickgui.impl.configs.render.ConfigNotificationRenderer;
import royale.util.render.Render2D;
import royale.util.theme.ClientTheme;
public class ConfigsRenderer
{
public static final float PANEL_X_OFFSET = 92.0F;
public static final float PANEL_Y_OFFSET = 38.0F;
public static final float PANEL_WIDTH = 298.0F;
public static final float PANEL_HEIGHT = 204.0F;
public static final float CORNER_RADIUS = 6.0F;
private final ConfigAnimationHandler animationHandler;
private final ConfigDataHandler dataHandler;
private final ConfigHeaderRenderer headerRenderer;
private final ConfigListRenderer listRenderer;
private final ConfigCreateBoxRenderer createBoxRenderer;
private final ConfigNotificationRenderer notificationRenderer;
private boolean isActive = false;
private boolean wasActive = false;
public ConfigsRenderer() {
this.animationHandler = new ConfigAnimationHandler();
this.dataHandler = new ConfigDataHandler(this.animationHandler);
this.notificationRenderer = new ConfigNotificationRenderer();
this.headerRenderer = new ConfigHeaderRenderer(this.dataHandler);
this.listRenderer = new ConfigListRenderer(this.animationHandler, this.dataHandler, this.notificationRenderer);
this.createBoxRenderer = new ConfigCreateBoxRenderer(this.dataHandler, this.notificationRenderer);
}
public void render(DrawContext context, float bgX, float bgY, float mouseX, float mouseY, float delta, int guiScale, float alphaMultiplier, ModuleCategory category) {
boolean shouldBeActive = (category == ModuleCategory.AUTOBUY);
if (shouldBeActive && !this.wasActive) {
this.isActive = true;
this.animationHandler.reset();
this.dataHandler.cancelEditing();
this.dataHandler.refreshConfigs();
this.animationHandler.initItemAnimations(this.dataHandler.getConfigs());
} else if (!shouldBeActive && this.wasActive) {
this.isActive = false;
this.dataHandler.cancelEditing();
} 
this.wasActive = shouldBeActive;
this.animationHandler.update(this.isActive, this.dataHandler.getConfigs(), this.dataHandler.isCreating());
if (this.animationHandler.isFullyHidden() && !this.isActive) {
return;
}
float panelX = bgX + 92.0F;
float panelY = bgY + 38.0F;
float slideOffset = (1.0F - this.animationHandler.getPanelSlide()) * 20.0F;
float finalAlpha = clamp01(alphaMultiplier * this.animationHandler.getPanelAlpha());
context.getMatrices().pushMatrix();
context.getMatrices().translate(slideOffset, 0.0F);
renderPanel(panelX, panelY, finalAlpha);
this.headerRenderer.render(panelX, panelY, mouseX - slideOffset, mouseY, finalAlpha);
this.listRenderer.render(context, panelX, panelY, mouseX - slideOffset, mouseY, guiScale, finalAlpha);
this.createBoxRenderer.render(panelX, panelY, finalAlpha);
this.notificationRenderer.render(panelX, panelY, finalAlpha);
context.getMatrices().popMatrix();
}
private void renderPanel(float x, float y, float alpha) {
alpha = clamp01(alpha);
int panelAlpha = (int)(15.0F * alpha);
int outlineAlpha = (int)(215.0F * alpha);
Render2D.rect(x, y, 298.0F, 204.0F, (new Color(64, 64, 64, panelAlpha)).getRGB(), 6.0F);
Render2D.outline(x, y, 298.0F, 204.0F, 0.5F, 
ClientTheme.blendWithAccentAndAlpha((new Color(55, 55, 55, 255)).getRGB(), 0.25F, outlineAlpha), 6.0F);
}
public boolean mouseClicked(double mouseX, double mouseY, int button, boolean doubled, float bgX, float bgY, ModuleCategory category) {
if (category != ModuleCategory.AUTOBUY) {
return false;
}
if (this.animationHandler.getPanelAlpha() < 0.4F) {
return false;
}
float panelX = bgX + 92.0F;
float panelY = bgY + 38.0F;
float slideOffset = (1.0F - this.animationHandler.getPanelSlide()) * 20.0F;
mouseX -= slideOffset;
if (this.headerRenderer.mouseClicked(mouseX, mouseY, button, panelX, panelY)) {
return true;
}
if (this.createBoxRenderer.mouseClicked(mouseX, mouseY, button, panelX, panelY)) {
return true;
}
return this.listRenderer.mouseClicked(mouseX, mouseY, button, doubled, panelX, panelY);
}
public boolean mouseScrolled(double mouseX, double mouseY, double vertical, float bgX, float bgY, ModuleCategory category) {
if (category != ModuleCategory.AUTOBUY) {
return false;
}
if (this.animationHandler.getPanelAlpha() < 0.4F) {
return false;
}
float panelX = bgX + 92.0F;
float panelY = bgY + 38.0F;
return this.listRenderer.mouseScrolled(mouseX, mouseY, vertical, panelX, panelY);
}
public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
return this.createBoxRenderer.keyPressed(keyCode);
}
public boolean charTyped(char chr, int modifiers) {
return this.createBoxRenderer.charTyped(chr);
}
public void mouseReleased(double mouseX, double mouseY, int button) {}
public boolean isEditing() {
return this.dataHandler.isCreating();
}
public void cancelEditing() {
this.dataHandler.cancelEditing();
}
private float clamp01(float value) {
return Math.max(0.0F, Math.min(1.0F, value));
}
}


