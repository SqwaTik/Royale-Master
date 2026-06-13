package royale.screens.account;
import java.util.List;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import royale.util.ColorUtil;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.render.shader.Scissor;
public class AccountRenderer
{
private static final float BLUR_RADIUS = 15.0F;
private static final float OUTLINE_THICKNESS = 1.0F;
public void renderLeftPanelTop(float x, float y, float width, float height, float contentAlpha, String nicknameText, boolean nicknameFieldFocused, float scaledMouseX, float scaledMouseY, long currentTime) {
int bgAlpha = (int)(contentAlpha * 120.0F);
int headerAlpha = (int)(contentAlpha * 150.0F);
int outlineAlpha = (int)(contentAlpha * 100.0F);
int titleAlpha = (int)(contentAlpha * 255.0F);
int titleTextAlpha = (int)(contentAlpha * 155.0F);
int bgTopLeft = withAlpha(855828, bgAlpha);
int bgTopRight = withAlpha(1053208, bgAlpha);
int bgBottomLeft = withAlpha(526604, bgAlpha);
int bgBottomRight = withAlpha(855828, bgAlpha);
int headerTopLeft = withAlpha(1316639, headerAlpha);
int headerTopRight = withAlpha(1579812, headerAlpha);
int headerBottomLeft = withAlpha(1053466, headerAlpha);
int headerBottomRight = withAlpha(1316639, headerAlpha);
int outlineColor = withAlpha(2435638, outlineAlpha);
int[] bgColors = { bgTopLeft, bgTopRight, bgBottomRight, bgBottomLeft };
Render2D.gradientRect(x, y, width, height, bgColors, 6.0F);
int[] headerColors = { headerTopLeft, headerTopRight, headerBottomRight, headerBottomLeft };
Render2D.gradientRect(x, y, width, 22.0F, headerColors, 6.0F, 6.0F, 0.0F, 0.0F);
Render2D.outline(x, y, width, height, 1.0F, outlineColor, 6.0F);
Fonts.BOLD.drawCentered("Account Panel", x + width / 2.0F - 15.0F, y + 7.0F, 8.0F, withAlpha(16777215, titleAlpha));
Fonts.REGULARNEW.draw("Nickname", x + 5.0F, y + 28.0F, 5.5F, withAlpha(16777215, titleTextAlpha));
float fieldX = x + 5.0F;
float fieldY = y + 38.0F;
float fieldHeight = 14.0F;
float addButtonSize = 14.0F;
float buttonGap = 3.0F;
float fieldWidth = width - 10.0F - addButtonSize - buttonGap;
renderNicknameField(fieldX, fieldY, fieldWidth, fieldHeight, contentAlpha, nicknameText, nicknameFieldFocused, currentTime);
float addButtonX = fieldX + fieldWidth + buttonGap;
boolean addButtonHovered = isMouseOver(scaledMouseX, scaledMouseY, addButtonX, fieldY, addButtonSize, addButtonSize);
renderAddButton(addButtonX, fieldY, addButtonSize, contentAlpha, addButtonHovered, titleAlpha);
float buttonWidth = width - 10.0F;
float buttonHeight = 16.0F;
float randomButtonX = x + 5.0F;
float randomButtonY = fieldY + fieldHeight + 6.0F;
boolean randomButtonHovered = isMouseOver(scaledMouseX, scaledMouseY, randomButtonX, randomButtonY, buttonWidth, buttonHeight);
renderRandomButton(randomButtonX, randomButtonY, buttonWidth, buttonHeight, contentAlpha, randomButtonHovered, titleAlpha);
float clearButtonX = x + 5.0F;
float clearButtonY = randomButtonY + buttonHeight + 5.0F;
boolean clearButtonHovered = isMouseOver(scaledMouseX, scaledMouseY, clearButtonX, clearButtonY, buttonWidth, buttonHeight);
renderClearAllButton(clearButtonX, clearButtonY, buttonWidth, buttonHeight, contentAlpha, clearButtonHovered, titleAlpha);
}
private void renderNicknameField(float x, float y, float width, float height, float contentAlpha, String nicknameText, boolean focused, long currentTime) {
int titleAlpha = (int)(contentAlpha * 255.0F);
int titleTextAlpha = (int)(contentAlpha * 155.0F);
int fieldBgAlpha = (int)(contentAlpha * 180.0F);
int fieldOutlineAlpha = focused ? (int)(contentAlpha * 180.0F) : (int)(contentAlpha * 80.0F);
int fieldBgTop = withAlpha(658448, fieldBgAlpha);
int fieldBgBottom = withAlpha(526862, fieldBgAlpha);
int[] fieldBgColors = { fieldBgTop, fieldBgTop, fieldBgBottom, fieldBgBottom };
Render2D.gradientRect(x, y, width, height, fieldBgColors, 3.0F);
int fieldOutlineColor = focused ? withAlpha(3820122, fieldOutlineAlpha) : withAlpha(2435638, fieldOutlineAlpha);
Render2D.outline(x, y, width, height, 0.5F, fieldOutlineColor, 3.0F);
String displayText = (nicknameText.isEmpty() && !focused) ? "Enter nick..." : nicknameText;
int textColor = (nicknameText.isEmpty() && !focused) ? withAlpha(6318200, titleTextAlpha) : withAlpha(13685980, titleAlpha);
Fonts.TEST.draw(displayText, x + 4.0F, y + 4.5F, 5.5F, textColor);
if (focused && currentTime / 500L % 2L == 0L) {
float cursorX = x + 4.0F + Fonts.TEST.getWidth(nicknameText, 5.5F);
Render2D.rect(cursorX, y + 3.0F, 0.5F, height - 6.0F, withAlpha(13685980, titleAlpha), 0.0F);
} 
}
private void renderAddButton(float x, float y, float size, float contentAlpha, boolean hovered, int titleAlpha) {
int btnAlpha = hovered ? (int)(contentAlpha * 180.0F) : (int)(contentAlpha * 140.0F);
int btnTopLeft = withAlpha(1316639, btnAlpha);
int btnTopRight = withAlpha(1579812, btnAlpha);
int btnBottomLeft = withAlpha(1053466, btnAlpha);
int btnBottomRight = withAlpha(1316639, btnAlpha);
int[] btnColors = { btnTopLeft, btnTopRight, btnBottomRight, btnBottomLeft };
Render2D.gradientRect(x, y, size, size, btnColors, 3.0F);
Render2D.outline(x, y, size, size, 0.5F, withAlpha(2435638, (int)(contentAlpha * 100.0F)), 3.0F);
float plusCenterX = x + size / 2.0F;
float plusCenterY = y + size / 2.0F;
float plusSize = 5.0F;
float plusThickness = 1.2F;
Render2D.rect(plusCenterX - plusSize / 2.0F, plusCenterY - plusThickness / 2.0F, plusSize, plusThickness, withAlpha(16777215, titleAlpha), 0.5F);
Render2D.rect(plusCenterX - plusThickness / 2.0F, plusCenterY - plusSize / 2.0F, plusThickness, plusSize, withAlpha(16777215, titleAlpha), 0.5F);
}
private void renderRandomButton(float x, float y, float width, float height, float contentAlpha, boolean hovered, int titleAlpha) {
int btnAlpha = hovered ? (int)(contentAlpha * 200.0F) : (int)(contentAlpha * 140.0F);
int btnTopLeft = hovered ? withAlpha(1711912, btnAlpha) : withAlpha(1316639, btnAlpha);
int btnTopRight = hovered ? withAlpha(1975085, btnAlpha) : withAlpha(1579812, btnAlpha);
int btnBottomLeft = hovered ? withAlpha(1316895, btnAlpha) : withAlpha(1053466, btnAlpha);
int btnBottomRight = hovered ? withAlpha(1711912, btnAlpha) : withAlpha(1316639, btnAlpha);
int[] btnColors = { btnTopLeft, btnTopRight, btnBottomRight, btnBottomLeft };
Render2D.gradientRect(x, y, width, height, btnColors, 3.0F);
int outlineColor = hovered ? withAlpha(3820122, (int)(contentAlpha * 150.0F)) : withAlpha(2435638, (int)(contentAlpha * 100.0F));
Render2D.outline(x, y, width, height, 0.5F, outlineColor, 3.0F);
int textColor = hovered ? withAlpha(16777215, titleAlpha) : withAlpha(13687012, titleAlpha);
Fonts.DEFAULT.draw("Random", x + 6.0F, y + 5.0F, 5.5F, textColor);
Fonts.ICONS.draw("R", x + 75.0F, y + 3.5F, 10.0F, textColor);
}
private void renderClearAllButton(float x, float y, float width, float height, float contentAlpha, boolean hovered, int titleAlpha) {
int btnAlpha = hovered ? (int)(contentAlpha * 200.0F) : (int)(contentAlpha * 140.0F);
int btnTopLeft = hovered ? withAlpha(2759194, btnAlpha) : withAlpha(1709078, btnAlpha);
int btnTopRight = hovered ? withAlpha(3022366, btnAlpha) : withAlpha(1971736, btnAlpha);
int btnBottomLeft = hovered ? withAlpha(2364436, btnAlpha) : withAlpha(1445906, btnAlpha);
int btnBottomRight = hovered ? withAlpha(2759194, btnAlpha) : withAlpha(1709078, btnAlpha);
int[] btnColors = { btnTopLeft, btnTopRight, btnBottomRight, btnBottomLeft };
Render2D.gradientRect(x, y, width, height, btnColors, 3.0F);
int outlineColor = hovered ? withAlpha(5913146, (int)(contentAlpha * 150.0F)) : withAlpha(3484202, (int)(contentAlpha * 100.0F));
Render2D.outline(x, y, width, height, 0.5F, outlineColor, 3.0F);
int textColor = hovered ? withAlpha(16744576, titleAlpha) : withAlpha(13672608, titleAlpha);
Fonts.DEFAULT.draw("Clear All", x + 6.0F, y + 5.0F, 5.5F, textColor);
Fonts.GUI_ICONS.draw("O", x + 77.0F, y + 2.5F, 11.0F, textColor);
}
public void renderLeftPanelBottom(float x, float y, float width, float height, float contentAlpha, String activeAccountName, String activeAccountDate, Identifier activeAccountSkin) {
int bgAlpha = (int)(contentAlpha * 120.0F);
int headerAlpha = (int)(contentAlpha * 150.0F);
int outlineAlpha = (int)(contentAlpha * 100.0F);
int blurAlpha = (int)(contentAlpha * 80.0F);
int titleAlpha = (int)(contentAlpha * 255.0F);
int titleTextAlpha = (int)(contentAlpha * 155.0F);
int bgTopLeft = withAlpha(855828, bgAlpha);
int bgTopRight = withAlpha(1053208, bgAlpha);
int bgBottomLeft = withAlpha(526604, bgAlpha);
int bgBottomRight = withAlpha(855828, bgAlpha);
int headerTopLeft = withAlpha(1316639, headerAlpha);
int headerTopRight = withAlpha(1579812, headerAlpha);
int headerBottomLeft = withAlpha(1053466, headerAlpha);
int headerBottomRight = withAlpha(1316639, headerAlpha);
int outlineColor = withAlpha(2435638, outlineAlpha);
int blurTint = withAlpha(395280, blurAlpha);
Render2D.blur(x, y, width, height, 15.0F, 6.0F, blurTint);
int[] bgColors = { bgTopLeft, bgTopRight, bgBottomRight, bgBottomLeft };
Render2D.gradientRect(x, y, width, height, bgColors, 6.0F);
int[] headerColors = { headerTopLeft, headerTopRight, headerBottomRight, headerBottomLeft };
Render2D.gradientRect(x, y, width, 22.0F, headerColors, 6.0F, 6.0F, 0.0F, 0.0F);
Render2D.outline(x, y, width, height, 1.0F, outlineColor, 6.0F);
Fonts.BOLD.drawCentered("Active Session", x + width / 2.0F - 15.0F, y + 6.0F, 8.0F, withAlpha(16777215, titleAlpha));
if (!activeAccountName.isEmpty()) {
float faceX = x + 8.0F;
float faceY = y + 28.0F;
float faceSize = 24.0F;
Identifier skinTexture = SkinManager.getSkin(activeAccountName);
int faceColor = withAlpha(16777215, titleAlpha);
drawPlayerFace(skinTexture, faceX, faceY, faceSize, faceColor);
float textX = faceX + faceSize + 6.0F;
float nameY = faceY + 4.0F;
float dateY = nameY + 10.0F;
Fonts.TEST.draw(activeAccountName, textX, nameY, 6.0F, withAlpha(16777215, titleAlpha));
Fonts.TEST.draw(activeAccountDate, textX, dateY, 4.5F, withAlpha(8423568, titleAlpha));
} else {
Fonts.REGULARNEW.drawCentered("No account selected", x + 50.0F, y + 36.0F, 5.0F, withAlpha(6318200, titleTextAlpha));
} 
}
public void renderRightPanel(float x, float y, float width, float height, float contentAlpha, List<AccountEntry> accounts, float scrollOffset, float scaledMouseX, float scaledMouseY, float scale, int guiScale) {
int bgAlpha = (int)(contentAlpha * 120.0F);
int headerAlpha = (int)(contentAlpha * 150.0F);
int outlineAlpha = (int)(contentAlpha * 100.0F);
int blurAlpha = (int)(contentAlpha * 80.0F);
int titleAlpha = (int)(contentAlpha * 255.0F);
int titleTextAlpha = (int)(contentAlpha * 155.0F);
int bgTopLeft = withAlpha(855828, bgAlpha);
int bgTopRight = withAlpha(1053208, bgAlpha);
int bgBottomLeft = withAlpha(526604, bgAlpha);
int bgBottomRight = withAlpha(855828, bgAlpha);
int headerTopLeft = withAlpha(1316639, headerAlpha);
int headerTopRight = withAlpha(1579812, headerAlpha);
int headerBottomLeft = withAlpha(1053466, headerAlpha);
int headerBottomRight = withAlpha(1316639, headerAlpha);
int outlineColor = withAlpha(2435638, outlineAlpha);
int blurTint = withAlpha(395280, blurAlpha);
Render2D.blur(x, y, width, height, 15.0F, 6.0F, blurTint);
int[] bgColors = { bgTopLeft, bgTopRight, bgBottomRight, bgBottomLeft };
Render2D.gradientRect(x, y, width, height, bgColors, 6.0F);
int[] headerColors = { headerTopLeft, headerTopRight, headerBottomRight, headerBottomLeft };
Render2D.gradientRect(x, y, width, 22.0F, headerColors, 6.0F, 6.0F, 0.0F, 0.0F);
Render2D.outline(x, y, width, height, 1.0F, outlineColor, 6.0F);
Fonts.BOLD.draw("Accounts List", x + 8.0F, y + 7.0F, 8.0F, withAlpha(16777215, titleAlpha));
Render2D.blur(x, y, width, height, 0.0F, 0.0F, ColorUtil.rgba(0, 0, 0, 1));
float accountListX = x + 5.0F;
float accountListY = y + 28.0F;
float accountListWidth = width - 10.0F;
float accountListHeight = height - 31.0F;
float cardWidth = (accountListWidth - 5.0F) / 2.0F;
float cardHeight = 40.0F;
float cardGap = 5.0F;
float scissorScale = guiScale / scale;
Scissor.enable(accountListX * scale, accountListY * scale, accountListWidth * scale, accountListHeight * scale, scissorScale);
for (int i = 0; i < accounts.size(); i++) {
AccountEntry account = accounts.get(i);
int col = i % 2;
int row = i / 2;
float cardX = accountListX + col * (cardWidth + cardGap);
float cardY = accountListY + row * (cardHeight + cardGap) - scrollOffset;
if (cardY + cardHeight >= accountListY - 10.0F && cardY <= accountListY + accountListHeight + 10.0F)
{
renderAccountCard(cardX, cardY, cardWidth, cardHeight, account, contentAlpha, scaledMouseX, scaledMouseY, accountListY, accountListHeight);
}
} 
Scissor.disable();
if (accounts.isEmpty()) {
Fonts.REGULARNEW.drawCentered("No accounts added", x + width / 2.0F, y + height / 2.0F + 2.0F, 6.0F, withAlpha(6318200, titleTextAlpha));
}
}
private void renderAccountCard(float x, float y, float width, float height, AccountEntry account, float contentAlpha, float mouseX, float mouseY, float listY, float listHeight) {
int pinBtnColor, pinOutlineColor, titleAlpha = (int)(contentAlpha * 255.0F);
boolean cardHovered = (isMouseOver(mouseX, mouseY, x, y, width, height) && mouseY >= listY && mouseY <= listY + listHeight);
int cardAlpha = cardHovered ? (int)(contentAlpha * 160.0F) : (int)(contentAlpha * 120.0F);
int cardTopLeft = withAlpha(1185052, cardAlpha);
int cardTopRight = withAlpha(1448482, cardAlpha);
int cardBottomLeft = withAlpha(921622, cardAlpha);
int cardBottomRight = withAlpha(1185052, cardAlpha);
int[] cardColors = { cardTopLeft, cardTopRight, cardBottomRight, cardBottomLeft };
Render2D.gradientRect(x, y, width, height, cardColors, 4.0F);
Render2D.blur(x, y, 1.0F, 1.0F, 0.0F, 0.0F, ColorUtil.rgba(0, 0, 0, 0));
int cardOutlineColor = withAlpha(2435638, (int)(contentAlpha * 80.0F));
Render2D.outline(x, y, width, height, 0.5F, cardOutlineColor, 4.0F);
float faceX = x + 7.0F;
float faceY = y + 7.0F;
float faceSize = 25.0F;
Identifier skinTexture = SkinManager.getSkin(account.getName());
drawPlayerFace(skinTexture, faceX, faceY, faceSize, withAlpha(16777215, titleAlpha));
float textX = faceX + faceSize + 5.0F;
float nameY = faceY + 2.0F;
float dateY = nameY + 9.0F;
String displayName = account.getName();
float maxNameWidth = width - faceSize - 45.0F;
if (Fonts.TEST.getWidth(displayName, 7.0F) > maxNameWidth) {
while (Fonts.TEST.getWidth(displayName + "...", 7.0F) > maxNameWidth && displayName.length() > 3) {
displayName = displayName.substring(0, displayName.length() - 1);
}
displayName = displayName + "...";
} 
Fonts.TEST.draw(displayName, textX, nameY, 7.0F, withAlpha(16777215, titleAlpha));
Fonts.TEST.draw(account.getDate(), textX, dateY, 6.0F, withAlpha(7370888, titleAlpha));
float buttonSize = 12.0F;
float buttonYPos = y + height - buttonSize - 5.0F;
float pinButtonX = x + width - buttonSize * 2.0F - 8.0F;
float deleteButtonX = x + width - buttonSize - 5.0F;
boolean pinHovered = (isMouseOver(mouseX, mouseY, pinButtonX, buttonYPos, buttonSize, buttonSize) && mouseY >= listY && mouseY <= listY + listHeight);
boolean deleteHovered = (isMouseOver(mouseX, mouseY, deleteButtonX, buttonYPos, buttonSize, buttonSize) && mouseY >= listY && mouseY <= listY + listHeight);
int pinBtnAlpha = pinHovered ? (int)(contentAlpha * 220.0F) : (int)(contentAlpha * 160.0F);
if (account.isPinned()) {
pinBtnColor = withAlpha(4864528, pinBtnAlpha);
pinOutlineColor = withAlpha(13934615, (int)(contentAlpha * 180.0F));
} else {
pinBtnColor = withAlpha(1711396, pinBtnAlpha);
pinOutlineColor = withAlpha(3488326, (int)(contentAlpha * 100.0F));
} 
int[] pinBtnColors = { pinBtnColor, pinBtnColor, pinBtnColor, pinBtnColor };
Render2D.gradientRect(pinButtonX, buttonYPos, buttonSize, buttonSize, pinBtnColors, 3.0F);
Render2D.outline(pinButtonX, buttonYPos, buttonSize, buttonSize, 0.5F, pinOutlineColor, 3.0F);
Render2D.blur(x, y, 1.0F, 1.0F, 0.0F, 0.0F, ColorUtil.rgba(0, 0, 0, 0));
int pinIconColor = account.isPinned() ? withAlpha(16766720, titleAlpha) : withAlpha(12634324, titleAlpha);
Fonts.MAINMENUSCREEN.drawCentered("c", pinButtonX + buttonSize / 2.0F, buttonYPos + 1.5F, 9.0F, pinIconColor);
int delBtnAlpha = deleteHovered ? (int)(contentAlpha * 200.0F) : (int)(contentAlpha * 140.0F);
int delBtnColor = deleteHovered ? withAlpha(5909034, delBtnAlpha) : withAlpha(1711396, delBtnAlpha);
int[] delBtnColors = { delBtnColor, delBtnColor, delBtnColor, delBtnColor };
Render2D.gradientRect(deleteButtonX, buttonYPos, buttonSize, buttonSize, delBtnColors, 3.0F);
Render2D.outline(deleteButtonX, buttonYPos, buttonSize, buttonSize, 0.5F, withAlpha(3488326, (int)(contentAlpha * 100.0F)), 3.0F);
Render2D.blur(x, y, 1.0F, 1.0F, 0.0F, 0.0F, ColorUtil.rgba(0, 0, 0, 0));
int delIconColor = deleteHovered ? withAlpha(16744576, titleAlpha) : withAlpha(12634324, titleAlpha);
Fonts.GUI_ICONS.drawCentered("O", deleteButtonX + buttonSize / 2.0F, buttonYPos + 0.5F, 11.0F, delIconColor);
}
public void drawPlayerFace(Identifier skin, float x, float y, float size, int color) {
float u0 = 0.125F;
float v0 = 0.125F;
float u1 = 0.25F;
float v1 = 0.25F;
Render2D.texture(skin, x, y, size, size, u0, v0, u1, v1, color, 0.0F, 3.0F);
float hatScale = 1.12F;
float hatSize = size * hatScale;
float hatOffset = (hatSize - size) / 2.0F;
float hatU0 = 0.625F;
float hatV0 = 0.125F;
float hatU1 = 0.75F;
float hatV1 = 0.25F;
Render2D.texture(skin, x - hatOffset, y - hatOffset, hatSize, hatSize, hatU0, hatV0, hatU1, hatV1, color, 0.0F, 3.0F);
}
public boolean isMouseOver(float mouseX, float mouseY, float x, float y, float width, float height) {
return (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height);
}
public int withAlpha(int color, int alpha) {
return color & 0xFFFFFF | MathHelper.clamp(alpha, 0, 255) << 24;
}
}


