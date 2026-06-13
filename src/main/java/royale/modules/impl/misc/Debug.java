package royale.modules.impl.misc;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import royale.events.api.EventHandler;
import royale.events.impl.ClickSlotEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.BooleanSetting;
import royale.util.Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Debug extends ModuleStructure {
    private static final long PIN_DURATION_MS = 5000L;

    public final BooleanSetting hoverOverlay = (new BooleanSetting(
            "Оверлей",
            "Показывает карточку с данными блока или предмета"
    )).setValue(true);

    public final BooleanSetting inventoryInspect = (new BooleanSetting(
            "ПКМ в инвентаре",
            "По правому клику показывает карточку предмета"
    )).setValue(true);

    public final BooleanSetting blockStateDetails = (new BooleanSetting(
            "Состояние блока",
            "Добавляет свойства блока в debug карточку"
    )).setValue(true);

    private DebugInfo pinnedInfo;
    private long pinnedUntil;

    public Debug() {
        super("Debug", "Показывает id и данные предметов", ModuleCategory.MISC);
        settings(new Setting[]{this.hoverOverlay, this.inventoryInspect, this.blockStateDetails});
    }

    public static Debug getInstance() {
        return Instance.get(Debug.class);
    }

    @EventHandler
    public void onClickSlot(ClickSlotEvent event) {
        if (!this.inventoryInspect.isValue() || mc.player == null || !isState()) {
            return;
        }

        if (event.getButton() != 1 || event.getActionType() != SlotActionType.PICKUP) {
            return;
        }

        int slotId = event.getSlotId();
        if (slotId < 0 || slotId >= mc.player.currentScreenHandler.slots.size()) {
            return;
        }

        Slot slot = mc.player.currentScreenHandler.getSlot(slotId);
        if (slot == null || !slot.hasStack()) {
            return;
        }

        ItemStack stack = slot.getStack();
        if (stack.isEmpty()) {
            return;
        }

        event.cancel();
        pinInfo(buildItemInfo(stack, "Инвентарь"));
    }

    public DebugInfo getOverlayInfo(boolean allowPlaceholder) {
        if (!isState()) {
            return null;
        }

        DebugInfo pinned = getPinnedInfo();
        if (pinned != null) {
            return pinned;
        }

        if (this.hoverOverlay.isValue()) {
            DebugInfo live = resolveCrosshairInfo();
            if (live != null) {
                return live;
            }
        }

        if (allowPlaceholder && mc.currentScreen instanceof ChatScreen) {
            return new DebugInfo("Debug Overlay", List.of(
                    "Перетаскивай карточку мышкой.",
                    "ПКМ по предмету покажет его данные."
            ));
        }

        return null;
    }

    public boolean shouldRenderOverlay(boolean allowPlaceholder) {
        return getOverlayInfo(allowPlaceholder) != null;
    }

    private void pinInfo(DebugInfo info) {
        this.pinnedInfo = info;
        this.pinnedUntil = System.currentTimeMillis() + PIN_DURATION_MS;
    }

    private DebugInfo getPinnedInfo() {
        if (this.pinnedInfo == null) {
            return null;
        }

        if (System.currentTimeMillis() > this.pinnedUntil) {
            this.pinnedInfo = null;
            this.pinnedUntil = 0L;
            return null;
        }

        return this.pinnedInfo;
    }

    private DebugInfo resolveCrosshairInfo() {
        HitResult target = mc.crosshairTarget;
        if (target == null) {
            return null;
        }

        if (target instanceof BlockHitResult blockHitResult && target.getType() == HitResult.Type.BLOCK) {
            return buildBlockInfo(blockHitResult);
        }

        if (target instanceof EntityHitResult entityHitResult && target.getType() == HitResult.Type.ENTITY) {
            Entity entity = entityHitResult.getEntity();
            if (entity instanceof ItemEntity itemEntity) {
                return buildItemInfo(itemEntity.getStack(), "Предмет");
            }
            return buildEntityInfo(entity);
        }

        return null;
    }

    private DebugInfo buildBlockInfo(BlockHitResult result) {
        if (mc.world == null) {
            return null;
        }

        BlockState state = mc.world.getBlockState(result.getBlockPos());
        Identifier blockId = Registries.BLOCK.getId(state.getBlock());

        List<String> lines = new ArrayList<>();
        lines.add("Блок: " + state.getBlock().getName().getString());
        lines.add("ID: " + blockId);
        lines.add("Позиция: " + result.getBlockPos().getX() + " " + result.getBlockPos().getY() + " " + result.getBlockPos().getZ());

        if (this.blockStateDetails.isValue()) {
            String stateLine = formatBlockState(state);
            if (!stateLine.isBlank()) {
                lines.add("Состояние: " + stateLine);
            }
        }

        return new DebugInfo("Блок", lines);
    }

    private DebugInfo buildEntityInfo(Entity entity) {
        Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
        List<String> lines = new ArrayList<>();
        lines.add("Сущность: " + entity.getName().getString());
        lines.add("ID: " + entityId);
        lines.add("Позиция: "
                + Math.round(entity.getX() * 10.0D) / 10.0D + " "
                + Math.round(entity.getY() * 10.0D) / 10.0D + " "
                + Math.round(entity.getZ() * 10.0D) / 10.0D);
        return new DebugInfo("Сущность", lines);
    }

    private DebugInfo buildItemInfo(ItemStack stack, String sourceLabel) {
        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        List<String> lines = new ArrayList<>();
        lines.add("Источник: " + sourceLabel);
        lines.add("Предмет: " + stack.getName().getString());
        lines.add("ID: " + itemId);
        lines.add("Количество: " + stack.getCount());

        if (stack.isDamageable()) {
            int durabilityLeft = Math.max(0, stack.getMaxDamage() - stack.getDamage());
            lines.add("Прочность: " + durabilityLeft + "/" + stack.getMaxDamage());
        }

        String defaultName = stack.getItem().getName().getString();
        String currentName = stack.getName().getString();
        if (!currentName.equals(defaultName)) {
            lines.add("Имя: " + currentName);
        }

        return new DebugInfo("Предмет", lines);
    }

    private String formatBlockState(BlockState state) {
        if (state.getEntries().isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (Map.Entry<Property<?>, Comparable<?>> entry : state.getEntries().entrySet()) {
            if (count++ > 0) {
                builder.append(", ");
            }
            builder.append(entry.getKey().getName()).append('=').append(entry.getValue());
            if (count >= 4) {
                break;
            }
        }
        return builder.toString();
    }

    public static final class DebugInfo {
        private final String title;
        private final List<String> lines;

        public DebugInfo(String title, List<String> lines) {
            this.title = title;
            this.lines = List.copyOf(lines);
        }

        public String getTitle() {
            return this.title;
        }

        public List<String> getLines() {
            return this.lines;
        }
    }
}
