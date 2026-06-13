package royale.screens.clickgui.impl.background.render;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import royale.modules.module.category.ModuleCategory;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.theme.ClientTheme;

public class CategoryRenderer {
    private static final ModuleCategory[] MAIN_CATEGORIES = new ModuleCategory[]{
            ModuleCategory.COMBAT,
            ModuleCategory.MOVEMENT,
            ModuleCategory.RENDER,
            ModuleCategory.PLAYER,
            ModuleCategory.MISC,
            ModuleCategory.MODS
    };

    private static final String[] MAIN_CATEGORY_ICONS = new String[]{"a", "b", "c", "d", "e", "f"};

    private final Map<ModuleCategory, Float> categoryAnimations = new HashMap<>();

    public CategoryRenderer() {
        for (ModuleCategory category : MAIN_CATEGORIES) {
            this.categoryAnimations.put(category, 0.0F);
        }
    }

    public void updateAnimations(ModuleCategory selectedCategory, float deltaTime) {
        for (ModuleCategory category : MAIN_CATEGORIES) {
            updateCategoryAnimation(category, selectedCategory, deltaTime);
        }
    }

    private void updateCategoryAnimation(ModuleCategory category, ModuleCategory selectedCategory, float deltaTime) {
        float target = category == selectedCategory ? 1.0F : 0.0F;
        float current = this.categoryAnimations.getOrDefault(category, 0.0F);
        float diff = target - current;
        float change = diff * 8.0F * deltaTime;
        if (Math.abs(diff) < 0.001F) {
            this.categoryAnimations.put(category, target);
        } else {
            this.categoryAnimations.put(category, current + change);
        }
    }

    public void render(float bgX, float bgY, ModuleCategory selectedCategory, float alphaMultiplier) {
        renderSectionHeader(bgX, bgY + 52.0F, "Категории", alphaMultiplier);
        renderMainCategories(bgX, bgY, alphaMultiplier);
    }

    private void renderSectionHeader(float bgX, float sectionY, String title, float alphaMultiplier) {
        float lineWidth = 18.0F;
        float textWidth = Fonts.BOLD.getWidth(title, 5.0F);
        float totalWidth = 65.0F;
        float textX = bgX + 15.0F + (totalWidth - textWidth) / 2.0F;
        float lineY = sectionY + 3.0F;
        int lineAlpha = (int) (40.0F * alphaMultiplier);
        int textAlpha = (int) (100.0F * alphaMultiplier);
        Render2D.rect(bgX + 15.0F, lineY, lineWidth, 0.5F, (new Color(255, 255, 255, lineAlpha)).getRGB(), 0.0F);
        Render2D.rect(bgX + 15.0F + totalWidth - lineWidth, lineY, lineWidth, 0.5F, (new Color(255, 255, 255, lineAlpha)).getRGB(), 0.0F);
        Fonts.BOLD.draw(title, textX, sectionY, 5.0F, (new Color(150, 150, 150, textAlpha)).getRGB());
    }

    private void renderMainCategories(float bgX, float bgY, float alphaMultiplier) {
        for (int i = 0; i < MAIN_CATEGORIES.length; i++) {
            ModuleCategory category = MAIN_CATEGORIES[i];
            float animation = this.categoryAnimations.getOrDefault(category, 0.0F);
            float textY = bgY + 65.0F + i * 15.0F;
            renderCategoryItem(bgX, textY, category.getReadableName(), MAIN_CATEGORY_ICONS[i], animation, alphaMultiplier);
        }
    }

    private void renderCategoryItem(float bgX, float textY, String name, String icon, float animation, float alphaMultiplier) {
        float offsetX = animation * 5.0F;
        int baseGray = 128;
        int alpha = (int) ((128.0F + 127.0F * animation) * alphaMultiplier);
        int textColor = ClientTheme.blendWithAccentAndAlpha((new Color(baseGray, baseGray, baseGray, 255)).getRGB(), animation, alpha);
        float iconX = bgX + 17.0F + offsetX;
        float iconWidth = Fonts.CATEGORY_ICONS.getWidth(icon, 6.0F);
        float textX = iconX + iconWidth + 4.0F;
        float textWidth = Fonts.BOLD.getWidth(name, 6.0F);
        Fonts.CATEGORY_ICONS.draw(icon, iconX, textY + 0.5F, 6.0F, textColor);
        if (animation > 0.01F) {
            float lineWidth = (iconWidth + 4.0F + textWidth) * animation;
            float lineAlpha = animation * 60.0F * alphaMultiplier;
            Render2D.rect(iconX, textY + 9.0F, lineWidth, 0.5F, ClientTheme.accentWithAlpha((int) lineAlpha), 0.0F);
            float ballAlpha = animation * 200.0F * alphaMultiplier;
            float ballX = bgX + 12.0F;
            float ballY = textY + 2.5F;
            Render2D.rect(ballX, ballY, 3.0F, 3.0F, ClientTheme.accentWithAlpha((int) ballAlpha), 1.5F);
        }
        Fonts.BOLD.draw(name, textX, textY, 6.0F, textColor);
    }

    public ModuleCategory getCategoryAtPosition(double mouseX, double mouseY, float bgX, float bgY) {
        if (mouseX < bgX + 10.0F || mouseX > bgX + 95.0F) {
            return null;
        }
        for (int i = 0; i < MAIN_CATEGORIES.length; i++) {
            float catY = 65.0F + i * 15.0F;
            if (mouseY >= bgY + catY && mouseY <= bgY + catY + 13.0F) {
                return MAIN_CATEGORIES[i];
            }
        }
        return null;
    }
}