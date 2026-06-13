package royale.screens.loading;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.render.font.SafeTextRenderer;
import royale.util.theme.ClientTheme;

public class Loading {
    private static final Identifier LOADING_LOGO_TEXTURE = Identifier.of("royale", "images/elements/hud_r_logo.png");
    private static final String[] LOADING_TEXTS = {
            "Загрузка",
            "Подготовка",
            "Инициализация",
            "Почти готово"
    };

    private static final long[] STAGE_DISPLAY_DURATIONS = {640L, 680L, 640L};
    private static final long LAST_TEXT_DISPLAY_DURATION = 120L;
    private static final long TEXT_TRANSITION_DURATION = 120L;

    private float animatedProgress = 0.0F;
    private float targetProgress = 0.0F;
    private float pulseTime = 0.0F;
    private long lastRenderTime = 0L;
    private long startTime = 0L;

    private boolean initialized = false;
    private int currentTextIndex = 0;
    private float currentTextOffsetY = 0.0F;
    private float currentTextAlpha = 1.0F;
    private float newTextOffsetY = -12.0F;
    private float newTextAlpha = 0.0F;
    private long lastTextChangeTime = 0L;
    private boolean isTransitioning = false;
    private long transitionStartTime = 0L;

    private float backgroundAlpha = 0.0F;
    private float contentAlpha = 0.0F;

    private boolean isFadingOut = false;
    private boolean readyToClose = false;
    private boolean resourcesLoaded = false;
    private boolean allTextsShown = false;
    private long lastTextShownTime = 0L;
    private boolean useCustomTextRenderer = false;

    public Loading() {
        this.startTime = Util.getMeasuringTimeMs();
        this.lastTextChangeTime = this.startTime;
    }

