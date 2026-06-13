package royale.screens.clickgui.impl.background.render;

import java.awt.Color;
import royale.modules.module.category.ModuleCategory;
import royale.screens.clickgui.impl.background.search.SearchHandler;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.render.shader.Scissor;
import royale.util.theme.ClientTheme;

public class HeaderRenderer {
    private static final float HEADER_SLIDE_DISTANCE = 8.0F;
    private static final float SEARCH_BOX_X_OFFSET = 302.0F;
    private static final float SEARCH_BOX_Y_OFFSET = 11.5F;
    private static final float SEARCH_BOX_W = 84.0F;
    private static final float SEARCH_BOX_H = 16.5F;

    public void render(
            float bgX,
            float bgY,
            float bgWidth,
            ModuleCategory selectedCategory,
            ModuleCategory previousCategory,
            ModuleCategory currentCategory,
            float headerTransition,
            SearchHandler searchHandler,
            float alphaMultiplier
    ) {
        renderHeaderPanel(bgX, bgY, bgWidth, alphaMultiplier);
        renderSearchBox(bgX, bgY, searchHandler, alphaMultiplier);
        renderCategoryLabel(bgX, bgY, previousCategory, currentCategory, headerTransition, searchHandler, alphaMultiplier);
    }

    private void renderHeaderPanel(float bgX, float bgY, float bgWidth, float alphaMultiplier) {
        int panelAlpha = (int) (28.0F * alphaMultiplier);
        int outlineAlpha = (int) (255.0F * alphaMultiplier);
        Render2D.rect(bgX + 92.0F, bgY + 7.5F, bgWidth - 100.0F, 25.0F, new Color(34, 36, 41, panelAlpha).getRGB(), 8.0F);
        Render2D.outline(bgX + 92.0F, bgY + 7.5F, bgWidth - 100.0F, 25.0F, 0.5F, new Color(70, 74, 84, outlineAlpha).getRGB(), 8.0F);
    }

    private void renderSearchBox(float bgX, float bgY, SearchHandler searchHandler, float alphaMultiplier) {
        float searchBoxX = bgX + SEARCH_BOX_X_OFFSET;
        float searchBoxY = bgY + SEARCH_BOX_Y_OFFSET;
        float searchBoxW = SEARCH_BOX_W;
        float searchBoxH = SEARCH_BOX_H;

        int outlineAlpha = (int) (255.0F * alphaMultiplier);
        int panelAlpha = (int) (32.0F * alphaMultiplier);
        int searchBgAlpha = (int) ((28.0F + searchHandler.getSearchFocusAnimation() * 26.0F) * alphaMultiplier);

        Color passiveOutline = new Color(76, 80, 90, outlineAlpha);
        Color activeOutline = new Color(ClientTheme.accentWithAlpha(outlineAlpha), true);
        Color searchOutline = searchHandler.isSearchActive() ? activeOutline : passiveOutline;

        Render2D.rect(searchBoxX, searchBoxY, searchBoxW, searchBoxH, new Color(27, 30, 35, searchBgAlpha).getRGB(), 5.0F);
        Render2D.outline(searchBoxX, searchBoxY, searchBoxW, searchBoxH, 0.6F, searchOutline.getRGB(), 5.0F);

        float textAreaX = searchBoxX + 6.0F;
        if (searchHandler.isSearchActive() && !searchHandler.getSearchText().isEmpty()) {
            renderSearchText(searchBoxX, searchBoxY, searchBoxW, searchBoxH, textAreaX, searchHandler, alphaMultiplier);
        } else if (searchHandler.isSearchActive()) {
            renderSearchPlaceholder(searchBoxY, searchBoxH, textAreaX, searchHandler, alphaMultiplier, true);
        } else {
            Fonts.BOLD.draw("Поиск модулей...", textAreaX, searchBoxY + 5.5F, 5.0F, new Color(136, 141, 152, outlineAlpha).getRGB());
        }

        float dividerX = searchBoxX + searchBoxW - 17.0F;
        Render2D.rect(dividerX, searchBoxY + 3.7F, 1.0F, searchBoxH - 7.4F, new Color(122, 128, 138, panelAlpha).getRGB(), 8.0F);
        Fonts.ICONS.draw("U", dividerX + 2.5F, searchBoxY + 1.7F, 12.0F, new Color(140, 146, 160, outlineAlpha).getRGB());
    }

    private void renderSearchText(
            float searchBoxX,
            float searchBoxY,
            float searchBoxW,
            float searchBoxH,
            float textAreaX,
            SearchHandler searchHandler,
            float alphaMultiplier
    ) {
        Scissor.enable(searchBoxX + 3.0F, searchBoxY, searchBoxW - 22.0F, searchBoxH, 2.0F);
        if (searchHandler.hasSearchSelection() && searchHandler.getSearchSelectionAnimation() > 0.01F) {
            renderSearchSelection(textAreaX, searchBoxY, searchBoxH, searchHandler, alphaMultiplier);
        }
        Fonts.BOLD.draw(
                searchHandler.getSearchText(),
                textAreaX,
                searchBoxY + 5.5F,
                5.0F,
                new Color(216, 220, 228, (int) (255.0F * alphaMultiplier)).getRGB()
        );
        Scissor.disable();

        if (!searchHandler.hasSearchSelection()) {
            renderSearchCursor(textAreaX, searchBoxY, searchBoxH, searchHandler, alphaMultiplier);
        }
    }

