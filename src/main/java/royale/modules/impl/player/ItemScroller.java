package royale.modules.impl.player;

import java.util.stream.Stream;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import royale.events.api.EventHandler;
import royale.events.impl.ClickSlotEvent;
import royale.events.impl.HandledScreenEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.util.inventory.InventoryUtils;
import royale.util.string.PlayerInteractionHelper;
import royale.util.timer.StopWatch;

public class ItemScroller extends ModuleStructure {
    private final StopWatch stopWatch = new StopWatch();
    private final SliderSettings scrollerSetting = (new SliderSettings("Задержка прокрутки", "Интервал между переносом предметов"))
            .setValue(50.0F)
            .range(0, 200);

    public ItemScroller() {
        super("ItemScroller", "Быстро переносит одинаковые предметы в контейнерах", ModuleCategory.PLAYER);
        settings(new Setting[]{this.scrollerSetting});
    }

    @EventHandler
    public void onHandledScreen(HandledScreenEvent event) {
        if (mc.player == null) {
            return;
        }

        Slot hoverSlot = event.getSlotHover();
        SlotActionType actionType = getActionType();
        if (PlayerInteractionHelper.isKey(mc.options.sneakKey)
                && !PlayerInteractionHelper.isKey(mc.options.sprintKey)
                && hoverSlot != null
                && hoverSlot.hasStack()
                && actionType != null
                && this.stopWatch.every(this.scrollerSetting.getValue())) {
            InventoryUtils.click(hoverSlot.id, actionType.equals(SlotActionType.THROW) ? 1 : 0, actionType);
        }
    }

    @EventHandler
    public void onClickSlot(ClickSlotEvent event) {
        if (mc.player == null) {
            return;
        }

        int slotId = event.getSlotId();
        if (slotId < 0 || slotId >= mc.player.currentScreenHandler.slots.size()) {
            return;
        }

        Slot slot = mc.player.currentScreenHandler.getSlot(slotId);
        Item item = slot.getStack().getItem();
        if (item != null
                && PlayerInteractionHelper.isKey(mc.options.sneakKey)
                && PlayerInteractionHelper.isKey(mc.options.sprintKey)
                && this.stopWatch.every(50.0D)) {
            processSlotClick(slot, item, event);
        }
    }

    private SlotActionType getActionType() {
        if (PlayerInteractionHelper.isKey(mc.options.dropKey)) {
            return SlotActionType.THROW;
        }
        return PlayerInteractionHelper.isKey(mc.options.attackKey) ? SlotActionType.QUICK_MOVE : null;
    }

    private void processSlotClick(Slot slot, Item item, ClickSlotEvent event) {
        getSlots()
                .filter(candidate -> candidate.getStack().getItem().equals(item) && candidate.inventory.equals(slot.inventory))
                .forEach(candidate -> InventoryUtils.click(candidate.id, 1, event.getActionType()));
    }

    private Stream<Slot> getSlots() {
        return mc.player.currentScreenHandler.slots.stream();
    }
}
