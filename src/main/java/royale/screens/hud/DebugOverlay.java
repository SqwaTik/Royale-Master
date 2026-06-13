package royale.screens.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import royale.client.draggables.AbstractHudElement;
import royale.modules.impl.misc.Debug;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.theme.ClientTheme;

import java.awt.Color;

public class DebugOverlay extends AbstractHudElement {
    private static final float TITLE_SIZE = 6.4F;
    private static final float LINE_SIZE = 5.9F;
    private static final float PADDING_X = 10.0F;
    private static final float PADDING_Y = 8.0F;
    private static final float LINE_GAP = 2.5F;

    public DebugOverlay() {
        super("DebugOverlay", defaultX(), 6, 190, 58, true);
        startAnimation();
    }

    @Override
    public boolean visible() {
        Debug debug = Debug.getInstance();
        if (debug == null || !debug.isState()) {
            return false;
        }

        boolean allowPlaceholder = this.mc.currentScreen instanceof ChatScreen;
        return debug.shouldRenderOverlay(allowPlaceholder);
    }

    @Override
    public void drawDraggable(DrawContext context, int alpha) {
        Debug debug = Debug.getInstance();
        if (debug == null) {
            return;
        }

        boolean allowPlaceholder = this.mc.currentScreen instanceof ChatScreen;
        Debug.DebugInfo info = debug.getOverlayInfo(allowPlaceholder);
        if (info == null) {
            return;
        }

        float width = Fonts.BOLD.getWidth(info.getTitle(), TITLE_SIZE);
        for (String line : info.getLines()) {
            width = Math.max(width, Fonts.TEST.getWidth(line, LINE_SIZE));
        }

        float titleHeight = Fonts.BOLD.getHeight(TITLE_SIZE);
        float lineHeight = Fonts.TEST.getHeight(LINE_SIZE);
        float linesHeight = info.getLines().isEmpty()
                ? 0.0F
                : info.getLines().size() * lineHeight + Math.max(0, info.getLines().size() - 1) * LINE_GAP;

        float boxWidth = Math.max(170.0F, width + PADDING_X * 2.0F);
        float boxHeight = titleHeight + linesHeight + PADDING_Y * 2.0F + (info.getLines().isEmpty() ? 0.0F : 6.0F);

        setWidth((int) Math.ceil(boxWidth));
        setHeight((int) Math.ceil(boxHeight));

        float x = this.x;
        float y = this.y;
        int bg = withAlpha(new Color(10, 12, 18, 210).getRGB(), alpha);
        int panel = withAlpha(new Color(18, 22, 30, 140).getRGB(), alpha);
        int outline = ClientTheme.accentWithAlpha(Math.min(255, alpha));
        int titleColor = withAlpha(new Color(255, 236, 196, 255).getRGB(), alpha);
        int lineColor = withAlpha(new Color(235, 237, 242, 245).getRGB(), alpha);

        Render2D.blur(x, y, boxWidth, boxHeight, 9.0F, 12.0F, withAlpha(new Color(12, 16, 24, 90).getRGB(), alpha));
        Render2D.rect(x, y, boxWidth, boxHeight, bg, 8.0F);
        Render2D.rect(x + 1.5F, y + 1.5F, boxWidth - 3.0F, boxHeight - 3.0F, panel, 7.0F);
        Render2D.outline(x, y, boxWidth, boxHeight, 0.6F, outline, 8.0F);

        float drawX = x + PADDING_X;
        float drawY = y + PADDING_Y;
        Fonts.BOLD.draw(info.getTitle(), drawX, drawY, TITLE_SIZE, titleColor);
        drawY += titleHeight + 6.0F;

        for (String line : info.getLines()) {
            Fonts.TEST.draw(line, drawX, drawY, LINE_SIZE, lineColor);
            drawY += lineHeight + LINE_GAP;
        }
    }

    private static int defaultX() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) {
            return 80;
        }
        return Math.max(8, client.getWindow().getScaledWidth() / 2 - 95);
    }

    private int withAlpha(int color, int alpha) {
        int baseAlpha = color >>> 24;
        int resolvedAlpha = baseAlpha <= 0 ? alpha : Math.min(255, baseAlpha * alpha / 255);
        return color & 0xFFFFFF | resolvedAlpha << 24;
    }
}
