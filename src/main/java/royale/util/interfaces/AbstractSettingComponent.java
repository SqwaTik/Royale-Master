package royale.util.interfaces;

import java.awt.Color;
import royale.modules.module.setting.Setting;
import royale.util.render.font.Fonts;
import royale.util.render.shader.Scissor;

public abstract class AbstractSettingComponent extends AbstractComponent {
    private static final float DESCRIPTION_TEXT_SIZE = 5.0F;
    private static final float DESCRIPTION_SCROLL_SPEED = 18.0F;
    private static final float DESCRIPTION_SCROLL_PAUSE = 0.8F;
    private final Setting setting;
    protected float alphaMultiplier;

    public AbstractSettingComponent(Setting setting) {
        this.alphaMultiplier = 1.0F;
        this.setting = setting;
    }

    public float getAlphaMultiplier() {
        return this.alphaMultiplier;
    }

    public Setting getSetting() {
        return this.setting;
    }

    public void setAlphaMultiplier(float alpha) {
        this.alphaMultiplier = alpha;
    }

    protected int applyAlpha(int color, float extraAlpha) {
        int a = color >> 24 & 0xFF;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int newAlpha = Math.max(0, Math.min(255, (int) (a * this.alphaMultiplier * extraAlpha)));
        return newAlpha << 24 | r << 16 | g << 8 | b;
    }

    protected int applyAlpha(int color) {
        return applyAlpha(color, 1.0F);
    }

    protected Color applyAlpha(Color color) {
        int newAlpha = Math.max(0, Math.min(255, (int) (color.getAlpha() * this.alphaMultiplier)));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), newAlpha);
    }

    protected Color applyAlpha(Color color, float extraAlpha) {
        int newAlpha = Math.max(0, Math.min(255, (int) (color.getAlpha() * this.alphaMultiplier * extraAlpha)));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), newAlpha);
    }

    protected void drawSettingDescription(String description, float x, float y, float maxWidth) {
        if (description == null || description.isEmpty() || maxWidth <= 2.0F) {
            return;
        }

        String displayText = description.trim();
        if (displayText.isEmpty()) {
            return;
        }

        float textWidth = Fonts.BOLD.getWidth(displayText, DESCRIPTION_TEXT_SIZE);
        float offsetX = 0.0F;
        if (textWidth > maxWidth) {
            float overflow = textWidth - maxWidth;
            float travelDuration = overflow / DESCRIPTION_SCROLL_SPEED;
            float cycleDuration = DESCRIPTION_SCROLL_PAUSE + travelDuration + DESCRIPTION_SCROLL_PAUSE + travelDuration;
            float cycleTime = (System.currentTimeMillis() % (long) (cycleDuration * 1000.0F)) / 1000.0F;

            if (cycleTime <= DESCRIPTION_SCROLL_PAUSE) {
                offsetX = 0.0F;
            } else if (cycleTime <= DESCRIPTION_SCROLL_PAUSE + travelDuration) {
                offsetX = (cycleTime - DESCRIPTION_SCROLL_PAUSE) * DESCRIPTION_SCROLL_SPEED;
            } else if (cycleTime <= DESCRIPTION_SCROLL_PAUSE * 2.0F + travelDuration) {
                offsetX = overflow;
            } else {
                float reverseTime = cycleTime - (DESCRIPTION_SCROLL_PAUSE * 2.0F + travelDuration);
                offsetX = overflow - reverseTime * DESCRIPTION_SCROLL_SPEED;
            }
        }

        Scissor.enable(x, y - 2.0F, maxWidth, 10.0F, 2.0F);
        Fonts.BOLD.draw(displayText, x - offsetX, y, DESCRIPTION_TEXT_SIZE, applyAlpha(new Color(128, 128, 128, 128)).getRGB());
        Scissor.disable();
    }

    protected String trimText(String text, float maxWidth, float size) {
        if (text == null || text.isEmpty() || maxWidth <= 2.0F) {
            return "";
        }
        if (Fonts.BOLD.getWidth(text, size) <= maxWidth) {
            return text;
        }

        String ellipsis = "..";
        if (Fonts.BOLD.getWidth(ellipsis, size) >= maxWidth) {
            return "";
        }

        String trimmed = text;
        while (!trimmed.isEmpty() && Fonts.BOLD.getWidth(trimmed + ellipsis, size) > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed.isEmpty() ? "" : trimmed + ellipsis;
    }
}
