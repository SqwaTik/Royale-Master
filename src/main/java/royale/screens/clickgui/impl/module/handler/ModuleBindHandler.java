package royale.screens.clickgui.impl.module.handler;
import org.lwjgl.glfw.GLFW;
public class ModuleBindHandler
{
public String getBindDisplayName(int key) {
if (key == -1 || key == -1) return "";
if (key == 1000) return "Up"; 
if (key == 1001) return "Dn"; 
if (key == 1002) return "M3";
String keyName = GLFW.glfwGetKeyName(key, 0);
if (keyName != null) {
return keyName.toUpperCase();
}
switch (key) { case 340: 
case 344: 
case 341: 
case 345: 
case 342: 
case 346: 
case 32: 
case 258: 
case 280: 
case 257: 
case 259: 
case 260: 
case 261: 
case 268: 
case 269: 
case 266: 
case 267: 
case 265: 
case 264: 
case 263: 
case 262: 
case 290: 
case 291: 
case 292: 
case 293: 
case 294: 
case 295: 
case 296: 
case 297: 
case 298: 
case 299: 
case 300: 
case 301:
case 256:
}  return "K" + key;
}
}


