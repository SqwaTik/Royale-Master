package royale.screens.hud;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.gui.DrawContext;
import royale.Initialization;
import royale.client.draggables.AbstractHudElement;
import royale.modules.module.ModuleStructure;
import royale.util.animations.Direction;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.render.shader.Scissor;
import royale.util.string.KeyHelper;
import royale.util.theme.ClientTheme;

public class HotKeys
extends AbstractHudElement {
    private List<ModuleStructure> keysList = new ArrayList<ModuleStructure>();
    private long lastKeyChange = 0L;
    private String currentRandomKey = "NONE";
    private float animatedWidth = 80.0f;
    private float animatedHeight = 23.0f;
    private long lastUpdateTime = System.currentTimeMillis();
    private static final float ANIMATION_SPEED = 8.0f;

    public HotKeys() {
        super("HotKeys", 300, 40, 80, 23, true);
        this.stopAnimation();
    }

    @Override
    public boolean visible() {
        return !this.scaleAnimation.isFinished(Direction.BACKWARDS);
    }

    @Override
    public void tick() {
        long currentTime;
        if (Initialization.getInstance() == null || Initialization.getInstance().getManager() == null || Initialization.getInstance().getManager().getModuleProvider() == null) {
            return;
        }
        this.keysList = Initialization.getInstance().getManager().getModuleProvider().getModuleStructures().stream().filter(module -> module.isState() && module.getKey() != -1).toList();
        boolean hasActiveKeys = !this.keysList.isEmpty();
        boolean inChat = this.isChat(this.mc.currentScreen);
        if (hasActiveKeys || inChat) {
            this.startAnimation();
        } else {
            this.stopAnimation();
        }
        if (!hasActiveKeys && inChat && (currentTime = System.currentTimeMillis()) - this.lastKeyChange >= 1000L) {
            List<String> availableKeys = List.of("A", "B", "C", "D", "E");
            this.currentRandomKey = availableKeys.get(new Random().nextInt(availableKeys.size()));
            this.lastKeyChange = currentTime;
        }
    }

    private float lerp(float current, float target, float deltaTime) {
        float factor = (float)(1.0 - Math.pow(0.001, deltaTime * 8.0f));
        return current + (target - current) * factor;
    }

    @Override
    public void drawDraggable(DrawContext context, int alpha) {
        if (alpha <= 0) {
            return;
        }
        float alphaFactor = (float)alpha / 255.0f;
        long currentTime = System.currentTimeMillis();
        float deltaTime = (float)(currentTime - this.lastUpdateTime) / 1000.0f;
        this.lastUpdateTime = currentTime;
        deltaTime = Math.min(deltaTime, 0.1f);
        float x = this.getX();
        float y = this.getY();
        boolean hasActiveKeys = !this.keysList.isEmpty();
        boolean showExample = !hasActiveKeys && this.isChat(this.mc.currentScreen);
        int offset = 23;
        float targetWidth = 80.0f;
        if (showExample) {
            offset += 11;
            String name = "Example Module";
            String bind = "[" + this.currentRandomKey + "]";
            float bindWidth = Fonts.BOLD.getWidth(bind, 6.0f);
            float nameWidth = Fonts.BOLD.getWidth(name, 6.0f);
            targetWidth = Math.max(nameWidth + bindWidth + 50.0f, targetWidth);
        } else {
            for (ModuleStructure module : this.keysList) {
                offset += 11;
                String bind = "[" + KeyHelper.getKeyName(module.getKey()) + "]";
                float bindWidth = Fonts.BOLD.getWidth(bind, 6.0f);
                float nameWidth = Fonts.BOLD.getWidth(module.getName(), 6.0f);
                targetWidth = Math.max(nameWidth + bindWidth + 50.0f, targetWidth);
            }
        }
        float targetHeight = offset + 2;
        this.animatedWidth = this.lerp(this.animatedWidth, targetWidth, deltaTime);
        this.animatedHeight = this.lerp(this.animatedHeight, targetHeight, deltaTime);
        if (Math.abs(this.animatedWidth - targetWidth) < 0.3f) {
            this.animatedWidth = targetWidth;
        }
        if (Math.abs(this.animatedHeight - targetHeight) < 0.3f) {
            this.animatedHeight = targetHeight;
        }
        this.setWidth((int)Math.ceil(this.animatedWidth));
        this.setHeight((int)Math.ceil(this.animatedHeight));
        float contentHeight = this.animatedHeight;
        int bgAlpha = (int)(255.0f * alphaFactor);
        if (contentHeight > 0.0f) {
            Render2D.gradientRect(x, y, this.getWidth(), contentHeight, new int[]{new Color(52, 52, 52, bgAlpha).getRGB(), new Color(32, 32, 32, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(32, 32, 32, bgAlpha).getRGB()}, 5.0f);
            Render2D.outline(x, y, this.getWidth(), contentHeight, 0.35f, ClientTheme.blendWithAccentAndAlpha(new Color(90, 90, 90, 255).getRGB(), 0.35f, bgAlpha), 5.0f);
        }
        Scissor.enable(x, y, this.getWidth(), contentHeight, 2.0f);
        long activeModules = this.keysList.size();
        String moduleCountText = String.valueOf(activeModules);
        float countTextWidth = Fonts.BOLD.getWidth(moduleCountText, 6.0f);
        float activeTextWidth = Fonts.BOLD.getWidth("Active:", 6.0f);
        Render2D.gradientRect(x + (float)this.getWidth() - countTextWidth - activeTextWidth + 2.0f, y + 5.0f, 14.0f, 12.0f, new int[]{new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB()}, 3.0f);
        Fonts.HUD_ICONS.draw("g", x + (float)this.getWidth() - countTextWidth - activeTextWidth + 4.0f, y + 6.0f, 10.0f, ClientTheme.accentWithAlpha(bgAlpha));
        Fonts.BOLD.draw("Binds", x + 8.0f, y + 6.5f, 6.0f, new Color(255, 255, 255, bgAlpha).getRGB());
        int moduleOffset = 23;
        if (showExample) {
            String name = "Example Module";
            String bind = "[" + this.currentRandomKey + "]";
            float bindWidth = Fonts.BOLD.getWidth(bind, 6.0f);
            float bindBoxX = x + (float)this.getWidth() - bindWidth - 11.5f;
            Render2D.gradientRect(bindBoxX, y + (float)moduleOffset - 2.0f, bindWidth + 4.0f, 9.0f, new int[]{new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB()}, 3.0f);
            Render2D.outline(bindBoxX, y + (float)moduleOffset - 2.0f, bindWidth + 4.0f, 9.0f, 0.05f, ClientTheme.accentWithAlpha(bgAlpha), 2.0f);
            Render2D.rect(x + 8.0f, y + (float)moduleOffset - 1.0f, 1.0f, 7.0f, ClientTheme.accentWithAlpha((int)(128.0f * alphaFactor)), 1.0f);
            Fonts.BOLD.draw(name, x + 13.0f, y + (float)moduleOffset - 1.5f, 6.0f, new Color(255, 255, 255, bgAlpha).getRGB());
            Fonts.BOLD.draw(bind, bindBoxX + 2.0f, y + (float)moduleOffset - 1.0f, 6.0f, ClientTheme.accentWithAlpha(bgAlpha));
        } else {
            for (ModuleStructure module : this.keysList) {
                String bind = "[" + KeyHelper.getKeyName(module.getKey()) + "]";
                float bindWidth = Fonts.BOLD.getWidth(bind, 6.0f);
                int textColor = new Color(255, 255, 255, bgAlpha).getRGB();
                int accentColor = ClientTheme.accentWithAlpha(bgAlpha);
                int separatorColor = ClientTheme.accentWithAlpha((int)(128.0f * alphaFactor));
                float bindBoxX = x + (float)this.getWidth() - bindWidth - 11.5f;
                Render2D.gradientRect(bindBoxX, y + (float)moduleOffset - 2.0f, bindWidth + 4.0f, 9.0f, new int[]{new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB()}, 3.0f);
                Render2D.outline(bindBoxX, y + (float)moduleOffset - 2.0f, bindWidth + 4.0f, 9.0f, 0.05f, ClientTheme.accentWithAlpha(bgAlpha), 2.0f);
                Render2D.rect(x + 8.0f, y + (float)moduleOffset - 1.0f, 1.0f, 7.0f, separatorColor, 1.0f);
                Fonts.BOLD.draw(module.getName(), x + 13.0f, y + (float)moduleOffset - 1.5f, 6.0f, textColor);
                Fonts.BOLD.draw(bind, bindBoxX + 2.0f, y + (float)moduleOffset - 1.0f, 6.0f, accentColor);
                moduleOffset += 11;
            }
        }
        Scissor.disable();
    }
}

