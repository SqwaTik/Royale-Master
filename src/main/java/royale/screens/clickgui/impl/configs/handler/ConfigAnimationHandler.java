package royale.screens.clickgui.impl.configs.handler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class ConfigAnimationHandler
{
private final Map<String, Float> hoverAnimations = new HashMap<>(); public Map<String, Float> getHoverAnimations() { return this.hoverAnimations; }
private final Map<String, Float> deleteHoverAnimations = new HashMap<>(); public Map<String, Float> getDeleteHoverAnimations() { return this.deleteHoverAnimations; }
private final Map<String, Float> loadHoverAnimations = new HashMap<>(); public Map<String, Float> getLoadHoverAnimations() { return this.loadHoverAnimations; }
private final Map<String, Float> refreshHoverAnimations = new HashMap<>(); public Map<String, Float> getRefreshHoverAnimations() { return this.refreshHoverAnimations; }
private final Map<String, Float> itemAppearAnimations = new HashMap<>(); public Map<String, Float> getItemAppearAnimations() { return this.itemAppearAnimations; }
private float panelAlpha = 0.0F; public float getPanelAlpha() { return this.panelAlpha; }
private float panelSlide = 0.0F; public float getPanelSlide() { return this.panelSlide; }
private float createBoxAnimation = 0.0F; public float getCreateBoxAnimation() { return this.createBoxAnimation; }
private float cursorBlink = 0.0F; public float getCursorBlink() { return this.cursorBlink; }
private float selectedAnimation = 0.0F; public float getSelectedAnimation() { return this.selectedAnimation; }
private long lastUpdateTime = System.currentTimeMillis(); public long getLastUpdateTime() { return this.lastUpdateTime; }
public void reset() {
this.panelAlpha = 0.0F;
this.panelSlide = 0.0F;
this.createBoxAnimation = 0.0F;
this.itemAppearAnimations.clear();
this.hoverAnimations.clear();
this.deleteHoverAnimations.clear();
this.loadHoverAnimations.clear();
this.refreshHoverAnimations.clear();
}
public void initItemAnimations(List<String> configs) {
for (String config : configs) {
if (!this.itemAppearAnimations.containsKey(config)) {
this.itemAppearAnimations.put(config, Float.valueOf(0.0F));
}
} 
}
public void update(boolean isActive, List<String> configs, boolean isCreating) {
long currentTime = System.currentTimeMillis();
float deltaTime = Math.min((float)(currentTime - this.lastUpdateTime) / 1000.0F, 0.1F);
this.lastUpdateTime = currentTime;
updatePanelAnimations(isActive, deltaTime);
updateCreateBoxAnimation(isCreating, deltaTime);
updateCursorBlink(deltaTime);
updateItemAnimations(isActive, configs, deltaTime);
updateHoverAnimations(configs, deltaTime);
}
private void updatePanelAnimations(boolean isActive, float deltaTime) {
float targetPanelAlpha = isActive ? 1.0F : 0.0F;
float alphaDiff = targetPanelAlpha - this.panelAlpha;
this.panelAlpha += alphaDiff * 16.0F * deltaTime;
this.panelAlpha = Math.max(0.0F, Math.min(1.0F, this.panelAlpha));
float targetSlide = isActive ? 1.0F : 0.0F;
float slideDiff = targetSlide - this.panelSlide;
this.panelSlide += slideDiff * 20.0F * deltaTime;
this.panelSlide = Math.max(0.0F, Math.min(1.0F, this.panelSlide));
}
private void updateCreateBoxAnimation(boolean isCreating, float deltaTime) {
float targetCreate = isCreating ? 1.0F : 0.0F;
this.createBoxAnimation += (targetCreate - this.createBoxAnimation) * 14.0F * deltaTime;
}
private void updateCursorBlink(float deltaTime) {
this.cursorBlink += deltaTime * 2.0F;
if (this.cursorBlink > 1.0F) this.cursorBlink--; 
}
private void updateItemAnimations(boolean isActive, List<String> configs, float deltaTime) {
int index = 0;
for (String config : configs) {
float targetAppear, currentAppear = ((Float)this.itemAppearAnimations.getOrDefault(config, Float.valueOf(0.0F))).floatValue();
if (isActive) {
float delay = index * 0.02F;
if (this.panelAlpha > delay) {
targetAppear = 1.0F;
} else {
targetAppear = 0.0F;
} 
} else {
targetAppear = 0.0F;
} 
float speed = isActive ? 20.0F : 16.0F;
float appearDiff = targetAppear - currentAppear;
currentAppear += appearDiff * speed * deltaTime;
this.itemAppearAnimations.put(config, Float.valueOf(Math.max(0.0F, Math.min(1.0F, currentAppear))));
index++;
} 
}
private void updateHoverAnimations(List<String> configs, float deltaTime) {
for (String config : configs) {
float current = ((Float)this.hoverAnimations.getOrDefault(config, Float.valueOf(0.0F))).floatValue();
this.hoverAnimations.put(config, Float.valueOf(current + (0.0F - current) * 8.0F * deltaTime));
float deleteCurrent = ((Float)this.deleteHoverAnimations.getOrDefault(config, Float.valueOf(0.0F))).floatValue();
this.deleteHoverAnimations.put(config, Float.valueOf(deleteCurrent + (0.0F - deleteCurrent) * 8.0F * deltaTime));
float loadCurrent = ((Float)this.loadHoverAnimations.getOrDefault(config, Float.valueOf(0.0F))).floatValue();
this.loadHoverAnimations.put(config, Float.valueOf(loadCurrent + (0.0F - loadCurrent) * 8.0F * deltaTime));
float refreshCurrent = ((Float)this.refreshHoverAnimations.getOrDefault(config, Float.valueOf(0.0F))).floatValue();
this.refreshHoverAnimations.put(config, Float.valueOf(refreshCurrent + (0.0F - refreshCurrent) * 8.0F * deltaTime));
} 
}
public void updateSelectedAnimation(boolean hasSelection, float deltaTime) {
float targetSelected = hasSelection ? 1.0F : 0.0F;
this.selectedAnimation += (targetSelected - this.selectedAnimation) * 8.0F * deltaTime;
}
public void setHoverAnimation(String config, float value) {
this.hoverAnimations.put(config, Float.valueOf(value));
}
public void setDeleteHoverAnimation(String config, float value) {
this.deleteHoverAnimations.put(config, Float.valueOf(value));
}
public void setLoadHoverAnimation(String config, float value) {
this.loadHoverAnimations.put(config, Float.valueOf(value));
}
public void setRefreshHoverAnimation(String config, float value) {
this.refreshHoverAnimations.put(config, Float.valueOf(value));
}
public float getItemAppearAnimation(String config) {
return ((Float)this.itemAppearAnimations.getOrDefault(config, Float.valueOf(0.0F))).floatValue();
}
public float getHoverAnimation(String config) {
return ((Float)this.hoverAnimations.getOrDefault(config, Float.valueOf(0.0F))).floatValue();
}
public float getDeleteHoverAnimation(String config) {
return ((Float)this.deleteHoverAnimations.getOrDefault(config, Float.valueOf(0.0F))).floatValue();
}
public float getLoadHoverAnimation(String config) {
return ((Float)this.loadHoverAnimations.getOrDefault(config, Float.valueOf(0.0F))).floatValue();
}
public float getRefreshHoverAnimation(String config) {
return ((Float)this.refreshHoverAnimations.getOrDefault(config, Float.valueOf(0.0F))).floatValue();
}
public boolean isFullyHidden() {
return (this.panelAlpha < 0.01F && this.panelSlide < 0.01F);
}
}


