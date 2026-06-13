package royale.events.impl;

import net.minecraft.item.ItemStack;
import royale.events.api.events.Event;

public class HotbarItemRenderEvent
implements Event {
    private ItemStack stack;
    private final int hotbarIndex;

    public HotbarItemRenderEvent(ItemStack stack, int hotbarIndex) {
        this.stack = stack;
        this.hotbarIndex = hotbarIndex;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public int getHotbarIndex() {
        return this.hotbarIndex;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }
}

