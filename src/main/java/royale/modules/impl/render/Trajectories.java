package royale.modules.impl.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
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
import royale.events.impl.DrawEvent;
import royale.events.impl.WorldRenderEvent;
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
import royale.util.render.Render3D;
import royale.util.render.font.Fonts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Trajectories extends ModuleStructure {
    private static final int DEFAULT_COLOR = new Color(108, 178, 255, 230).getRGB();
    private static final int OTHER_COLOR = new Color(255, 186, 92, 220).getRGB();
    private static final int PROJECTILE_COLOR = new Color(166, 116, 255, 220).getRGB();
    private static final int HIT_COLOR = new Color(94, 255, 123, 230).getRGB();
    private static final int WARNING_COLOR = new Color(255, 92, 92, 230).getRGB();
    private static final long TRAIL_LIFETIME_MS = 2500L;

    public final ColorSetting lineColor = (new ColorSetting(
            "Цвет", "Цвет линии своей траектории"
    )).value(DEFAULT_COLOR);

    public final BooleanSetting ownTrajectory = (new BooleanSetting(
            "Своя траектория", "Показывает дугу предмета в вашей руке"
    )).setValue(true);

    public final BooleanSetting otherPlayers = (new BooleanSetting(
            "Другие игроки", "Показывает дугу броска у игроков рядом"
    )).setValue(true);

    public final BooleanSetting thrownProjectiles = (new BooleanSetting(
            "Летящие предметы", "Показывает прогноз для уже брошенных предметов"
    )).setValue(true);

    public final BooleanSetting keepTrails = (new BooleanSetting(
            "След после броска", "Оставляет дугу на короткое время после броска"
    )).setValue(true);

    public final BooleanSetting showLanding = (new BooleanSetting(
            "Точка падения", "Подсвечивает место попадания"
    )).setValue(true);

    public final BooleanSetting showTime = (new BooleanSetting(
            "Время падения", "Показывает через сколько предмет приземлится"
    )).setValue(true);

    public final BooleanSetting movingMarker = (new BooleanSetting(
            "Маркер движения", "Показывает точку, которая движется по дуге"
    )).setValue(true);

    public final BooleanSetting throughWalls = (new BooleanSetting(
            "Через стены", "Рисует траекторию поверх блоков"
    )).setValue(true);

    public final SliderSettings maxTicks = (new SliderSettings(
            "Длина", "Максимальная длина просчета в тиках"
    )).setValue(80.0F).range(20, 140);

    public final SliderSettings scanRadius = (new SliderSettings(
            "Радиус игроков", "На какой дистанции показывать траектории других игроков"
    )).setValue(48.0F).range(8, 96);

    public final SliderSettings lineWidth = (new SliderSettings(
            "Толщина", "Толщина линии"
    )).setValue(2.0F).range(1.0F, 4.0F);

    private final List<LandingLabel> labels = new ArrayList<>();
    private final Map<Integer, TrailSnapshot> trails = new HashMap<>();
    private long nextProjectileScanMs;

    public Trajectories() {
        super("Trajectories", "Показывает дуги бросков, место и время приземления предметов", ModuleCategory.RENDER);
        settings(new Setting[]{lineColor, ownTrajectory, otherPlayers, thrownProjectiles, keepTrails, showLanding, showTime, movingMarker, throughWalls, maxTicks, scanRadius, lineWidth});
    }

    public static Trajectories getInstance() {
        return Instance.get(Trajectories.class);
    }

    @Override
    public void deactivate() {
        labels.clear();
        trails.clear();
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
        }

        labels.clear();
        boolean depth = !throughWalls.isValue();
        int baseColor = lineColor.getColor();

        if (ownTrajectory.isValue()) {
            TrajectoryData own = resolveHeldProjectile(mc.player, event.getPartialTicks());
            if (own != null) {
                renderTrajectory(own, mc.player, baseColor, depth, "Вы");
            }
        }

        if (otherPlayers.isValue()) {
            int processed = 0;
            double radiusSq = scanRadius.getValue() * scanRadius.getValue();
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player == mc.player || player.squaredDistanceTo(mc.player) > radiusSq) {
                    continue;
                }
                TrajectoryData data = resolveHeldProjectile(player, event.getPartialTicks());
                if (data != null) {
                    renderTrajectory(data, player, OTHER_COLOR, depth, player.getName().getString());
                    if (++processed >= 12) {
                        break;
                    }
                }
            }
        }

        if (thrownProjectiles.isValue()) {
            renderActiveProjectiles(depth);
        }

        renderStoredTrails(depth);
    }

    @EventHandler
    public void onDraw(DrawEvent event) {
        if (!showTime.isValue() || labels.isEmpty() || mc.player == null) {
            return;
        }

        DrawContext context = event.getDrawContext();
        int rendered = 0;
        for (LandingLabel label : labels) {
            if (rendered++ > 24) {
                break;
            }
            Vec3d screen = Projection.worldSpaceToScreenSpace(label.position());
            if (screen.z < 0.0D || screen.z > 1.0D) {
                continue;
            }
            drawLabel(context, (float) screen.x, (float) screen.y, label.text(), label.color());
        }
    }

    private void renderActiveProjectiles(boolean depth) {
        long now = System.currentTimeMillis();
        if (now < nextProjectileScanMs) {
            return;
        }
        nextProjectileScanMs = now + 35L;

        Box scanBox = mc.player.getBoundingBox().expand(scanRadius.getValue());
        List<ProjectileEntity> projectiles = mc.world.getEntitiesByClass(ProjectileEntity.class, scanBox, projectile -> projectile.getVelocity().lengthSquared() > 0.0025D);
        int processed = 0;
        for (ProjectileEntity projectile : projectiles) {
            if (processed++ >= 24) {
                break;
            }
            TrajectoryData data = new TrajectoryData(new Vec3d(projectile.getX(), projectile.getY(), projectile.getZ()), projectile.getVelocity(), gravityForProjectile(projectile), 0.99D, 1.0D);
            PathResult result = renderTrajectory(data, projectile, PROJECTILE_COLOR, depth, "Бросок");
            if (keepTrails.isValue() && result != null && result.points().size() > 1) {
                trails.put(projectile.getId(), new TrailSnapshot(new ArrayList<>(result.points()), now + TRAIL_LIFETIME_MS, PROJECTILE_COLOR));
            }
        }
    }

    private void renderStoredTrails(boolean depth) {
        if (!keepTrails.isValue() || trails.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Integer, TrailSnapshot>> iterator = trails.entrySet().iterator();
        while (iterator.hasNext()) {
            TrailSnapshot trail = iterator.next().getValue();
            if (trail.expiresAt() < now) {
                iterator.remove();
                continue;
            }
            float alpha = Math.max(0.15F, (trail.expiresAt() - now) / (float) TRAIL_LIFETIME_MS);
            int color = ColorUtil.multAlpha(trail.color(), alpha * 0.55F);
            drawPath(trail.points(), color, depth);
        }
    }

    private TrajectoryData resolveHeldProjectile(PlayerEntity player, float tickDelta) {
        ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
        TrajectorySettings settings = settingsFor(stack, player);
        if (settings == null) {
            stack = player.getStackInHand(Hand.OFF_HAND);
            settings = settingsFor(stack, player);
        }
        if (settings == null) {
            return null;
        }

        Vec3d look = player.getRotationVec(tickDelta).normalize();
        Vec3d start = player.getEyePos().add(look.multiply(0.32D));
        Vec3d velocity = look.multiply(settings.power());
        return new TrajectoryData(start, velocity, settings.gravity(), settings.drag(), settings.maxDistanceScale());
    }

    private TrajectorySettings settingsFor(ItemStack stack, PlayerEntity player) {
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

    private PathResult renderTrajectory(TrajectoryData data, Entity source, int color, boolean depth, String labelOwner) {
        PathResult result = calculatePath(data, source);
        if (result.points().size() < 2) {
            return null;
        }

        drawPath(result.points(), color, depth);
        if (showLanding.isValue()) {
            Vec3d landing = result.landing();
            int boxColor = result.hit() ? HIT_COLOR : WARNING_COLOR;
            Render3D.drawBox(new Box(landing.x - 0.12D, landing.y - 0.12D, landing.z - 0.12D, landing.x + 0.12D, landing.y + 0.12D, landing.z + 0.12D), boxColor, 1.8F, true, true, depth);
        }
        if (movingMarker.isValue()) {
            Vec3d marker = samplePath(result.points(), (System.currentTimeMillis() % 1200L) / 1200.0D);
            Render3D.drawBox(new Box(marker.x - 0.08D, marker.y - 0.08D, marker.z - 0.08D, marker.x + 0.08D, marker.y + 0.08D, marker.z + 0.08D), ColorUtil.multAlpha(color, 0.95F), 1.2F, true, true, depth);
        }
        if (showTime.isValue()) {
            labels.add(new LandingLabel(result.landing().add(0.0D, 0.42D, 0.0D), labelOwner + ": " + formatTicks(result.ticks()), color));
        }
        return result;
    }

    private PathResult calculatePath(TrajectoryData data, Entity source) {
        ArrayList<Vec3d> points = new ArrayList<>(Math.max(8, maxTicks.getInt()));
        Vec3d position = data.start();
        Vec3d velocity = data.velocity();
        points.add(position);
        boolean hit = false;
        int ticks = 0;
        int max = Math.max(10, maxTicks.getInt());
        for (int i = 0; i < max; i++) {
            Vec3d next = position.add(velocity.multiply(data.maxDistanceScale()));
            BlockHitResult blockHit = mc.world.raycast(new RaycastContext(position, next, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, source));
            if (blockHit != null && blockHit.getType() == HitResult.Type.BLOCK) {
                next = blockHit.getPos();
                hit = true;
            }

            points.add(next);
            ticks++;
            if (hit) {
                position = next;
                break;
            }

            position = next;
            velocity = velocity.multiply(data.drag()).subtract(0.0D, data.gravity(), 0.0D);
            if (position.y < mc.world.getBottomY() - 4.0D || position.y > mc.world.getBottomY() + 512.0D) {
                break;
            }
        }
        return new PathResult(points, position, ticks, hit);
    }

    private void drawPath(List<Vec3d> points, int color, boolean depth) {
        int fadeColor = ColorUtil.multAlpha(color, 0.35F);
        for (int i = 1; i < points.size(); i++) {
            float progress = i / (float) points.size();
            int segmentColor = ColorUtil.lerpColor(color, fadeColor, progress);
            Render3D.drawLine(points.get(i - 1), points.get(i), segmentColor, lineWidth.getValue(), depth);
        }
    }

    private Vec3d samplePath(List<Vec3d> points, double t) {
        if (points.isEmpty()) {
            return Vec3d.ZERO;
        }
        int index = Math.min(points.size() - 1, Math.max(0, (int) Math.floor(t * (points.size() - 1))));
        return points.get(index);
    }

    private double gravityForProjectile(ProjectileEntity projectile) {
        String name = projectile.getType().toString().toLowerCase(java.util.Locale.ROOT);
        if (name.contains("potion") || name.contains("experience")) {
            return 0.05D;
        }
        if (name.contains("arrow") || name.contains("trident")) {
            return 0.05D;
        }
        return 0.03D;
    }

    private String formatTicks(int ticks) {
        double seconds = Math.max(0.05D, ticks / 20.0D);
        return String.format(java.util.Locale.US, "%.2fс", seconds);
    }

    private void drawLabel(DrawContext context, float centerX, float centerY, String text, int color) {
        float size = 5.8F;
        float width = Fonts.BOLD.getWidth(text, size);
        float x = centerX - width / 2.0F - 5.0F;
        float y = centerY - 12.0F;
        Render2D.rect(x, y, width + 10.0F, 12.0F, new Color(8, 10, 16, 178).getRGB(), 4.0F);
        Render2D.outline(x, y, width + 10.0F, 12.0F, 0.35F, ColorUtil.multAlpha(color, 0.65F), 4.0F);
        Fonts.BOLD.draw(text, centerX - width / 2.0F, y + 3.0F, size, Color.WHITE.getRGB());
    }

    private record TrajectoryData(Vec3d start, Vec3d velocity, double gravity, double drag, double maxDistanceScale) {
    }

    private record TrajectorySettings(double power, double gravity, double drag, double maxDistanceScale) {
    }

    private record PathResult(List<Vec3d> points, Vec3d landing, int ticks, boolean hit) {
    }

    private record LandingLabel(Vec3d position, String text, int color) {
    }

    private record TrailSnapshot(List<Vec3d> points, long expiresAt, int color) {
    }
}
