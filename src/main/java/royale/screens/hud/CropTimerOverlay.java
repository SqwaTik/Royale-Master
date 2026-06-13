package royale.screens.hud;

import net.minecraft.block.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import royale.client.draggables.AbstractHudElement;
import royale.modules.impl.render.CropTimer;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;

import java.awt.Color;

public class CropTimerOverlay extends AbstractHudElement {
    private static final float TITLE_SIZE = 6.0F;
    private static final float LINE_SIZE = 5.5F;
    private static final float PADDING = 8.0F;

    public CropTimerOverlay() {
        super("CropTimerOverlay", 6, 6, 130, 50, true);
        startAnimation();
    }

    @Override
    public boolean visible() {
        CropTimer timer = CropTimer.getInstance();
        return timer != null && timer.isState();
    }

    @Override
    public void drawDraggable(DrawContext context, int alpha) {
        if (mc.world == null || mc.player == null) return;

        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) mc.crosshairTarget;
        BlockPos pos = blockHit.getBlockPos();
        var state = mc.world.getBlockState(pos);
        Block block = state.getBlock();

        CropInfo info = getCropInfo(block, state, pos);
        if (info == null) return;

        CropTimer timer = CropTimer.getInstance();

        float boxWidth = 130.0F;
        float boxHeight = 50.0F;

        int bg = withAlpha(new Color(10, 12, 18, 200).getRGB(), alpha);
        Render2D.rect(this.x, this.y, boxWidth, boxHeight, bg, 6.0F);
        Render2D.outline(this.x, this.y, boxWidth, boxHeight, 0.5F, withAlpha(new Color(60, 60, 70, 200).getRGB(), alpha), 6.0F);

        float drawX = this.x + PADDING;
        float drawY = this.y + PADDING;

        if (timer.showTitle.isValue()) {
            Fonts.BOLD.draw(info.name, drawX, drawY, TITLE_SIZE, withAlpha(new Color(210, 210, 220, 255).getRGB(), alpha));
            drawY += 10.0F;
        }

        boolean fullyGrown = info.maxStage <= 0 || info.currentStage >= info.maxStage;

        if (timer.showStage.isValue()) {
            String stageText;
            if (info.maxStage == 0) {
                stageText = "Стадия: " + info.currentStage;
            } else if (fullyGrown) {
                stageText = "Готово к сбору!";
            } else {
                int percent = (int) ((float) info.currentStage / (float) info.maxStage * 100.0F);
                stageText = info.currentStage + " / " + info.maxStage + " (" + percent + "%)";
            }

            int stageColor;
            if (fullyGrown) {
                stageColor = withAlpha(new Color(80, 220, 100, 255).getRGB(), alpha);
            } else {
                stageColor = withAlpha(new Color(180, 180, 190, 255).getRGB(), alpha);
            }

            Fonts.BOLD.draw(stageText, drawX, drawY, LINE_SIZE, stageColor);
            drawY += 9.0F;
        }

        if (timer.showTime.isValue() && !fullyGrown && info.maxStage > 0 && info.currentStage >= 0) {
            int remaining = info.maxStage - info.currentStage;
            if (remaining > 0) {
                int minutes = remaining * 8;
                Fonts.BOLD.draw("~" + minutes + " мин", drawX, drawY, 5.0F, withAlpha(new Color(140, 140, 150, 200).getRGB(), alpha));
            }
        }

        setWidth((int) boxWidth);
        setHeight((int) boxHeight);
    }

    private CropInfo getCropInfo(Block block, BlockState state, BlockPos pos) {
        String name = null;
        int maxStage = -1;
        int currentStage = -1;

        if (block instanceof CropBlock crop) {
            name = crop.getName().getString();
            maxStage = crop.getMaxAge();
            currentStage = crop.getAge(state);
        } else if (block instanceof NetherWartBlock) {
            name = "Адский нарост";
            maxStage = 3;
            currentStage = state.get(Properties.AGE_3);
        } else if (block instanceof StemBlock) {
            name = "Тыква/Арбуз";
            maxStage = 7;
            currentStage = state.get(Properties.AGE_7);
        } else if (block instanceof CocoaBlock) {
            name = "Какао-бобы";
            maxStage = 2;
            currentStage = state.get(Properties.AGE_2);
        } else if (block instanceof SugarCaneBlock) {
            return new CropInfo("Сахарный тростник", countHeight(pos, block), 3);
        } else if (block instanceof CactusBlock) {
            return new CropInfo("Кактус", countHeight(pos, block), 3);
        } else if (block instanceof BambooBlock) {
            return new CropInfo("Бамбук", Math.min(countBambooHeight(pos), 15), 15);
        }

        if (name == null || maxStage <= 0 || currentStage < 0) return null;
        return new CropInfo(name, currentStage, maxStage);
    }

    private int countHeight(BlockPos pos, Block block) {
        int h = 1;
        if (mc.world == null) return h;
        BlockPos current = pos.down();
        while (h < 10 && mc.world.getBlockState(current).getBlock() == block) {
            h++;
            current = current.down();
        }
        return h;
    }

    private int countBambooHeight(BlockPos pos) {
        int h = 1;
        if (mc.world == null) return h;
        BlockPos current = pos.down();
        while (h < 16 && mc.world.getBlockState(current).getBlock() instanceof BambooBlock) {
            h++;
            current = current.down();
        }
        return h;
    }

    private int withAlpha(int color, int alpha) {
        int baseAlpha = color >>> 24;
        int resolvedAlpha = baseAlpha <= 0 ? alpha : Math.min(255, baseAlpha * alpha / 255);
        return color & 0xFFFFFF | resolvedAlpha << 24;
    }

    private record CropInfo(String name, int currentStage, int maxStage) { }
}