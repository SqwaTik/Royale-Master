package royale.events.impl;

import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.screen.Screen;
import royale.IMinecraft;
import royale.events.api.events.Event;

public record KeyEvent(Screen screen, InputUtil.Type type, int key, int action) implements Event,
IMinecraft
{
    public boolean isKeyDown(int key) {
        return this.isKeyDown(key, KeyEvent.mc.currentScreen == null);
    }

    public boolean isKeyDown(int key, boolean screen) {
        return this.key == key && this.action == 1 && screen;
    }

    public boolean isKeyReleased(int key) {
        return this.isKeyReleased(key, KeyEvent.mc.currentScreen == null);
    }

    public boolean isKeyReleased(int key, boolean screen) {
        return this.key == key && this.action == 0 && screen;
    }
}

