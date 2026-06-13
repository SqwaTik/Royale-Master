package royale.events.impl;
import net.minecraft.client.gui.screen.Screen;
import royale.events.api.events.Event;
public class SetScreenEvent
implements Event
{
public Screen screen;
public void setScreen(Screen screen) {
this.screen = screen; } public SetScreenEvent(Screen screen) {
this.screen = screen;
}
public Screen getScreen() {
return this.screen;
}
}


