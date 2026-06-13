package royale.modules.impl.render;

import net.minecraft.block.BambooBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import royale.events.api.EventHandler;
import royale.events.impl.DrawEvent;
import royale.events.impl.WorldLoadEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.BooleanSetting;
import royale.modules.module.setting.implement.ColorSetting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.util.ColorUtil;
import royale.util.Instance;
import royale.util.math.Projection;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CropTimer extends ModuleStructure {
    private static final long DEFAULT_STAGE_TIME_MS = 8L * 60L * 1000L;
    private static final long FAST_STAGE_TIME_MS = 2L * 60L * 1000L;
    private static final long VERTICAL_STAGE_TIME_MS = 18L * 60L * 1000L;
    private static final int READY_COLOR = new Color(94, 255, 123, 255).getRGB();
    private static final int GROWING_COLOR = new Color(255, 214, 101, 255).getRGB();
    private static final int TEXT_COLOR = new Color(238, 241, 247, 255).getRGB();
    private static final int BG_COLOR = new Color(8, 10, 16, 178).getRGB();

    public final BooleanSetting showTitle = (new BooleanSetting(
            "Название", "Показывать название культуры"
    )).setValue(true);

    public final BooleanSetting showStage = (new BooleanSetting(
            "Стадия", "Показывать стадию роста"
    )).setValue(true);

    public final BooleanSetting showTime = (new BooleanSetting(
            "Время", "Показывать примерное время до созревания"
    )).setValue(true);

    public final BooleanSetting worldLabels = (new BooleanSetting(
            "Над растениями", "Показывает процент и время прямо над культурами"
    )).setValue(true);

    public final BooleanSetting background = (new BooleanSetting(
            "Фон", "Рисует мягкий фон под текстом"
    )).setValue(true);

    public final SliderSettings scanRadius = (new SliderSettings(
            "Радиус", "Радиус поиска растений"
    )).setValue(16.0F).range(4, 48);

    public final SliderSettings maxLabels = (new SliderSettings(
            "Лимит", "Максимум подписей на экране"
    )).setValue(80.0F).range(10, 200);

    public final SliderSettings updateInterval = (new SliderSettings(
            "Обновление", "Задержка обновления (мс)"
    )).setValue(350.0F).range(100, 2000);

    private final List<CropInfo> cachedCrops = new ArrayList<>();
    private long lastScanMs;
    private BlockPos lastScanCenter = BlockPos.ORIGIN;

    public CropTimer() {
        super("CropTimer", "Показывает точный процент стадии и расчетное время роста культур", ModuleCategory.RENDER);
        settings(new Setting[]{showTitle, showStage, showTime, worldLabels, background, scanRadius, maxLabels, updateInterval});
    }

    public static CropTimer getInstance() {
        return Instance.get(CropTimer.class);
    }

    @Override
    public void deactivate() {
        cachedCrops.clear();
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        cachedCrops.clear();
        lastScanMs = 0L;
    }

    @EventHandler
    public void onDraw(DrawEvent event) {
        if (!worldLabels.isValue() || mc.world == null || mc.player == null) {
            return;
        }

        refreshCacheIfNeeded();
        if (cachedCrops.isEmpty()) {
            return;
        }

        DrawContext context = event.getDrawContext();
        int rendered = 0;
        int limit = Math.max(1, maxLabels.getInt());
        for (CropInfo crop : cachedCrops) {
            if (rendered >= limit) {
                break;
            }

            Vec3d screen = Projection.worldSpaceToScreenSpace(crop.labelPos());
            if (screen.z < 0.0D || screen.z > 1.0D) {
                continue;
            }

            float x = (float) screen.x;
            float y = (float) screen.y;
            if (x < -80.0F || y < -40.0F || x > mc.getWindow().getScaledWidth() + 80.0F || y > mc.getWindow().getScaledHeight() + 40.0F) {
                continue;
            }

            drawCropLabel(context, crop, x, y);
            rendered++;
        }
    }

    private void refreshCacheIfNeeded() {
        long now = System.currentTimeMillis();
        BlockPos center = mc.player.getBlockPos();
        boolean centerMoved = center.getSquaredDistance(lastScanCenter) > 9.0D;
        if (!centerMoved && now - lastScanMs < Math.max(100L, (long) updateInterval.getValue())) {
            return;
        }

        cachedCrops.clear();
        lastScanMs = now;
        lastScanCenter = center;

        int radius = Math.max(2, scanRadius.getInt());
        int vertical = Math.min(10, Math.max(4, radius / 2));
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
            for (int y = center.getY() - vertical; y <= center.getY() + vertical; y++) {
                for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
                    mutable.set(x, y, z);
                    if (mutable.getSquaredDistance(center) > radius * radius) {
                        continue;
                    }

                    BlockState state = mc.world.getBlockState(mutable);
                    CropInfo info = getCropInfo(state, mutable.toImmutable());
                    if (info != null) {
                        cachedCrops.add(info);
                    }
                }
            }
        }

        Vec3d cameraPos = mc.gameRenderer.getCamera().getCameraPos();
        cachedCrops.sort(Comparator.comparingDouble(info -> info.labelPos().squaredDistanceTo(cameraPos)));
    }

    public CropInfo getCropInfo(BlockState state, BlockPos pos) {
        if (state == null || state.isAir() || mc.world == null) {
            return null;
        }

        Block block = state.getBlock();
        String name = null;
        int currentStage = -1;
        int maxStage = -1;
        long stageTime = DEFAULT_STAGE_TIME_MS;

        if (block instanceof CropBlock crop) {
            name = crop.getName().getString();
            maxStage = crop.getMaxAge();
            currentStage = crop.getAge(state);
        } else if (block instanceof NetherWartBlock) {
            name = "Адский нарост";
            maxStage = 3;
            currentStage = state.get(Properties.AGE_3);
            stageTime = FAST_STAGE_TIME_MS;
        } else if (block instanceof StemBlock) {
            name = block.getName().getString();
            maxStage = 7;
            currentStage = state.get(Properties.AGE_7);
        } else if (block instanceof CocoaBlock) {
            name = "Какао-бобы";
            maxStage = 2;
            currentStage = state.get(Properties.AGE_2);
            stageTime = FAST_STAGE_TIME_MS;
        } else if (block instanceof SugarCaneBlock || block instanceof CactusBlock) {
            name = block instanceof SugarCaneBlock ? "Сахарный тростник" : "Кактус";
            currentStage = countStackHeight(pos, block);
            maxStage = 3;
            stageTime = VERTICAL_STAGE_TIME_MS;
        } else if (block instanceof BambooBlock) {
            name = "Бамбук";
            currentStage = Math.min(countStackHeight(pos, block), 16);
            maxStage = 16;
            stageTime = FAST_STAGE_TIME_MS;
        } else if (state.contains(Properties.AGE_7)) {
            name = block.getName().getString();
            maxStage = 7;
            currentStage = state.get(Properties.AGE_7);
        } else if (state.contains(Properties.AGE_5)) {
            name = block.getName().getString();
            maxStage = 5;
            currentStage = state.get(Properties.AGE_5);
        } else if (state.contains(Properties.AGE_3)) {
            name = block.getName().getString();
            maxStage = 3;
            currentStage = state.get(Properties.AGE_3);
        } else if (state.contains(Properties.AGE_2)) {
            name = block.getName().getString();
            maxStage = 2;
            currentStage = state.get(Properties.AGE_2);
        }

        if (name == null || maxStage <= 0 || currentStage < 0) {
            return null;
        }

        currentStage = Math.min(currentStage, maxStage);
        double percent = maxStage <= 0 ? 100.0D : (currentStage * 100.0D / maxStage);
        boolean ready = currentStage >= maxStage;
        long etaMs = ready ? 0L : Math.max(1, maxStage - currentStage) * stageTime;
        Vec3d labelPos = Vec3d.ofCenter(pos).add(0.0D, getVisualHeight(state, pos) + 0.35D, 0.0D);
        return new CropInfo(pos, labelPos, name, currentStage, maxStage, percent, etaMs, ready);
    }

    private int countStackHeight(BlockPos pos, Block block) {
        if (mc.world == null) {
            return 1;
        }

        BlockPos base = pos;
        while (base.getY() > mc.world.getBottomY() && mc.world.getBlockState(base.down()).getBlock() == block) {
            base = base.down();
        }

        int height = 0;
        BlockPos cursor = base;
        while (height < 32 && mc.world.getBlockState(cursor).getBlock() == block) {
            height++;
            cursor = cursor.up();
        }
        return Math.max(1, height);
    }

    private double getVisualHeight(BlockState state, BlockPos pos) {
        try {
            VoxelShape shape = state.getOutlineShape(mc.world, pos);
            if (!shape.isEmpty()) {
                return Math.max(0.35D, shape.getMax(Direction.Axis.Y));
            }
        } catch (Throwable ignored) {
        }
        return 1.0D;
    }

    private void drawCropLabel(DrawContext context, CropInfo crop, float centerX, float y) {
        String main = crop.ready() ? "100% • готово" : formatPercent(crop.percent()) + "% • " + formatEta(crop.etaMs());
        String stage = crop.currentStage() + "/" + crop.maxStage();
        String title = showTitle.isValue() ? crop.name() : null;

        float titleSize = 5.7F;
        float mainSize = 6.0F;
        float smallSize = 5.0F;
        float width = Fonts.BOLD.getWidth(main, mainSize);
        if (title != null) {
            width = Math.max(width, Fonts.TEST.getWidth(title, titleSize));
        }
        if (showStage.isValue()) {
            width = Math.max(width, Fonts.TEST.getWidth(stage, smallSize));
        }

        float height = 13.0F + (title != null ? 8.0F : 0.0F) + (showStage.isValue() ? 7.0F : 0.0F);
        float x = centerX - width / 2.0F - 5.0F;
        float top = y - height;
        if (background.isValue()) {
            Render2D.rect(x, top, width + 10.0F, height, ColorUtil.multAlpha(BG_COLOR, 0.92F), 5.0F);
            Render2D.outline(x, top, width + 10.0F, height, 0.45F, crop.ready() ? ColorUtil.multAlpha(READY_COLOR, 0.7F) : ColorUtil.multAlpha(GROWING_COLOR, 0.7F), 5.0F);
        }

        float drawY = top + 4.0F;
        if (title != null) {
            Fonts.TEST.draw(title, centerX - Fonts.TEST.getWidth(title, titleSize) / 2.0F, drawY, titleSize, TEXT_COLOR);
            drawY += 7.5F;
        }

        int color = crop.ready() ? READY_COLOR : GROWING_COLOR;
        Fonts.BOLD.draw(main, centerX - Fonts.BOLD.getWidth(main, mainSize) / 2.0F, drawY, mainSize, color);
        drawY += 8.0F;
        if (showStage.isValue()) {
            Fonts.TEST.draw(stage, centerX - Fonts.TEST.getWidth(stage, smallSize) / 2.0F, drawY, smallSize, ColorUtil.multAlpha(TEXT_COLOR, 0.75F));
        }
    }

    private String formatPercent(double percent) {
        double rounded = Math.round(percent * 10.0D) / 10.0D;
        if (Math.abs(rounded - Math.rint(rounded)) < 0.001D) {
            return Integer.toString((int) rounded);
        }
        return String.format(java.util.Locale.US, "%.1f", rounded);
    }

    private String formatEta(long etaMs) {
        if (etaMs <= 0L) {
            return "готово";
        }
        long seconds = Math.max(1L, etaMs / 1000L);
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        if (hours > 0L) {
            return hours + "ч " + (minutes % 60L) + "м";
        }
        if (minutes > 0L) {
            return minutes + "м";
        }
        return seconds + "с";
    }

    public record CropInfo(BlockPos pos, Vec3d labelPos, String name, int currentStage, int maxStage, double percent, long etaMs, boolean ready) {
    }
}
