package royale.util.render.item;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.random.Random;
import net.minecraft.item.ItemDisplayContext;
import org.joml.Matrix3x2fStack;
import royale.util.math.OptionValueUtil;
import royale.util.render.Render2D;

public class ItemRender {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Map<String, CachedSprite> SPRITE_CACHE = new ConcurrentHashMap<String, CachedSprite>();
    private static final Random RANDOM = Random.create();
    private static final int FORCED_GUI_SCALE = 2;

    private static int getCurrentGuiScale() {
        int scale = OptionValueUtil.toInt(ItemRender.mc.options.getGuiScale().getValue(), 0);
        if (scale == 0) {
            scale = mc.getWindow().calculateScaleFactor(0, mc.forcesUnicodeFont());
        }
        return scale;
    }

    private static float getScaleCompensation() {
        return 2.0f / (float)ItemRender.getCurrentGuiScale();
    }

    public static boolean isBlockItem(ItemStack stack) {
        return stack.getItem() instanceof BlockItem;
    }

    public static boolean isPotionItem(ItemStack stack) {
        return stack.getItem() == Items.POTION || stack.getItem() == Items.SPLASH_POTION || stack.getItem() == Items.LINGERING_POTION || stack.getItem() == Items.TIPPED_ARROW;
    }

    public static boolean hasGlint(ItemStack stack) {
        return stack.hasGlint();
    }

    public static boolean needsContextRender(ItemStack stack) {
        return ItemRender.isBlockItem(stack) || ItemRender.isPotionItem(stack) || ItemRender.hasGlint(stack);
    }

    public static void drawItem(ItemStack stack, float x, float y, float scale, float alpha) {
        ItemRender.drawItem(stack, x, y, scale, alpha, -1);
    }

    public static void drawItem(ItemStack stack, float x, float y, float scale, float alpha, int tintColor) {
        if (stack.isEmpty() || alpha <= 0.01f) {
            return;
        }
        if (ItemRender.needsContextRender(stack)) {
            return;
        }
        Sprite sprite = ItemRender.getSpriteForStack(stack);
        if (sprite != null) {
            int color = ItemRender.applyAlpha(tintColor, alpha);
            float size = 16.0f * scale;
            Render2D.drawSprite(sprite, x, y, size, size, color, true);
        }
    }

    public static void drawBlockItem(DrawContext context, ItemStack stack, float x, float y, float scale, float alpha) {
        if (stack.isEmpty() || alpha <= 0.01f) {
            return;
        }
        float compensation = ItemRender.getScaleCompensation();
        float finalScale = scale * compensation;
        float size = 16.0f * scale;
        float centerX = x + size / 2.0f;
        float centerY = y + size / 2.0f;
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(centerX, centerY);
        matrices.scale(finalScale, finalScale);
        matrices.translate(-8.0f, -8.0f);
        context.drawItem(stack, 0, 0);
        matrices.popMatrix();
    }

    public static void drawItemWithContext(DrawContext context, ItemStack stack, float x, float y, float scale, float alpha) {
        if (stack.isEmpty() || alpha <= 0.01f) {
            return;
        }
        float compensation = ItemRender.getScaleCompensation();
        float finalScale = scale * compensation;
        float size = 16.0f * scale;
        float centerX = x + size / 2.0f;
        float centerY = y + size / 2.0f;
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(centerX, centerY);
        matrices.scale(finalScale, finalScale);
        matrices.translate(-8.0f, -8.0f);
        context.drawItem(stack, 0, 0);
        matrices.popMatrix();
    }

    public static void drawItemCentered(ItemStack stack, float centerX, float centerY, float scale, float alpha) {
        float size = 16.0f * scale;
        float x = centerX - size / 2.0f;
        float y = centerY - size / 2.0f;
        ItemRender.drawItem(stack, x, y, scale, alpha);
    }

    public static void drawItemCenteredWithContext(DrawContext context, ItemStack stack, float centerX, float centerY, float scale, float alpha) {
        float size = 16.0f * scale;
        float x = centerX - size / 2.0f;
        float y = centerY - size / 2.0f;
        ItemRender.drawItemWithContext(context, stack, x, y, scale, alpha);
    }

    private static Sprite getSpriteForStack(ItemStack stack) {
        String cacheKey = ItemRender.getCacheKey(stack);
        CachedSprite cached = SPRITE_CACHE.get(cacheKey);
        if (cached != null) {
            return cached.sprite;
        }
        try {
            ItemRenderState state = new ItemRenderState();
            mc.getItemModelManager().clearAndUpdate(state, stack, ItemDisplayContext.GUI, (World)ItemRender.mc.world, null, 0);
            Sprite sprite = state.getParticleSprite(RANDOM);
            if (sprite != null) {
                SPRITE_CACHE.put(cacheKey, new CachedSprite(sprite));
                return sprite;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    private static String getCacheKey(ItemStack stack) {
        return stack.getItem().toString() + "_" + stack.getComponents().hashCode();
    }

    private static int applyAlpha(int color, float alpha) {
        int a = (int)((float)(color >> 24 & 0xFF) * alpha);
        return a << 24 | color & 0xFFFFFF;
    }

    public static void clearCache() {
        SPRITE_CACHE.clear();
    }

    private record CachedSprite(Sprite sprite) {
    }
}