    private void renderSearchSelection(float textAreaX, float searchBoxY, float searchBoxH, SearchHandler searchHandler, float alphaMultiplier) {
        int start = searchHandler.getSearchSelectionStart();
        int end = searchHandler.getSearchSelectionEnd();
        String beforeSelection = searchHandler.getSearchText().substring(0, start);
        String selection = searchHandler.getSearchText().substring(start, end);
        float selectionX = textAreaX + Fonts.BOLD.getWidth(beforeSelection, 5.0F);
        float selectionWidth = Fonts.BOLD.getWidth(selection, 5.0F);
        int selAlpha = (int) (96.0F * searchHandler.getSearchSelectionAnimation() * alphaMultiplier);
        Render2D.rect(selectionX, searchBoxY + 2.0F, selectionWidth, searchBoxH - 4.0F, ClientTheme.accentWithAlpha(selAlpha), 2.0F);
    }

    private void renderSearchCursor(float textAreaX, float searchBoxY, float searchBoxH, SearchHandler searchHandler, float alphaMultiplier) {
        float cursorAlpha = (float) (Math.sin(searchHandler.getSearchCursorBlink() * Math.PI * 2.0D) * 0.5D + 0.5D);
        if (cursorAlpha > 0.3F) {
            String beforeCursor = searchHandler.getSearchText().substring(0, searchHandler.getSearchCursorPosition());
            float cursorX = textAreaX + Fonts.BOLD.getWidth(beforeCursor, 5.0F);
            int cursorAlphaInt = (int) (255.0F * cursorAlpha * alphaMultiplier);
            Render2D.rect(cursorX, searchBoxY + 3.0F, 0.5F, searchBoxH - 6.0F, new Color(186, 190, 198, cursorAlphaInt).getRGB(), 0.0F);
        }
    }

    private void renderSearchPlaceholder(float searchBoxY, float searchBoxH, float textAreaX, SearchHandler searchHandler, float alphaMultiplier, boolean showCursor) {
        Fonts.BOLD.draw("Введите запрос...", textAreaX, searchBoxY + 5.5F, 5.0F, new Color(108, 113, 124, (int) (150.0F * alphaMultiplier)).getRGB());
        if (showCursor) {
            float cursorAlpha = (float) (Math.sin(searchHandler.getSearchCursorBlink() * Math.PI * 2.0D) * 0.5D + 0.5D);
            if (cursorAlpha > 0.3F) {
                int cursorAlphaInt = (int) (255.0F * cursorAlpha * alphaMultiplier);
                Render2D.rect(textAreaX, searchBoxY + 3.0F, 0.5F, searchBoxH - 6.0F, new Color(186, 190, 198, cursorAlphaInt).getRGB(), 0.0F);
            }
        }
    }

    private void renderCategoryLabel(
            float bgX,
            float bgY,
            ModuleCategory previousCategory,
            ModuleCategory currentCategory,
            float headerTransition,
            SearchHandler searchHandler,
            float alphaMultiplier
    ) {
        float baseX = bgX + 100.0F;
        float baseY = bgY + 16.0F;
        float categoryAlpha = searchHandler.getNormalPanelAlpha() * alphaMultiplier;

        if (categoryAlpha > 0.01F) {
            float eased = easeOutQuart(headerTransition);
            if (previousCategory != null && headerTransition < 1.0F) {
                float oldAlpha = (1.0F - eased) * categoryAlpha;
                float oldOffsetY = eased * HEADER_SLIDE_DISTANCE;
                int oldAlphaInt = (int) (128.0F * oldAlpha);
                if (oldAlphaInt > 0) {
                    Fonts.BOLD.draw(previousCategory.getReadableName(), baseX, baseY + oldOffsetY, 7.0F, new Color(132, 132, 138, oldAlphaInt).getRGB());
                }
            }

            if (currentCategory != null) {
                float newAlpha = eased * categoryAlpha;
                float newOffsetY = (1.0F - eased) * -HEADER_SLIDE_DISTANCE;
                int newAlphaInt = (int) (128.0F * newAlpha);
                if (newAlphaInt > 0) {
                    int tinted = ClientTheme.blendWithAccentAndAlpha(new Color(130, 130, 136, 255).getRGB(), 0.65F, newAlphaInt);
                    Fonts.BOLD.draw(currentCategory.getReadableName(), baseX, baseY + newOffsetY, 7.0F, tinted);
                }
            }
        }

        float searchLabelAlpha = searchHandler.getSearchPanelAlpha() * alphaMultiplier;
        if (searchLabelAlpha > 0.01F) {
            int searchLabelAlphaInt = (int) (180.0F * searchLabelAlpha);
            if (searchLabelAlphaInt > 0) {
                String searchLabel = "Результаты поиска";
                String searchText = searchHandler.getSearchText();
                if (!searchText.isEmpty()) {
                    String shown = searchText.length() > 12 ? searchText.substring(0, 12) + "..." : searchText;
                    searchLabel = "\u041d\u0430\u0439\u0434\u0435\u043d\u043e \u043f\u043e \u0437\u0430\u043f\u0440\u043e\u0441\u0443 \"" + shown + "\"";
                }
                Fonts.BOLD.draw(searchLabel, baseX, baseY, 7.0F, new Color(164, 164, 170, searchLabelAlphaInt).getRGB());
            }
        }
    }

    private float easeOutQuart(float x) {
        return 1.0F - (float) Math.pow((1.0F - x), 4.0D);
    }

    public boolean isSearchBoxHovered(double mouseX, double mouseY, float bgX, float bgY) {
        float searchBoxX = bgX + SEARCH_BOX_X_OFFSET;
        float searchBoxY = bgY + SEARCH_BOX_Y_OFFSET;
        return mouseX >= searchBoxX
                && mouseX <= (searchBoxX + SEARCH_BOX_W)
                && mouseY >= searchBoxY
                && mouseY <= (searchBoxY + SEARCH_BOX_H);
    }
}