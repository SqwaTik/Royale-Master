package royale.screens.clickgui.impl.configs.render;

import java.awt.Color;
import royale.screens.clickgui.impl.configs.handler.ConfigDataHandler;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.theme.ClientTheme;

public class ConfigHeaderRenderer {
    private final ConfigDataHandler dataHandler;

    public ConfigHeaderRenderer(ConfigDataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    public void render(float x, float y, float mouseX, float mouseY, float alpha) {
        alpha = clamp01(alpha);
        Fonts.BOLD.draw("Конфиги", x + 10.0F, y + 10.0F, 7.0F, (new Color(255, 255, 255, clampAlpha(200.0F * alpha))).getRGB());
        renderCreateButton(x, y, mouseX, mouseY, alpha);
        renderSeparator(x, y, alpha);
    }

    private void renderCreateButton(float x, float y, float mouseX, float mouseY, float alpha) {
        float buttonX = x + 298.0F - 70.0F;
        float buttonY = y + 8.0F;
        float buttonW = 60.0F;
        float buttonH = 16.0F;
        boolean hovered = mouseX >= buttonX && mouseX <= buttonX + buttonW && mouseY >= buttonY && mouseY <= buttonY + buttonH;
        int bgAlpha = (int) ((hovered ? 40 : 25) * alpha);
        int outlineAlpha = (int) ((hovered ? 130 : 70) * alpha);
        boolean active = hovered || this.dataHandler.isCreating();
        int outlineColor = active ? ClientTheme.accentWithAlpha(outlineAlpha) : (new Color(100, 100, 100, clampAlpha(outlineAlpha))).getRGB();
        Render2D.rect(buttonX, buttonY, buttonW, buttonH, (new Color(64, 64, 64, clampAlpha(bgAlpha))).getRGB(), 4.0F);
        Render2D.outline(buttonX, buttonY, buttonW, buttonH, 0.5F, outlineColor, 4.0F);
        String text = this.dataHandler.isCreating() ? "Отмена" : "+ Созд";
        float textWidth = Fonts.BOLD.getWidth(text, 5.0F);
        int textColor = active ? ClientTheme.accentWithAlpha(clampAlpha(220.0F * alpha)) : (new Color(180, 180, 180, clampAlpha(255.0F * alpha))).getRGB();
        Fonts.BOLD.draw(text, buttonX + (buttonW - textWidth) / 2.0F, buttonY + 5.5F, 5.0F, textColor);
    }

    private void renderSeparator(float x, float y, float alpha) {
        Render2D.rect(x + 10.0F, y + 28.0F, 278.0F, 0.5F, (new Color(64, 64, 64, clampAlpha(100.0F * alpha))).getRGB(), 0.0F);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, float panelX, float panelY) {
        float buttonX = panelX + 298.0F - 70.0F;
        float buttonY = panelY + 8.0F;
        if (mouseX >= buttonX && mouseX <= (buttonX + 60.0F) && mouseY >= buttonY && mouseY <= (buttonY + 16.0F) && button == 0) {
            this.dataHandler.toggleCreating();
            return true;
        }
        return false;
    }

    private int clampAlpha(float alpha) {
        return Math.max(0, Math.min(255, Math.round(alpha)));
    }

    private float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
