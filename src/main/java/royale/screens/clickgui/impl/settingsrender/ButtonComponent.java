package royale.screens.clickgui.impl.settingsrender;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.ButtonSetting;
import royale.util.interfaces.AbstractSettingComponent;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
public class ButtonComponent extends AbstractSettingComponent {
private final ButtonSetting buttonSetting;
private float pressAnimation = 0.0F;
private float hoverAnimation = 0.0F;
private float scaleAnimation = 1.0F;
private float rippleAnimation = 0.0F;
private float rippleX = 0.0F;
private float rippleY = 0.0F;
private boolean wasPressed = false;
private boolean rippleActive = false;
private long lastUpdateTime = System.currentTimeMillis();
private static final float ANIMATION_SPEED = 8.0F;
private static final float FAST_ANIMATION_SPEED = 12.0F;
private static final float BUTTON_WIDTH = 65.0F;
private static final float BUTTON_HEIGHT = 12.0F;
public ButtonComponent(ButtonSetting setting) {
super((Setting)setting);
this.buttonSetting = setting;
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
private int clamp(int value) {
return Math.max(0, Math.min(255, value));
}
public void render(DrawContext context, int mouseX, int mouseY, float delta) {
float deltaTime = getDeltaTime();
boolean hovered = isButtonHover(mouseX, mouseY);
this.hoverAnimation = lerp(this.hoverAnimation, hovered ? 1.0F : 0.0F, deltaTime * 8.0F);
float scaleTarget = this.wasPressed ? 0.95F : (hovered ? 1.02F : 1.0F);
this.scaleAnimation = lerp(this.scaleAnimation, scaleTarget, deltaTime * 12.0F);
this.pressAnimation = lerp(this.pressAnimation, this.wasPressed ? 1.0F : 0.0F, deltaTime * 12.0F);
if (this.rippleActive) {
this.rippleAnimation += deltaTime * 3.0F;
if (this.rippleAnimation >= 1.0F) {
this.rippleAnimation = 0.0F;
this.rippleActive = false;
} 
} 
if (this.pressAnimation < 0.05F && this.wasPressed) {
this.wasPressed = false;
}
int iconAlpha = (int)(200.0F * this.alphaMultiplier);
Fonts.GUI_ICONS.draw("U", this.x + 0.5F, this.y + this.height / 2.0F - 12.0F, 13.0F, (new Color(210, 210, 210, iconAlpha)).getRGB());
Fonts.BOLD.draw(this.buttonSetting.getName(), this.x + 9.5F, this.y + this.height / 2.0F - 7.5F, 6.0F, applyAlpha(new Color(210, 210, 220, 200)).getRGB());
String description = this.buttonSetting.getDescription();
float buttonX = this.x + this.width - 65.0F - 2.0F;
if (description != null && !description.isEmpty()) {
drawSettingDescription(description, this.x + 0.5F, this.y + this.height / 2.0F + 0.5F, buttonX - this.x - 6.0F);
}
renderButton(mouseX, mouseY);
}
private void renderButton(int mouseX, int mouseY) {
float buttonX = this.x + this.width - 65.0F - 2.0F;
float buttonY = this.y + this.height / 2.0F - 6.0F;
float scaledWidth = 65.0F * this.scaleAnimation;
float scaledHeight = 12.0F * this.scaleAnimation;
float scaledX = buttonX - (scaledWidth - 65.0F) / 2.0F;
float scaledY = buttonY - (scaledHeight - 12.0F) / 2.0F;
float pressOffset = this.pressAnimation * 1.0F;
scaledY += pressOffset;
int bgAlpha = clamp((int)((30.0F + this.hoverAnimation * 20.0F + this.pressAnimation * 15.0F) * this.alphaMultiplier));
int bgGray = clamp((int)(35.0F + this.hoverAnimation * 15.0F + this.pressAnimation * 20.0F));
Color bgColor = new Color(bgGray, bgGray, bgGray, bgAlpha);
Render2D.rect(scaledX, scaledY, scaledWidth, scaledHeight, bgColor.getRGB(), 4.0F);
if (this.rippleActive && this.rippleAnimation > 0.0F) {
float currentRippleSize = 20.0F * this.rippleAnimation;
float rippleAlpha = (1.0F - this.rippleAnimation) * 0.4F;
int rippleAlphaInt = clamp((int)(255.0F * rippleAlpha * this.alphaMultiplier));
float localRippleX = this.rippleX - scaledX;
float localRippleY = this.rippleY - scaledY;
Render2D.rect(scaledX + localRippleX - currentRippleSize / 2.0F, scaledY + localRippleY - currentRippleSize / 2.0F, currentRippleSize, currentRippleSize, (new Color(200, 200, 210, rippleAlphaInt))
.getRGB(), currentRippleSize / 2.0F);
} 
int outlineAlpha = clamp((int)((60.0F + this.hoverAnimation * 60.0F + this.pressAnimation * 40.0F) * this.alphaMultiplier));
int outlineGray = clamp((int)(80.0F + this.hoverAnimation * 40.0F + this.pressAnimation * 30.0F));
Color outlineColor = new Color(outlineGray, outlineGray, outlineGray, outlineAlpha);
Render2D.outline(scaledX, scaledY, scaledWidth, scaledHeight, 0.5F, outlineColor.getRGB(), 4.0F);
renderButtonContent(scaledX, scaledY, scaledWidth, scaledHeight);
}
private void renderButtonContent(float buttonX, float buttonY, float buttonWidth, float buttonHeight) {
String buttonText = (this.buttonSetting.getButtonName() != null) ? this.buttonSetting.getButtonName() : "Run";
float iconSize = 4.0F;
float textWidth = Fonts.BOLD.getWidth(buttonText, 5.0F);
float totalWidth = iconSize + 4.0F + textWidth;
float startX = buttonX + (buttonWidth - totalWidth) / 2.0F;
float iconX = startX;
float iconY = buttonY + buttonHeight / 2.0F - iconSize / 2.0F;
renderPlayIcon(iconX - 5.0F, iconY, iconSize);
float textX = startX + iconSize;
float textY = buttonY + buttonHeight / 2.0F - 3.0F;
int textAlpha = clamp((int)((180.0F + this.hoverAnimation * 50.0F + this.pressAnimation * 25.0F) * this.alphaMultiplier));
int textGray = clamp((int)(180.0F + this.hoverAnimation * 40.0F + this.pressAnimation * 30.0F));
Color textColor = new Color(textGray, textGray, textGray, textAlpha);
Fonts.BOLD.draw(buttonText, textX, textY, 5.0F, textColor.getRGB());
}
private void renderPlayIcon(float iconX, float iconY, float size) {
int iconAlpha = clamp((int)((160.0F + this.hoverAnimation * 60.0F + this.pressAnimation * 35.0F) * this.alphaMultiplier));
int iconGray = clamp((int)(170.0F + this.hoverAnimation * 50.0F + this.pressAnimation * 30.0F));
Color iconColor = new Color(iconGray, iconGray, iconGray, iconAlpha);
float triangleWidth = size * 0.8F;
float triangleHeight = size;
Render2D.rect(iconX, iconY, triangleWidth * 0.4F, triangleHeight, iconColor.getRGB(), 1.0F);
float dotSize = size * 0.35F;
float dotX = iconX + triangleWidth * 0.5F;
float dotY = iconY + (triangleHeight - dotSize) / 2.0F;
Render2D.rect(dotX, dotY, dotSize, dotSize, iconColor.getRGB(), dotSize / 2.0F);
}
private boolean isButtonHover(double mouseX, double mouseY) {
float buttonX = this.x + this.width - 65.0F - 2.0F;
float buttonY = this.y + this.height / 2.0F - 6.0F;
return (mouseX >= buttonX && mouseX <= (buttonX + 65.0F) && mouseY >= buttonY && mouseY <= (buttonY + 12.0F));
}
public boolean mouseClicked(double mouseX, double mouseY, int button) {
if (isButtonHover(mouseX, mouseY) && button == 0) {
if (this.buttonSetting.getRunnable() != null) {
this.buttonSetting.getRunnable().run();
}
this.wasPressed = true;
this.pressAnimation = 1.0F;
this.rippleActive = true;
this.rippleAnimation = 0.0F;
this.rippleX = (float)mouseX;
this.rippleY = (float)mouseY;
return true;
} 
return false;
}
public void tick() {}
public boolean isHover(double mouseX, double mouseY) {
return (mouseX >= this.x && mouseX <= (this.x + this.width) && mouseY >= this.y && mouseY <= (this.y + this.height));
}
}


