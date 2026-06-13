package royale.events.impl;
import net.minecraft.item.ItemStack;
import royale.events.api.events.Event;
public class HeldItemUpdateEvent
implements Event {
private ItemStack mainHand;
private ItemStack offHand;
public void setMainHand(ItemStack mainHand) {
this.mainHand = mainHand; } public void setOffHand(ItemStack offHand) { this.offHand = offHand; } public HeldItemUpdateEvent(ItemStack mainHand, ItemStack offHand) {
this.mainHand = mainHand; this.offHand = offHand;
}
public ItemStack getMainHand() { return this.mainHand; } public ItemStack getOffHand() {
return this.offHand;
}
}


