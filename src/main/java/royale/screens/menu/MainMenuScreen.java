package royale.screens.menu;

import java.awt.Color;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.Click;
import net.minecraft.util.Util;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerWarningScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import royale.Initialization;
import royale.screens.account.AccountEntry;
import royale.screens.account.AccountRenderer;
import royale.util.config.impl.account.AccountConfig;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.session.SessionChanger;
import royale.util.sounds.SoundManager;

public class MainMenuScreen
extends Screen {
    private static final Identifier BACKGROUND_TEXTURE = Identifier.of((String)"royale", (String)"textures/menu/backmenu.png");
    private static final float FIXED_GUI_SCALE = 2.0f;
    private static final int BUTTON_SIZE = 42;
    private static final int BUTTON_SPACING = 16;
    private static final float BLUR_RADIUS = 15.0f;
    private static final float OUTLINE_THICKNESS = 1.0f;
    private static final String[] BUTTON_ICONS = new String[]{"a", "b", "x", "s", "i"};
    private static final float LEFT_PANEL_WIDTH = 100.0f;
    private static final float LEFT_PANEL_TOP_HEIGHT = 100.0f;
    private static final float LEFT_PANEL_BOTTOM_HEIGHT = 58.0f;
    private static final float RIGHT_PANEL_WIDTH = 300.0f;
    private static final float RIGHT_PANEL_HEIGHT = 165.0f;
    private static final float GAP = 5.0f;
    private static final long UNLOCK_FADE_DURATION = 300L;
    private static final long MENU_APPEAR_DURATION = 800L;
    private static final long MENU_APPEAR_DELAY = 200L;
    private static final long VIEW_FADE_OUT_DURATION = 200L;
    private static final long VIEW_FADE_IN_DURATION = 250L;
    private static final float SLIDE_DISTANCE = 40.0f;
    private static final float ZOOM_INITIAL = 1.08f;
    private static final float ZOOM_NORMAL = 1.0f;
    private static final float ZOOM_SPEED = 3.0f;
    private View currentView = View.MAIN_MENU;
    private View targetView = View.MAIN_MENU;
    private TransitionPhase transitionPhase = TransitionPhase.NONE;
    private long transitionStart = 0L;
    private boolean initialized = false;
    private long lastRenderTime = 0L;
    private float[] buttonScales = new float[5];
    private float[] buttonHoverProgress = new float[5];
    private int hoveredButton = -1;
    private float exitButtonRedProgress = 0.0f;
    private boolean welcomeSoundPlayed = false;
    private boolean isUnlocked = false;
    private long unlockTime = 0L;
    private float unlockTextPulse = 0.0f;
    private float currentZoom = 1.08f;
    private float targetZoom = 1.08f;
    private final AccountRenderer accountRenderer;
    private final AccountConfig accountConfig;
    private String nicknameText = "";
    private boolean nicknameFieldFocused = false;
    private float scrollOffset = 0.0f;
    private float targetScrollOffset = 0.0f;

    public MainMenuScreen() {
        super((Text)Text.literal((String)"Main Menu"));
        for (int i = 0; i < 5; ++i) {
            this.buttonScales[i] = 1.0f;
            this.buttonHoverProgress[i] = 0.0f;
        }
        this.accountRenderer = new AccountRenderer();
        this.accountConfig = AccountConfig.getInstance();
        this.accountConfig.load();
    }

    protected void init() {
        this.initialized = false;
    }

    private int getFixedScaledWidth() {
        return (int)Math.ceil((double)this.client.getWindow().getFramebufferWidth() / 2.0);
    }

    private int getFixedScaledHeight() {
        return (int)Math.ceil((double)this.client.getWindow().getFramebufferHeight() / 2.0);
    }

    private float toFixedCoord(double coord) {
        float currentScale = this.client.getWindow().getScaleFactor();
        return (float)(coord * (double)currentScale / 2.0);
    }

    private void unlock() {
        if (!this.isUnlocked) {
            this.isUnlocked = true;
            this.unlockTime = Util.getMeasuringTimeMs();
            this.targetZoom = 1.0f;
        }
    }

    private float getUnlockTextAlpha(long currentTime) {
        if (!this.isUnlocked) {
            return 1.0f;
        }
        long elapsed = currentTime - this.unlockTime;
        return 1.0f - MathHelper.clamp((float)((float)elapsed / 300.0f), (float)0.0f, (float)1.0f);
    }

    private float getMenuProgress(long currentTime) {
        if (!this.isUnlocked) {
            return 0.0f;
        }
        long elapsed = currentTime - this.unlockTime - 200L;
        if (elapsed < 0L) {
            return 0.0f;
        }
        return MathHelper.clamp((float)((float)elapsed / 800.0f), (float)0.0f, (float)1.0f);
    }

    private float easeOutCubic(float x) {
        return 1.0f - (float)Math.pow(1.0f - x, 3.0);
    }

    private float easeInCubic(float x) {
        return x * x * x;
    }

    private float easeOutQuart(float x) {
        return 1.0f - (float)Math.pow(1.0f - x, 4.0);
    }

    private void switchToView(View view) {
        if (this.currentView != view && this.transitionPhase == TransitionPhase.NONE) {
            this.targetView = view;
            this.transitionPhase = TransitionPhase.FADE_OUT;
            this.transitionStart = Util.getMeasuringTimeMs();
        }
    }

    private void updateTransition(long currentTime) {
        if (this.transitionPhase == TransitionPhase.NONE) {
            return;
        }
        long elapsed = currentTime - this.transitionStart;
        if (this.transitionPhase == TransitionPhase.FADE_OUT) {
            if (elapsed >= 200L) {
                this.currentView = this.targetView;
                this.transitionPhase = TransitionPhase.FADE_IN;
                this.transitionStart = currentTime;
            }
        } else if (this.transitionPhase == TransitionPhase.FADE_IN && elapsed >= 250L) {
            this.transitionPhase = TransitionPhase.NONE;
        }
    }

    private float getMainMenuAlpha(long currentTime) {
        if (this.currentView == View.ALT_SCREEN && this.transitionPhase == TransitionPhase.NONE) {
            return 0.0f;
        }
        if (this.currentView == View.MAIN_MENU && this.transitionPhase == TransitionPhase.NONE) {
            return 1.0f;
        }
        long elapsed = currentTime - this.transitionStart;
        if (this.transitionPhase == TransitionPhase.FADE_OUT) {
            if (this.currentView == View.MAIN_MENU) {
                return 1.0f - this.easeInCubic(MathHelper.clamp((float)((float)elapsed / 200.0f), (float)0.0f, (float)1.0f));
            }
            return 0.0f;
        }
        if (this.transitionPhase == TransitionPhase.FADE_IN) {
            if (this.currentView == View.MAIN_MENU) {
                return this.easeOutCubic(MathHelper.clamp((float)((float)elapsed / 250.0f), (float)0.0f, (float)1.0f));
            }
            return 0.0f;
        }
        return this.currentView == View.MAIN_MENU ? 1.0f : 0.0f;
    }

    private float getAltScreenAlpha(long currentTime) {
        if (this.currentView == View.MAIN_MENU && this.transitionPhase == TransitionPhase.NONE) {
            return 0.0f;
        }
        if (this.currentView == View.ALT_SCREEN && this.transitionPhase == TransitionPhase.NONE) {
            return 1.0f;
        }
        long elapsed = currentTime - this.transitionStart;
        if (this.transitionPhase == TransitionPhase.FADE_OUT) {
            if (this.currentView == View.ALT_SCREEN) {
                return 1.0f - this.easeInCubic(MathHelper.clamp((float)((float)elapsed / 200.0f), (float)0.0f, (float)1.0f));
            }
            return 0.0f;
        }
        if (this.transitionPhase == TransitionPhase.FADE_IN) {
            if (this.currentView == View.ALT_SCREEN) {
                return this.easeOutCubic(MathHelper.clamp((float)((float)elapsed / 250.0f), (float)0.0f, (float)1.0f));
            }
            return 0.0f;
        }
        return this.currentView == View.ALT_SCREEN ? 1.0f : 0.0f;
    }

    private void drawBackground(float zoom) {
        int screenWidth = this.getFixedScaledWidth();
        int screenHeight = this.getFixedScaledHeight();
        float zoomedWidth = (float)screenWidth * zoom;
        float zoomedHeight = (float)screenHeight * zoom;
        float offsetX = ((float)screenWidth - zoomedWidth) / 2.0f;
        float offsetY = ((float)screenHeight - zoomedHeight) / 2.0f;
        int[] colors = new int[]{-1, -1, -1, -1};
        float[] radii = new float[]{0.0f, 0.0f, 0.0f, 0.0f};
        Initialization.getInstance().getManager().getRenderCore().getTexturePipeline().drawTexture(BACKGROUND_TEXTURE, offsetX, offsetY, zoomedWidth, zoomedHeight, 0.0f, 0.0f, 1.0f, 1.0f, colors, radii, 1.0f);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long currentTime = Util.getMeasuringTimeMs();
        if (!this.initialized) {
            this.lastRenderTime = currentTime;
            this.initialized = true;
        }
        float deltaTime = (float)(currentTime - this.lastRenderTime) / 1000.0f;
        this.lastRenderTime = currentTime;
        deltaTime = MathHelper.clamp((float)deltaTime, (float)0.0f, (float)0.1f);
        this.updateTransition(currentTime);
        this.unlockTextPulse += deltaTime * 3.0f;
        this.currentZoom = MathHelper.lerp((float)(deltaTime * 3.0f), (float)this.currentZoom, (float)this.targetZoom);
        float scrollSpeed = 12.0f;
        float scrollDiff = this.targetScrollOffset - this.scrollOffset;
        this.scrollOffset += scrollDiff * Math.min(1.0f, deltaTime * scrollSpeed);
        if (Math.abs(scrollDiff) < 0.1f) {
            this.scrollOffset = this.targetScrollOffset;
        }
        float unlockTextAlpha = this.getUnlockTextAlpha(currentTime);
        float menuProgress = this.easeOutQuart(this.getMenuProgress(currentTime));
        float mainAlpha = this.getMainMenuAlpha(currentTime);
        float altAlpha = this.getAltScreenAlpha(currentTime);
        if (!this.welcomeSoundPlayed && menuProgress > 0.1f) {
            SoundManager.playSoundDirect(SoundManager.WELCOME, 1.0f, 1.0f);
            this.welcomeSoundPlayed = true;
        }
        float scaledMouseX = this.toFixedCoord(mouseX);
        float scaledMouseY = this.toFixedCoord(mouseY);
        int fixedWidth = this.getFixedScaledWidth();
        int fixedHeight = this.getFixedScaledHeight();
        boolean canInteractMain = this.currentView == View.MAIN_MENU && this.transitionPhase == TransitionPhase.NONE && menuProgress > 0.8f;
        this.hoveredButton = canInteractMain ? this.getHoveredButton(scaledMouseX, scaledMouseY, fixedWidth, fixedHeight, menuProgress) : -1;
        this.updateButtonAnimations(deltaTime);
        Render2D.beginOverlay();
        this.drawBackground(this.currentZoom);
        if (mainAlpha > 0.01f) {
            this.renderMainMenuContent(fixedWidth, fixedHeight, scaledMouseX, scaledMouseY, menuProgress, mainAlpha, unlockTextAlpha, currentTime);
        }
        if (altAlpha > 0.01f) {
            this.renderAltScreenContent(fixedWidth, fixedHeight, scaledMouseX, scaledMouseY, altAlpha, currentTime);
        }
        Render2D.blur(scaledMouseX, scaledMouseY, 1.0f, 1.0f, 15.0f, 1.0f, new Color(128, 128, 128, 0).getRGB());
        Fonts.TEST.drawCentered("Royale Master Client \u00a9 All Rights Reserved", (float)fixedWidth / 2.0f, fixedHeight - 6, 5.0f, new Color(128, 128, 128, 128).getRGB());
        Render2D.blur(scaledMouseX, scaledMouseY, 1.0f, 1.0f, 15.0f, 1.0f, new Color(128, 128, 128, 0).getRGB());
        Render2D.endOverlay();
    }

    private void renderMainMenuContent(int screenWidth, int screenHeight, float mouseX, float mouseY, float menuProgress, float alpha, float unlockTextAlpha, long currentTime) {
        float slideOffset = (1.0f - alpha) * 20.0f;
        if (unlockTextAlpha > 0.01f && alpha > 0.5f) {
            this.renderUnlockText(unlockTextAlpha * alpha, screenWidth, screenHeight);
        }
        if (menuProgress > 0.01f) {
            this.renderTime(menuProgress * alpha, screenWidth, screenHeight, menuProgress, slideOffset);
            this.renderButtons(mouseX, mouseY, menuProgress * alpha, screenWidth, screenHeight, menuProgress, slideOffset);
        }
    }

    private void renderAltScreenContent(int screenWidth, int screenHeight, float mouseX, float mouseY, float alpha, long currentTime) {
        float totalWidth = 405.0f;
        float totalHeight = 163.0f;
        float centerX = (float)screenWidth / 2.0f;
        float centerY = (float)screenHeight / 2.0f;
        float startX = centerX - totalWidth / 2.0f;
        float startY = centerY - totalHeight / 2.0f;
        float leftPanelX = startX;
        float leftPanelTopY = startY;
        float accountPanelOffsetX = (1.0f - alpha) * -40.0f;
        if (alpha > 0.01f) {
            this.accountRenderer.renderLeftPanelTop(leftPanelX + accountPanelOffsetX, leftPanelTopY, 100.0f, 100.0f, alpha, this.nicknameText, this.nicknameFieldFocused, mouseX - accountPanelOffsetX, mouseY, currentTime);
        }
        float leftPanelBottomY = startY + 100.0f + 5.0f;
        float activeSessionOffsetY = (1.0f - alpha) * 40.0f;
        if (alpha > 0.01f) {
            this.accountRenderer.renderLeftPanelBottom(leftPanelX, leftPanelBottomY + activeSessionOffsetY, 100.0f, 58.0f, alpha, this.accountConfig.getActiveAccountName(), this.accountConfig.getActiveAccountDate(), this.accountConfig.getActiveAccountSkin());
        }
        float rightPanelX = startX + 100.0f + 5.0f;
        float rightPanelY = startY;
        int guiScale = 2;
        List<AccountEntry> sortedAccounts = this.accountConfig.getSortedAccounts();
        float accountsListOffsetX = (1.0f - alpha) * 40.0f;
        if (alpha > 0.01f) {
            this.accountRenderer.renderRightPanel(rightPanelX + accountsListOffsetX, rightPanelY, 300.0f, 165.0f, alpha, sortedAccounts, this.scrollOffset, mouseX - accountsListOffsetX, mouseY, 1.0f, guiScale);
        }
    }

    private void updateButtonAnimations(float deltaTime) {
        for (int i = 0; i < 5; ++i) {
            float targetHover = this.hoveredButton == i ? 1.0f : 0.0f;
            this.buttonHoverProgress[i] = MathHelper.lerp((float)(deltaTime * 10.0f), (float)this.buttonHoverProgress[i], (float)targetHover);
            float targetScale = this.hoveredButton == i ? 1.08f : 1.0f;
            this.buttonScales[i] = MathHelper.lerp((float)(deltaTime * 12.0f), (float)this.buttonScales[i], (float)targetScale);
        }
        float targetRed = this.hoveredButton == 4 ? 1.0f : 0.0f;
        this.exitButtonRedProgress = MathHelper.lerp((float)(deltaTime * 8.0f), (float)this.exitButtonRedProgress, (float)targetRed);
    }

    private void renderUnlockText(float opacity, int screenWidth, int screenHeight) {
        if (opacity < 0.01f) {
            return;
        }
        float centerX = (float)screenWidth / 2.0f;
        float centerY = (float)screenHeight / 2.0f;
        String text = "\u041d\u0430\u0436\u043c\u0438\u0442\u0435 \u043b\u044e\u0431\u0443\u044e \u043a\u043b\u0430\u0432\u0438\u0448\u0443 \u0434\u043b\u044f \u043f\u0440\u043e\u0434\u043e\u043b\u0436\u0435\u043d\u0438\u044f";
        float fontSize = 14.0f;
        float pulse = (float)Math.sin(this.unlockTextPulse) * 0.15f + 0.85f;
        int textAlpha = (int)(opacity * 255.0f * pulse);
        Fonts.REGULARNEW.drawCentered(text, centerX, centerY - 5.0f, fontSize, this.withAlpha(0xFFFFFF, textAlpha));
        float arrowY = centerY + 25.0f;
        float arrowBounce = (float)Math.sin(this.unlockTextPulse * 1.5f) * 3.0f;
        int arrowAlpha = (int)(opacity * 200.0f * pulse);
        Fonts.REGULARNEW.drawCentered("\u25bc", centerX, arrowY + arrowBounce, fontSize, this.withAlpha(0xFFFFFF, arrowAlpha));
    }

    private void renderTime(float opacity, int screenWidth, int screenHeight, float menuProgress, float extraSlideOffset) {
        float centerX = (float)screenWidth / 2.0f;
        float slideOffset = (1.0f - menuProgress) * 40.0f + extraSlideOffset;
        float centerY = (float)screenHeight / 2.0f - 55.0f + slideOffset;
        LocalTime now = LocalTime.now();
        String timeText = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        int textAlpha = (int)(opacity * 255.0f);
        float fontSize = 48.0f;
        float textHeight = Fonts.BOLD.getHeight(fontSize);
        Fonts.BOLD.drawCentered(timeText, centerX, centerY - textHeight / 2.0f, fontSize, this.withAlpha(0xFFFFFF, textAlpha));
        Object dateText = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.forLanguageTag("ru-RU")));
        if (!((String)dateText).isEmpty()) {
            dateText = Character.toUpperCase(((String)dateText).charAt(0)) + ((String)dateText).substring(1);
        }
        int dateAlpha = (int)(opacity * 200.0f);
        Fonts.BOLD.drawCentered((String)dateText, centerX, centerY + textHeight / 2.0f + 4.0f, 12.0f, this.withAlpha(0xFFFFFF, dateAlpha));
    }

    private void renderButtons(float mouseX, float mouseY, float opacity, int screenWidth, int screenHeight, float menuProgress, float extraSlideOffset) {
        float totalWidth = 274.0f;
        float startX = ((float)screenWidth - totalWidth) / 2.0f;
        float slideOffset = (1.0f - menuProgress) * 60.0f + extraSlideOffset;
        float centerY = (float)screenHeight / 2.0f + 30.0f + slideOffset;
        for (int i = 0; i < 5; ++i) {
            float buttonDelay = (float)i * 0.12f;
            float buttonProgress = MathHelper.clamp((float)((menuProgress - buttonDelay) / (1.0f - buttonDelay * 0.5f)), (float)0.0f, (float)1.0f);
            float easedProgress = this.easeOutCubic(buttonProgress);
            float buttonX = startX + (float)(i * 58);
            float buttonOpacity = opacity * easedProgress;
            this.renderCircleButton(i, buttonX, centerY, buttonOpacity);
        }
    }

    private void renderCircleButton(int index, float x, float y, float opacity) {
        int iconColor;
        int outlineColor;
        int bgBottomRight;
        int bgBottomLeft;
        int bgTopRight;
        int bgTopLeft;
        if (opacity < 0.01f) {
            return;
        }
        float scaleVal = this.buttonScales[index];
        float hoverProgress = this.buttonHoverProgress[index];
        float size = 42.0f * scaleVal;
        float halfSize = size / 2.0f;
        float centerX = x + 21.0f;
        float centerY = y + 21.0f;
        float drawX = centerX - halfSize;
        float drawY = centerY - halfSize;
        float radius = size / 2.0f;
        int headerAlpha = (int)(opacity * (150.0f + hoverProgress * 50.0f));
        int outlineAlpha = (int)(opacity * (150.0f + hoverProgress * 80.0f));
        int blurAlpha = (int)(opacity * 80.0f);
        if (index == 4) {
            float redLerp = this.exitButtonRedProgress;
            int rBg = MathHelper.lerp((float)redLerp, (int)20, (int)42);
            int gBg = MathHelper.lerp((float)redLerp, (int)23, (int)20);
            int bBg = MathHelper.lerp((float)redLerp, (int)31, (int)20);
            bgTopLeft = this.withAlpha(rBg << 16 | gBg << 8 | bBg, headerAlpha);
            bgTopRight = this.withAlpha(rBg + 4 << 16 | gBg + 4 << 8 | bBg + 5, headerAlpha);
            bgBottomLeft = this.withAlpha(rBg - 4 << 16 | gBg - 4 << 8 | bBg - 5, headerAlpha);
            bgBottomRight = this.withAlpha(rBg << 16 | gBg << 8 | bBg, headerAlpha);
            int outR = MathHelper.lerp((float)redLerp, (int)37, (int)90);
            int outG = MathHelper.lerp((float)redLerp, (int)42, (int)58);
            int outB = MathHelper.lerp((float)redLerp, (int)54, (int)58);
            outlineColor = this.withAlpha(outR << 16 | outG << 8 | outB, outlineAlpha);
            int iconR = 255;
            int iconG = MathHelper.lerp((float)redLerp, (int)255, (int)140);
            int iconB = MathHelper.lerp((float)redLerp, (int)255, (int)140);
            iconColor = this.withAlpha(iconR << 16 | iconG << 8 | iconB, (int)(opacity * 255.0f));
        } else {
            bgTopLeft = this.withAlpha(1316639, headerAlpha);
            bgTopRight = this.withAlpha(1579812, headerAlpha);
            bgBottomLeft = this.withAlpha(1053466, headerAlpha);
            bgBottomRight = this.withAlpha(1316639, headerAlpha);
            outlineColor = this.withAlpha(2435638, outlineAlpha);
            iconColor = this.withAlpha(0xFFFFFF, (int)(opacity * 255.0f));
        }
        int blurTint = this.withAlpha(395280, blurAlpha);
        Render2D.blur(drawX, drawY, size, size, 15.0f, radius, blurTint);
        int[] bgColors = new int[]{bgTopLeft, bgTopRight, bgBottomRight, bgBottomLeft};
        Render2D.gradientRect(drawX, drawY, size, size, bgColors, radius);
        Render2D.outline(drawX, drawY, size, size, 1.0f, outlineColor, radius);
        float iconSize = 17.0f * scaleVal;
        String icon = BUTTON_ICONS[index];
        float iconWidth = Fonts.MAINMENUSCREEN.getWidth(icon, iconSize);
        float iconHeight = Fonts.MAINMENUSCREEN.getHeight(iconSize);
        Fonts.MAINMENUSCREEN.draw(icon, centerX - iconWidth / 2.0f + 0.5f, centerY - iconHeight / 2.0f, iconSize, iconColor);
    }

    private int getHoveredButton(float mouseX, float mouseY, int screenWidth, int screenHeight, float menuProgress) {
        float totalWidth = 274.0f;
        float startX = ((float)screenWidth - totalWidth) / 2.0f;
        float slideOffset = (1.0f - menuProgress) * 60.0f;
        float centerY = (float)screenHeight / 2.0f + 30.0f + slideOffset;
        for (int i = 0; i < 5; ++i) {
            float buttonX = startX + (float)(i * 58);
            float buttonCenterX = buttonX + 21.0f;
            float dx = mouseX - buttonCenterX;
            float buttonCenterY = centerY + 21.0f;
            float dy = mouseY - buttonCenterY;
            if (!(dx * dx + dy * dy <= 441.0f)) continue;
            return i;
        }
        return -1;
    }

    public boolean mouseClicked(Click click, boolean doubled) {
        if (this.transitionPhase != TransitionPhase.NONE) {
            return false;
        }
        float scaledMouseX = this.toFixedCoord(click.x());
        float scaledMouseY = this.toFixedCoord(click.y());
        if (this.currentView == View.MAIN_MENU) {
            if (!this.isUnlocked) {
                this.unlock();
                return true;
            }
            if (click.button() == 0 && this.hoveredButton >= 0) {
                this.handleMainMenuButtonClick(this.hoveredButton);
                return true;
            }
        } else if (this.currentView == View.ALT_SCREEN) {
            return this.handleAltScreenClick(scaledMouseX, scaledMouseY, click);
        }
        return super.mouseClicked(click, doubled);
    }

    private void handleMainMenuButtonClick(int index) {
        switch (index) {
            case 0: {
                this.client.setScreen((Screen)new SelectWorldScreen((Screen)this));
                break;
            }
            case 1: {
                Screen screen = this.client.options.skipMultiplayerWarning ? new MultiplayerScreen((Screen)this) : new MultiplayerWarningScreen((Screen)this);
                this.client.setScreen((Screen)screen);
                break;
            }
            case 2: {
                this.switchToView(View.ALT_SCREEN);
                break;
            }
            case 3: {
                this.client.setScreen((Screen)new OptionsScreen((Screen)this, this.client.options));
                break;
            }
            case 4: {
                this.client.scheduleStop();
            }
        }
    }

    private boolean handleAltScreenClick(float mouseX, float mouseY, Click click) {
        float fieldHeight;
        float buttonGap;
        float addButtonSize;
        float fieldWidth;
        float totalHeight;
        int screenHeight;
        float centerY;
        float startY;
        float leftPanelTopY;
        float fieldY;
        float totalWidth;
        int screenWidth = this.getFixedScaledWidth();
        float centerX = (float)screenWidth / 2.0f;
        float startX = centerX - (totalWidth = 405.0f) / 2.0f;
        float leftPanelX = startX;
        float fieldX = leftPanelX + 5.0f;
        if (this.accountRenderer.isMouseOver(mouseX, mouseY, fieldX, fieldY = (leftPanelTopY = (startY = (centerY = (float)(screenHeight = this.getFixedScaledHeight()) / 2.0f) - (totalHeight = 163.0f) / 2.0f)) + 38.0f, fieldWidth = 90.0f - (addButtonSize = 14.0f) - (buttonGap = 3.0f), fieldHeight = 14.0f)) {
            this.nicknameFieldFocused = true;
            return true;
        }
        this.nicknameFieldFocused = false;
        float addButtonX = fieldX + fieldWidth + buttonGap;
        float addButtonY = fieldY;
        if (this.accountRenderer.isMouseOver(mouseX, mouseY, addButtonX, addButtonY, addButtonSize, addButtonSize)) {
            if (!this.nicknameText.isEmpty()) {
                this.addAccount(this.nicknameText);
                this.nicknameText = "";
            }
            return true;
        }
        float randomButtonX = leftPanelX + 5.0f;
        float randomButtonY = fieldY + fieldHeight + 6.0f;
        float buttonWidth = 90.0f;
        float buttonHeight = 16.0f;
        if (this.accountRenderer.isMouseOver(mouseX, mouseY, randomButtonX, randomButtonY, buttonWidth, buttonHeight)) {
            String randomNick = this.generateRandomNickname();
            this.addAccount(randomNick);
            this.nicknameText = "";
            return true;
        }
        float clearButtonX = leftPanelX + 5.0f;
        float clearButtonY = randomButtonY + buttonHeight + 5.0f;
        if (this.accountRenderer.isMouseOver(mouseX, mouseY, clearButtonX, clearButtonY, buttonWidth, buttonHeight)) {
            this.accountConfig.clearAllAccounts();
            this.targetScrollOffset = 0.0f;
            this.scrollOffset = 0.0f;
            return true;
        }
        float rightPanelX = startX + 100.0f + 5.0f;
        float accountListX = rightPanelX + 5.0f;
        float rightPanelY = startY;
        float accountListY = rightPanelY + 26.0f;
        float accountListWidth = 290.0f;
        float accountListHeight = 134.0f;
        if (!this.accountRenderer.isMouseOver(mouseX, mouseY, accountListX, accountListY, accountListWidth, accountListHeight)) {
            return false;
        }
        float cardWidth = (accountListWidth - 5.0f) / 2.0f;
        float cardHeight = 40.0f;
        float cardGap = 5.0f;
        List<AccountEntry> sortedAccounts = this.accountConfig.getSortedAccounts();
        for (int i = 0; i < sortedAccounts.size(); ++i) {
            int col = i % 2;
            int row = i / 2;
            float cardX = accountListX + (float)col * (cardWidth + cardGap);
            float cardY = accountListY + (float)row * (cardHeight + cardGap) - this.scrollOffset;
            if (cardY + cardHeight < accountListY || cardY > accountListY + accountListHeight) continue;
            float btnSize = 12.0f;
            float buttonYPos = cardY + cardHeight - btnSize - 5.0f;
            float pinButtonX = cardX + cardWidth - btnSize * 2.0f - 8.0f;
            float deleteButtonX = cardX + cardWidth - btnSize - 5.0f;
            if (this.accountRenderer.isMouseOver(mouseX, mouseY, pinButtonX, buttonYPos, btnSize, btnSize)) {
                AccountEntry entry = sortedAccounts.get(i);
                entry.togglePinned();
                if (entry.isPinned()) {
                    this.setActiveAccount(entry);
                }
                this.accountConfig.save();
                return true;
            }
            if (this.accountRenderer.isMouseOver(mouseX, mouseY, deleteButtonX, buttonYPos, btnSize, btnSize)) {
                this.accountConfig.removeAccountByIndex(i);
                return true;
            }
            if (!this.accountRenderer.isMouseOver(mouseX, mouseY, cardX, cardY, cardWidth, cardHeight)) continue;
            this.setActiveAccount(sortedAccounts.get(i));
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        float totalHeight;
        int screenHeight;
        float centerY;
        float startY;
        float rightPanelY;
        float totalWidth;
        int screenWidth;
        float centerX;
        float startX;
        float rightPanelX;
        float scaledMouseY;
        if (this.currentView != View.ALT_SCREEN || this.transitionPhase != TransitionPhase.NONE) {
            return false;
        }
        float scaledMouseX = this.toFixedCoord(mouseX);
        if (this.accountRenderer.isMouseOver(scaledMouseX, scaledMouseY = this.toFixedCoord(mouseY), rightPanelX = (startX = (centerX = (float)(screenWidth = this.getFixedScaledWidth()) / 2.0f) - (totalWidth = 405.0f) / 2.0f) + 100.0f + 5.0f, rightPanelY = (startY = (centerY = (float)(screenHeight = this.getFixedScaledHeight()) / 2.0f) - (totalHeight = 163.0f) / 2.0f), 300.0f, 165.0f)) {
            float cardHeight = 40.0f;
            float cardGap = 5.0f;
            float accountListHeight = 134.0f;
            int rows = (int)Math.ceil((double)this.accountConfig.getSortedAccounts().size() / 2.0);
            float maxScroll = Math.max(0.0f, (float)rows * (cardHeight + cardGap) - accountListHeight);
            this.targetScrollOffset -= (float)verticalAmount * 25.0f;
            this.targetScrollOffset = MathHelper.clamp((float)this.targetScrollOffset, (float)0.0f, (float)maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean keyPressed(KeyInput input) {
        if (this.transitionPhase != TransitionPhase.NONE) {
            return false;
        }
        if (this.currentView == View.MAIN_MENU) {
            if (!this.isUnlocked) {
                this.unlock();
                return true;
            }
        } else if (this.currentView == View.ALT_SCREEN) {
            if (this.nicknameFieldFocused) {
                int keyCode = input.key();
                if (keyCode == 259) {
                    if (!this.nicknameText.isEmpty()) {
                        this.nicknameText = this.nicknameText.substring(0, this.nicknameText.length() - 1);
                    }
                    return true;
                }
                if (keyCode == 256) {
                    this.nicknameFieldFocused = false;
                    return true;
                }
                if (keyCode == 257 || keyCode == 335) {
                    if (!this.nicknameText.isEmpty()) {
                        this.addAccount(this.nicknameText);
                        this.nicknameText = "";
                    }
                    this.nicknameFieldFocused = false;
                    return true;
                }
            }
            if (input.key() == 256) {
                this.switchToView(View.MAIN_MENU);
                this.accountConfig.save();
                return true;
            }
        }
        return super.keyPressed(input);
    }

    public boolean charTyped(CharInput input) {
        int codepoint;
        if (this.currentView == View.ALT_SCREEN && this.nicknameFieldFocused && this.transitionPhase == TransitionPhase.NONE && (Character.isLetterOrDigit(codepoint = input.codepoint()) || codepoint == 95)) {
            if (this.nicknameText.length() < 16) {
                this.nicknameText = this.nicknameText + Character.toString(codepoint);
            }
            return true;
        }
        return super.charTyped(input);
    }

    private void setActiveAccount(AccountEntry account) {
        this.accountConfig.setActiveAccount(account.getName(), account.getDate(), account.getSkin());
        SessionChanger.changeUsername(account.getName());
    }

    private void addAccount(String nickname) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String date = now.format(formatter);
        AccountEntry entry = new AccountEntry(nickname, date, null);
        this.accountConfig.addAccount(entry);
        this.setActiveAccount(entry);
        SessionChanger.changeUsername(nickname);
    }

    private String generateRandomNickname() {
        Random random = new Random();
        StringBuilder username = new StringBuilder();
        char[] vowels = new char[]{'a', 'e', 'i', 'o', 'u'};
        char[] consonants = new char[]{'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'r', 's', 't', 'v', 'w', 'x', 'y', 'z'};
        String finalUsername = null;
        int attempts = 0;
        int MAX_ATTEMPTS = 10;
        List<AccountEntry> existingAccounts = this.accountConfig.getAccounts();
        do {
            username.setLength(0);
            int length = 6 + random.nextInt(5);
            boolean startWithVowel = random.nextBoolean();
            for (int i = 0; i < length; ++i) {
                if (i % 2 == 0) {
                    username.append(startWithVowel ? vowels[random.nextInt(vowels.length)] : consonants[random.nextInt(consonants.length)]);
                    continue;
                }
                username.append(startWithVowel ? consonants[random.nextInt(consonants.length)] : vowels[random.nextInt(vowels.length)]);
            }
            if (random.nextInt(100) < 30) {
                username.append(random.nextInt(100));
            }
            String tempUsername = username.substring(0, 1).toUpperCase() + username.substring(1);
            ++attempts;
            boolean exists = false;
            for (AccountEntry account : existingAccounts) {
                if (!account.getName().equalsIgnoreCase(tempUsername)) continue;
                exists = true;
                break;
            }
            if (exists) continue;
            finalUsername = tempUsername;
            break;
        } while (attempts < 10);
        if (finalUsername == null) {
            finalUsername = username.substring(0, 1).toUpperCase() + username.substring(1) + System.currentTimeMillis() % 1000L;
        }
        return finalUsername;
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.drawBackground(this.currentZoom);
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    public boolean shouldPause() {
        return false;
    }

    private int withAlpha(int color, int alpha) {
        return color & 0xFFFFFF | MathHelper.clamp((int)alpha, (int)0, (int)255) << 24;
    }

    private static enum View {
        MAIN_MENU,
        ALT_SCREEN;

    }

    private static enum TransitionPhase {
        NONE,
        FADE_OUT,
        FADE_IN;

    }
}

