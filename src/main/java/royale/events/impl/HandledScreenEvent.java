package royale.events.impl;

import net.minecraft.screen.slot.Slot;
import net.minecraft.client.gui.DrawContext;
import royale.events.api.events.Event;

public class HandledScreenEvent
implements Event {
    private DrawContext drawContext;
    private Slot slotHover;
    private int backgroundWidth;
    private int backgroundHeight;

    public DrawContext getDrawContext() {
        return this.drawContext;
    }

    public Slot getSlotHover() {
        return this.slotHover;
    }

    public int getBackgroundWidth() {
        return this.backgroundWidth;
    }

    public int getBackgroundHeight() {
        return this.backgroundHeight;
    }

    public HandledScreenEvent(DrawContext drawContext, Slot slotHover, int backgroundWidth, int backgroundHeight) {
        this.drawContext = drawContext;
        this.slotHover = slotHover;
        this.backgroundWidth = backgroundWidth;
        this.backgroundHeight = backgroundHeight;
    }
}

