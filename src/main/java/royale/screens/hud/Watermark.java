package royale.screens.hud;

import java.awt.Color;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.client.gui.DrawContext;
import royale.client.draggables.AbstractHudElement;
import royale.modules.impl.render.Hud;
import royale.util.render.BrandMarkRenderer;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.theme.ClientTheme;
import royale.util.tps.TPSCalculate;

public class Watermark
extends AbstractHudElement {
    private String lastFps = "";
    private String oldFps = "";
    private long fpsAnimationStart = 0L;
    private String lastTime = "";
    private String oldTime = "";
    private long timeAnimationStart = 0L;
    private String lastTps = "";
    private String oldTps = "";
    private long tpsAnimationStart = 0L;
    private static final long ANIMATION_DURATION = 200L;
    private static final float ANIMATION_OFFSET = 8.0f;

    public Watermark() {
        super("Watermark", 10, 10, 200, 24, false);
        this.startAnimation();
    }

    @Override
    public void tick() {
    }

    private int clampAlpha(float alpha) {
        return Math.max(0, Math.min(255, (int)(alpha * 255.0f)));
    }

    @Override
    public void drawDraggable(DrawContext context, int alpha) {
        if (alpha <= 0) {
            return;
        }
        float x = 20.0f;
        float y = 5.0f;
        String username = this.mc.getSession().getUsername();
        String fpsNumber = String.valueOf(this.mc.getCurrentFps());
        String fpsText = "fps";
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        boolean showTps = Hud.getInstance() != null && Hud.getInstance().showTps.isValue();
        float tpsValue = 20.0f;
        if (TPSCalculate.getInstance() != null) {
            tpsValue = TPSCalculate.getInstance().getTpsRounded();
        }
        String tpsNumber = String.format("%.1f", Float.valueOf(tpsValue));
        String tpsText = "tps";
        long currentTime = System.currentTimeMillis();
        if (!fpsNumber.equals(this.lastFps)) {
            this.oldFps = this.lastFps;
            this.lastFps = fpsNumber;
            this.fpsAnimationStart = currentTime;
        }
        if (!time.equals(this.lastTime)) {
            this.oldTime = this.lastTime;
            this.lastTime = time;
            this.timeAnimationStart = currentTime;
        }
        if (!tpsNumber.equals(this.lastTps)) {
            this.oldTps = this.lastTps;
            this.lastTps = tpsNumber;
            this.tpsAnimationStart = currentTime;
        }
        float fpsAnimation = Math.min(1.0f, (float)(currentTime - this.fpsAnimationStart) / 200.0f);
        float timeAnimation = Math.min(1.0f, (float)(currentTime - this.timeAnimationStart) / 200.0f);
        float tpsAnimation = Math.min(1.0f, (float)(currentTime - this.tpsAnimationStart) / 200.0f);
        float usernameWidth = Fonts.BOLD.getWidth(username, 6.0f);
        float fpsNumberWidth = Fonts.BOLD.getWidth(fpsNumber, 6.0f);
        float fpsTextWidth = Fonts.BOLD.getWidth(fpsText, 6.0f);
        float timeWidth = Fonts.BOLD.getWidth(time, 6.0f);
        float tpsNumberWidth = Fonts.BOLD.getWidth(tpsNumber, 6.0f);
        float tpsTextWidth = Fonts.BOLD.getWidth(tpsText, 6.0f);
        float totalWidth = 22.0f + usernameWidth + 10.0f + 8.0f + 10.0f + 12.0f + fpsNumberWidth + 2.0f + fpsTextWidth + 10.0f + 8.0f + 10.0f + 12.0f + timeWidth - 18.0f;
        float tpsBoxWidth = 34.0f + tpsNumberWidth + 2.0f + tpsTextWidth + 2.0f;
        if (showTps) {
            this.setWidth((int)(totalWidth + tpsBoxWidth + 30.0f));
        } else {
            this.setWidth((int)(totalWidth + 30.0f));
        }
        this.setHeight(22);
        Render2D.gradientRect(x - 12.0f, y + 3.0f, 20.0f, 20.0f, new int[]{new Color(52, 52, 52, 255).getRGB(), new Color(22, 22, 22, 255).getRGB(), new Color(52, 52, 52, 255).getRGB(), new Color(22, 22, 22, 255).getRGB()}, 5.0f);
        Render2D.outline(x - 12.0f, y + 3.0f, 20.0f, 20.0f, 0.35f, ClientTheme.blendWithAccentAndAlpha(new Color(90, 90, 90, 255).getRGB(), 0.35f, 255), 5.0f);
        Render2D.gradientRect(x + 10.0f, y + 3.0f, totalWidth, 20.0f, new int[]{new Color(52, 52, 52, 255).getRGB(), new Color(22, 22, 22, 255).getRGB(), new Color(52, 52, 52, 255).getRGB(), new Color(22, 22, 22, 255).getRGB()}, 5.0f);
        Render2D.outline(x + 10.0f, y + 3.0f, totalWidth, 20.0f, 0.35f, ClientTheme.blendWithAccentAndAlpha(new Color(90, 90, 90, 255).getRGB(), 0.35f, 255), 5.0f);
        float tpsBoxX = x + 12.0f + totalWidth;
        if (showTps) {
            Render2D.gradientRect(tpsBoxX, y + 3.0f, tpsBoxWidth, 20.0f, new int[]{new Color(52, 52, 52, 255).getRGB(), new Color(22, 22, 22, 255).getRGB(), new Color(52, 52, 52, 255).getRGB(), new Color(22, 22, 22, 255).getRGB()}, 5.0f);
            Render2D.outline(tpsBoxX, y + 3.0f, tpsBoxWidth, 20.0f, 0.35f, ClientTheme.blendWithAccentAndAlpha(new Color(90, 90, 90, 255).getRGB(), 0.35f, 255), 5.0f);
        }
        float textY = y + 7.0f;
        float textX = x + 10.0f;
        float iconCenterX = x - 2.0f;
        float iconCenterY = y + 13.0f;
        BrandMarkRenderer.drawR(iconCenterX, iconCenterY, 13.4f, 255);
        float offsetX = textX + 5.0f;
        Fonts.CATEGORY_ICONS.draw("d", offsetX, textY + 1.0f, 10.0f, new Color(225, 225, 225, 255).getRGB());
        Fonts.BOLD.draw(username, offsetX += 12.0f, textY + 3.0f, 6.0f, new Color(255, 255, 255, 255).getRGB());
        Fonts.TEST.draw("\u00bb", offsetX += usernameWidth + 5.0f, textY + 1.5f, 8.0f, ClientTheme.accentWithAlpha(195));
        Fonts.CATEGORY_ICONS.draw("b", offsetX += 12.0f, textY + 2.5f, 9.0f, new Color(225, 225, 225, 255).getRGB());
        float fpsOffsetX = offsetX += 12.0f;
        this.drawAnimatedTextPerChar(fpsNumber, this.oldFps, fpsOffsetX, textY + 3.0f, 6.0f, fpsAnimation);
        Fonts.BOLD.draw(fpsText, offsetX += fpsNumberWidth + 2.0f, textY + 3.0f, 6.0f, new Color(155, 155, 155, 255).getRGB());
        Fonts.TEST.draw("\u00bb", offsetX += fpsTextWidth + 5.0f, textY + 1.5f, 8.0f, ClientTheme.accentWithAlpha(195));
        Fonts.CATEGORY_ICONS.draw("n", offsetX += 12.0f, textY + 2.5f, 9.0f, new Color(225, 225, 225, 255).getRGB());
        float timeOffsetX = offsetX += 12.0f;
        this.drawAnimatedTextPerChar(time, this.oldTime, timeOffsetX, textY + 3.0f, 6.0f, timeAnimation);
        if (showTps) {
            Fonts.ICONSTYPETHO.draw("t", tpsBoxX + 5.0f, textY, 12.0f, ClientTheme.accentWithAlpha(255));
            float tpsOffsetX = tpsBoxX + 19.0f;
            Fonts.TEST.draw("\u00bb", tpsOffsetX, textY + 1.5f, 8.0f, ClientTheme.accentWithAlpha(195));
            this.drawAnimatedTextPerChar(tpsNumber, this.oldTps, tpsOffsetX += 8.0f, textY + 3.0f, 6.0f, tpsAnimation);
            Fonts.BOLD.draw(tpsText, tpsOffsetX += tpsNumberWidth + 2.0f, textY + 3.0f, 6.0f, new Color(155, 155, 155, 255).getRGB());
        }
    }

    private void drawAnimatedTextPerChar(String newText, String oldText, float x, float y, float size, float progress) {
        if (oldText.isEmpty() || progress >= 1.0f) {
            Fonts.BOLD.draw(newText, x, y, size, new Color(255, 255, 255, 255).getRGB());
            return;
        }
        float offsetX = x;
        int maxLen = Math.max(newText.length(), oldText.length());
        String paddedNew = this.padLeft(newText, maxLen);
        String paddedOld = this.padLeft(oldText, maxLen);
        for (int i = 0; i < paddedNew.length(); ++i) {
            boolean hasChanged;
            char newChar = paddedNew.charAt(i);
            char oldChar = paddedOld.charAt(i);
            if (newChar == ' ' && oldChar == ' ') continue;
            float charWidth = Fonts.BOLD.getWidth(String.valueOf(newChar != ' ' ? newChar : oldChar), size);
            boolean isNewDigit = Character.isDigit(newChar) || newChar == '.';
            boolean isOldDigit = Character.isDigit(oldChar) || oldChar == '.';
            boolean bl = hasChanged = newChar != oldChar;
            if (!hasChanged || !isNewDigit && !isOldDigit) {
                if (newChar != ' ') {
                    Fonts.BOLD.draw(String.valueOf(newChar), offsetX, y, size, new Color(255, 255, 255, 255).getRGB());
                }
            } else {
                float easedProgress = this.easeOutCubic(progress);
                if (oldChar != ' ' && isOldDigit) {
                    float oldAlpha = 1.0f - easedProgress;
                    float oldOffsetY = easedProgress * 8.0f;
                    int oldAlphaClamped = this.clampAlpha(oldAlpha);
                    if (oldAlphaClamped > 0) {
                        int oldColor = new Color(255, 255, 255, oldAlphaClamped).getRGB();
                        Fonts.BOLD.draw(String.valueOf(oldChar), offsetX, y + oldOffsetY, size, oldColor);
                    }
                }
                if (newChar != ' ' && isNewDigit) {
                    float newAlpha = easedProgress;
                    float newOffsetY = (1.0f - easedProgress) * -8.0f;
                    int newAlphaClamped = this.clampAlpha(newAlpha);
                    if (newAlphaClamped > 0) {
                        int newColor = new Color(255, 255, 255, newAlphaClamped).getRGB();
                        Fonts.BOLD.draw(String.valueOf(newChar), offsetX, y + newOffsetY, size, newColor);
                    }
                }
            }
            if (newChar == ' ') continue;
            offsetX += charWidth;
        }
    }

    private String padLeft(String text, int length) {
        if (text.length() >= length) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length - text.length(); ++i) {
            sb.append(' ');
        }
        sb.append(text);
        return sb.toString();
    }

    private float easeOutCubic(float t) {
        return 1.0f - (float)Math.pow(1.0 - (double)t, 3.0);
    }
}

