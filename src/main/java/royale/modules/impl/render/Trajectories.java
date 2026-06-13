package royale.modules.impl.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
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
import royale.util.render.Render3D;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Trajectories extends ModuleStructure {

    private static final int DEFAULT_COLOR    = new Color(108, 178, 255, 230).getRGB();
    private static final int OTHER_COLOR      = new Color(255, 186, 92,  220).getRGB();
    private static final int PROJECTILE_COLOR = new Color(166, 116, 255, 220).getRGB();
    private static final long TRAIL_LIFETIME_MS = 2500L;

    public final ColorSetting lineColor = (new ColorSetting(
            "цвет", "цвет линии своей траектории"
    )).value(DEFAULT_COLOR);
    public final BooleanSetting ownTrajectory = (new BooleanSetting(
            "своя траектория", "показывает дугу предмета в вашей руке"
    )).setValue(true);
    public final BooleanSetting otherPlayers = (new BooleanSetting(
            "другие игроки", "показывает дугу броска у игроков рядом"
    )).setValue(true);
    public final BooleanSetting thrownProjectiles = (new BooleanSetting(
            "летящие предметы", "показывает прогноз для уже брошенных предметов"
    )).setValue(true);
    public final BooleanSetting keepTrails = (new BooleanSetting(
            "след после броска", "оставляет дугу на короткое время после броска"
    )).setValue(true);
    public final BooleanSetting showLanding = (new BooleanSetting(
            "точка падения", "подсвечивает место попадания"
    )).setValue(true);
    public final BooleanSetting showTime = (new BooleanSetting(
            "время падения", "показывает через сколько предмет приземлится"
    )).setValue(true);
    public final BooleanSetting movingMarker = (new BooleanSetting(
            "маркер движения", "показывает точку, которая движется по дуге"
    )).setValue(true);
    public final BooleanSetting throughWalls = (new BooleanSetting(
            "через стены", "рисует траекторию поверх блоков"
    )).setValue(true);
    public final SliderSettings maxTicks = (new SliderSettings(
            "длина", "максимальная длина просчета в тиках"
    )).setValue(80.0F).range(20, 140);
    public final SliderSettings scanRadius = (new SliderSettings(
            "радиус игроков", "на какой дистанции показывать траектории других игроков"
    )).setValue(48.0F).range(8, 96);
    public final SliderSettings lineWidth = (new SliderSettings(
            "толщина", "толщина линии"
    )).setValue(2.0F).range(1.0F, 4.0F);

    private final List<LandingLabel>          pendingLabels = new ArrayList<>();
    private final Map<Integer, TrailSnapshot> trails        = new HashMap<>();
    private long nextProjectileScanMs;

    public Trajectories() {
        super("Trajectories",
              "Показывает дуги бросков, место и время приземления предметов",
              ModuleCategory.RENDER);
        settings(new Setting[]{
            lineColor, ownTrajectory, otherPlayers, thrownProjectiles,
            keepTrails, showLanding, showTime, movingMarker, throughWalls,
            maxTicks, scanRadius, lineWidth
        });
    }

    public static Trajectories getInstance() { return Instance.get(Trajectories.class); }

    @Override
    public void deactivate() { pendingLabels.clear(); trails.clear(); }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (mc.world == null || mc.player == null) return;
        pendingLabels.clear();
        boolean depth     = !throughWalls.isValue();
        int     baseColor = lineColor.getColor();

        if (ownTrajectory.isValue()) {
            TrajectoryMath.Data own = TrajectoryMath.fromPlayer(mc.player, event.getPartialTicks());
            if (own != null) renderTrajectory(own, mc.player, baseColor, depth, "Вы");
        }

        if (otherPlayers.isValue()) {
            double radiusSq = scanRadius.getValue() * scanRadius.getValue();
            int processed = 0;
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player == mc.player || player.squaredDistanceTo(mc.player) > radiusSq) continue;
                TrajectoryMath.Data data = TrajectoryMath.fromPlayer(player, event.getPartialTicks());
                if (data != null) {
                    renderTrajectory(data, player, OTHER_COLOR, depth, player.getName().getString());
                    if (++processed >= 12) break;
                }
            }
        }

        if (thrownProjectiles.isValue()) renderActiveProjectiles(depth);
        renderStoredTrails(depth);
    }

    @EventHandler
    public void onDraw(DrawEvent event) {
        if (!showTime.isValue() || pendingLabels.isEmpty() || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int rendered = 0;
        for (LandingLabel label : pendingLabels) {
            if (rendered++ > 24) break;
            TrajectoryPreview.drawLabel(ctx, label.position(), label.text(), label.color());
        }
    }

    private void renderActiveProjectiles(boolean depth) {
        long now = System.currentTimeMillis();
        if (now < nextProjectileScanMs) return;
        nextProjectileScanMs = now + 35L;
        Box scanBox = mc.player.getBoundingBox().expand(scanRadius.getValue());
        List<ProjectileEntity> projectiles = mc.world.getEntitiesByClass(
            ProjectileEntity.class, scanBox,
            p -> p.getVelocity().lengthSquared() > 0.0025);
        int processed = 0;
        for (ProjectileEntity projectile : projectiles) {
            if (processed++ >= 24) break;
            TrajectoryMath.Data   data   = TrajectoryMath.fromProjectile(projectile);
            TrajectoryMath.Result result = renderTrajectory(data, projectile, PROJECTILE_COLOR, depth, "Бросок");
            if (keepTrails.isValue() && result != null && result.points().size() > 1) {
                trails.put(projectile.getId(),
                    new TrailSnapshot(new ArrayList<>(result.points()), now + TRAIL_LIFETIME_MS, PROJECTILE_COLOR));
            }
        }
    }

    private void renderStoredTrails(boolean depth) {
        if (!keepTrails.isValue() || trails.isEmpty()) return;
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Integer, TrailSnapshot>> it = trails.entrySet().iterator();
        while (it.hasNext()) {
            TrailSnapshot trail = it.next().getValue();
            if (trail.expiresAt() < now) { it.remove(); continue; }
            float alpha = Math.max(0.15F, (trail.expiresAt() - now) / (float) TRAIL_LIFETIME_MS);
            drawPath(trail.points(), ColorUtil.multAlpha(trail.color(), alpha * 0.55F), depth);
        }
    }

    private TrajectoryMath.Result renderTrajectory(
            TrajectoryMath.Data data, Entity source,
            int color, boolean depth, String labelOwner) {
        TrajectoryMath.Result result = TrajectoryMath.calculate(mc.world, source, data, maxTicks.getInt());
        if (result.points().size() < 2) return null;
        drawPath(result.points(), color, depth);
        if (showLanding.isValue())  TrajectoryPreview.renderLanding(result, depth);
        if (movingMarker.isValue()) TrajectoryPreview.renderMovingMarker(result, color, depth);
        if (showTime.isValue()) {
            pendingLabels.add(new LandingLabel(
                result.landing().add(0.0, 0.42, 0.0),
                labelOwner + ": " + TrajectoryMath.formatTicks(result.ticks()),
                color));
        }
        return result;
    }

    private void drawPath(List<Vec3d> points, int color, boolean depth) {
        int fadeColor = ColorUtil.multAlpha(color, 0.35F);
        for (int i = 1; i < points.size(); i++) {
            float progress = i / (float) points.size();
            int   segColor = ColorUtil.lerpColor(color, fadeColor, progress);
            Render3D.drawLine(points.get(i - 1), points.get(i), segColor, lineWidth.getValue(), depth);
        }
    }

    private record LandingLabel(Vec3d position, String text, int color) {}
    private record TrailSnapshot(List<Vec3d> points, long expiresAt, int color) {}
}