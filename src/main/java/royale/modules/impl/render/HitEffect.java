package royale.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import royale.IMinecraft;
import royale.events.api.EventHandler;
import royale.events.impl.AttackEvent;
import royale.events.impl.WorldRenderEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.implement.ColorSetting;
import royale.util.ColorUtil;
import royale.util.Instance;
import royale.util.render.Render3D;

public class HitEffect
extends ModuleStructure {
    private final List<WaveEffect> waveEffects = Collections.synchronizedList(new ArrayList());
    public ColorSetting colorSetting = new ColorSetting("\u0426\u0432\u0435\u0442", "\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0446\u0432\u0435\u0442 \u0434\u043b\u044f \u044d\u0444\u0444\u0435\u043a\u0442\u0430").setColor(new Color(137, 97, 72, 255).getRGB());

    public static HitEffect getInstance() {
        return Instance.get(HitEffect.class);
    }

    public HitEffect() {
        super("HitEffect", "Hit Effect", ModuleCategory.RENDER);
        this.settings(this.colorSetting);
    }

    public void addWave(BlockPos pos) {
        BlockPos groundPos;
        if (HitEffect.mc.world != null && pos != null && (groundPos = this.findGround(pos)) != null) {
            this.waveEffects.add(new WaveEffect(groundPos, System.currentTimeMillis()));
        }
    }

    private BlockPos findGround(BlockPos pos) {
        for (int y = 0; y <= 10; ++y) {
            BlockPos down = pos.down(y);
            if (!HitEffect.mc.world.isInBuildLimit(down) || HitEffect.mc.world.getBlockState(down).isAir()) continue;
            return down;
        }
        return pos;
    }

    @EventHandler
    public void onAttack(AttackEvent e) {
        if (!this.isState()) {
            return;
        }
        if (e.getTarget() != null) {
            this.addWave(e.getTarget().getBlockPos());
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (this.waveEffects.isEmpty() || HitEffect.mc.world == null) {
            return;
        }
        Iterator<WaveEffect> iterator = this.waveEffects.iterator();
        while (iterator.hasNext()) {
            WaveEffect wave = iterator.next();
            if (wave.isExpired()) {
                iterator.remove();
                continue;
            }
            wave.render();
        }
    }

    private class WaveEffect {
        private final BlockPos centerPos;
        private final long startTime;
        private final long duration = 475L;
        private final int maxRadius = 8;
        private Map<Long, Integer> reachableBlocks;
        private boolean calculated = false;

        public WaveEffect(BlockPos centerPos, long startTime) {
            this.centerPos = centerPos;
            this.startTime = startTime;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - this.startTime > 475L;
        }

        private void calculateReachableBlocks() {
            if (this.calculated) {
                return;
            }
            this.calculated = true;
            this.reachableBlocks = new HashMap<Long, Integer>();
            LinkedList<BlockPos> queue = new LinkedList<BlockPos>();
            HashMap<Long, Integer> visited = new HashMap<Long, Integer>();
            BlockPos startPos = this.centerPos;
            if (IMinecraft.mc.world.getBlockState(startPos).isAir()) {
                for (int y = 1; y <= 5; ++y) {
                    BlockPos down = startPos.down(y);
                    if (IMinecraft.mc.world.getBlockState(down).isAir()) continue;
                    startPos = down;
                    break;
                }
            }
            queue.add(startPos);
            visited.put(startPos.asLong(), 0);
            while (!queue.isEmpty()) {
                VoxelShape shape;
                BlockPos current = (BlockPos)queue.poll();
                int currentDistance = (Integer)visited.get(current.asLong());
                if (currentDistance > 8) continue;
                BlockState state = IMinecraft.mc.world.getBlockState(current);
                if (!state.isAir() && !(shape = state.getOutlineShape((BlockView)IMinecraft.mc.world, current)).isEmpty()) {
                    this.reachableBlocks.put(current.asLong(), currentDistance);
                }
                for (Direction dir : Direction.values()) {
                    long aboveLong;
                    BlockPos above;
                    long belowLong;
                    BlockPos neighbor = current.offset(dir);
                    if (!IMinecraft.mc.world.isInBuildLimit(neighbor)) continue;
                    long neighborLong = neighbor.asLong();
                    int newDistance = currentDistance + 1;
                    if (visited.containsKey(neighborLong) && (Integer)visited.get(neighborLong) <= newDistance || newDistance > 8) continue;
                    BlockState neighborState = IMinecraft.mc.world.getBlockState(neighbor);
                    if (!neighborState.isAir()) {
                        visited.put(neighborLong, newDistance);
                        queue.add(neighbor);
                        continue;
                    }
                    BlockPos below = neighbor.down();
                    if (!(!IMinecraft.mc.world.isInBuildLimit(below) || IMinecraft.mc.world.getBlockState(below).isAir() || visited.containsKey(belowLong = below.asLong()) && (Integer)visited.get(belowLong) <= newDistance)) {
                        visited.put(belowLong, newDistance);
                        queue.add(below);
                    }
                    if (!IMinecraft.mc.world.isInBuildLimit(above = neighbor.up()) || IMinecraft.mc.world.getBlockState(above).isAir() || visited.containsKey(aboveLong = above.asLong()) && (Integer)visited.get(aboveLong) <= newDistance) continue;
                    visited.put(aboveLong, newDistance);
                    queue.add(above);
                }
            }
        }

        public void render() {
            if (IMinecraft.mc.world == null) {
                return;
            }
            this.calculateReachableBlocks();
            if (this.reachableBlocks == null || this.reachableBlocks.isEmpty()) {
                return;
            }
            long elapsed = System.currentTimeMillis() - this.startTime;
            float progress = (float)elapsed / 475.0f;
            float currentRadius = progress * 8.0f;
            float waveWidth = 2.5f;
            float globalAlpha = 1.0f - progress;
            globalAlpha = (float)Math.pow(globalAlpha, 0.5);
            int rendered = 0;
            int maxPerFrame = 500;
            for (Map.Entry<Long, Integer> entry : this.reachableBlocks.entrySet()) {
                VoxelShape shape;
                BlockPos pos;
                BlockState state;
                if (rendered >= maxPerFrame) break;
                int blockDistance = entry.getValue();
                if ((float)blockDistance < currentRadius - waveWidth || (float)blockDistance > currentRadius + 0.5f || (state = IMinecraft.mc.world.getBlockState(pos = BlockPos.fromLong((long)entry.getKey()))).isAir() || (shape = state.getOutlineShape((BlockView)IMinecraft.mc.world, pos)).isEmpty()) continue;
                ++rendered;
                float localAlpha = 1.0f - Math.abs((float)blockDistance - currentRadius) / waveWidth;
                localAlpha = Math.max(0.0f, Math.min(1.0f, localAlpha));
                if (!((localAlpha *= globalAlpha) > 0.02f)) continue;
                int baseColor = HitEffect.this.colorSetting.getColor();
                int color = ColorUtil.setAlpha(baseColor, (int)(localAlpha * 75.0f));
                try {
                    Render3D.drawShapeAlternative(pos, shape, color, 1.0f, true, true);
                }
                catch (Exception exception) {}
            }
        }
    }
}

