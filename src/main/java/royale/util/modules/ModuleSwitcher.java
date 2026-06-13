package royale.util.modules;
import java.util.List;
import org.lwjgl.glfw.GLFW;
import royale.IMinecraft;
import royale.events.api.EventHandler;
import royale.events.api.EventManager;
import royale.events.impl.KeyEvent;
import royale.modules.module.ModuleStructure;
public class ModuleSwitcher
implements IMinecraft
{
private final List<ModuleStructure> moduleStructures;
public ModuleSwitcher(List<ModuleStructure> moduleStructures, EventManager eventManager) {
this.moduleStructures = moduleStructures;
EventManager.register(this);
}
@EventHandler
public void onKey(KeyEvent event) {
if (event.action() != 1 || mc.currentScreen != null || isAltPressed())
return; 
for (ModuleStructure moduleStructure : this.moduleStructures) {
if (event.key() == moduleStructure.getKey()) {
try {
handleModuleState(moduleStructure, event.action());
} catch (Exception exception) {}
}
} 
}
private boolean isAltPressed() {
if (mc == null || mc.getWindow() == null)
return false; 
long windowHandle = mc.getWindow().getHandle();
if (windowHandle == 0L)
return false; 
return (GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS);
}
private void handleModuleState(ModuleStructure moduleStructure, int action) {
if (moduleStructure.getType() == 1 && action == 1)
moduleStructure.switchState(); 
}
}


