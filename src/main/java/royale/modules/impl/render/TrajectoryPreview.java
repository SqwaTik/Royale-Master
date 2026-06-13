package royale.modules.impl.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import royale.util.ColorUtil;
import royale.util.math.Projection;
import royale.util.render.Render2D;
import royale.util.render.Render3D;
import royale.util.render.font.Fonts;

import java.awt.Color;

/**
 * Package-private renderer for the trajectory preview overlay:
 *   – landing box (точка падения)
 *   – moving marker (маркер движения)
 *   – time/label (время падения)
 *
 * All methods are stateless and operate on a {@link TrajectoryMath.Result}.
 */
final class TrajectoryPreview {

    private static final int HIT_COLOR     = new Color(94,  255, 123, 230).getRGB();
    private static final int WARNING_COLOR = new Color(255, 92,  92,  230).getRGB();

    private TrajectoryPreview() {}

    // ───────────────────────────── landing box ──────────────────────────────

    static void renderLanding(TrajectoryMath.Result result, boolean depth) {
        Vec3d p = result.landing();
        int c   = result.hit() ? HIT_COLOR : WARNING_COLOR;
        Render3D.drawBox(
            new Box(p.x - 0.12, p.y - 0.12, p.z - 0.12,
                    p.x + 0.12, p.y + 0.12, p.z + 0.12),
            c, 1.8F, true, true, depth);
    }

    // ──────────────────────────── moving marker ──────────────────────────────

    static void renderMovingMarker(TrajectoryMath.Result result, int color, boolean depth) {
        double t = (System.currentTimeMillis() % 1200L) / 1200.0;
        Vec3d m  = TrajectoryMath.sample(result.points(), t);
        Render3D.drawBox(
            new Box(m.x - 0.08, m.y - 0.08, m.z - 0.08,
                    m.x + 0.08, m.y + 0.08, m.z + 0.08),
            ColorUtil.multAlpha(color, 0.95F), 1.2F, true, true, depth);
    }

    // ─────────────────────────── screen-space label ──────────────────────────

    /**
     * Draws a rounded pill-shaped label at {@code worldPos} projected to 2-D screen space.
     * No-ops if the point is behind the camera.
     */
    static void drawLabel(DrawContext context, Vec3d worldPos, String text, int color) {
        Vec3d screen = Projection.worldSpaceToScreenSpace(worldPos);
        if (screen.z < 0.0 || screen.z > 1.0) return;

        float cx   = (float) screen.x;
        float cy   = (float) screen.y;
        float size = 5.8F;
        float tw   = Fonts.BOLD.getWidth(text, size);
        float x    = cx - tw / 2F - 5F;
        float y    = cy - 12F;

        Render2D.rect(x, y, tw + 10F, 12F,
            new Color(8, 10, 16, 178).getRGB(), 4F);
        Render2D.outline(x, y, tw + 10F, 12F, 0.35F,
            ColorUtil.multAlpha(color, 0.65F), 4F);
        Fonts.BOLD.draw(text, cx - tw / 2F, y + 3F, size, Color.WHITE.getRGB());
    }
}
