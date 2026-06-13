package royale.screens.clickgui.impl.module.handler;
public class ModuleScrollHandler
{
private double moduleTargetScroll = 0.0D, moduleDisplayScroll = 0.0D; public double getModuleTargetScroll() { return this.moduleTargetScroll; } public double getModuleDisplayScroll() { return this.moduleDisplayScroll; }
private double settingTargetScroll = 0.0D; private double settingDisplayScroll = 0.0D; public double getSettingTargetScroll() { return this.settingTargetScroll; } public double getSettingDisplayScroll() { return this.settingDisplayScroll; }
private float moduleScrollTopFade = 0.0F, moduleScrollBottomFade = 0.0F; public float getModuleScrollTopFade() { return this.moduleScrollTopFade; } public float getModuleScrollBottomFade() { return this.moduleScrollBottomFade; }
private float settingScrollTopFade = 0.0F, settingScrollBottomFade = 0.0F; public float getSettingScrollTopFade() { return this.settingScrollTopFade; } public float getSettingScrollBottomFade() { return this.settingScrollBottomFade; }
private float lastSettingsPanelHeight = 0.0F; public float getLastSettingsPanelHeight() { return this.lastSettingsPanelHeight; }
private float lastModuleListHeight = 0.0F; public float getLastModuleListHeight() { return this.lastModuleListHeight; }
private long lastScrollUpdateTime = System.currentTimeMillis(); private static final float SCROLL_SPEED = 12.0F; public long getLastScrollUpdateTime() { return this.lastScrollUpdateTime; }
private static final float FADE_SPEED = 8.0F;
private static final float CORNER_INSET = 3.0F;
private static final float MODULE_ITEM_HEIGHT = 22.0F;
public void resetModuleScroll() {
this.moduleTargetScroll = this.moduleDisplayScroll = 0.0D;
}
public void resetSettingScroll() {
this.settingTargetScroll = this.settingDisplayScroll = 0.0D;
}
public void update(float delta) {
long currentTime = System.currentTimeMillis();
float deltaTime = Math.min((float)(currentTime - this.lastScrollUpdateTime) / 1000.0F, 0.1F);
this.lastScrollUpdateTime = currentTime;
this.moduleDisplayScroll = smoothScroll(this.moduleDisplayScroll, this.moduleTargetScroll, deltaTime);
this.settingDisplayScroll = smoothScroll(this.settingDisplayScroll, this.settingTargetScroll, deltaTime);
}
private double smoothScroll(double current, double target, float deltaTime) {
double diff = target - current;
if (Math.abs(diff) < 0.5D) return target; 
return current + diff * 12.0D * deltaTime;
}
public void updateFades(int moduleCount, float totalSettingHeight, float moduleListHeight, float settingsPanelHeight) {
this.lastSettingsPanelHeight = settingsPanelHeight;
this.lastModuleListHeight = moduleListHeight;
long currentTime = System.currentTimeMillis();
float deltaTime = Math.min((float)(currentTime - this.lastScrollUpdateTime) / 1000.0F, 0.1F);
float maxModuleScroll = Math.max(0.0F, moduleCount * 24.0F - moduleListHeight + 10.0F);
float maxSettingScroll = Math.max(0.0F, totalSettingHeight - settingsPanelHeight + 45.0F);
this.moduleScrollTopFade = updateFade(this.moduleScrollTopFade, (this.moduleDisplayScroll < -0.5D), deltaTime);
this.moduleScrollBottomFade = updateFade(this.moduleScrollBottomFade, (this.moduleDisplayScroll > (-maxModuleScroll + 0.5F) && maxModuleScroll > 0.0F), deltaTime);
this.settingScrollTopFade = updateFade(this.settingScrollTopFade, (this.settingDisplayScroll < -0.5D), deltaTime);
this.settingScrollBottomFade = updateFade(this.settingScrollBottomFade, (this.settingDisplayScroll > (-maxSettingScroll + 0.5F) && maxSettingScroll > 0.0F), deltaTime);
}
private float updateFade(float current, boolean condition, float deltaTime) {
float target = condition ? 1.0F : 0.0F;
float diff = target - current;
if (Math.abs(diff) < 0.01F) return target; 
return current + diff * 8.0F * deltaTime;
}
public void handleModuleScroll(double vertical, float listHeight, int moduleCount) {
float effectiveHeight = listHeight - 6.0F - 2.0F;
float maxScroll = Math.max(0.0F, moduleCount * 24.0F - effectiveHeight + 10.0F);
this.moduleTargetScroll = Math.max(-maxScroll, Math.min(0.0D, this.moduleTargetScroll + vertical * 25.0D));
}
public void handleSettingScroll(double vertical, float panelHeight, float totalSettingHeight) {
float effectiveHeight = panelHeight - 31.0F - 3.0F - 3.0F;
float maxScroll = Math.max(0.0F, totalSettingHeight - effectiveHeight + 10.0F);
this.settingTargetScroll = Math.max(-maxScroll, Math.min(0.0D, this.settingTargetScroll + vertical * 25.0D));
}
public void scrollToModule(int moduleIndex, int totalModules) {
float moduleY = moduleIndex * 24.0F;
float visibleHeight = this.lastModuleListHeight - 6.0F - 4.0F;
float centerOffset = (visibleHeight - 22.0F) / 2.0F;
float targetScroll = -(moduleY - centerOffset);
float maxScroll = Math.max(0.0F, totalModules * 24.0F - visibleHeight);
targetScroll = Math.max(-maxScroll, Math.min(0.0F, targetScroll));
this.moduleTargetScroll = targetScroll;
}
public void correctSettingScrollPosition(float totalSettingHeight) {
if (this.lastSettingsPanelHeight <= 0.0F)
return; 
float maxScroll = Math.max(0.0F, totalSettingHeight - this.lastSettingsPanelHeight + 45.0F);
if (this.settingTargetScroll < -maxScroll) {
this.settingTargetScroll = -maxScroll;
}
if (this.settingDisplayScroll < -maxScroll)
this.settingDisplayScroll = -maxScroll; 
}
}


