package royale.screens.clickgui.impl.background.render;

import net.minecraft.client.gui.DrawContext;
import royale.util.render.BrandMarkRenderer;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.theme.ClientTheme;

import java.awt.Color;

public class BackgroundRenderer {
    public void render(DrawContext context, float bgX, float bgY, float alphaMultiplier) {
        int baseAlpha = (int) (255.0F * alphaMultiplier);
        int[] gradientColors = {
                new Color(26, 26, 26, baseAlpha).getRGB(),
                new Color(0, 0, 0, baseAlpha).getRGB(),
                new Color(26, 26, 26, baseAlpha).getRGB(),
                new Color(0, 0, 0, baseAlpha).getRGB(),
                new Color(26, 26, 20, baseAlpha).getRGB()
        };
        Render2D.gradientRect(bgX, bgY, 400.0F, 250.0F, gradientColors, 15.0F);
    }

    public void renderCategoryPanel(float bgX, float bgY, float bgHeight, float alphaMultiplier) {
        int panelAlpha = (int) (25.0F * alphaMultiplier);
        int outlineAlpha = (int) (255.0F * alphaMultiplier);
        int blurAlpha = (int) (155.0F * alphaMultiplier);
        Render2D.rect(bgX + 7.5F, bgY + 7.5F, 80.0F, bgHeight - 15.0F, new Color(128, 128, 128, panelAlpha).getRGB(), 10.0F);
        Render2D.outline(bgX + 7.5F, bgY + 7.5F, 80.0F, bgHeight - 15.0F, 0.5F, new Color(55, 55, 55, outlineAlpha).getRGB(), 10.0F);

        float buttonX = bgX + 12.5F;
        float buttonY = bgY + 220.5F;
        float buttonW = 70.0F;
        float buttonH = 17.0F;
        Render2D.outline(buttonX, buttonY, buttonW, buttonH, 0.5F, ClientTheme.accentWithAlpha(outlineAlpha), 5.0F);
        Render2D.blur(buttonX, buttonY, buttonW, buttonH, 4.0F, 5.0F, new Color(25, 25, 25, blurAlpha).getRGB());

        String label = "Конфиги";
        float labelSize = 6.0F;
        float labelWidth = Fonts.BOLD.getWidth(label, labelSize);
        float labelHeight = Fonts.BOLD.getHeight(labelSize);
        float iconSize = 7.2F;
        float iconVisualWidth = iconSize * 0.68F;
        float iconGap = 4.0F;
        float contentWidth = iconVisualWidth + iconGap + labelWidth;
        float contentStartX = buttonX + (buttonW - contentWidth) / 2.0F;
        float contentCenterY = buttonY + buttonH / 2.0F;
        float iconCenterX = contentStartX + iconVisualWidth / 2.0F;
        BrandMarkRenderer.drawR(iconCenterX, contentCenterY, iconSize, (int) (225.0F * alphaMultiplier));

        float textX = contentStartX + iconVisualWidth + iconGap;
        float textY = buttonY + (buttonH - labelHeight) / 2.0F;
        Fonts.BOLD.draw(label, textX, textY, labelSize, ClientTheme.accentWithAlpha((int) (220.0F * alphaMultiplier)));
    }
}