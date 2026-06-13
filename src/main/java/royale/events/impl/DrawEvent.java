package royale.events.impl;
import net.minecraft.client.gui.DrawContext;
import royale.events.api.events.Event;
import royale.util.render.draw.DrawEngine;
public class DrawEvent implements Event {
private DrawContext drawContext;
private DrawEngine drawEngine;
private float partialTicks;
public DrawEvent(DrawContext drawContext, DrawEngine drawEngine, float partialTicks) {
this.drawContext = drawContext; this.drawEngine = drawEngine; this.partialTicks = partialTicks;
}
public DrawContext getDrawContext() { return this.drawContext; }
public DrawEngine getDrawEngine() { return this.drawEngine; } public float getPartialTicks() {
return this.partialTicks;
}
}


