package royale.modules.impl.render;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import royale.events.api.EventHandler;
import royale.events.impl.WorldRenderEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.BooleanSetting;
import royale.modules.module.setting.implement.ColorSetting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.util.ColorUtil;
import royale.util.Instance;
import royale.util.render.Render3D;

import java.awt.Color;

public class Trajectories extends ModuleStructure {
    private static final int DEFAULT_COLOR = new Color(108, 178, 255, 230).getRGB();
    private static final int HIT_COLOR = new Color(94, 255, 123, 230).getRGB();
    private static final int WARNING_COLOR = new Color(255, 92, 92, 230).getRGB();

    public final ColorSetting lineColor = (new ColorSetting(
            "Цвет", "Цвет линии траектории"
    )).value(DEFAULT_COLOR);

    public final BooleanSetting showLanding = (new BooleanSetting(
            "Точка падения", "Подсвечивает место попадания"
    )).setValue(true);

    public final BooleanSetting throughWalls = (new BooleanSetting(
            "Через стены", "Рисует траекторию поверх блоков"
    )).setValue(true);

    public final SliderSettings maxTicks = (new SliderSettings(
            "Длина", "Максимальная длина просчета в тиках"
    )).setValue(90.0F).range(20, 160);

    public final SliderSettings lineWidth = (new SliderSettings(
            "Толщина", "Толщина линии"
    )).setValue(2.0F).range(1.0F, 5.0F);

    public Trajectories() {
        super("Trajectories", "Показывает траекторию снежков, перлов, зелий, стрел и трезубцев", ModuleCategory.RENDER);
        settings(new Setting[]{lineColor, showLanding, throughWalls, maxTicks, lineWidth});
    }

    public static Trajectories getInstance() {
        return Instance.get(Trajectories.class);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
        }

        TrajectoryData data = resolveHeldProjectile(mc.player, event.getPartialTicks());
        if (data == null) {
            return;
        }

        simulate(data);
    }

    private TrajectoryData resolveHeldProjectile(PlayerEntity player, float tickDelta) {
        ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
        TrajectorySettings settings = settingsFor(stack, player, tickDelta);
        if (settings == null) {
            stack = player.getStackInHand(Hand.OFF_HAND);
            settings = settingsFor(stack, player, tickDelta);
        }

        if (settings == null) {
            return null;
        }

        Vec3d look = player.getRotationVec(tickDelta).normalize();
        Vec3d start = player.getEyePos().add(look.multiply(0.32D));
        Vec3d velocity = look.multiply(settings.power());
        return new TrajectoryData(start, velocity, settings.gravity(), settings.drag(), settings.maxDistanceScale());
    }

    private TrajectorySettings settingsFor(ItemStack stack, PlayerEntity player, float tickDelta) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }

        Item item = stack.getItem();
        if (item == Items.SNOWBALL || item == Items.EGG || item == Items.ENDER_PEARL) {
            return new TrajectorySettings(1.5D, 0.03D, 0.99D, 1.0D);
        }
        if (item == Items.SPLASH_POTION || item == Items.LINGERING_POTION || item == Items.EXPERIENCE_BOTTLE) {
            return new TrajectorySettings(0.75D, 0.05D, 0.99D, 0.85D);
        }
        if (item == Items.TRIDENT) {
            return new TrajectorySettings(player.isUsingItem() ? 2.5D : 2.2D, 0.05D, 0.99D, 1.15D);
        }
        if (item == Items.BOW) {
            double pull = getBowPull(player);
            if (pull <= 0.08D && !player.isUsingItem()) {
                pull = 1.0D;
            }
            return new TrajectorySettings(3.0D * pull, 0.05D, 0.99D, 1.2D);
        }
        if (item == Items.CROSSBOW) {
            return new TrajectorySettings(3.15D, 0.05D, 0.99D, 1.2D);
        }
        if (item == Items.FISHING_ROD) {
            return new TrajectorySettings(1.25D, 0.04D, 0.92D, 0.8D);
        }
        return null;
    }

    private double getBowPull(PlayerEntity player) {
        int useTicks = Math.max(0, player.getItemUseTime());
        double pull = useTicks / 20.0D;
        pull = (pull * pull + pull * 2.0D) / 3.0D;
        return Math.min(1.0D, Math.max(0.0D, pull));
    }

    private void simulate(TrajectoryData data) {
        Vec3d position = data.start();
        Vec3d velocity = data.velocity();
        boolean depth = !throughWalls.isValue();
        int color = lineColor.getColor();
        int fadeColor = ColorUtil.multAlpha(color, 0.45F);
        HitResult hit = null;

        int ticks = Math.max(10, maxTicks.getInt());
        for (int i = 0; i < ticks; i++) {
            Vec3d next = position.add(velocity.multiply(data.maxDistanceScale()));
            BlockHitResult blockHit = mc.world.raycast(new RaycastContext(
                    position,
                    next,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    mc.player
            ));

            if (blockHit != null && blockHit.getType() == HitResult.Type.BLOCK) {
                next = blockHit.getPos();
                hit = blockHit;
            }

            float progress = i / (float) ticks;
            int segmentColor = ColorUtil.lerpColor(color, fadeColor, progress);
            Render3D.drawLine(position, next, segmentColor, lineWidth.getValue(), depth);

            if (hit != null) {
                break;
            }

            position = next;
            velocity = velocity.multiply(data.drag()).subtract(0.0D, data.gravity(), 0.0D);
            if (position.y < mc.world.getBottomY() - 4.0D || position.y > mc.world.getBottomY() + 512.0D) {
                break;
            }
        }

        if (showLanding.isValue()) {
            Vec3d landing = hit != null ? hit.getPos() : position;
            int boxColor = hit != null ? HIT_COLOR : WARNING_COLOR;
            Render3D.drawBox(new Box(landing.x - 0.12D, landing.y - 0.12D, landing.z - 0.12D, landing.x + 0.12D, landing.y + 0.12D, landing.z + 0.12D), boxColor, 1.8F, true, true, depth);
        }
    }

    private record TrajectoryData(Vec3d start, Vec3d velocity, double gravity, double drag, double maxDistanceScale) {
    }

    private record TrajectorySettings(double power, double gravity, double drag, double maxDistanceScale) {
    }
}
