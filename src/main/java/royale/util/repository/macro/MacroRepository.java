package royale.util.repository.macro;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import royale.events.api.EventHandler;
import royale.events.api.EventManager;
import royale.events.impl.KeyEvent;
import royale.util.config.impl.macro.MacroConfig;
public class MacroRepository
{
private static MacroRepository instance;
private final List<Macro> macroList = new ArrayList<>(); public List<Macro> getMacroList() { return this.macroList; }
private final MinecraftClient mc = MinecraftClient.getInstance(); public MinecraftClient getMc() { return this.mc; }
public MacroRepository() {
instance = this;
}
public static MacroRepository getInstance() {
if (instance == null) {
instance = new MacroRepository();
}
return instance;
}
public void init() {
EventManager.register(this);
MacroConfig.getInstance().load();
}
public void addMacro(String name, String message, int key) {
this.macroList.add(new Macro(name, message, key));
}
public void addMacroAndSave(String name, String message, int key) {
addMacro(name, message, key);
MacroConfig.getInstance().save();
}
public boolean hasMacro(String name) {
return this.macroList.stream().anyMatch(macro -> macro.name().equalsIgnoreCase(name));
}
public Optional<Macro> getMacro(String name) {
return this.macroList.stream()
.filter(macro -> macro.name().equalsIgnoreCase(name))
.findFirst();
}
public void deleteMacro(String name) {
this.macroList.removeIf(macro -> macro.name().equalsIgnoreCase(name));
}
public void deleteMacroAndSave(String name) {
deleteMacro(name);
MacroConfig.getInstance().save();
}
public void clearList() {
this.macroList.clear();
}
public void clearListAndSave() {
clearList();
MacroConfig.getInstance().save();
}
public int size() {
return this.macroList.size();
}
public List<String> getMacroNames() {
return (List<String>)this.macroList.stream().map(Macro::name).collect(Collectors.toList());
}
public void setMacros(List<Macro> macros) {
this.macroList.clear();
this.macroList.addAll(macros);
}
@EventHandler
public void onKey(KeyEvent event) {
if (this.mc.player == null || this.mc.currentScreen != null)
return;  if (event.action() != 1)
return; 
if (isAltPressed())
return; 
this.macroList.stream()
.filter(macro -> (macro.key() == event.key()))
.findFirst()
.ifPresent(macro -> {
String message = macro.message();
if (message.startsWith("/")) {
this.mc.player.networkHandler.sendChatCommand(message.substring(1));
} else {
this.mc.player.networkHandler.sendChatMessage(message);
} 
});
}
private boolean isAltPressed() {
if (this.mc == null || this.mc.getWindow() == null)
return false; 
long windowHandle = this.mc.getWindow().getHandle();
if (windowHandle == 0L)
return false; 
return (GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS);
}
}


