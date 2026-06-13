package royale.modules.impl.render;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import royale.events.api.EventHandler;
import royale.events.impl.WorldRenderEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.ColorSetting;
import royale.modules.module.setting.implement.SelectSetting;
import royale.util.ColorUtil;
import royale.util.Instance;
import royale.util.render.Render3D;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockOverlay extends ModuleStructure {
    private static final float LINE_WIDTH = 1.6F;
    private static final double SHAPE_EXPANSION = 0.0025D;
    private static final String MODE_FILL = "Заливка";
    private static final String MODE_OUTLINE = "Контур";
    private static final String MODE_POINTS_3D = "3D точки";
    private static final Map<VoxelShape, List<Box>> EXPANDED_SHAPE_BOXES = new HashMap<>();

    public final ColorSetting overlayColor = (new ColorSetting("Цвет", "Цвет подсветки блока"))
            .value(-428999425);

    public final SelectSetting overlayMode = (new SelectSetting("Режим", "Вид подсветки блока"))
            .value(new String[]{MODE_FILL, MODE_OUTLINE, MODE_POINTS_3D}).selected(MODE_FILL);

    public BlockOverlay() {
        super("BlockOverlay", "Подсвечивает блок под прицелом", ModuleCategory.RENDER);
        settings(new Setting[]{this.overlayColor, this.overlayMode});
    }

    public static BlockOverlay getInstance() {
        return Instance.get(BlockOverlay.class);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (mc.world == null || !(mc.crosshairTarget instanceof BlockHitResult result) || result.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = result.getBlockPos();
        VoxelShape shape = mc.world.getBlockState(pos).getOutlineShape((BlockView) mc.world, pos);
        if (shape.isEmpty()) {
            return;
        }

        String mode = this.overlayMode.getSelected();
        int color = this.overlayColor.getColor();
        if (MODE_FILL.equals(mode)) {
            Render3D.drawShapeAlternative(pos, shape, color, LINE_WIDTH, true, true);
            return;
        }
        if (MODE_POINTS_3D.equals(mode)) {
            renderFullWireframe(pos, shape, color);
            return;
        }

        Render3D.drawShapeAlternative(pos, shape, color, LINE_WIDTH, false, true);
    }

    private void renderFullWireframe(BlockPos pos, VoxelShape shape, int color) {
        List<Box> boxes = EXPANDED_SHAPE_BOXES.computeIfAbsent(shape, this::createExpandedBoxes);
        int fillColor = ColorUtil.multAlpha(color, 0.08F);
        for (Box box : boxes) {
            Render3D.drawBoxWithCrossFull(box.offset(pos), color, fillColor, LINE_WIDTH);
        }
    }

    private List<Box> createExpandedBoxes(VoxelShape shape) {
        return shape.getBoundingBoxes().stream()
                .map(box -> box.expand(SHAPE_EXPANSION))
                .toList();
    }
}
