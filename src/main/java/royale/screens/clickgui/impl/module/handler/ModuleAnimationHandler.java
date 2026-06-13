package royale.screens.clickgui.impl.module.handler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import royale.modules.module.ModuleStructure;
import royale.util.interfaces.AbstractSettingComponent;
public class ModuleAnimationHandler {
public void setModuleAnimations(Map<ModuleStructure, Float> moduleAnimations) { this.moduleAnimations = moduleAnimations; } public void setModuleAnimStartTimes(Map<ModuleStructure, Long> moduleAnimStartTimes) { this.moduleAnimStartTimes = moduleAnimStartTimes; } public void setOldModuleAnimations(Map<ModuleStructure, Float> oldModuleAnimations) { this.oldModuleAnimations = oldModuleAnimations; } public void setSettingAnimations(Map<AbstractSettingComponent, Float> settingAnimations) { this.settingAnimations = settingAnimations; } public void setSettingAnimStartTimes(Map<AbstractSettingComponent, Long> settingAnimStartTimes) { this.settingAnimStartTimes = settingAnimStartTimes; } public void setVisibilityAnimations(Map<AbstractSettingComponent, Float> visibilityAnimations) { this.visibilityAnimations = visibilityAnimations; } public void setHeightAnimations(Map<AbstractSettingComponent, Float> heightAnimations) { this.heightAnimations = heightAnimations; } public void setHoverAnimations(Map<ModuleStructure, Float> hoverAnimations) { this.hoverAnimations = hoverAnimations; } public void setStateAnimations(Map<ModuleStructure, Float> stateAnimations) { this.stateAnimations = stateAnimations; } public void setSelectedIconAnimations(Map<ModuleStructure, Float> selectedIconAnimations) { this.selectedIconAnimations = selectedIconAnimations; } public void setFavoriteAnimations(Map<ModuleStructure, Float> favoriteAnimations) { this.favoriteAnimations = favoriteAnimations; } public void setPositionAnimations(Map<ModuleStructure, Float> positionAnimations) { this.positionAnimations = positionAnimations; } public void setModuleAlphaAnimations(Map<ModuleStructure, Float> moduleAlphaAnimations) { this.moduleAlphaAnimations = moduleAlphaAnimations; } public void setBindBoxWidthAnimations(Map<ModuleStructure, Float> bindBoxWidthAnimations) { this.bindBoxWidthAnimations = bindBoxWidthAnimations; } public void setBindBoxAlphaAnimations(Map<ModuleStructure, Float> bindBoxAlphaAnimations) { this.bindBoxAlphaAnimations = bindBoxAlphaAnimations; } public void setOldModules(List<ModuleStructure> oldModules) { this.oldModules = oldModules; } public void setOldModuleDisplayScroll(double oldModuleDisplayScroll) { this.oldModuleDisplayScroll = oldModuleDisplayScroll; } public void setSelectedPulseAnimation(float selectedPulseAnimation) { this.selectedPulseAnimation = selectedPulseAnimation; } public void setLastHoverUpdateTime(long lastHoverUpdateTime) { this.lastHoverUpdateTime = lastHoverUpdateTime; } public void setLastStateUpdateTime(long lastStateUpdateTime) { this.lastStateUpdateTime = lastStateUpdateTime; } public void setLastIconUpdateTime(long lastIconUpdateTime) { this.lastIconUpdateTime = lastIconUpdateTime; } public void setLastFavoriteUpdateTime(long lastFavoriteUpdateTime) { this.lastFavoriteUpdateTime = lastFavoriteUpdateTime; } public void setLastBindUpdateTime(long lastBindUpdateTime) { this.lastBindUpdateTime = lastBindUpdateTime; } public void setLastVisibilityUpdateTime(long lastVisibilityUpdateTime) { this.lastVisibilityUpdateTime = lastVisibilityUpdateTime; } public void setHighlightedModule(ModuleStructure highlightedModule) { this.highlightedModule = highlightedModule; } public void setHighlightStartTime(long highlightStartTime) { this.highlightStartTime = highlightStartTime; } public void setHighlightAnimation(float highlightAnimation) { this.highlightAnimation = highlightAnimation; } public void setScrollToModule(boolean scrollToModule) { this.scrollToModule = scrollToModule; } public void setScrollTargetModule(ModuleStructure scrollTargetModule) { this.scrollTargetModule = scrollTargetModule; } public void setCategoryTransitioning(boolean isCategoryTransitioning) { this.isCategoryTransitioning = isCategoryTransitioning; } public void setCategoryTransitionProgress(float categoryTransitionProgress) { this.categoryTransitionProgress = categoryTransitionProgress; } public void setCategoryTransitionStartTime(long categoryTransitionStartTime) { this.categoryTransitionStartTime = categoryTransitionStartTime; }
private Map<ModuleStructure, Float> moduleAnimations = new HashMap<>(); public Map<ModuleStructure, Float> getModuleAnimations() { return this.moduleAnimations; }
private Map<ModuleStructure, Long> moduleAnimStartTimes = new HashMap<>(); public Map<ModuleStructure, Long> getModuleAnimStartTimes() { return this.moduleAnimStartTimes; }
private Map<ModuleStructure, Float> oldModuleAnimations = new HashMap<>(); public Map<ModuleStructure, Float> getOldModuleAnimations() { return this.oldModuleAnimations; }
private Map<AbstractSettingComponent, Float> settingAnimations = new HashMap<>(); public Map<AbstractSettingComponent, Float> getSettingAnimations() { return this.settingAnimations; }
private Map<AbstractSettingComponent, Long> settingAnimStartTimes = new HashMap<>(); public Map<AbstractSettingComponent, Long> getSettingAnimStartTimes() { return this.settingAnimStartTimes; }
private Map<AbstractSettingComponent, Float> visibilityAnimations = new HashMap<>(); public Map<AbstractSettingComponent, Float> getVisibilityAnimations() { return this.visibilityAnimations; }
private Map<AbstractSettingComponent, Float> heightAnimations = new HashMap<>(); public Map<AbstractSettingComponent, Float> getHeightAnimations() { return this.heightAnimations; }
private Map<ModuleStructure, Float> hoverAnimations = new HashMap<>(); public Map<ModuleStructure, Float> getHoverAnimations() { return this.hoverAnimations; }
private Map<ModuleStructure, Float> stateAnimations = new HashMap<>(); public Map<ModuleStructure, Float> getStateAnimations() { return this.stateAnimations; }
private Map<ModuleStructure, Float> selectedIconAnimations = new HashMap<>(); public Map<ModuleStructure, Float> getSelectedIconAnimations() { return this.selectedIconAnimations; }
private Map<ModuleStructure, Float> favoriteAnimations = new HashMap<>(); public Map<ModuleStructure, Float> getFavoriteAnimations() { return this.favoriteAnimations; }
private Map<ModuleStructure, Float> positionAnimations = new HashMap<>(); public Map<ModuleStructure, Float> getPositionAnimations() { return this.positionAnimations; }
private Map<ModuleStructure, Float> moduleAlphaAnimations = new HashMap<>(); public Map<ModuleStructure, Float> getModuleAlphaAnimations() { return this.moduleAlphaAnimations; }
private Map<ModuleStructure, Float> bindBoxWidthAnimations = new HashMap<>(); public Map<ModuleStructure, Float> getBindBoxWidthAnimations() { return this.bindBoxWidthAnimations; }
private Map<ModuleStructure, Float> bindBoxAlphaAnimations = new HashMap<>(); public Map<ModuleStructure, Float> getBindBoxAlphaAnimations() { return this.bindBoxAlphaAnimations; }
private List<ModuleStructure> oldModules = new ArrayList<>(); public List<ModuleStructure> getOldModules() { return this.oldModules; }
private double oldModuleDisplayScroll = 0.0D; public double getOldModuleDisplayScroll() { return this.oldModuleDisplayScroll; }
private float selectedPulseAnimation = 0.0F; public float getSelectedPulseAnimation() { return this.selectedPulseAnimation; }
private long lastHoverUpdateTime = System.currentTimeMillis(); public long getLastHoverUpdateTime() { return this.lastHoverUpdateTime; }
private long lastStateUpdateTime = System.currentTimeMillis(); public long getLastStateUpdateTime() { return this.lastStateUpdateTime; }
private long lastIconUpdateTime = System.currentTimeMillis(); public long getLastIconUpdateTime() { return this.lastIconUpdateTime; }
private long lastFavoriteUpdateTime = System.currentTimeMillis(); public long getLastFavoriteUpdateTime() { return this.lastFavoriteUpdateTime; }
private long lastBindUpdateTime = System.currentTimeMillis(); public long getLastBindUpdateTime() { return this.lastBindUpdateTime; }
private long lastVisibilityUpdateTime = System.currentTimeMillis(); public long getLastVisibilityUpdateTime() { return this.lastVisibilityUpdateTime; }
private ModuleStructure highlightedModule = null; public ModuleStructure getHighlightedModule() { return this.highlightedModule; }
private long highlightStartTime = 0L; public long getHighlightStartTime() { return this.highlightStartTime; }
private float highlightAnimation = 0.0F; public float getHighlightAnimation() { return this.highlightAnimation; }
private boolean scrollToModule = false; public boolean isScrollToModule() {
return this.scrollToModule;
} private ModuleStructure scrollTargetModule = null; public ModuleStructure getScrollTargetModule() { return this.scrollTargetModule; }
private boolean isCategoryTransitioning = false; public boolean isCategoryTransitioning() {
return this.isCategoryTransitioning;
} private float categoryTransitionProgress = 1.0F; public float getCategoryTransitionProgress() { return this.categoryTransitionProgress; }
private long categoryTransitionStartTime = 0L; private static final float MODULE_ANIM_DURATION = 300.0F; public long getCategoryTransitionStartTime() { return this.categoryTransitionStartTime; }
private static final float SETTING_ANIM_DURATION = 450.0F;
private static final float CATEGORY_TRANSITION_DURATION = 280.0F;
private static final float HIGHLIGHT_DURATION = 2000.0F;
private static final float HOVER_ANIM_SPEED = 8.0F;
private static final float STATE_ANIM_SPEED = 10.0F;
private static final float ICON_ANIM_SPEED = 10.0F;
private static final float FAVORITE_ANIM_SPEED = 8.0F;
private static final float POSITION_ANIM_SPEED = 6.0F;
private static final float BIND_WIDTH_ANIM_SPEED = 12.0F;
private static final float PULSE_SPEED = 5.5F;
private static final float VISIBILITY_ANIM_SPEED = 8.0F;
private static final float HEIGHT_ANIM_SPEED = 10.0F;
private static final float CORNER_INSET = 3.0F;
private static final float MODULE_ITEM_HEIGHT = 22.0F;
public void prepareTransition(List<ModuleStructure> modules, List<ModuleStructure> displayModules) {
if (!modules.isEmpty()) {
this.oldModules = new ArrayList<>(modules);
this.oldModuleAnimations = new HashMap<>(this.moduleAnimations);
this.isCategoryTransitioning = true;
this.categoryTransitionStartTime = System.currentTimeMillis();
this.categoryTransitionProgress = 0.0F;
} 
}
public void initModuleAnimations(List<ModuleStructure> displayModules) {
this.moduleAnimations.clear();
this.moduleAnimStartTimes.clear();
this.hoverAnimations.clear();
this.stateAnimations.clear();
this.selectedIconAnimations.clear();
this.bindBoxWidthAnimations.clear();
this.bindBoxAlphaAnimations.clear();
long currentTime = System.currentTimeMillis();
long delayBase = 84L;
for (int i = 0; i < displayModules.size(); i++) {
ModuleStructure mod = displayModules.get(i);
this.moduleAnimations.put(mod, Float.valueOf(0.0F));
this.moduleAnimStartTimes.put(mod, Long.valueOf(currentTime + delayBase + i * 25L));
this.hoverAnimations.put(mod, Float.valueOf(0.0F));
this.stateAnimations.put(mod, Float.valueOf(mod.isState() ? 1.0F : 0.0F));
this.selectedIconAnimations.put(mod, Float.valueOf(0.0F));
this.favoriteAnimations.put(mod, Float.valueOf(mod.isFavorite() ? 1.0F : 0.0F));
this.positionAnimations.put(mod, Float.valueOf(1.0F));
this.moduleAlphaAnimations.put(mod, Float.valueOf(1.0F));
} 
}
public void initSettingAnimations(List<AbstractSettingComponent> settingComponents) {
long currentTime = System.currentTimeMillis();
for (int i = 0; i < settingComponents.size(); i++) {
AbstractSettingComponent comp = settingComponents.get(i);
this.settingAnimations.put(comp, Float.valueOf(0.0F));
this.settingAnimStartTimes.put(comp, Long.valueOf(currentTime + i * 25L));
boolean visible = comp.getSetting().isVisible();
this.visibilityAnimations.put(comp, Float.valueOf(visible ? 1.0F : 0.0F));
this.heightAnimations.put(comp, Float.valueOf(visible ? 1.0F : 0.0F));
} 
}
public void clearSettingAnimations() {
this.settingAnimations.clear();
this.settingAnimStartTimes.clear();
this.visibilityAnimations.clear();
this.heightAnimations.clear();
}
public void updateAll(List<ModuleStructure> displayModules, ModuleStructure selectedModule, ModuleStructure bindingModule, float mouseX, float mouseY, float listX, float listY, float listWidth, float listHeight, float scrollOffset) {
updateCategoryTransition();
updateModuleAnimations(displayModules);
updateStateAnimations(displayModules);
updateSelectedIconAnimations(displayModules, selectedModule);
updateFavoriteAnimations(displayModules);
updateBindAnimations(displayModules, bindingModule);
updateHighlightAnimation();
updateHoverAnimations(displayModules, mouseX, mouseY, listX, listY, listWidth, listHeight, scrollOffset);
}
private void updateCategoryTransition() {
if (!this.isCategoryTransitioning)
return; 
long elapsed = System.currentTimeMillis() - this.categoryTransitionStartTime;
float progress = Math.min(1.0F, (float)elapsed / 280.0F);
this.categoryTransitionProgress = easeOutCubic(progress);
if (progress >= 1.0F) {
this.isCategoryTransitioning = false;
this.oldModules.clear();
this.oldModuleAnimations.clear();
this.categoryTransitionProgress = 1.0F;
} 
}
private void updateModuleAnimations(List<ModuleStructure> displayModules) {
long currentTime = System.currentTimeMillis();
for (ModuleStructure mod : displayModules) {
Long startTime = this.moduleAnimStartTimes.get(mod);
if (startTime == null)
continue; 
float elapsed = (float)(currentTime - startTime.longValue());
float progress = Math.min(1.0F, Math.max(0.0F, elapsed / 300.0F));
progress = easeOutCubic(progress);
this.moduleAnimations.put(mod, Float.valueOf(progress));
} 
}
private void updateStateAnimations(List<ModuleStructure> displayModules) {
long currentTime = System.currentTimeMillis();
float deltaTime = Math.min((float)(currentTime - this.lastStateUpdateTime) / 1000.0F, 0.1F);
this.lastStateUpdateTime = currentTime;
for (ModuleStructure module : displayModules) {
float currentAnim = ((Float)this.stateAnimations.getOrDefault(module, Float.valueOf(module.isState() ? 1.0F : 0.0F))).floatValue();
float targetAnim = module.isState() ? 1.0F : 0.0F;
this.stateAnimations.put(module, Float.valueOf(animateTowards(currentAnim, targetAnim, 10.0F, deltaTime)));
} 
}
private void updateSelectedIconAnimations(List<ModuleStructure> displayModules, ModuleStructure selectedModule) {
long currentTime = System.currentTimeMillis();
float deltaTime = Math.min((float)(currentTime - this.lastIconUpdateTime) / 1000.0F, 0.1F);
this.lastIconUpdateTime = currentTime;
for (ModuleStructure module : displayModules) {
float currentAnim = ((Float)this.selectedIconAnimations.getOrDefault(module, Float.valueOf(0.0F))).floatValue();
float targetAnim = (module == selectedModule) ? 1.0F : 0.0F;
this.selectedIconAnimations.put(module, Float.valueOf(animateTowards(currentAnim, targetAnim, 10.0F, deltaTime)));
} 
}
private void updateFavoriteAnimations(List<ModuleStructure> displayModules) {
long currentTime = System.currentTimeMillis();
float deltaTime = Math.min((float)(currentTime - this.lastFavoriteUpdateTime) / 1000.0F, 0.1F);
this.lastFavoriteUpdateTime = currentTime;
for (ModuleStructure module : displayModules) {
float currentFavAnim = ((Float)this.favoriteAnimations.getOrDefault(module, Float.valueOf(0.0F))).floatValue();
float targetFavAnim = module.isFavorite() ? 1.0F : 0.0F;
this.favoriteAnimations.put(module, Float.valueOf(animateTowards(currentFavAnim, targetFavAnim, 8.0F, deltaTime)));
float currentPosAnim = ((Float)this.positionAnimations.getOrDefault(module, Float.valueOf(1.0F))).floatValue();
if (currentPosAnim < 1.0F) {
this.positionAnimations.put(module, Float.valueOf(Math.min(1.0F, currentPosAnim + 6.0F * deltaTime)));
}
float currentAlphaAnim = ((Float)this.moduleAlphaAnimations.getOrDefault(module, Float.valueOf(1.0F))).floatValue();
if (currentAlphaAnim < 1.0F) {
this.moduleAlphaAnimations.put(module, Float.valueOf(Math.min(1.0F, currentAlphaAnim + 6.0F * deltaTime)));
}
} 
}
private void updateBindAnimations(List<ModuleStructure> displayModules, ModuleStructure bindingModule) {
long currentTime = System.currentTimeMillis();
float deltaTime = Math.min((float)(currentTime - this.lastBindUpdateTime) / 1000.0F, 0.1F);
this.lastBindUpdateTime = currentTime;
for (ModuleStructure module : displayModules) {
int key = module.getKey();
boolean hasBind = (key != -1 && key != -1);
float currentAlpha = ((Float)this.bindBoxAlphaAnimations.getOrDefault(module, Float.valueOf(0.0F))).floatValue();
float targetAlpha = (hasBind || module == bindingModule) ? 1.0F : 0.0F;
this.bindBoxAlphaAnimations.put(module, Float.valueOf(animateTowards(currentAlpha, targetAlpha, 12.0F, deltaTime)));
} 
}
private void updateHoverAnimations(List<ModuleStructure> displayModules, float mouseX, float mouseY, float listX, float listY, float listWidth, float listHeight, float scrollOffset) {
long currentTime = System.currentTimeMillis();
float deltaTime = Math.min((float)(currentTime - this.lastHoverUpdateTime) / 1000.0F, 0.1F);
this.lastHoverUpdateTime = currentTime;
this.selectedPulseAnimation += deltaTime * 5.5F;
if (this.selectedPulseAnimation > 6.283185307179586D) {
this.selectedPulseAnimation -= 6.2831855F;
}
float topInset = 3.0F;
float bottomInset = 3.0F;
float startY = listY + topInset + 2.0F + scrollOffset;
float itemHeight = 22.0F;
float visibleTop = listY + topInset;
float visibleBottom = listY + listHeight - bottomInset;
for (int i = 0; i < displayModules.size(); i++) {
ModuleStructure module = displayModules.get(i);
float modY = startY + i * (itemHeight + 2.0F);
boolean isInVisibleArea = (modY + itemHeight >= visibleTop && modY <= visibleBottom);
boolean isHovered = (!this.isCategoryTransitioning && isInVisibleArea && mouseX >= listX + 3.0F && mouseX <= listX + listWidth - 3.0F && mouseY >= Math.max(modY, visibleTop) && mouseY <= Math.min(modY + itemHeight, visibleBottom) && mouseY >= modY && mouseY <= modY + itemHeight);
float currentHover = ((Float)this.hoverAnimations.getOrDefault(module, Float.valueOf(0.0F))).floatValue();
float targetHover = isHovered ? 1.0F : 0.0F;
this.hoverAnimations.put(module, Float.valueOf(animateTowards(currentHover, targetHover, 8.0F, deltaTime)));
} 
}
private void updateHighlightAnimation() {
if (this.highlightedModule == null)
return; 
long elapsed = System.currentTimeMillis() - this.highlightStartTime;
if ((float)elapsed >= 2000.0F) {
long fadeElapsed = elapsed - 2000L;
float fadeProgress = (float)fadeElapsed / 500.0F;
if (fadeProgress >= 1.0F) {
this.highlightedModule = null;
this.highlightAnimation = 0.0F;
} else {
this.highlightAnimation = 1.0F - fadeProgress;
} 
} else {
this.highlightAnimation = 1.0F;
} 
}
public void updateSettingAnimations(List<AbstractSettingComponent> settingComponents) {
long currentTime = System.currentTimeMillis();
for (AbstractSettingComponent comp : settingComponents) {
Long startTime = this.settingAnimStartTimes.get(comp);
if (startTime == null)
continue; 
float elapsed = (float)(currentTime - startTime.longValue());
float progress = Math.min(1.0F, Math.max(0.0F, elapsed / 450.0F));
progress = easeOutCubic(progress);
this.settingAnimations.put(comp, Float.valueOf(progress));
} 
}
public void updateVisibilityAnimations(List<AbstractSettingComponent> settingComponents) {
long currentTime = System.currentTimeMillis();
float deltaTime = Math.min((float)(currentTime - this.lastVisibilityUpdateTime) / 1000.0F, 0.1F);
this.lastVisibilityUpdateTime = currentTime;
for (AbstractSettingComponent comp : settingComponents) {
boolean isVisible = comp.getSetting().isVisible();
float currentVisAnim = ((Float)this.visibilityAnimations.getOrDefault(comp, Float.valueOf(isVisible ? 1.0F : 0.0F))).floatValue();
float currentHeightAnim = ((Float)this.heightAnimations.getOrDefault(comp, Float.valueOf(isVisible ? 1.0F : 0.0F))).floatValue();
float visTarget = isVisible ? 1.0F : 0.0F;
float heightTarget = isVisible ? 1.0F : 0.0F;
this.heightAnimations.put(comp, Float.valueOf(animateTowards(currentHeightAnim, heightTarget, 10.0F, deltaTime)));
this.visibilityAnimations.put(comp, Float.valueOf(animateTowards(currentVisAnim, visTarget, 8.0F, deltaTime)));
} 
}
public void startHighlight(ModuleStructure module) {
this.highlightedModule = module;
this.highlightStartTime = System.currentTimeMillis();
this.highlightAnimation = 1.0F;
}
public void setScrollTarget(ModuleStructure module) {
this.scrollToModule = true;
this.scrollTargetModule = module;
}
public boolean shouldScrollToModule() {
return this.scrollToModule;
}
public void clearScrollTarget() {
this.scrollToModule = false;
this.scrollTargetModule = null;
}
private float animateTowards(float current, float target, float speed, float deltaTime) {
float diff = target - current;
if (Math.abs(diff) < 0.001F) return target; 
return current + diff * speed * deltaTime;
}
private float easeOutCubic(float x) {
return 1.0F - (float)Math.pow((1.0F - x), 3.0D);
}
public float easeInCubic(float x) {
return x * x * x;
}
public float easeOutQuart(float x) {
return 1.0F - (float)Math.pow((1.0F - x), 4.0D);
}
public float getCategorySlideDistance() {
return 40.0F;
}
}


