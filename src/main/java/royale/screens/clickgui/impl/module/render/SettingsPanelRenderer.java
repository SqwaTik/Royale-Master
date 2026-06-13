package royale.screens.clickgui.impl.module.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import royale.modules.module.ModuleStructure;
import royale.screens.clickgui.impl.module.handler.ModuleAnimationHandler;
import royale.screens.clickgui.impl.module.handler.ModuleScrollHandler;
import royale.screens.clickgui.impl.settingsrender.ColorComponent;
import royale.screens.clickgui.impl.settingsrender.MultiSelectComponent;
import royale.screens.clickgui.impl.settingsrender.SelectComponent;
import royale.util.interfaces.AbstractSettingComponent;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.render.shader.Scissor;
import royale.util.theme.ClientTheme;

public class SettingsPanelRenderer {
    private static final float SETTINGS_PANEL_CORNER_RADIUS = 7.0F;
    private static final float CORNER_INSET = 3.0F;
    private static final int SETTING_HEIGHT = 16;
    private static final int SETTING_SPACING = 2;

    public void render(
            DrawContext context,
            ModuleStructure selectedModule,
            List<AbstractSettingComponent> settingComponents,
            float x,
            float y,
            float width,
            float height,
            float mouseX,
            float mouseY,
            float delta,
            int guiScale,
            float alphaMultiplier,
            ModuleScrollHandler scrollHandler,
            ModuleAnimationHandler animHandler
    ) {
        animHandler.updateSettingAnimations(settingComponents);
        animHandler.updateVisibilityAnimations(settingComponents);

        int panelAlpha = (int) (15.0F * alphaMultiplier);
        int outlineAlpha = (int) (215.0F * alphaMultiplier);
        Render2D.rect(x, y, width, height, (new Color(64, 64, 64, panelAlpha)).getRGB(), SETTINGS_PANEL_CORNER_RADIUS);
        Render2D.outline(
                x,
                y,
                width,
                height,
                0.5F,
                ClientTheme.blendWithAccentAndAlpha((new Color(55, 55, 55, 255)).getRGB(), 0.2F, outlineAlpha),
                SETTINGS_PANEL_CORNER_RADIUS
        );

        if (selectedModule == null) {
            String text = "Выберите модуль";
            float textSize = 6.0F;
            float textWidth = Fonts.BOLD.getWidth(text, textSize);
            float textHeight = Fonts.BOLD.getHeight(textSize);
            float centerX = x + (width - textWidth) / 2.0F;
            float centerY = y + (height - textHeight) / 2.0F;
            Fonts.BOLD.draw(
                    text,
                    centerX,
                    centerY,
                    textSize,
                    (new Color(100, 100, 100, (int) (150.0F * alphaMultiplier))).getRGB()
            );
            return;
        }

        Fonts.BOLD.draw(
                selectedModule.getName(),
                x + 8.0F,
                y + 8.0F,
                7.0F,
                (new Color(255, 255, 255, (int) (200.0F * alphaMultiplier))).getRGB()
        );

        Render2D.rect(x + 8.0F, y + 30.0F, width - 16.0F, 1.25F, (new Color(64, 64, 64, (int) (64.0F * alphaMultiplier))).getRGB(), 10.0F);

        float sideInset = CORNER_INSET;
        float bottomInset = 6.0F;
        float clipY = y + 31.0F;
        float clipH = height - 26.0F - bottomInset;
        float clipX = x + sideInset;
        float clipW = width - sideInset * 2.0F;

        Scissor.enable(clipX, clipY, clipW, clipH, guiScale);

        List<Float> finalYPositions = new ArrayList<>();
        List<Float> animatedHeights = new ArrayList<>();
        float posY = y + 38.0F + (float) scrollHandler.getSettingDisplayScroll();

        for (AbstractSettingComponent component : settingComponents) {
            float heightAnim = animHandler.getHeightAnimations().getOrDefault(
                    component,
                    component.getSetting().isVisible() ? 1.0F : 0.0F
            );
            if (heightAnim <= 0.001F) {
                finalYPositions.add(null);
                animatedHeights.add(0.0F);
                continue;
            }

            finalYPositions.add(posY);
            float baseHeight = getComponentBaseHeight(component);
            float layoutHeight = baseHeight * heightAnim;
            animatedHeights.add(layoutHeight);
            posY += layoutHeight + SETTING_SPACING * heightAnim;
        }

        float visibleTop = clipY;
        float visibleBottom = clipY + clipH;

        for (int i = 0; i < settingComponents.size(); i++) {
            AbstractSettingComponent component = settingComponents.get(i);
            Float startY = finalYPositions.get(i);
            if (startY == null) {
                continue;
            }

            float visAnim = animHandler.getVisibilityAnimations().getOrDefault(
                    component,
                    component.getSetting().isVisible() ? 1.0F : 0.0F
            );
            float heightAnim = animHandler.getHeightAnimations().getOrDefault(
                    component,
                    component.getSetting().isVisible() ? 1.0F : 0.0F
            );

            if (visAnim <= 0.001F && heightAnim <= 0.001F) {
                continue;
            }

            float animatedHeight = animatedHeights.get(i);
            float progress = animHandler.getSettingAnimations().getOrDefault(component, 1.0F);
            float componentAlpha = progress * visAnim * alphaMultiplier;

            component.position(x + 8.0F, startY);
            component.size(width - 16.0F, SETTING_HEIGHT);
            component.setAlphaMultiplier(componentAlpha);

            if (startY + animatedHeight >= visibleTop && startY <= visibleBottom && componentAlpha > 0.01F) {
                float itemClipTop = Math.max(startY, visibleTop);
                float itemClipBottom = Math.min(startY + animatedHeight, visibleBottom);
                float itemClipHeight = itemClipBottom - itemClipTop;

                if (itemClipHeight > 0.5F) {
                    Scissor.enable(clipX, itemClipTop, clipW, itemClipHeight, guiScale);
                    context.getMatrices().pushMatrix();
                    component.render(context, (int) mouseX, (int) mouseY, delta);
                    context.getMatrices().popMatrix();
                    Scissor.disable();
                }
            }
        }

        Scissor.disable();

        boolean hasVisibleSettings = false;
        for (AbstractSettingComponent component : settingComponents) {
            float visAnim = animHandler.getVisibilityAnimations().getOrDefault(component, 0.0F);
            if (visAnim > 0.01F) {
                hasVisibleSettings = true;
                break;
            }
        }

        if (!hasVisibleSettings) {
            String text = "У этого модуля нет настроек";
            float textSize = 6.0F;
            float textWidth = Fonts.BOLD.getWidth(text, textSize);
            float textHeight = Fonts.BOLD.getHeight(textSize);
            float centerX = x + (width - textWidth) / 2.0F;
            float centerY = y + (height - textHeight) / 2.0F + 10.0F;
            Fonts.BOLD.draw(
                    text,
                    centerX,
                    centerY,
                    textSize,
                    (new Color(100, 100, 100, (int) (150.0F * alphaMultiplier))).getRGB()
            );
        }

        renderScrollFade(
                x + sideInset,
                clipY,
                width - sideInset * 2.0F,
                clipH,
                scrollHandler.getSettingScrollTopFade() * alphaMultiplier,
                scrollHandler.getSettingScrollBottomFade() * alphaMultiplier,
                60,
                12
        );
    }

