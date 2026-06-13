package royale.screens.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import royale.client.draggables.AbstractHudElement;
import royale.modules.impl.render.CropTimer;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;

import java.awt.Color;
import java.util.Locale;

public class CropTimerOverlay extends AbstractHudElement {
    private static final float TITLE_SIZE = 6.0F;
    private static final float LINE_SIZE = 5.5F;
    private static final float PADDING = 8.0F;

    public CropTimerOverlay() {
        super("CropTimerOverlay", 6, 6, 145, 54, true);
        startAnimation();
    }

    @Override
    public boolean visible() {
        CropTimer timer = CropTimer.getInstance();
        return timer != null && timer.isState();
    }

    @Override
    public void drawDraggable(DrawContext context, int alpha) {
        if (mc.world == null || mc.player == null || mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            return;
        }

        CropTimer timer = CropTimer.getInstance();
        if (timer == null) {
            return;
        }

        BlockHitResult blockHit = (BlockHitResult) mc.crosshairTarget;
        BlockPos pos = blockHit.getBlockPos();
        CropTimer.CropInfo info = timer.getCropInfo(mc.world.getBlockState(pos), pos);
        if (info == null) {
            return;
        }

        float boxWidth = 145.0F;
        float boxHeight = 54.0F;
        int bg = withAlpha(new Color(10, 12, 18, 205).getRGB(), alpha);
        int outline = withAlpha(info.ready() ? new Color(80, 220, 100, 220).getRGB() : new Color(255, 214, 101, 210).getRGB(), alpha);
        Render2D.rect(this.x, this.y, boxWidth, boxHeight, bg, 6.0F);
        Render2D.outline(this.x, this.y, boxWidth, boxHeight, 0.5F, outline, 6.0F);

        float drawX = this.x + PADDING;
        float drawY = this.y + PADDING;

        if (timer.showTitle.isValue()) {
            Fonts.BOLD.draw(info.name(), drawX, drawY, TITLE_SIZE, withAlpha(new Color(226, 229, 238, 255).getRGB(), alpha));
            drawY += 10.0F;
        }

        if (timer.showStage.isValue()) {
            String stageText = info.ready()
                    ? "Готово к сбору!"
                    : info.currentStage() + " / " + info.maxStage() + " (" + formatPercent(info.percent()) + "%)";
            int stageColor = info.ready()
                    ? withAlpha(new Color(80, 220, 100, 255).getRGB(), alpha)
                    : withAlpha(new Color(255, 214, 101, 255).getRGB(), alpha);
            Fonts.BOLD.draw(stageText, drawX, drawY, LINE_SIZE, stageColor);
            drawY += 9.0F;
        }

        if (timer.showTime.isValue()) {
            String text = info.ready() ? "Время: 0с" : "До роста: " + formatEta(info.etaMs());
            Fonts.BOLD.draw(text, drawX, drawY, 5.0F, withAlpha(new Color(170, 174, 185, 230).getRGB(), alpha));
        }

        setWidth((int) boxWidth);
        setHeight((int) boxHeight);
    }

    private String formatPercent(double percent) {
        double rounded = Math.round(percent * 10.0D) / 10.0D;
        if (Math.abs(rounded - Math.rint(rounded)) < 0.001D) {
            return Integer.toString((int) rounded);
        }
        return String.format(Locale.US, "%.1f", rounded);
    }

    private String formatEta(long etaMs) {
        if (etaMs <= 0L) {
            return "0с";
        }
        long seconds = Math.max(1L, etaMs / 1000L);
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        if (hours > 0L) {
            return hours + "ч " + (minutes % 60L) + "м";
        }
        if (minutes > 0L) {
            return minutes + "м";
        }
        return seconds + "с";
    }

    private int withAlpha(int color, int alpha) {
        int baseAlpha = color >>> 24;
        int resolvedAlpha = baseAlpha <= 0 ? alpha : Math.min(255, baseAlpha * alpha / 255);
        return color & 0xFFFFFF | resolvedAlpha << 24;
    }
}
