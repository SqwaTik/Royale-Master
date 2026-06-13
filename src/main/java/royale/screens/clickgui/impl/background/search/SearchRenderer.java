package royale.screens.clickgui.impl.background.search;
import java.awt.Color;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import royale.modules.module.ModuleStructure;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.render.shader.Scissor;
public class SearchRenderer
{
private final SearchHandler searchHandler;
public SearchRenderer(SearchHandler searchHandler) {
this.searchHandler = searchHandler;
}
public void render(DrawContext context, float bgX, float bgY, float bgWidth, float bgHeight, float mouseX, float mouseY, int guiScale, float alphaMultiplier) {
if (this.searchHandler.getSearchPanelAlpha() <= 0.01F)
return; 
float panelX = bgX + 92.0F;
float panelY = bgY + 38.0F;
float panelW = bgWidth - 100.0F;
float panelH = bgHeight - 46.0F;
float resultAlpha = this.searchHandler.getSearchPanelAlpha() * alphaMultiplier;
renderPanelBackground(panelX, panelY, panelW, panelH, resultAlpha);
List<ModuleStructure> results = this.searchHandler.getSearchResults();
if (results.isEmpty()) {
renderEmptyState(panelX, panelY, panelW, panelH, resultAlpha);
return;
} 
Scissor.enable(panelX + 3.0F, panelY + 3.0F, panelW - 6.0F, panelH - 6.0F, 2.0F);
renderResults(panelX, panelY, panelW, panelH, mouseX, mouseY, resultAlpha);
Scissor.disable();
renderScrollIndicators(panelX, panelY, panelW, panelH, resultAlpha);
}
private void renderPanelBackground(float panelX, float panelY, float panelW, float panelH, float resultAlpha) {
int panelBgAlpha = (int)(15.0F * resultAlpha);
int outlineAlpha = (int)(215.0F * resultAlpha);
Render2D.rect(panelX, panelY, panelW, panelH, (new Color(64, 64, 64, panelBgAlpha)).getRGB(), 7.0F);
Render2D.outline(panelX, panelY, panelW, panelH, 0.5F, (new Color(55, 55, 55, outlineAlpha)).getRGB(), 7.0F);
}
private void renderEmptyState(float panelX, float panelY, float panelW, float panelH, float resultAlpha) {
String noResults = this.searchHandler.getSearchText().isEmpty() ? "Start typing to search..." : "No modules found";
float textSize = 6.0F;
float textWidth = Fonts.BOLD.getWidth(noResults, textSize);
float textHeight = Fonts.BOLD.getHeight(textSize);
float centerX = panelX + (panelW - textWidth) / 2.0F;
float centerY = panelY + (panelH - textHeight) / 2.0F;
Fonts.BOLD.draw(noResults, centerX, centerY, textSize, (new Color(100, 100, 100, (int)(150.0F * resultAlpha))).getRGB());
}
private void renderResults(float panelX, float panelY, float panelW, float panelH, float mouseX, float mouseY, float resultAlpha) {
List<ModuleStructure> results = this.searchHandler.getSearchResults();
float startY = panelY + 5.0F + this.searchHandler.getSearchScrollOffset();
float resultHeight = this.searchHandler.getSearchResultHeight();
int newHoveredIndex = -1;
for (int i = 0; i < results.size(); i++) {
ModuleStructure module = results.get(i);
float itemY = startY + i * (resultHeight + 2.0F);
if (itemY + resultHeight >= panelY && itemY <= panelY + panelH) {
float itemAnim = ((Float)this.searchHandler.getSearchResultAnimations().getOrDefault(module, Float.valueOf(0.0F))).floatValue();
float itemAlpha = itemAnim * resultAlpha;
if (itemAlpha > 0.01F) {
float itemOffsetX = (1.0F - itemAnim) * 20.0F;
boolean hovered = (mouseX >= panelX + 5.0F && mouseX <= panelX + panelW - 5.0F && mouseY >= itemY && mouseY <= itemY + resultHeight);
if (hovered) {
newHoveredIndex = i;
}
boolean selected = (module == this.searchHandler.getSelectedSearchModule());
renderResultItem(module, panelX, itemY, panelW, resultHeight, itemOffsetX, itemAlpha, hovered, selected);
} 
} 
} 
this.searchHandler.setHoveredSearchIndex(newHoveredIndex);
}
private void renderResultItem(ModuleStructure module, float panelX, float itemY, float panelW, float resultHeight, float itemOffsetX, float itemAlpha, boolean hovered, boolean selected) {
Color bg;
if (selected) {
bg = new Color(140, 140, 140, (int)(60.0F * itemAlpha));
} else if (hovered) {
bg = new Color(100, 100, 100, (int)(40.0F * itemAlpha));
} else {
bg = new Color(64, 64, 64, (int)(25.0F * itemAlpha));
} 
float itemX = panelX + 5.0F + itemOffsetX;
float itemW = panelW - 10.0F;
Render2D.rect(itemX, itemY, itemW, resultHeight, bg.getRGB(), 5.0F);
if (selected) {
Render2D.outline(itemX, itemY, itemW, resultHeight, 0.5F, (new Color(160, 160, 160, (int)(100.0F * itemAlpha)))
.getRGB(), 5.0F);
}
Color textColor = module.isState() ? new Color(255, 255, 255, (int)(255.0F * itemAlpha)) : new Color(180, 180, 180, (int)(200.0F * itemAlpha));
Fonts.BOLD.draw(module.getName(), itemX + 5.0F, itemY + 3.0F, 6.0F, textColor.getRGB());
String categoryName = module.getCategory().getReadableName();
Color categoryColor = new Color(140, 140, 140, (int)(180.0F * itemAlpha));
Fonts.BOLD.draw(categoryName, itemX + 5.0F, itemY + 11.0F, 4.0F, categoryColor.getRGB());
if (module.isState()) {
float indicatorX = itemX + itemW - 10.0F;
float indicatorY = itemY + resultHeight / 2.0F - 2.0F;
Render2D.rect(indicatorX, indicatorY, 4.0F, 4.0F, (new Color(100, 200, 100, (int)(200.0F * itemAlpha)))
.getRGB(), 2.0F);
} 
}
private void renderScrollIndicators(float panelX, float panelY, float panelW, float panelH, float resultAlpha) {
List<ModuleStructure> results = this.searchHandler.getSearchResults();
float resultHeight = this.searchHandler.getSearchResultHeight();
float maxScroll = Math.max(0.0F, results.size() * (resultHeight + 2.0F) - panelH + 10.0F);
if (maxScroll > 0.0F) {
if (this.searchHandler.getSearchScrollOffset() < -0.5F) {
for (int i = 0; i < 10; i++) {
float fadeAlpha = 60.0F * resultAlpha * (1.0F - i / 10.0F);
Render2D.rect(panelX + 3.0F, panelY + 3.0F + i, panelW - 6.0F, 1.0F, (new Color(20, 20, 20, (int)fadeAlpha))
.getRGB(), 0.0F);
} 
}
if (this.searchHandler.getSearchScrollOffset() > -maxScroll + 0.5F) {
for (int i = 0; i < 10; i++) {
float fadeAlpha = 60.0F * resultAlpha * i / 10.0F;
Render2D.rect(panelX + 3.0F, panelY + panelH - 13.0F + i, panelW - 6.0F, 1.0F, (new Color(20, 20, 20, (int)fadeAlpha))
.getRGB(), 0.0F);
} 
}
} 
}
public ModuleStructure getModuleAtPosition(double mouseX, double mouseY, float bgX, float bgY, float bgWidth, float bgHeight, SearchHandler handler) {
if (!handler.isSearchActive() || handler.getSearchResults().isEmpty()) return null;
float panelX = bgX + 92.0F;
float panelY = bgY + 38.0F;
float panelW = bgWidth - 100.0F;
float panelH = bgHeight - 46.0F;
if (mouseX < (panelX + 5.0F) || mouseX > (panelX + panelW - 5.0F) || mouseY < panelY || mouseY > (panelY + panelH)) {
return null;
}
float startY = panelY + 5.0F + handler.getSearchScrollOffset();
float resultHeight = handler.getSearchResultHeight();
List<ModuleStructure> results = handler.getSearchResults();
for (int i = 0; i < results.size(); i++) {
float itemY = startY + i * (resultHeight + 2.0F);
if (mouseY >= itemY && mouseY <= (itemY + resultHeight)) {
return results.get(i);
}
} 
return null;
}
}


