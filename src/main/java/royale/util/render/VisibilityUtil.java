package royale.util.render;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class VisibilityUtil {
    private static final double RAY_STEP = 0.2D;
    private static final double LAVA_SAMPLE_INSET = 0.05D;

    private VisibilityUtil() {
    }

    public static boolean hasClearLine(MinecraftClient mc, Vec3d from, Vec3d to) {
        if (mc == null || mc.world == null) {
            return false;
        }

        Vec3d delta = to.subtract(from);
        if (delta.lengthSquared() < 1.0E-6D) {
            return true;
        }

        Vec3d direction = delta.normalize();
        double distance = delta.length();
        for (double traveled = 0.0D; traveled <= distance; traveled += RAY_STEP) {
            Vec3d point = from.add(direction.multiply(traveled));
            BlockPos pos = BlockPos.ofFloored(point);
            if (isVisionBlocking(mc, pos)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEntityFullyInLava(MinecraftClient mc, Entity entity) {
        return isEntityFullyInLava(mc, entity, 1.0F);
    }

    public static boolean isEntityFullyInLava(MinecraftClient mc, Entity entity, float tickDelta) {
        if (mc == null || mc.world == null || entity == null) {
            return false;
        }

        Box box = getInterpolatedBox(entity, tickDelta);
        double minX = box.minX + LAVA_SAMPLE_INSET;
        double maxX = box.maxX - LAVA_SAMPLE_INSET;
        double minY = box.minY + LAVA_SAMPLE_INSET;
        double maxY = box.maxY - LAVA_SAMPLE_INSET;
        double minZ = box.minZ + LAVA_SAMPLE_INSET;
        double maxZ = box.maxZ - LAVA_SAMPLE_INSET;

        if (minX > maxX) {
            minX = maxX = box.getCenter().x;
        }
        if (minY > maxY) {
            minY = maxY = box.getCenter().y;
        }
        if (minZ > maxZ) {
            minZ = maxZ = box.getCenter().z;
        }

        double midX = (minX + maxX) * 0.5D;
        double midY = (minY + maxY) * 0.5D;
        double midZ = (minZ + maxZ) * 0.5D;
        Vec3d[] samples = new Vec3d[] {
                new Vec3d(minX, minY, minZ),
                new Vec3d(maxX, minY, minZ),
                new Vec3d(minX, minY, maxZ),
                new Vec3d(maxX, minY, maxZ),
                new Vec3d(minX, midY, minZ),
                new Vec3d(maxX, midY, minZ),
                new Vec3d(minX, midY, maxZ),
                new Vec3d(maxX, midY, maxZ),
                new Vec3d(minX, maxY, minZ),
                new Vec3d(maxX, maxY, minZ),
                new Vec3d(minX, maxY, maxZ),
                new Vec3d(maxX, maxY, maxZ),
                new Vec3d(midX, midY, midZ)
        };

        for (Vec3d sample : samples) {
            if (!mc.world.getFluidState(BlockPos.ofFloored(sample)).isIn(FluidTags.LAVA)) {
                return false;
            }
        }
        return true;
    }

    private static Box getInterpolatedBox(Entity entity, float tickDelta) {
        Vec3d lerpedPos = entity.getLerpedPos(tickDelta);
        Vec3d currentPos = entity.getEntityPos();
        return entity.getBoundingBox().offset(lerpedPos.subtract(currentPos));
    }

    private static boolean isVisionBlocking(MinecraftClient mc, BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }

        if (state.isOf(Blocks.BARRIER)) {
            return false;
        }

        if (!state.getFluidState().isEmpty()) {
            return state.getFluidState().isIn(FluidTags.LAVA);
        }

        Block block = state.getBlock();
        Identifier blockId = Registries.BLOCK.getId(block);
        String blockPath = blockId.getPath();
        if (blockPath.contains("glass") || blockPath.contains("pane") || blockPath.contains("fence_gate") || blockPath.contains("barrier")) {
            return false;
        }

        return !state.getCollisionShape(mc.world, pos).isEmpty();
    }
}


