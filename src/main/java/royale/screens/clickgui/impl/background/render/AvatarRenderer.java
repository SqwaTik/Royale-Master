package royale.screens.clickgui.impl.background.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import royale.modules.impl.misc.GifManager;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.render.gif.GifRender;
import royale.util.render.shader.Scissor;

import java.awt.Color;

public class AvatarRenderer {
    private static final MinecraftClient MC = MinecraftClient.getInstance();

    public void render(DrawContext context, float bgX, float bgY, float alphaMultiplier) {
        int alpha = (int) (255.0F * alphaMultiplier);
        int panelAlpha = (int) (105.0F * alphaMultiplier);
        int textAlpha = (int) (200.0F * alphaMultiplier);

        String currentNick = (MC.getSession() != null && !isInvalidProfileValue(MC.getSession().getUsername()))
                ? MC.getSession().getUsername()
                : "Player";
        String username = currentNick;
        String uid = "67";

        GifManager gifManager = GifManager.getInstance();
        int avatarTint = gifManager != null ? withAlpha(gifManager.getAvatarTint(), alpha) : applyAlpha(-1, alpha);
        int backgroundTint = gifManager != null ? withAlpha(gifManager.getBackgroundTint(), alpha) : applyAlpha(-1, alpha);

        GifRender.drawBackground(bgX + 12.5F, bgY + 12.5F, 70.0F, 30.0F, 7.0F, backgroundTint);
        Render2D.rect(bgX + 12.5F, bgY + 12.5F, 70.0F, 30.0F, new Color(0, 0, 0, panelAlpha).getRGB(), 7.0F);
        Render2D.rect(bgX + 15.0F, bgY + 15.0F, 25.0F, 25.0F, new Color(42, 42, 42, alpha).getRGB(), 15.0F);
        GifRender.drawAvatar(bgX + 16.0F, bgY + 16.0F, 23.0F, 23.0F, 15.0F, avatarTint);
        Render2D.rect(bgX + 33.0F, bgY + 33.0F, 5.0F, 5.0F, new Color(0, 255, 0, alpha).getRGB(), 10.0F);

        float textX = bgX + 44.0F;
        float textY = bgY + 22.0F;
        float maxTextWidth = 35.0F;
        float textHeight = 14.0F;
        Scissor.enable(textX, textY - 2.0F, maxTextWidth, textHeight, 2.0F);
        Fonts.BOLD.draw(username, textX, textY, 6.0F, new Color(255, 255, 255, textAlpha).getRGB());
        Fonts.BOLD.draw("UID: " + uid, textX, textY + 7.0F, 5.0F, new Color(255, 255, 255, textAlpha).getRGB());
        Scissor.disable();
    }

    private int applyAlpha(int color, int alpha) {
        return color & 0xFFFFFF | alpha << 24;
    }

    private int withAlpha(int color, int alpha) {
        int baseAlpha = color >>> 24;
        int resolvedAlpha = (baseAlpha <= 0) ? alpha : Math.min(255, baseAlpha * alpha / 255);
        return color & 0xFFFFFF | resolvedAlpha << 24;
    }

    private boolean isInvalidProfileValue(String value) {
        return value == null || value.isBlank() || "null".equalsIgnoreCase(value);
    }
}
