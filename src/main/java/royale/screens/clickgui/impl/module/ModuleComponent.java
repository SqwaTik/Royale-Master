package royale.screens.clickgui.impl.module;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import royale.IMinecraft;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.SettingComponentAdder;
import royale.screens.clickgui.impl.module.handler.ModuleAnimationHandler;
import royale.screens.clickgui.impl.module.handler.ModuleBindHandler;
import royale.screens.clickgui.impl.module.handler.ModuleFavoriteHandler;
import royale.screens.clickgui.impl.module.handler.ModuleScrollHandler;
import royale.screens.clickgui.impl.module.render.ModuleListRenderer;
import royale.screens.clickgui.impl.module.render.SettingsPanelRenderer;
import royale.screens.clickgui.impl.module.util.ModuleDisplayHelper;
import royale.util.interfaces.AbstractComponent;
import royale.util.interfaces.AbstractSettingComponent;
public class ModuleComponent
implements IMinecraft {
public void setModules(List<ModuleStructure> modules) {
this.modules = modules; } public void setDisplayModules(List<ModuleStructure> displayModules) { this.displayModules = displayModules; } public void setSelectedModule(ModuleStructure selectedModule) { this.selectedModule = selectedModule; } public void setBindingModule(ModuleStructure bindingModule) { this.bindingModule = bindingModule; } public void setSettingComponents(List<AbstractSettingComponent> settingComponents) { this.settingComponents = settingComponents; } public void setCurrentCategory(ModuleCategory currentCategory) { this.currentCategory = currentCategory; } public void setSavedGuiScale(int savedGuiScale) { this.savedGuiScale = savedGuiScale; } public void setLastMouseX(float lastMouseX) { this.lastMouseX = lastMouseX; } public void setLastMouseY(float lastMouseY) { this.lastMouseY = lastMouseY; } public void setLastListX(float lastListX) { this.lastListX = lastListX; } public void setLastListY(float lastListY) { this.lastListY = lastListY; } public void setLastListWidth(float lastListWidth) { this.lastListWidth = lastListWidth; } public void setLastListHeight(float lastListHeight) { this.lastListHeight = lastListHeight; }
private List<ModuleStructure> modules = new ArrayList<>(); public List<ModuleStructure> getModules() { return this.modules; }
private List<ModuleStructure> displayModules = new ArrayList<>(); public List<ModuleStructure> getDisplayModules() { return this.displayModules; }
private ModuleStructure selectedModule = null; public ModuleStructure getSelectedModule() { return this.selectedModule; }
private ModuleStructure bindingModule = null; public ModuleStructure getBindingModule() { return this.bindingModule; }
private List<AbstractSettingComponent> settingComponents = new ArrayList<>(); public List<AbstractSettingComponent> getSettingComponents() { return this.settingComponents; }
private final ModuleAnimationHandler animationHandler; private final ModuleScrollHandler scrollHandler; private final ModuleFavoriteHandler favoriteHandler; private final ModuleBindHandler bindHandler; private ModuleCategory currentCategory = null; private final ModuleListRenderer listRenderer; private final SettingsPanelRenderer settingsRenderer; private final ModuleDisplayHelper displayHelper; public ModuleCategory getCurrentCategory() { return this.currentCategory; }
public ModuleAnimationHandler getAnimationHandler() { return this.animationHandler; }
public ModuleScrollHandler getScrollHandler() { return this.scrollHandler; }
public ModuleFavoriteHandler getFavoriteHandler() { return this.favoriteHandler; }
public ModuleBindHandler getBindHandler() { return this.bindHandler; }
public ModuleListRenderer getListRenderer() { return this.listRenderer; }
public SettingsPanelRenderer getSettingsRenderer() { return this.settingsRenderer; } public ModuleDisplayHelper getDisplayHelper() {
return this.displayHelper;
}
private int savedGuiScale = 1; public int getSavedGuiScale() { return this.savedGuiScale; }
private float lastMouseX = 0.0F, lastMouseY = 0.0F; public float getLastMouseX() { return this.lastMouseX; } public float getLastMouseY() { return this.lastMouseY; }
private float lastListX = 0.0F, lastListY = 0.0F, lastListWidth = 0.0F, lastListHeight = 0.0F; public float getLastListX() { return this.lastListX; } public float getLastListY() { return this.lastListY; } public float getLastListWidth() { return this.lastListWidth; } public float getLastListHeight() { return this.lastListHeight; }
public ModuleComponent() {
this.animationHandler = new ModuleAnimationHandler();
this.scrollHandler = new ModuleScrollHandler();
this.favoriteHandler = new ModuleFavoriteHandler();
this.bindHandler = new ModuleBindHandler();
this.displayHelper = new ModuleDisplayHelper();
this.listRenderer = new ModuleListRenderer(this.bindHandler, this.displayHelper);
this.settingsRenderer = new SettingsPanelRenderer();
}
public void updateModules(List<ModuleStructure> newModules, ModuleCategory category) {
boolean sameCategory = (category == this.currentCategory);
boolean sameSize = (this.modules.size() == newModules.size());
boolean sameModules = sameSize && this.modules.containsAll(newModules) && newModules.containsAll(this.modules);
if (sameCategory && sameModules)
return; 
if (!sameCategory) {
this.animationHandler.prepareTransition(this.modules, this.displayModules);
}
this.currentCategory = category;
this.modules = new ArrayList<>(newModules);
rebuildDisplayList();
this.scrollHandler.resetModuleScroll();
this.animationHandler.initModuleAnimations(this.displayModules);
this.displayHelper.updateModulesWithSettings(this.displayModules);
if (this.animationHandler.shouldScrollToModule() && this.displayModules.contains(this.animationHandler.getScrollTargetModule())) {
scrollToModuleAndHighlight(this.animationHandler.getScrollTargetModule());
this.animationHandler.clearScrollTarget();
} else if (!this.displayModules.isEmpty() && (this.selectedModule == null || !this.displayModules.contains(this.selectedModule))) {
selectModule(this.displayModules.get(0));
} else if (this.displayModules.isEmpty()) {
this.selectedModule = null;
this.settingComponents.clear();
} 
}
private void rebuildDisplayList() {
this.displayModules.clear();
List<ModuleStructure> favorites = new ArrayList<>();
List<ModuleStructure> nonFavorites = new ArrayList<>();
for (ModuleStructure mod : this.modules) {
if (mod.isFavorite()) { favorites.add(mod); continue; }
nonFavorites.add(mod);
} 
this.displayModules.addAll(favorites);
this.displayModules.addAll(nonFavorites);
}
public void toggleFavorite(ModuleStructure module) {
this.favoriteHandler.toggleFavorite(module, this.displayModules, this.animationHandler);
rebuildDisplayList();
}
public void selectModuleFromSearch(ModuleStructure module) {
this.animationHandler.setScrollTarget(module);
}
public void scrollToModuleAndHighlight(ModuleStructure module) {
if (module == null || !this.displayModules.contains(module))
return; 
selectModule(module);
int moduleIndex = this.displayModules.indexOf(module);
if (moduleIndex >= 0 && this.scrollHandler.getLastModuleListHeight() > 0.0F) {
this.scrollHandler.scrollToModule(moduleIndex, this.displayModules.size());
}
this.animationHandler.startHighlight(module);
}
public void selectModule(ModuleStructure module) {
if (module == this.selectedModule)
return; 
this.selectedModule = module;
this.scrollHandler.resetSettingScroll();
this.settingComponents.clear();
this.animationHandler.clearSettingAnimations();
if (module == null)
return; 
(new SettingComponentAdder()).addSettingComponent(module.settings(), this.settingComponents);
this.animationHandler.initSettingAnimations(this.settingComponents);
}
public void renderModuleList(DrawContext context, float x, float y, float width, float height, float mouseX, float mouseY, int guiScale, float alphaMultiplier) {
this.lastMouseX = mouseX;
this.lastMouseY = mouseY;
this.lastListX = x;
this.lastListY = y;
this.lastListWidth = width;
this.lastListHeight = height;
this.animationHandler.updateAll(this.displayModules, this.selectedModule, this.bindingModule, mouseX, mouseY, x, y, width, height, 
(float)this.scrollHandler.getModuleDisplayScroll());
this.listRenderer.render(context, this.displayModules, this.selectedModule, this.bindingModule, x, y, width, height, mouseX, mouseY, guiScale, alphaMultiplier, this.animationHandler, this.scrollHandler);
}
public void renderSettingsPanel(DrawContext context, float x, float y, float width, float height, float mouseX, float mouseY, float delta, int guiScale, float alphaMultiplier) {
this.savedGuiScale = guiScale;
this.settingsRenderer.render(context, this.selectedModule, this.settingComponents, x, y, width, height, mouseX, mouseY, delta, guiScale, alphaMultiplier, this.scrollHandler, this.animationHandler);
}
public void updateScroll(float delta) {
this.scrollHandler.update(delta);
}
public void updateScrollFades(float moduleListHeight, float settingsPanelHeight) {
this.scrollHandler.updateFades(this.displayModules.size(), calculateTotalSettingHeight(), moduleListHeight, settingsPanelHeight);
}
public float calculateTotalSettingHeight() {
return this.settingsRenderer.calculateTotalHeight(this.settingComponents, this.animationHandler);
}
public ModuleStructure getModuleAtPosition(double mouseX, double mouseY, float listX, float listY, float listWidth, float listHeight) {
return this.listRenderer.getModuleAtPosition(this.displayModules, mouseX, mouseY, listX, listY, listWidth, listHeight, this.scrollHandler
.getModuleDisplayScroll(), this.animationHandler.isCategoryTransitioning());
}
public boolean isStarClicked(double mouseX, double mouseY, float listX, float listY, float listWidth, float listHeight) {
return this.listRenderer.isStarClicked(this.displayModules, mouseX, mouseY, listX, listY, listWidth, listHeight, this.scrollHandler
.getModuleDisplayScroll(), this.displayHelper, this.animationHandler.isCategoryTransitioning());
}
public ModuleStructure getModuleForStarClick(double mouseX, double mouseY, float listX, float listY, float listWidth, float listHeight) {
return this.listRenderer.getModuleForStarClick(this.displayModules, mouseX, mouseY, listX, listY, listWidth, listHeight, this.scrollHandler
.getModuleDisplayScroll(), this.displayHelper, this.animationHandler.isCategoryTransitioning());
}
public void handleModuleScroll(double vertical, float listHeight) {
if (this.animationHandler.isCategoryTransitioning())
return;  this.scrollHandler.handleModuleScroll(vertical, listHeight, this.displayModules.size());
}
public void handleSettingScroll(double vertical, float panelHeight) {
this.scrollHandler.handleSettingScroll(vertical, panelHeight, calculateTotalSettingHeight());
}
public void tick() {
this.settingComponents.forEach(AbstractComponent::tick);
}
public boolean isTransitioning() {
return this.animationHandler.isCategoryTransitioning();
}
}



