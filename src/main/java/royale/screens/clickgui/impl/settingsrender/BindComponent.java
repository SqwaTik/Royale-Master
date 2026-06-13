package royale.screens.clickgui.impl.settingsrender;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.BindSetting;
import royale.util.interfaces.AbstractSettingComponent;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
public class BindComponent extends AbstractSettingComponent {
private boolean listening = false;
private float listeningAnimation = 0.0F;
private float hoverAnimation = 0.0F;
private float bindHoverAnimation = 0.0F;
private float pulseAnimation = 0.0F;
private float scaleAnimation = 1.0F;
private float glowAnimation = 0.0F;
private float textChangeAnimation = 0.0F;
private String previousBindText = "";
private String currentBindText = "";
private long lastUpdateTime = System.currentTimeMillis();
private static final float ANIMATION_SPEED = 8.0F;
private static final float FAST_ANIMATION_SPEED = 12.0F;
private static final float BIND_BOX_WIDTH = 32.0F;
private static final float BIND_BOX_HEIGHT = 10.0F;
public static final int SCROLL_UP_BIND = 1000;
public static final int SCROLL_DOWN_BIND = 1001;
public static final int MIDDLE_MOUSE_BIND = 1002;
public BindComponent(BindSetting setting) {
super((Setting)setting);
BindSetting bindSetting = (BindSetting)getSetting();
this.currentBindText = getBindDisplayName(bindSetting.getKey(), bindSetting.getType());
this.previousBindText = this.currentBindText;
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
boolean hovered = isHover(mouseX, mouseY);
boolean bindHovered = isBindHover(mouseX, mouseY);
this.hoverAnimation = lerp(this.hoverAnimation, hovered ? 1.0F : 0.0F, deltaTime * 8.0F);
this.bindHoverAnimation = lerp(this.bindHoverAnimation, bindHovered ? 1.0F : 0.0F, deltaTime * 8.0F);
this.listeningAnimation = lerp(this.listeningAnimation, this.listening ? 1.0F : 0.0F, deltaTime * 12.0F);
float scaleTarget = this.listening ? 1.05F : (bindHovered ? 1.02F : 1.0F);
this.scaleAnimation = lerp(this.scaleAnimation, scaleTarget, deltaTime * 8.0F);
this.glowAnimation = lerp(this.glowAnimation, this.listening ? 1.0F : 0.0F, deltaTime * 8.0F);
if (this.listening) {
this.pulseAnimation += deltaTime * 4.0F;
if (this.pulseAnimation > 6.283185307179586D) {
this.pulseAnimation -= 6.2831855F;
}
} else {
this.pulseAnimation = lerp(this.pulseAnimation, 0.0F, deltaTime * 8.0F);
} 
BindSetting bindSetting = (BindSetting)getSetting();
String newBindText = this.listening ? "..." : getBindDisplayName(bindSetting.getKey(), bindSetting.getType());
if (!newBindText.equals(this.currentBindText)) {
this.previousBindText = this.currentBindText;
this.currentBindText = newBindText;
this.textChangeAnimation = 0.0F;
} 
this.textChangeAnimation = lerp(this.textChangeAnimation, 1.0F, deltaTime * 12.0F);
int iconAlpha = (int)(200.0F * this.alphaMultiplier);
Fonts.GUI_ICONS.draw("L", this.x + 1.5F, this.y + this.height / 2.0F - 6.0F, 6.0F, (new Color(210, 210, 210, iconAlpha)).getRGB());
Fonts.BOLD.draw(getSetting().getName(), this.x + 9.5F, this.y + this.height / 2.0F - 7.5F, 6.0F, applyAlpha(new Color(210, 210, 220, 200)).getRGB());
String description = getSetting().getDescription();
float bindBoxX = this.x + this.width - 32.0F - 2.0F;
if (description != null && !description.isEmpty()) {
drawSettingDescription(description, this.x + 0.5F, this.y + this.height / 2.0F + 0.5F, bindBoxX - this.x - 6.0F);
}
renderBindBox(mouseX, mouseY, bindSetting);
}
private void renderBindBox(int mouseX, int mouseY, BindSetting bindSetting) {
Color bgColor, outlineColor;
float bindBoxX = this.x + this.width - 32.0F - 2.0F;
float bindBoxY = this.y + this.height / 2.0F - 5.0F;
float scaledWidth = 32.0F * this.scaleAnimation;
float scaledHeight = 10.0F * this.scaleAnimation;
float scaledX = bindBoxX - (scaledWidth - 32.0F) / 2.0F;
float scaledY = bindBoxY - (scaledHeight - 10.0F) / 2.0F;
int bgAlpha = (int)(25.0F + this.bindHoverAnimation * 15.0F + this.listeningAnimation * 20.0F);
if (this.listening) {
float pulse = (float)(Math.sin(this.pulseAnimation) * 0.15D + 0.85D);
bgColor = new Color((int)(60.0F + 40.0F * pulse), (int)(80.0F + 40.0F * pulse), (int)(120.0F + 35.0F * pulse), (int)(bgAlpha * this.alphaMultiplier));
}
else if (bindSetting.getKey() != -1 && bindSetting.getKey() != -1) {
bgColor = applyAlpha(new Color(40, 60, 50, bgAlpha));
} else {
bgColor = applyAlpha(new Color(40, 40, 45, bgAlpha));
} 
Render2D.rect(scaledX, scaledY, scaledWidth, scaledHeight, bgColor.getRGB(), 3.0F);
if (this.listening) {
float pulse = (float)(Math.sin(this.pulseAnimation) * 0.3D + 0.7D);
float outlineAlpha = 150.0F * pulse * this.listeningAnimation;
outlineColor = new Color(120, 160, 220, (int)(outlineAlpha * this.alphaMultiplier));
} else if (bindSetting.getKey() != -1 && bindSetting.getKey() != -1) {
float outlineAlpha = 80.0F + this.bindHoverAnimation * 40.0F;
outlineColor = new Color(100, 160, 120, (int)(outlineAlpha * this.alphaMultiplier));
} else {
float outlineAlpha = 60.0F + this.bindHoverAnimation * 40.0F;
outlineColor = new Color(120, 120, 125, (int)(outlineAlpha * this.alphaMultiplier));
} 
Render2D.outline(scaledX, scaledY, scaledWidth, scaledHeight, 0.5F, outlineColor.getRGB(), 3.0F);
renderBindText(scaledX, scaledY, scaledWidth, scaledHeight, bindSetting);
if (this.listening)
renderListeningIndicator(scaledX, scaledY, scaledWidth, scaledHeight); 
}
private void renderBindText(float boxX, float boxY, float boxWidth, float boxHeight, BindSetting bindSetting) {
Color textColor;
float textY = boxY + boxHeight / 2.0F - 2.5F;
float centerX = boxX + boxWidth / 2.0F;
if (this.listening) {
float pulse = (float)(Math.sin((this.pulseAnimation * 2.0F)) * 0.2D + 0.8D);
int alpha = (int)(220.0F * pulse * this.alphaMultiplier);
textColor = new Color(180, 200, 240, alpha);
} else if (bindSetting.getKey() != -1 && bindSetting.getKey() != -1) {
int alpha = (int)(200.0F * this.alphaMultiplier);
textColor = new Color(140, 200, 150, alpha);
} else {
int alpha = (int)(150.0F * this.alphaMultiplier);
textColor = new Color(140, 140, 150, alpha);
} 
if (this.textChangeAnimation < 1.0F && !this.previousBindText.equals(this.currentBindText)) {
float oldAlpha = 1.0F - this.textChangeAnimation;
float newAlpha = this.textChangeAnimation;
float oldOffsetY = -3.0F * this.textChangeAnimation;
float newOffsetY = 3.0F * (1.0F - this.textChangeAnimation);
if (oldAlpha > 0.01F) {
Color oldColor = new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), (int)(textColor.getAlpha() * oldAlpha));
Fonts.BOLD.drawCentered(this.previousBindText, centerX, textY + oldOffsetY, 5.0F, oldColor.getRGB());
} 
Color newColor = new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), (int)(textColor.getAlpha() * newAlpha));
Fonts.BOLD.drawCentered(this.currentBindText, centerX, textY + newOffsetY, 5.0F, newColor.getRGB());
} else {
Fonts.BOLD.drawCentered(this.currentBindText, centerX, textY, 5.0F, textColor.getRGB());
} 
}
private void renderListeningIndicator(float boxX, float boxY, float boxWidth, float boxHeight) {
float dotSpacing = 3.0F;
float dotSize = 1.5F;
float dotsWidth = dotSpacing * 2.0F;
float startX = boxX + (boxWidth - dotsWidth) / 2.0F - dotSize / 2.0F;
float dotY = boxY + boxHeight - 5.5F;
for (int i = 0; i < 3; i++) {
float phase = this.pulseAnimation + i * 0.5F;
float pulse = (float)(Math.sin((phase * 2.0F)) * 0.5D + 0.5D);
float currentDotSize = dotSize * (0.5F + pulse * 0.5F);
int alpha = (int)(150.0F * (0.3F + pulse * 0.7F) * this.listeningAnimation * this.alphaMultiplier);
float dotX = startX + i * dotSpacing + (dotSize - currentDotSize) / 2.0F;
float adjustedDotY = dotY + (dotSize - currentDotSize) / 2.0F;
Render2D.rect(dotX, adjustedDotY, currentDotSize, currentDotSize, (new Color(120, 160, 220, alpha))
.getRGB(), currentDotSize / 2.0F);
} 
}
private String getBindDisplayName(int key, int type) {
if (key == -1 || key == -1) return "None";
if (key == 1000) return "ScrollUp"; 
if (key == 1001) return "ScrollDn"; 
if (key == 1002) return "MMB";
if (type == 0) {
switch (key) { case 0: 
case 1: 
case 2: 
case 3: 
case 4: 
case 5: 
case 6:
case 7:
}  return "M" + key;
} 
String keyName = GLFW.glfwGetKeyName(key, 0);
if (keyName == null) {
switch (key) { case 340: 
case 344: 
case 341: 
case 345: 
case 342: 
case 346: 
case 32: 
case 258: 
case 280: 
case 257: 
case 259: 
case 260: 
case 261: 
case 268: 
case 269: 
case 266: 
case 267: 
case 265: 
case 264: 
case 263: 
case 262: 
case 290: 
case 291: 
case 292: 
case 293: 
case 294: 
case 295: 
case 296: 
case 297: 
case 298: 
case 299: 
case 300: 
case 301: 
case 256: 
case 283: 
case 281: 
case 284: 
case 282: 
case 320: 
case 321: 
case 322: 
case 323: 
case 324: 
case 325: 
case 326: 
case 327: 
case 328: 
case 329: 
case 330: 
case 331: 
case 332: 
case 333: 
case 334:
case 335:
}  return "Key" + key;
} 
return keyName.toUpperCase();
}
private boolean isBindHover(double mouseX, double mouseY) {
float bindBoxX = this.x + this.width - 32.0F - 2.0F;
float bindBoxY = this.y + this.height / 2.0F - 5.0F;
return (mouseX >= bindBoxX && mouseX <= (bindBoxX + 32.0F) && mouseY >= bindBoxY && mouseY <= (bindBoxY + 10.0F));
}
public void handleScrollBind(double vertical) {
if (this.listening) {
BindSetting bindSetting = (BindSetting)getSetting();
if (vertical > 0.0D) {
bindSetting.setKey(1000);
} else {
bindSetting.setKey(1001);
} 
bindSetting.setType(2);
this.listening = false;
} 
}
public void handleMiddleMouseBind() {
if (this.listening) {
BindSetting bindSetting = (BindSetting)getSetting();
bindSetting.setKey(1002);
bindSetting.setType(2);
this.listening = false;
} 
}
public boolean mouseClicked(double mouseX, double mouseY, int button) {
if (isBindHover(mouseX, mouseY)) {
if (button == 1) {
((BindSetting)getSetting()).setKey(-1);
((BindSetting)getSetting()).setType(1);
this.listening = false;
return true;
}  if (this.listening) {
((BindSetting)getSetting()).setKey(button);
((BindSetting)getSetting()).setType(0);
this.listening = false;
return true;
}  if (button == 0) {
this.listening = true;
return true;
} 
} else if (this.listening) {
this.listening = false;
return false;
} 
return false;
}
public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
if (this.listening) {
if (keyCode == 256) {
((BindSetting)getSetting()).setKey(-1);
((BindSetting)getSetting()).setType(1);
this.listening = false;
return true;
}  if (keyCode == 259 || keyCode == 261) {
((BindSetting)getSetting()).setKey(-1);
((BindSetting)getSetting()).setType(1);
this.listening = false;
return true;
}  if (keyCode != -1) {
((BindSetting)getSetting()).setKey(keyCode);
((BindSetting)getSetting()).setType(1);
this.listening = false;
return true;
} 
return true;
} 
return false;
}
public void tick() {}
public boolean isHover(double mouseX, double mouseY) {
return (mouseX >= this.x && mouseX <= (this.x + this.width) && mouseY >= this.y && mouseY <= (this.y + this.height));
}
public boolean isListening() {
return this.listening;
}
}


