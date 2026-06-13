package royale.util.window;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.StdCallLibrary;
import org.lwjgl.glfw.GLFWNativeWin32;
public class WindowStyle {
public static interface DwmApi extends StdCallLibrary {
public static final DwmApi INSTANCE = (DwmApi)Native.load("dwmapi", DwmApi.class);
WinNT.HRESULT DwmSetWindowAttribute(WinDef.HWND param1HWND, int param1Int1, Pointer param1Pointer, int param1Int2); }
public static void setDarkMode(long windowHandle) {
long hwnd = GLFWNativeWin32.glfwGetWin32Window(windowHandle);
WinDef.HWND hwndJna = new WinDef.HWND(new Pointer(hwnd));
int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;
Memory darkModeEnabled = new Memory(4L);
darkModeEnabled.setInt(0L, 1);
DwmApi.INSTANCE.DwmSetWindowAttribute(hwndJna, DWMWA_USE_IMMERSIVE_DARK_MODE, (Pointer)darkModeEnabled, 4);
}
}