    private int getFixedScaledWidth() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) {
            return 960;
        }
        return (int) Math.ceil(client.getWindow().getFramebufferWidth() / 2.0D);
    }

    private int getFixedScaledHeight() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) {
            return 540;
        }
        return (int) Math.ceil(client.getWindow().getFramebufferHeight() / 2.0D);
    }

    public void render(DrawContext context, int width, int height, float opacity) {
        long currentTime = Util.getMeasuringTimeMs();

        if (!this.initialized) {
            this.lastRenderTime = currentTime;
            this.initialized = true;
        }

        float deltaTime = (currentTime - this.lastRenderTime) / 1000.0F;
        this.lastRenderTime = currentTime;
        deltaTime = MathHelper.clamp(deltaTime, 0.001F, 0.1F);

        updateAnimations(deltaTime, currentTime);

        int fixedWidth = getFixedScaledWidth();
        int fixedHeight = getFixedScaledHeight();

        Render2D.beginOverlay();
        Render2D.backgroundImage(this.backgroundAlpha * opacity, 1.08F);

        float finalContentAlpha = this.contentAlpha * opacity;
        if (finalContentAlpha > 0.001F) {
            renderLogo(context, fixedWidth, fixedHeight, finalContentAlpha);
            renderLoadingText(context, fixedWidth, fixedHeight, finalContentAlpha);
        }

        Render2D.endOverlay();
    }

    private void updateAnimations(float deltaTime, long currentTime) {
        this.pulseTime += deltaTime * 2.0F;
        this.animatedProgress = MathHelper.lerp(deltaTime * 5.0F, this.animatedProgress, this.targetProgress);

        this.backgroundAlpha = MathHelper.lerp(deltaTime * 5.0F, this.backgroundAlpha, 1.0F);
        if (this.backgroundAlpha > 0.99F) {
            this.backgroundAlpha = 1.0F;
        }

        if (!this.isFadingOut) {
            this.contentAlpha = MathHelper.lerp(deltaTime * 8.0F, this.contentAlpha, 1.0F);
            if (this.contentAlpha > 0.99F) {
                this.contentAlpha = 1.0F;
            }
        } else {
            this.contentAlpha -= deltaTime * 9.0F;
            if (this.contentAlpha < 0.0F) {
                this.contentAlpha = 0.0F;
                this.readyToClose = true;
            }
        }

        if (!this.isFadingOut) {
            updateTextAnimation(currentTime);
        }

        if (this.allTextsShown && this.resourcesLoaded && !this.isFadingOut) {
            long elapsed = currentTime - this.lastTextShownTime;
            if (elapsed >= LAST_TEXT_DISPLAY_DURATION) {
                this.isFadingOut = true;
            }
        }
    }

    private void updateTextAnimation(long currentTime) {
        if (this.allTextsShown) {
            return;
        }

        if (!this.isTransitioning) {
            long elapsed = currentTime - this.lastTextChangeTime;

            if (this.currentTextIndex >= LOADING_TEXTS.length - 1) {
                this.allTextsShown = true;
                this.lastTextShownTime = currentTime;
                return;
            }

            if (elapsed >= getStageDisplayDuration(this.currentTextIndex)) {
                this.isTransitioning = true;
                this.transitionStartTime = currentTime;
            }
        }

        if (this.isTransitioning) {
            long elapsed = currentTime - this.transitionStartTime;
            float rawProgress = MathHelper.clamp((float) elapsed / (float) TEXT_TRANSITION_DURATION, 0.0F, 1.0F);
            float eased = easeOutQuad(rawProgress);

            this.currentTextOffsetY = 12.0F * eased;
            this.currentTextAlpha = MathHelper.clamp(1.0F - eased * 1.5F, 0.0F, 1.0F);

            this.newTextOffsetY = -10.0F * (1.0F - eased);
            this.newTextAlpha = MathHelper.clamp(eased * 1.3F, 0.0F, 1.0F);

            if (rawProgress >= 1.0F) {
                this.isTransitioning = false;
                this.currentTextIndex++;
                this.currentTextOffsetY = 0.0F;
                this.currentTextAlpha = 1.0F;
                this.newTextOffsetY = -12.0F;
                this.newTextAlpha = 0.0F;
                this.lastTextChangeTime = currentTime;

                if (this.currentTextIndex >= LOADING_TEXTS.length - 1) {
                    this.allTextsShown = true;
                    this.lastTextShownTime = currentTime;
                }
            }
        }
    }

    private float easeOutQuad(float x) {
        return 1.0F - (1.0F - x) * (1.0F - x);
    }

    private void renderLogo(DrawContext context, int width, int height, float opacity) {
        int alpha = MathHelper.clamp((int) (opacity * 255.0F), 0, 255);
        int mainColor = withAlpha(ClientTheme.accent(), Math.min(255, (int) (alpha * 0.98F)));
        int shadowColor = withAlpha(0, Math.min(255, (int) (alpha * 0.34F)));

        float drawX = (width - 64) * 0.5F;
        float yBob = (float) Math.sin(this.pulseTime * 1.2F) * 1.8F;
        float drawY = height * 0.5F - 48.0F + yBob;

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(drawX + 1.0F, drawY + 1.0F);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, LOADING_LOGO_TEXTURE, 0, 0, 0.0F, 0.0F, 64, 64, 64, 64, shadowColor);
        context.getMatrices().popMatrix();

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(drawX, drawY);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, LOADING_LOGO_TEXTURE, 0, 0, 0.0F, 0.0F, 64, 64, 64, 64, mainColor);
        context.getMatrices().popMatrix();
    }

    private void renderLoadingText(DrawContext context, int width, int height, float opacity) {
        float fontSize = 11.0F;
        float baseY = height / 2.0F + 30.0F;
        float centerX = width / 2.0F;

        if (this.currentTextAlpha > 0.01F && this.currentTextIndex < LOADING_TEXTS.length) {
            drawLoadingLine(
                    context,
                    LOADING_TEXTS[this.currentTextIndex],
                    centerX,
                    baseY + this.currentTextOffsetY,
                    opacity * this.currentTextAlpha,
                    fontSize
            );
        }

        if (this.isTransitioning && this.newTextAlpha > 0.01F) {
            int nextIndex = this.currentTextIndex + 1;
            if (nextIndex < LOADING_TEXTS.length) {
                drawLoadingLine(
                        context,
                        LOADING_TEXTS[nextIndex],
                        centerX,
                        baseY + this.newTextOffsetY,
                        opacity * this.newTextAlpha,
                        fontSize
                );
            }
        }
    }

    private void drawLoadingLine(DrawContext context, String text, float centerX, float y, float alpha, float fontSize) {
        if (!this.useCustomTextRenderer && SafeTextRenderer.canUseCustom(Fonts.REGULARNEW, text)) {
            this.useCustomTextRenderer = true;
        }

        int mainColor = withAlpha(0xFFFFFF, (int) (MathHelper.clamp(alpha, 0.0F, 1.0F) * 255.0F));
        int shadowColor = withAlpha(0x000000, (int) (MathHelper.clamp(alpha, 0.0F, 1.0F) * 120.0F));
        float width = this.useCustomTextRenderer ? Fonts.REGULARNEW.getWidth(text, fontSize) : getVanillaWidth(text, fontSize);
        float drawX = centerX - width / 2.0F;

        if (this.useCustomTextRenderer) {
            Fonts.REGULARNEW.draw(text, drawX + 1.0F, y + 1.0F, fontSize, shadowColor);
            Fonts.REGULARNEW.draw(text, drawX, y, fontSize, mainColor);
            return;
        }

        drawVanilla(context, text, drawX + 1.0F, y + 1.0F, fontSize, shadowColor);
        drawVanilla(context, text, drawX, y, fontSize, mainColor);
    }

    public void markComplete() {
        this.resourcesLoaded = true;

        if (!this.allTextsShown) {
            this.currentTextIndex = LOADING_TEXTS.length - 1;
            this.currentTextOffsetY = 0.0F;
            this.currentTextAlpha = 1.0F;
            this.newTextOffsetY = -12.0F;
            this.newTextAlpha = 0.0F;
            this.isTransitioning = false;
            this.allTextsShown = true;
            this.lastTextShownTime = Util.getMeasuringTimeMs();
        }
        this.isFadingOut = true;
    }

    public boolean isReadyToClose() {
        return this.readyToClose;
    }

    public void setProgress(float progress) {
        this.targetProgress = MathHelper.clamp(progress, 0.0F, 1.0F);
    }

    public float getProgress() {
        return this.targetProgress;
    }

    public void reset() {
        this.animatedProgress = 0.0F;
        this.targetProgress = 0.0F;
        this.pulseTime = 0.0F;
        this.lastRenderTime = 0L;
        this.startTime = Util.getMeasuringTimeMs();
        this.initialized = false;

        this.currentTextIndex = 0;
        this.currentTextOffsetY = 0.0F;
        this.currentTextAlpha = 1.0F;
        this.newTextOffsetY = -12.0F;
        this.newTextAlpha = 0.0F;
        this.lastTextChangeTime = this.startTime;
        this.isTransitioning = false;
        this.transitionStartTime = 0L;

        this.backgroundAlpha = 0.0F;
        this.contentAlpha = 0.0F;
        this.isFadingOut = false;
        this.readyToClose = false;

        this.resourcesLoaded = false;
        this.allTextsShown = false;
        this.lastTextShownTime = 0L;
        this.useCustomTextRenderer = false;
    }

    public long getStartTime() {
        return this.startTime;
    }

    private long getStageDisplayDuration(int stageIndex) {
        if (stageIndex < 0 || stageIndex >= STAGE_DISPLAY_DURATIONS.length) {
            return STAGE_DISPLAY_DURATIONS[STAGE_DISPLAY_DURATIONS.length - 1];
        }
        return STAGE_DISPLAY_DURATIONS[stageIndex];
    }

    private int withAlpha(int color, int alpha) {
        return (color & 0xFFFFFF) | (MathHelper.clamp(alpha, 0, 255) << 24);
    }

    private float getVanillaWidth(String text, float size) {
        TextRenderer renderer = getVanillaRenderer();
        if (renderer == null || text == null || text.isEmpty()) {
            return 0.0F;
        }
        return renderer.getWidth(text) * getVanillaScale(size);
    }

    private void drawVanilla(DrawContext context, String text, float x, float y, float size, int color) {
        if (context == null || text == null || text.isEmpty()) {
            return;
        }

        TextRenderer renderer = getVanillaRenderer();
        if (renderer == null) {
            return;
        }

        float scale = getVanillaScale(size);
        if (Math.abs(scale - 1.0F) < 0.01F) {
            context.drawText(renderer, text, Math.round(x), Math.round(y), color, false);
            return;
        }

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        context.getMatrices().scale(scale, scale);
        context.drawText(renderer, text, 0, 0, color, false);
        context.getMatrices().popMatrix();
    }

    private TextRenderer getVanillaRenderer() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc == null ? null : mc.textRenderer;
    }

    private float getVanillaScale(float size) {
        return MathHelper.clamp(size / 9.0F, 0.5F, 4.0F);
    }
}




