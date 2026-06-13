package royale.screens.clickgui;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.Click;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderTickCounter;
import org.lwjgl.glfw.GLFW;
import royale.IMinecraft;
import royale.Initialization;
import royale.modules.module.ModuleRepository;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.screens.clickgui.impl.DragHandler;
import royale.screens.clickgui.impl.background.BackgroundComponent;
import royale.screens.clickgui.impl.configs.ConfigsRenderer;
import royale.screens.clickgui.impl.module.ModuleComponent;
import royale.screens.clickgui.impl.settingsrender.BindComponent;
import royale.screens.clickgui.impl.settingsrender.TextComponent;
import royale.util.animations.Direction;
import royale.util.animations.GuiAnimation;
import royale.util.interfaces.AbstractSettingComponent;
import royale.util.math.OptionValueUtil;
import royale.util.render.Render2D;
import royale.util.render.gif.GifRender;
import royale.util.render.shader.Scissor;
public class ClickGui extends Screen implements IMinecraft {
public static ClickGui INSTANCE = new ClickGui();
private static final int FIXED_GUI_SCALE = 2;
private final BackgroundComponent background = new BackgroundComponent();
private final ModuleComponent moduleComponent = new ModuleComponent();
private final ConfigsRenderer configsRenderer = new ConfigsRenderer();
private final DragHandler dragHandler = new DragHandler();
private ModuleCategory selectedCategory = ModuleCategory.COMBAT;
private final GuiAnimation openAnimation = new GuiAnimation();
private boolean closing = false;
private float hintAlphaAnimation = 0.0F;
private long lastHintUpdateTime = System.currentTimeMillis();
private static final float HINT_ANIM_SPEED = 6.0F;
private static final float OFFSET_THRESHOLD = 5.0F;
private int lastMouseX;
private int lastMouseY;
private float lastDelta;
public ClickGui() {
super(Text.of("MenuScreen"));
}
public boolean isClosing() {
return this.closing;
}
protected void init() {
super.init();
this.closing = false;
this.openAnimation.setMs(1).setValue(1.0D).setDirection(Direction.FORWARDS).reset();
this.hintAlphaAnimation = 0.0F;
this.lastHintUpdateTime = System.currentTimeMillis();
long handle = mc.getWindow().getHandle();
double centerX = mc.getWindow().getWidth() / 2.0D;
double centerY = mc.getWindow().getHeight() / 2.0D;
GLFW.glfwSetCursorPos(handle, centerX, centerY);
GifRender.init();
this.background.setSearchActive(false);
try {
ModuleRepository repo = Initialization.getInstance().getManager().getModuleRepository();
if (repo != null) {
repo.reloadExternalMods();
}
} catch (Exception ignored) {}
updateModules();
}
private void updateModules() {
List<ModuleStructure> modules = new ArrayList<>();
try {
ModuleRepository repo = Initialization.getInstance().getManager().getModuleRepository();
if (repo != null) {
for (ModuleStructure m : repo.modules()) {
if (m.getCategory() == this.selectedCategory) {
modules.add(m);
}
}
}
} catch (Exception exception) {}
this.moduleComponent.updateModules(modules, this.selectedCategory);
}
public void openGui() {
if (mc.currentScreen == null) {
this.closing = false;
this.openAnimation.setMs(1).setValue(1.0D).setDirection(Direction.FORWARDS).reset();
mc.setScreen(this);
} 
}
public void tick() {
GifRender.tick();
this.moduleComponent.tick();
super.tick();
}
private float[] calculateBackground(float scale) {
int vw = mc.getWindow().getWidth() / 2;
int vh = mc.getWindow().getHeight() / 2;
float bgX = (vw - 400) / 2.0F + this.dragHandler.getOffsetX();
float bgY = (vh - 250) / 2.0F + this.dragHandler.getOffsetY();
return new float[] { bgX, bgY, vw, vh };
}
private boolean isAnyBindListening() {
for (AbstractSettingComponent c : this.moduleComponent.getSettingComponents()) {
if (c instanceof BindComponent) { BindComponent bindComponent = (BindComponent)c; if (bindComponent.isListening())
return true;  }
} 
return false;
}
private void updateHintAnimation() {
long currentTime = System.currentTimeMillis();
float deltaTime = Math.min((float)(currentTime - this.lastHintUpdateTime) / 1000.0F, 0.1F);
this.lastHintUpdateTime = currentTime;
float offsetX = Math.abs(this.dragHandler.getOffsetX());
float offsetY = Math.abs(this.dragHandler.getOffsetY());
boolean shouldShow = (offsetX > 5.0F || offsetY > 5.0F);
float target = shouldShow ? 1.0F : 0.0F;
float diff = target - this.hintAlphaAnimation;
if (Math.abs(diff) < 0.001F) {
this.hintAlphaAnimation = target;
} else {
this.hintAlphaAnimation += diff * 6.0F * deltaTime;
this.hintAlphaAnimation = Math.max(0.0F, Math.min(1.0F, this.hintAlphaAnimation));
} 
}
private boolean isModuleCategory(ModuleCategory category) {
return (category != null && category != ModuleCategory.AUTOBUY);
}
public void render(DrawContext context, int mouseX, int mouseY, float delta) {
this.lastMouseX = mouseX;
this.lastMouseY = mouseY;
this.lastDelta = delta;

}
public void renderOverlay(DrawContext context, RenderTickCounter tickCounter) {
if (mc.getWindow() == null)
return; 
float delta = this.lastDelta;
int mouseX = this.lastMouseX;
int mouseY = this.lastMouseY;
float animValue = this.closing ? this.openAnimation.getOutput().floatValue() : 1.0F;
context.createNewRootLayer();
int dimAlpha = (int)(125.0F * animValue);
if (dimAlpha > 0) {
Render2D.rect(0.0F, 0.0F, 5000.0F, 5000.0F, (new Color(0, 0, 0, dimAlpha)).getRGB(), 0.0F);
}
int guiScale = getCurrentGuiScale();
float scale = 2.0F / guiScale;
float mx = mouseX / scale;
float my = mouseY / scale;
if (!this.closing) {
this.dragHandler.update(mx, my);
}
updateHintAnimation();
context.getMatrices().pushMatrix();
context.getMatrices().scale(scale, scale);
float[] bg = calculateBackground(scale);
float bgX = bg[0];
float bgY = bg[1];
int vw = (int)bg[2];
int vh = (int)bg[3];
float yOffset = (this.closing ? 30.0F : -15.0F) * (1.0F - animValue);
bgY += yOffset;
float alphaMultiplier = animValue;
context.getMatrices().pushMatrix();
this.background.render(context, bgX, bgY, this.selectedCategory, delta, alphaMultiplier);
this.background.renderCategoryPanel(bgX, bgY, alphaMultiplier);
this.background.renderHeader(bgX, bgY, this.selectedCategory, alphaMultiplier);
this.background.renderCategoryNames(bgX, bgY, this.selectedCategory, alphaMultiplier);
float mlX = bgX + 92.0F;
float mlY = bgY + 38.0F;
float mlW = 120.0F;
float mlH = 204.0F;
float spX = bgX + 218.0F;
float spY = bgY + 38.0F;
float spW = 172.0F;
float spH = 204.0F;
float normalAlpha = this.background.getNormalPanelAlpha();
float searchAlpha = this.background.getSearchPanelAlpha();
if (normalAlpha > 0.01F) {
this.configsRenderer.render(context, bgX, bgY, mx, my, delta, 2, alphaMultiplier * normalAlpha, this.selectedCategory);

if (isModuleCategory(this.selectedCategory)) {
this.moduleComponent.updateScroll(delta);
this.moduleComponent.updateScrollFades(mlH, spH);
this.moduleComponent.renderModuleList(context, mlX, mlY, mlW, mlH, mx, my, 2, alphaMultiplier * normalAlpha);
this.moduleComponent.renderSettingsPanel(context, spX, spY, spW, spH, mx, my, delta, 2, alphaMultiplier * normalAlpha);
} 
} 
if (searchAlpha > 0.01F) {
this.background.renderSearchResults(context, bgX, bgY, mx, my, 2, alphaMultiplier);
}
Scissor.reset();
context.getMatrices().popMatrix();
context.getMatrices().popMatrix();
}
public boolean mouseClicked(Click click, boolean doubled) {
if (this.closing) return false;
int guiScale = getCurrentGuiScale();
float scale = 2.0F / guiScale;
double mx = click.x() / scale;
double my = click.y() / scale;
float[] bg = calculateBackground(scale);
float bgX = bg[0];
float bgY = bg[1];
if (this.background.isSearchBoxHovered(mx, my, bgX, bgY) && click.button() == 0) {
this.background.setSearchActive(true);
return true;
} 
if (this.background.isSearchActive()) {
if (click.button() == 0) {
ModuleStructure searchModule = this.background.getSearchModuleAtPosition(mx, my, bgX, bgY);
if (searchModule != null) {
if (searchModule.getCategory() != ModuleCategory.MODS) {
searchModule.switchState();
}
return true;
} 
float panelX = bgX + 92.0F;
float panelY = bgY + 38.0F;
float panelW = 300.0F;
float panelH = 204.0F;
if (mx >= panelX && mx <= (panelX + panelW) && my >= panelY && my <= (panelY + panelH)) {
return true;
}
if (!this.background.isSearchBoxHovered(mx, my, bgX, bgY)) {
this.background.setSearchActive(false);
}
} else if (click.button() == 1) {
ModuleStructure searchModule = this.background.getSearchModuleAtPosition(mx, my, bgX, bgY);
if (searchModule != null) {
this.background.setSearchActive(false);
this.selectedCategory = searchModule.getCategory();
this.moduleComponent.selectModuleFromSearch(searchModule);
updateModules();
return true;
} 
} 
return true;
} 
float mlX = bgX + 92.0F;
float mlY = bgY + 38.0F;
float mlW = 120.0F;
float mlH = 202.0F;
if (click.button() == 2) {
if (isAnyBindListening()) {
for (AbstractSettingComponent c : this.moduleComponent.getSettingComponents()) {
if (c instanceof BindComponent) { BindComponent bindComponent = (BindComponent)c; if (bindComponent.isListening()) {
bindComponent.handleMiddleMouseBind();
return true;
}  }
} 
}
ModuleStructure module = this.moduleComponent.getModuleAtPosition(mx, my, mlX, mlY, mlW, mlH);
if (module != null && this.selectedCategory != ModuleCategory.MODS) {
this.moduleComponent.setBindingModule(module);
return true;
} 
if (this.moduleComponent.getBindingModule() != null) {
this.moduleComponent.setBindingModule(null);
return true;
} 
if (this.dragHandler.startDrag(mx, my, bgX, bgY, 400, 250)) {
return true;
}
} 
if (click.button() == 0 && this.background.isBottomShortcutHovered(mx, my, bgX, bgY)) {
this.selectedCategory = ModuleCategory.AUTOBUY;
updateModules();
return true;
} 
ModuleCategory cat = this.background.getCategoryAtPosition(mx, my, bgX, bgY);
if (cat != null) {
this.selectedCategory = cat;
updateModules();
return true;
} 
if (this.selectedCategory == ModuleCategory.AUTOBUY) {
if (this.configsRenderer.mouseClicked(mx, my, click.button(), doubled, bgX, bgY, this.selectedCategory)) {
return true;
}
return super.mouseClicked(click, doubled);
} 
if (isModuleCategory(this.selectedCategory)) {
if (this.selectedCategory == ModuleCategory.MODS) {
ModuleStructure module = this.moduleComponent.getModuleAtPosition(mx, my, mlX, mlY, mlW, mlH);
float spX = bgX + 218.0F;
float spY = bgY + 38.0F;
float spW = 172.0F;
float spH = 202.0F;
if (mx >= mlX && mx <= (mlX + mlW) && my >= mlY && my <= (mlY + mlH)) {
if (module != null && click.button() == 1) {
this.moduleComponent.selectModule(module);
}
return true;
}
if (mx >= spX && mx <= (spX + spW) && my >= spY && my <= (spY + spH)) {
return true;
}
return super.mouseClicked(click, doubled);
}
ModuleStructure starModule = this.moduleComponent.getModuleForStarClick(mx, my, mlX, mlY, mlW, mlH);
if (starModule != null && click.button() == 0) {
this.moduleComponent.toggleFavorite(starModule);
return true;
} 
ModuleStructure module = this.moduleComponent.getModuleAtPosition(mx, my, mlX, mlY, mlW, mlH);
if (module != null) {
if (click.button() == 0) {
module.switchState();
} else if (click.button() == 1) {
this.moduleComponent.selectModule(module);
} 
return true;
} 
float spX = bgX + 218.0F;
float spY = bgY + 38.0F;
float spW = 172.0F;
float spH = 202.0F;
if (mx >= spX && mx <= (spX + spW) && my >= spY && my <= (spY + spH)) {
for (AbstractSettingComponent c : this.moduleComponent.getSettingComponents()) {
if (c.getSetting().isVisible() && c.mouseClicked(mx, my, click.button())) {
return true;
}
} 
}
} 
return super.mouseClicked(click, doubled);
}
public boolean mouseReleased(Click click) {
if (this.closing) return false;
if (this.selectedCategory == ModuleCategory.AUTOBUY) {
this.configsRenderer.mouseReleased(click.x(), click.y(), click.button());
return super.mouseReleased(click);
} 
for (AbstractSettingComponent c : this.moduleComponent.getSettingComponents()) {
if (c.getSetting().isVisible() && c.mouseReleased(click.x(), click.y(), click.button())) {
return true;
}
} 
return super.mouseReleased(click);
}
public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
if (this.closing) return false;
if (isAnyBindListening()) {
for (AbstractSettingComponent c : this.moduleComponent.getSettingComponents()) {
if (c instanceof BindComponent) { BindComponent bindComponent = (BindComponent)c; if (bindComponent.isListening()) {
bindComponent.handleScrollBind(vertical);
return true;
}  }
} 
}
if (this.moduleComponent.getBindingModule() != null) {
return true;
}
int guiScale = getCurrentGuiScale();
float scale = 2.0F / guiScale;
double mx = mouseX / scale;
double my = mouseY / scale;
float[] bg = calculateBackground(scale);
float bgX = bg[0];
float bgY = bg[1];
if (this.background.isSearchActive()) {
float panelX = bgX + 92.0F;
float panelY = bgY + 38.0F;
float panelW = 300.0F;
float panelH = 204.0F;
if (mx >= panelX && mx <= (panelX + panelW) && my >= panelY && my <= (panelY + panelH)) {
this.background.handleSearchScroll(vertical, panelH);
return true;
} 
} 
if (this.selectedCategory == ModuleCategory.AUTOBUY) {
return this.configsRenderer.mouseScrolled(mx, my, vertical, bgX, bgY, this.selectedCategory);
}
float mlX = bgX + 92.0F;
float mlY = bgY + 38.0F;
float mlW = 120.0F;
float mlH = 202.0F;
if (mx >= mlX && mx <= (mlX + mlW) && my >= mlY && my <= (mlY + mlH)) {
this.moduleComponent.handleModuleScroll(vertical, mlH);
return true;
} 
float spX = bgX + 218.0F;
float spY = bgY + 38.0F;
float spW = 172.0F;
float spH = 202.0F;
if (mx >= spX && mx <= (spX + spW) && my >= spY && my <= (spY + spH)) {
this.moduleComponent.handleSettingScroll(vertical, spH);
return true;
} 
return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
}
public boolean keyPressed(KeyInput input) {
if (this.closing) return false;
ModuleStructure binding = this.moduleComponent.getBindingModule();
if (binding != null) {
if (input.key() == 256 || input.key() == 261 || input.key() == 259) {
binding.setKey(-1);
binding.setType(1);
} else {
binding.setKey(input.key());
} 
this.moduleComponent.setBindingModule(null);
return true;
} 
if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
if (this.configsRenderer.isEditing()) {
this.configsRenderer.cancelEditing();
}
this.background.setSearchActive(false);
close();
return true;
}
if (isModuleCategory(this.selectedCategory)) {
for (AbstractSettingComponent c : this.moduleComponent.getSettingComponents()) {
if (c.getSetting().isVisible() && c.keyPressed(input.key(), input.scancode(), input.modifiers())) {
return true;
}
} 
}
if (this.background.isSearchActive() && 
this.background.handleSearchKey(input.key())) {
return true;
}
if (this.selectedCategory == ModuleCategory.AUTOBUY && this.configsRenderer.keyPressed(input.key(), input.scancode(), input.modifiers())) {
return true;
}
if (this.selectedCategory == ModuleCategory.AUTOBUY) {
return super.keyPressed(input);
}
if (this.dragHandler.isResetNeeded(input.key(), input.modifiers())) {
this.dragHandler.reset();
return true;
} 
return super.keyPressed(input);
}
public boolean charTyped(CharInput input) {
if (this.closing) return false;
if (this.background.isSearchActive() && 
this.background.handleSearchChar((char)input.codepoint())) {
return true;
}
if (this.selectedCategory == ModuleCategory.AUTOBUY && this.configsRenderer.charTyped((char)input.codepoint(), input.modifiers())) {
return true;
}
if (this.selectedCategory == ModuleCategory.AUTOBUY) {
return super.charTyped(input);
}
for (AbstractSettingComponent c : this.moduleComponent.getSettingComponents()) {
if (c.getSetting().isVisible() && c.charTyped((char)input.codepoint(), input.modifiers())) {
return true;
}
} 
return super.charTyped(input);
}
public boolean shouldPause() {
return false;
}
private int getCurrentGuiScale() {
if (mc == null || mc.options == null) {
return FIXED_GUI_SCALE;
}
int requestedScale = OptionValueUtil.toInt(mc.options.getGuiScale().getValue(), 0);
int scale = mc.getWindow().calculateScaleFactor(requestedScale, mc.forcesUnicodeFont());
return Math.max(1, scale);
}
private void finishCloseImmediately() {
if (mc != null && mc.getWindow() != null) {
long handle = mc.getWindow().getHandle();
double centerX = mc.getWindow().getWidth() / 2.0D;
double centerY = mc.getWindow().getHeight() / 2.0D;
GLFW.glfwSetInputMode(handle, 208897, 212995);
GLFW.glfwSetCursorPos(handle, centerX, centerY);
}
TextComponent.typing = false;
this.moduleComponent.setBindingModule(null);
this.background.setSearchActive(false);
this.dragHandler.stopDrag();
if (mc != null) {
mc.setScreen(null);
}
}
public boolean shouldCloseOnEsc() {
return true;
}
public void close() {
if (this.closing) {
return;
}
this.closing = true;
try {
finishCloseImmediately();
} finally {
this.closing = false;
}
}
}