    public float calculateTotalHeight(List<AbstractSettingComponent> settingComponents, ModuleAnimationHandler animHandler) {
        float total = 0.0F;
        for (AbstractSettingComponent component : settingComponents) {
            float heightAnim = animHandler.getHeightAnimations().getOrDefault(
                    component,
                    component.getSetting().isVisible() ? 1.0F : 0.0F
            );
            if (heightAnim <= 0.001F) {
                continue;
            }

            float baseHeight = getComponentBaseHeight(component);
            total += (baseHeight + SETTING_SPACING) * heightAnim;
        }
        return total;
    }

    private float getComponentBaseHeight(AbstractSettingComponent component) {
        if (component instanceof SelectComponent) {
            return ((SelectComponent) component).getTotalHeight();
        }
        if (component instanceof MultiSelectComponent) {
            return ((MultiSelectComponent) component).getTotalHeight();
        }
        if (component instanceof ColorComponent) {
            return ((ColorComponent) component).getTotalHeight();
        }
        return SETTING_HEIGHT;
    }

    private void renderScrollFade(float x, float y, float w, float h, float topFade, float bottomFade, int alpha, int size) {
        if (topFade > 0.01F) {
            for (int i = 0; i < size; i++) {
                float fadeAlpha = alpha * topFade * (1.0F - i / (float) size);
                Render2D.rect(x, y + i, w, 1.0F, (new Color(20, 20, 20, (int) fadeAlpha)).getRGB(), 0.0F);
            }
        }

        if (bottomFade > 0.01F) {
            for (int i = 0; i < size; i++) {
                float fadeAlpha = alpha * bottomFade * i / (float) size;
                Render2D.rect(x, y + h - size + i, w, 1.0F, (new Color(20, 20, 20, (int) fadeAlpha)).getRGB(), 0.0F);
            }
        }
    }
}
