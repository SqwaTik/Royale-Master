package royale.screens.hud;

import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import royale.client.draggables.AbstractHudElement;
import royale.modules.impl.render.Hud;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.theme.ClientTheme;

public class Info
extends AbstractHudElement {
    private double lastX = 0.0;
    private double lastZ = 0.0;
    private double currentBps = 0.0;
    private double displayBps = 0.0;
    private double targetBps = 0.0;
    private long lastUpdateTime = 0L;
    private static final double BPS_SMOOTHING = 0.05;
    private static final double DISPLAY_SMOOTHING = 0.03;

    public Info() {
        super("Info", 10, 0, 200, 24, false);
        this.startAnimation();
    }

    @Override
    public void tick() {
    }

    private double roundToStep(double value, double step) {
        return (double)Math.round(value / step) * step;
    }

    @Override
    public void drawDraggable(DrawContext context, int alpha) {
        if (alpha <= 0) {
            return;
        }
        if (this.mc.player == null) {
            return;
        }
        boolean showBps = Hud.getInstance() != null && Hud.getInstance().showBps.isValue();
        long currentTime = System.currentTimeMillis();
        double deltaTime = (double)(currentTime - this.lastUpdateTime) / 1000.0;
        if (this.lastUpdateTime > 0L && deltaTime > 0.0) {
            double dx = this.mc.player.getX() - this.lastX;
            double dz = this.mc.player.getZ() - this.lastZ;
            double distance = Math.sqrt(dx * dx + dz * dz);
            double instantBps = distance / deltaTime;
            this.currentBps += (instantBps - this.currentBps) * 0.05;
            this.targetBps = this.roundToStep(this.currentBps, 0.5);
        }
        this.displayBps += (this.targetBps - this.displayBps) * 0.03;
        this.lastX = this.mc.player.getX();
        this.lastZ = this.mc.player.getZ();
        this.lastUpdateTime = currentTime;
        float x = -5.0f;
        float y = 28.0f;
        int playerX = (int)this.mc.player.getX();
        int playerY = (int)this.mc.player.getY();
        int playerZ = (int)this.mc.player.getZ();
        String xText = "x";
        String yText = "y";
        String zText = "z";
        String xValue = String.valueOf(playerX);
        String yValue = String.valueOf(playerY);
        String zValue = String.valueOf(playerZ);
        double roundedDisplayBps = this.roundToStep(this.displayBps, 0.5);
        String bpsValue = String.format("%.2f", roundedDisplayBps);
        String bpsText = "b/s";
        float xTextWidth = Fonts.BOLD.getWidth(xText, 6.0f);
        float yTextWidth = Fonts.BOLD.getWidth(yText, 6.0f);
        float zTextWidth = Fonts.BOLD.getWidth(zText, 6.0f);
        float xValueWidth = Fonts.BOLD.getWidth(xValue, 6.0f);
        float yValueWidth = Fonts.BOLD.getWidth(yValue, 6.0f);
        float zValueWidth = Fonts.BOLD.getWidth(zValue, 6.0f);
        float bpsValueWidth = Fonts.BOLD.getWidth(bpsValue, 6.0f);
        float bpsTextWidth = Fonts.BOLD.getWidth(bpsText, 6.0f);
        float coordsWidth = 22.0f + xTextWidth + 2.0f + xValueWidth + 8.0f + 8.0f + yTextWidth + 2.0f + yValueWidth + 8.0f + 8.0f + zTextWidth + 2.0f + zValueWidth;
        float bpsWidth = 34.0f + bpsValueWidth + 2.0f + bpsTextWidth + 5.0f;
        this.setX((int)x);
        this.setY((int)y);
        if (showBps) {
            this.setWidth((int)(coordsWidth + bpsWidth + 30.0f));
        } else {
            this.setWidth((int)(coordsWidth + 24.0f));
        }
        this.setHeight(22);
        Render2D.gradientRect(x + 12.0f, y + 3.0f, coordsWidth, 20.0f, new int[]{new Color(52, 52, 52, 255).getRGB(), new Color(22, 22, 22, 255).getRGB(), new Color(52, 52, 52, 255).getRGB(), new Color(22, 22, 22, 255).getRGB()}, 5.0f);
        Render2D.outline(x + 12.0f, y + 3.0f, coordsWidth, 20.0f, 0.35f, ClientTheme.blendWithAccentAndAlpha(new Color(90, 90, 90, 255).getRGB(), 0.35f, 255), 5.0f);
        float textY = y + 7.0f;
        float textX = x + 12.0f;
        Fonts.ICONSTYPETHO.draw("n", textX + 5.0f, textY + 0.5f, 11.0f, ClientTheme.accentWithAlpha(255));
        float offsetX = textX + 22.0f;
        Fonts.BOLD.draw(xText, offsetX, textY + 3.0f, 6.0f, new Color(155, 155, 155, 255).getRGB());
        Fonts.BOLD.draw(xValue, offsetX += xTextWidth + 2.0f, textY + 3.0f, 6.0f, new Color(255, 255, 255, 255).getRGB());
        Fonts.TEST.draw("\u00bb", (offsetX += xValueWidth) + 4.0f, textY + 1.5f, 8.0f, ClientTheme.accentWithAlpha(195));
        Fonts.BOLD.draw(yText, offsetX += 12.0f, textY + 3.0f, 6.0f, new Color(155, 155, 155, 255).getRGB());
        Fonts.BOLD.draw(yValue, offsetX += yTextWidth + 2.0f, textY + 3.0f, 6.0f, new Color(255, 255, 255, 255).getRGB());
        Fonts.TEST.draw("\u00bb", (offsetX += yValueWidth) + 4.0f, textY + 1.5f, 8.0f, ClientTheme.accentWithAlpha(195));
        Fonts.BOLD.draw(zText, offsetX += 12.0f, textY + 3.0f, 6.0f, new Color(155, 155, 155, 255).getRGB());
        Fonts.BOLD.draw(zValue, offsetX += zTextWidth + 2.0f, textY + 3.0f, 6.0f, new Color(255, 255, 255, 255).getRGB());
        if (showBps) {
            float bpsBoxX = x + 12.0f + coordsWidth + 4.0f;
            Render2D.gradientRect(bpsBoxX, y + 3.0f, bpsWidth, 20.0f, new int[]{new Color(52, 52, 52, 255).getRGB(), new Color(22, 22, 22, 255).getRGB(), new Color(52, 52, 52, 255).getRGB(), new Color(22, 22, 22, 255).getRGB()}, 5.0f);
            Render2D.outline(bpsBoxX, y + 3.0f, bpsWidth, 20.0f, 0.35f, ClientTheme.blendWithAccentAndAlpha(new Color(90, 90, 90, 255).getRGB(), 0.35f, 255), 5.0f);
            Fonts.ICONSTYPETHO.draw("l", bpsBoxX + 5.0f, textY + 0.5f, 11.0f, ClientTheme.accentWithAlpha(255));
            float bpsOffsetX = bpsBoxX + 20.0f;
            Fonts.TEST.draw("\u00bb", bpsOffsetX, textY + 1.5f, 8.0f, ClientTheme.accentWithAlpha(195));
            Fonts.BOLD.draw(bpsValue, bpsOffsetX += 10.0f, textY + 3.0f, 6.0f, new Color(255, 255, 255, 255).getRGB());
            Fonts.BOLD.draw(bpsText, bpsOffsetX += bpsValueWidth + 2.0f, textY + 3.0f, 6.0f, new Color(155, 155, 155, 255).getRGB());
        }
    }
}

