package royale.screens.hud;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.item.Item;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.gui.DrawContext;
import royale.client.draggables.AbstractHudElement;
import royale.util.ColorUtil;
import royale.util.animations.Direction;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.render.item.ItemRender;
import royale.util.render.shader.Scissor;

public class CoolDowns
extends AbstractHudElement {
    private static final int FORCED_GUI_SCALE = 2;
    private final Map<Item, CoolDownInfo> cooldownMap = new LinkedHashMap<Item, CoolDownInfo>();
    private final Map<Item, Float> cooldownAnimations = new LinkedHashMap<Item, Float>();
    private final Set<Item> activeCooldowns = new HashSet<Item>();
    private float animatedWidth = 80.0f;
    private float animatedHeight = 23.0f;
    private long lastRenderTime = System.currentTimeMillis();
    private long lastItemChange = 0L;
    private int currentItemIndex = 0;
    private static final float ANIMATION_SPEED = 8.0f;
    private static final float ITEM_SCALE = 0.5f;
    private static final String TIMER_TEMPLATE = "00:00";
    private static final Item[] EXAMPLE_ITEMS = new Item[]{Items.ENDER_EYE, Items.ENDER_PEARL, Items.SUGAR, Items.MACE, Items.ENCHANTED_GOLDEN_APPLE, Items.TRIDENT, Items.CROSSBOW, Items.DRIED_KELP, Items.NETHERITE_SCRAP};

    public CoolDowns() {
        super("CoolDowns", 10, 40, 80, 23, true);
        this.stopAnimation();
    }

    @Override
    public boolean visible() {
        return !this.scaleAnimation.isFinished(Direction.BACKWARDS);
    }

    @Override
    public void tick() {
        long currentTime;
        boolean shouldShow;
        ItemStack offHand;
        if (this.mc.player == null) {
            this.cooldownMap.clear();
            this.activeCooldowns.clear();
            this.cooldownAnimations.clear();
            this.stopAnimation();
            return;
        }
        this.activeCooldowns.clear();
        HashSet<Item> checkedItems = new HashSet<Item>();
        for (int i = 0; i < this.mc.player.getInventory().size(); ++i) {
            ItemStack stack = this.mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || checkedItems.contains(stack.getItem())) continue;
            checkedItems.add(stack.getItem());
            this.checkAndUpdateCooldown(stack.getItem());
        }
        ItemStack mainHand = this.mc.player.getMainHandStack();
        if (!mainHand.isEmpty() && !checkedItems.contains(mainHand.getItem())) {
            this.checkAndUpdateCooldown(mainHand.getItem());
        }
        if (!(offHand = this.mc.player.getOffHandStack()).isEmpty() && !checkedItems.contains(offHand.getItem())) {
            this.checkAndUpdateCooldown(offHand.getItem());
        }
        boolean bl = shouldShow = !this.cooldownAnimations.isEmpty() || this.isChat(this.mc.currentScreen);
        if (shouldShow) {
            this.startAnimation();
        } else {
            this.stopAnimation();
        }
        if (this.cooldownAnimations.isEmpty() && this.isChat(this.mc.currentScreen) && (currentTime = System.currentTimeMillis()) - this.lastItemChange >= 1000L) {
            this.currentItemIndex = (this.currentItemIndex + 1) % EXAMPLE_ITEMS.length;
            this.lastItemChange = currentTime;
        }
    }

    private void checkAndUpdateCooldown(Item item) {
        ItemStack stack;
        if (this.mc.player == null) {
            return;
        }
        ItemCooldownManager cooldownManager = this.mc.player.getItemCooldownManager();
        if (cooldownManager.isCoolingDown(stack = item.getDefaultStack())) {
            float progress = cooldownManager.getCooldownProgress(stack, 0.0f);
            this.activeCooldowns.add(item);
            CoolDownInfo info = this.cooldownMap.get(item);
            if (info == null) {
                info = new CoolDownInfo(progress);
                this.cooldownMap.put(item, info);
            } else {
                info.updateEstimate(progress);
            }
            if (!this.cooldownAnimations.containsKey(item)) {
                this.cooldownAnimations.put(item, Float.valueOf(0.0f));
            }
        }
    }

    private String formatDuration(int seconds) {
        if (seconds < 0) {
            return "...";
        }
        if (seconds == 0) {
            return "0:00";
        }
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    @Override
    public void drawDraggable(DrawContext context, int alpha) {
        float f;
        if (alpha <= 0) {
            return;
        }
        float alphaFactor = (float)alpha / 255.0f;
        long currentTime = System.currentTimeMillis();
        float deltaTime = (float)(currentTime - this.lastRenderTime) / 1000.0f;
        this.lastRenderTime = currentTime;
        deltaTime = Math.min(deltaTime, 0.1f);
        ArrayList<Item> toRemove = new ArrayList<Item>();
        for (Map.Entry<Item, Float> entry : this.cooldownAnimations.entrySet()) {
            float targetAnim;
            Item item = entry.getKey();
            float currentAnim = entry.getValue().floatValue();
            float newAnim = currentAnim + ((targetAnim = this.activeCooldowns.contains(item) ? 1.0f : 0.0f) - currentAnim) * Math.min(1.0f, deltaTime * 8.0f);
            if (Math.abs(newAnim - targetAnim) < 0.01f) {
                newAnim = targetAnim;
            }
            if (newAnim <= 0.01f && targetAnim == 0.0f) {
                toRemove.add(item);
                continue;
            }
            this.cooldownAnimations.put(item, Float.valueOf(newAnim));
        }
        for (Item item : toRemove) {
            this.cooldownAnimations.remove(item);
            this.cooldownMap.remove(item);
        }
        float x = this.getX();
        float y = this.getY();
        int offset = 23;
        float targetWidth = 80.0f;
        boolean hasAnimatingCooldowns = !this.cooldownAnimations.isEmpty();
        int blurTint = ColorUtil.rgba(0, 0, 0, 0);
        Render2D.blur(x, y, 1.0f, 1.0f, 0.0f, 0.0f, blurTint);
        float fixedTimerWidth = Fonts.BOLD.getWidth(TIMER_TEMPLATE, 6.0f);
        if (!hasAnimatingCooldowns) {
            offset += 11;
            String name = "Example CoolDown";
            f = Fonts.BOLD.getWidth(name, 6.0f);
            targetWidth = Math.max(f + fixedTimerWidth + 55.0f, targetWidth);
        } else {
            for (Map.Entry entry : this.cooldownAnimations.entrySet()) {
                CoolDownInfo info;
                Item item = (Item)entry.getKey();
                float animation = ((Float)entry.getValue()).floatValue();
                if (animation <= 0.0f || (info = this.cooldownMap.get(item)) == null) continue;
                offset += (int)(animation * 11.0f);
                String name = item.getDefaultStack().getName().getString();
                float nameWidth = Fonts.BOLD.getWidth(name, 6.0f);
                targetWidth = Math.max(nameWidth + fixedTimerWidth + 55.0f, targetWidth);
            }
        }
        float targetHeight = offset + 2;
        this.animatedWidth += (targetWidth - this.animatedWidth) * Math.min(1.0f, deltaTime * 8.0f);
        this.animatedHeight += (targetHeight - this.animatedHeight) * Math.min(1.0f, deltaTime * 8.0f);
        if (Math.abs(this.animatedWidth - targetWidth) < 0.3f) {
            this.animatedWidth = targetWidth;
        }
        if (Math.abs(this.animatedHeight - targetHeight) < 0.3f) {
            this.animatedHeight = targetHeight;
        }
        this.setWidth((int)Math.ceil(this.animatedWidth));
        this.setHeight((int)Math.ceil(this.animatedHeight));
        f = this.animatedHeight;
        int bgAlpha = (int)(255.0f * alphaFactor);
        if (f > 0.0f) {
            Render2D.gradientRect(x, y, this.getWidth(), f, new int[]{new Color(52, 52, 52, bgAlpha).getRGB(), new Color(32, 32, 32, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(32, 32, 32, bgAlpha).getRGB()}, 5.0f);
            Render2D.outline(x, y, this.getWidth(), f, 0.35f, new Color(90, 90, 90, bgAlpha).getRGB(), 5.0f);
        }
        Scissor.enable(x, y, this.getWidth(), f, 2.0f);
        Render2D.gradientRect(x + (float)this.getWidth() - 22.5f, y + 5.0f, 14.0f, 12.0f, new int[]{new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB()}, 3.0f);
        Fonts.ICONS.draw("D", x + (float)this.getWidth() - 20.0f, y + 6.5f, 9.0f, new Color(165, 165, 165, bgAlpha).getRGB());
        Fonts.BOLD.draw("CoolDowns", x + 8.0f, y + 6.5f, 6.0f, new Color(255, 255, 255, bgAlpha).getRGB());
        int moduleOffset = 23;
        float timerBoxWidth = fixedTimerWidth + 4.0f;
        float fixedTimerBoxX = x + (float)this.getWidth() - timerBoxWidth - 9.5f;
        if (!hasAnimatingCooldowns) {
            Item item = EXAMPLE_ITEMS[this.currentItemIndex];
            String name = "Example CoolDown";
            String duration = "0:00";
            Render2D.gradientRect(fixedTimerBoxX + 1.0f, y + (float)moduleOffset - 1.0f, timerBoxWidth, 9.0f, new int[]{new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB()}, 3.0f);
            Render2D.blur(x, y, 1.0f, 1.0f, 0.0f, 0.0f, blurTint);
            Render2D.outline(fixedTimerBoxX + 1.0f, y + (float)moduleOffset - 1.0f, timerBoxWidth, 9.0f, 0.05f, new Color(132, 132, 132, bgAlpha).getRGB(), 2.0f);
            float itemX = x + 8.0f;
            float itemY = y + (float)moduleOffset - 1.0f;
            if (ItemRender.needsContextRender(item.getDefaultStack())) {
                ItemRender.drawItemWithContext(context, item.getDefaultStack(), itemX, itemY, 0.5f, alphaFactor);
            } else {
                ItemRender.drawItem(item.getDefaultStack(), itemX, itemY, 0.5f, alphaFactor);
            }
            float nameX = x + 20.0f;
            Fonts.BOLD.draw(name, nameX, y + (float)moduleOffset - 1.0f, 6.0f, new Color(255, 255, 255, bgAlpha).getRGB());
            float durationWidth = Fonts.BOLD.getWidth(duration, 6.0f);
            float durationX = fixedTimerBoxX + (timerBoxWidth - durationWidth) / 2.0f;
            Fonts.BOLD.draw(duration, durationX + 1.0f, y + (float)moduleOffset, 6.0f, new Color(165, 165, 165, bgAlpha).getRGB());
        } else {
            for (Map.Entry<Item, Float> entry : this.cooldownAnimations.entrySet()) {
                CoolDownInfo info;
                Item item = entry.getKey();
                float animation = entry.getValue().floatValue();
                if (animation <= 0.0f || (info = this.cooldownMap.get(item)) == null) continue;
                ItemCooldownManager cooldownManager = this.mc.player.getItemCooldownManager();
                float currentProgress = cooldownManager.getCooldownProgress(item.getDefaultStack(), 0.0f);
                String name = item.getDefaultStack().getName().getString();
                int remainingSeconds = info.getDisplaySeconds(currentProgress);
                String duration = this.formatDuration(remainingSeconds);
                int textAlpha = (int)(255.0f * animation * alphaFactor);
                Render2D.gradientRect(fixedTimerBoxX + 1.0f, y + (float)moduleOffset - 1.0f, timerBoxWidth, 9.0f, new int[]{new Color(52, 52, 52, textAlpha).getRGB(), new Color(52, 52, 52, textAlpha).getRGB(), new Color(52, 52, 52, textAlpha).getRGB(), new Color(52, 52, 52, textAlpha).getRGB()}, 3.0f);
                Render2D.blur(x, y, 1.0f, 1.0f, 0.0f, 0.0f, blurTint);
                Render2D.outline(fixedTimerBoxX + 1.0f, y + (float)moduleOffset - 1.0f, timerBoxWidth, 9.0f, 0.05f, new Color(132, 132, 132, textAlpha).getRGB(), 2.0f);
                float itemX = x + 8.0f;
                float itemY = y + (float)moduleOffset - 1.0f;
                if (ItemRender.needsContextRender(item.getDefaultStack())) {
                    ItemRender.drawItemWithContext(context, item.getDefaultStack(), itemX, itemY, 0.5f, animation * alphaFactor);
                } else {
                    ItemRender.drawItem(item.getDefaultStack(), itemX, itemY, 0.5f, animation * alphaFactor);
                }
                float nameX = x + 20.0f;
                Fonts.BOLD.draw(name, nameX, y + (float)moduleOffset - 0.5f, 6.0f, new Color(255, 255, 255, textAlpha).getRGB());
                float durationWidth = Fonts.BOLD.getWidth(duration, 6.0f);
                float durationX = fixedTimerBoxX + (timerBoxWidth - durationWidth) / 2.0f;
                Fonts.BOLD.draw(duration, durationX + 1.0f, y + (float)moduleOffset, 6.0f, new Color(165, 165, 165, textAlpha).getRGB());
                moduleOffset += (int)(animation * 11.0f);
            }
        }
        Scissor.disable();
    }

    private static class CoolDownInfo {
        long startTime = System.currentTimeMillis();
        float startProgress;
        long estimatedTotalMs;
        int displaySeconds = -1;
        long nextTickTime = 0L;
        boolean estimateReady = false;

        CoolDownInfo(float progress) {
            this.startProgress = progress;
            this.estimatedTotalMs = 0L;
            this.nextTickTime = 0L;
            this.estimateReady = false;
        }

        void updateEstimate(float currentProgress) {
            float progressConsumed;
            if (this.estimateReady) {
                return;
            }
            long now = System.currentTimeMillis();
            long elapsed = now - this.startTime;
            if (elapsed < 200L) {
                return;
            }
            if (this.startProgress > currentProgress && this.startProgress > 0.01f && (progressConsumed = this.startProgress - currentProgress) > 0.01f) {
                this.estimatedTotalMs = (long)((float)elapsed / progressConsumed);
                long remainingMs = (long)(currentProgress * (float)this.estimatedTotalMs);
                this.displaySeconds = (int)Math.ceil((double)remainingMs / 1000.0);
                this.nextTickTime = now + 1000L;
                this.estimateReady = true;
            }
        }

        int getDisplaySeconds(float currentProgress) {
            if (currentProgress <= 0.0f) {
                this.displaySeconds = 0;
                return 0;
            }
            if (!this.estimateReady) {
                return -1;
            }
            long now = System.currentTimeMillis();
            if (now >= this.nextTickTime && this.nextTickTime > 0L) {
                int calculatedSeconds;
                this.displaySeconds = Math.max(0, this.displaySeconds - 1);
                this.nextTickTime = now + 1000L;
                if (this.estimatedTotalMs > 0L) {
                    long remainingMs = (long)(currentProgress * (float)this.estimatedTotalMs);
                    calculatedSeconds = (int)Math.ceil((double)remainingMs / 1000.0);
                } else {
                    calculatedSeconds = this.displaySeconds;
                }
                if (Math.abs(this.displaySeconds - calculatedSeconds) > 2) {
                    this.displaySeconds = calculatedSeconds;
                }
            }
            return Math.max(0, this.displaySeconds);
        }
    }
}

