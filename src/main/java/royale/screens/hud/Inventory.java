package royale.screens.hud;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.DrawContext;
import royale.client.draggables.AbstractHudElement;
import royale.util.animations.Direction;
import royale.util.render.Render2D;
import royale.util.render.item.ItemRender;

public class Inventory
extends AbstractHudElement {
    private static final int SLOT_SIZE = 12;
    private static final int SLOTS_PER_ROW = 9;
    private static final int INVENTORY_ROWS = 3;
    private static final float ITEM_SCALE = 0.5f;
    private int filledSlots = 0;

    public Inventory() {
        super("Inventory", 20, 60, 200, 80, true);
        this.stopAnimation();
    }

    @Override
    public boolean visible() {
        return !this.scaleAnimation.isFinished(Direction.BACKWARDS);
    }

    @Override
    public void tick() {
        if (this.mc.player == null) {
            this.filledSlots = 0;
            this.stopAnimation();
            return;
        }
        this.filledSlots = 0;
        for (int i = 9; i < 36; ++i) {
            ItemStack stack = this.mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            ++this.filledSlots;
        }
        boolean hasItems = this.filledSlots > 0;
        boolean inChat = this.isChat(this.mc.currentScreen);
        if (hasItems || inChat) {
            this.startAnimation();
        } else {
            this.stopAnimation();
        }
    }

    @Override
    public void drawDraggable(DrawContext context, int alpha) {
        if (alpha <= 0) {
            return;
        }
        if (this.mc.player == null) {
            return;
        }
        float alphaFactor = (float)alpha / 255.0f;
        float x = this.getX();
        float y = this.getY();
        float padding = 6.0f;
        float slotGap = 1.0f;
        float slotsWidth = 108.0f + 8.0f * slotGap;
        float slotsHeight = 36.0f + 2.0f * slotGap;
        float contentWidth = slotsWidth + padding * 2.0f;
        float contentHeight = slotsHeight + padding * 2.0f;
        this.setWidth((int)contentWidth);
        this.setHeight((int)(contentHeight + 4.0f));
        float contentY = y;
        int bgAlpha = (int)(255.0f * alphaFactor);
        Render2D.gradientRect(x + 2.0f, contentY + 2.0f, contentWidth - 4.0f, contentHeight - 4.0f, new int[]{new Color(52, 52, 52, bgAlpha).getRGB(), new Color(32, 32, 32, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(32, 32, 32, bgAlpha).getRGB()}, 5.0f);
        Render2D.outline(x + 2.0f, contentY + 2.0f, contentWidth - 4.0f, contentHeight - 4.0f, 0.35f, new Color(90, 90, 90, bgAlpha).getRGB(), 5.0f);
        float slotsStartX = x + padding;
        float slotsStartY = contentY + padding;
        ArrayList<CountLabel> countLabels = new ArrayList<CountLabel>();
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int slotIndex = 9 + row * 9 + col;
                float slotX = slotsStartX + (float)col * (12.0f + slotGap);
                float slotY = slotsStartY + (float)row * (12.0f + slotGap);
                ItemStack stack = this.mc.player.getInventory().getStack(slotIndex);
                Render2D.rect(slotX, slotY, 12.0f, 12.0f, new Color(28, 28, 28, bgAlpha).getRGB(), 2.0f);
                if (stack.isEmpty()) continue;
                float itemSize = 8.0f;
                float itemX = slotX + (12.0f - itemSize) / 2.0f;
                float itemY = slotY + (12.0f - itemSize) / 2.0f;
                if (ItemRender.needsContextRender(stack)) {
                    ItemRender.drawItemWithContext(context, stack, itemX, itemY, 0.5f, alphaFactor);
                } else {
                    ItemRender.drawItem(stack, itemX, itemY, 0.5f, alphaFactor);
                }
                int count = stack.getCount();
                if (count <= 1) continue;
                countLabels.add(new CountLabel(slotX, slotY, count));
            }
        }
        int textAlpha = (int)(255.0f * alphaFactor);
        int textColor = textAlpha << 24 | 0xFFFFFF;
        for (CountLabel label : countLabels) {
            String countText = String.valueOf(label.count);
            int textWidth = this.mc.textRenderer.getWidth(countText);
            int textX = (int)(label.slotX + 12.0f - (float)textWidth);
            float f = label.slotY + 12.0f;
            Objects.requireNonNull(this.mc.textRenderer);
            int textY = (int)(f - 9.0f + 1.0f);
            context.drawText(this.mc.textRenderer, countText, textX, textY, textColor, true);
        }
    }

    private record CountLabel(float slotX, float slotY, int count) {
    }
}

