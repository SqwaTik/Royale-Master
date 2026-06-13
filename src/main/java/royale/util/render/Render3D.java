package royale.util.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Pair;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import royale.IMinecraft;
import royale.events.impl.WorldRenderEvent;
import royale.util.ColorUtil;
import royale.util.math.MathUtils;

public final class Render3D
implements IMinecraft {
    private static final Map<VoxelShape, Pair<List<Box>, List<Line>>> SHAPE_OUTLINES = new HashMap<VoxelShape, Pair<List<Box>, List<Line>>>();
    private static final Map<VoxelShape, List<Box>> SHAPE_BOXES = new HashMap<VoxelShape, List<Box>>();
    public static final List<Line> LINE_DEPTH = new ArrayList<Line>();
    public static final List<Line> LINE = new ArrayList<Line>();
    public static final List<Quad> QUAD_DEPTH = new ArrayList<Quad>();
    public static final List<Quad> QUAD = new ArrayList<Quad>();
    public static final List<GradientQuad> GRADIENT_QUAD = new ArrayList<GradientQuad>();
    public static final List<GradientQuad> GRADIENT_QUAD_DEPTH = new ArrayList<GradientQuad>();
    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final Matrix4f lastModMat = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();
    public static MatrixStack.Entry lastWorldSpaceEntry = new MatrixStack().peek();
    public static float lastTickDelta = 1.0f;
    public static Vec3d lastCameraPos = Vec3d.ZERO;
    public static Quaternionf lastCameraRotation = new Quaternionf();
    private static float espValue = 1.0f;
    private static float espSpeed = 1.0f;
    private static float prevEspValue;
    private static float circleStep;
    private static boolean flipSpeed;
    private static double smoothY;
    private static double smoothY2;

    public static void updateTargetEsp(float deltaTime) {
        prevEspValue = espValue;
        espValue += espSpeed * deltaTime;
        if (espSpeed > 25.0f) {
            flipSpeed = true;
        }
        if (espSpeed < -25.0f) {
            flipSpeed = false;
        }
        espSpeed = flipSpeed ? espSpeed - 0.5f * deltaTime : espSpeed + 0.5f * deltaTime;
        circleStep += 0.06f * deltaTime;
    }

    public static void updateTargetEsp() {
        Render3D.updateTargetEsp(1.0f);
    }

    public static float getEspValue() {
        return espValue;
    }

    public static float getPrevEspValue() {
        return prevEspValue;
    }

    public static float getCircleStep() {
        return circleStep;
    }

    private static double easeInOutSine(double t) {
        return -(Math.cos(Math.PI * t) - 1.0) / 2.0;
    }

    private static double smoothSinAnimation(double input) {
        double sin = (Math.sin(input) + 1.0) / 2.0;
        return Render3D.easeInOutSine(sin);
    }

    public static void onWorldRender(WorldRenderEvent e) {
        if (Render3D.mc.world == null || Render3D.mc.player == null) {
            return;
        }
        MatrixStack matrices = e.getStack();
        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        Vec3d cameraPos = lastCameraPos;
        Render3D.renderGradientQuads(matrices, immediate, cameraPos);
        Render3D.renderQuads(matrices, immediate, cameraPos);
        Render3D.renderLines(matrices, immediate, cameraPos);
        immediate.draw();
    }

    private static void renderLines(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, Vec3d cameraPos) {
        if (LINE.isEmpty() && LINE_DEPTH.isEmpty()) {
            return;
        }
        VertexConsumer buffer = immediate.getBuffer(RenderLayers.lines());
        for (Line line : LINE) {
            Render3D.drawLineVertex(matrices, buffer, line, cameraPos);
        }
        for (Line line : LINE_DEPTH) {
            Render3D.drawLineVertex(matrices, buffer, line, cameraPos);
        }
        LINE.clear();
        LINE_DEPTH.clear();
    }

    private static void drawLineVertex(MatrixStack matrices, VertexConsumer buffer, Line line, Vec3d cameraPos) {
        MatrixStack.Entry entry = matrices.peek();
        Vector3f normal = Render3D.getNormal(line.start.toVector3f(), line.end.toVector3f());
        float x1 = (float)(line.start.x - cameraPos.x);
        float y1 = (float)(line.start.y - cameraPos.y);
        float z1 = (float)(line.start.z - cameraPos.z);
        float x2 = (float)(line.end.x - cameraPos.x);
        float y2 = (float)(line.end.y - cameraPos.y);
        float z2 = (float)(line.end.z - cameraPos.z);
        buffer.vertex(entry, x1, y1, z1).color(line.colorStart).normal(entry, normal).lineWidth(line.width);
        buffer.vertex(entry, x2, y2, z2).color(line.colorEnd).normal(entry, normal).lineWidth(line.width);
    }

    private static void renderQuads(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, Vec3d cameraPos) {
        if (QUAD.isEmpty() && QUAD_DEPTH.isEmpty()) {
            return;
        }
        VertexConsumer buffer = immediate.getBuffer(RenderLayers.debugFilledBox());
        for (Quad quad : QUAD) {
            Render3D.drawQuadVertex(matrices, buffer, quad, cameraPos);
        }
        for (Quad quad : QUAD_DEPTH) {
            Render3D.drawQuadVertex(matrices, buffer, quad, cameraPos);
        }
        QUAD.clear();
        QUAD_DEPTH.clear();
    }

    private static void drawQuadVertex(MatrixStack matrices, VertexConsumer buffer, Quad quad, Vec3d cameraPos) {
        MatrixStack.Entry entry = matrices.peek();
        float x1 = (float)(quad.x.x - cameraPos.x);
        float y1 = (float)(quad.x.y - cameraPos.y);
        float z1 = (float)(quad.x.z - cameraPos.z);
        float x2 = (float)(quad.y.x - cameraPos.x);
        float y2 = (float)(quad.y.y - cameraPos.y);
        float z2 = (float)(quad.y.z - cameraPos.z);
        float x3 = (float)(quad.w.x - cameraPos.x);
        float y3 = (float)(quad.w.y - cameraPos.y);
        float z3 = (float)(quad.w.z - cameraPos.z);
        float x4 = (float)(quad.z.x - cameraPos.x);
        float y4 = (float)(quad.z.y - cameraPos.y);
        float z4 = (float)(quad.z.z - cameraPos.z);
        buffer.vertex(entry, x1, y1, z1).color(quad.color);
        buffer.vertex(entry, x2, y2, z2).color(quad.color);
        buffer.vertex(entry, x3, y3, z3).color(quad.color);
        buffer.vertex(entry, x4, y4, z4).color(quad.color);
    }

    private static void renderGradientQuads(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, Vec3d cameraPos) {
        if (GRADIENT_QUAD.isEmpty() && GRADIENT_QUAD_DEPTH.isEmpty()) {
            return;
        }
        VertexConsumer buffer = immediate.getBuffer(RenderLayers.debugFilledBox());
        for (GradientQuad quad : GRADIENT_QUAD) {
            Render3D.drawGradientQuadVertex(matrices, buffer, quad, cameraPos);
        }
        for (GradientQuad quad : GRADIENT_QUAD_DEPTH) {
            Render3D.drawGradientQuadVertex(matrices, buffer, quad, cameraPos);
        }
        GRADIENT_QUAD.clear();
        GRADIENT_QUAD_DEPTH.clear();
    }

    private static void drawGradientQuadVertex(MatrixStack matrices, VertexConsumer buffer, GradientQuad quad, Vec3d cameraPos) {
        MatrixStack.Entry entry = matrices.peek();
        float x1 = (float)(quad.p1.x - cameraPos.x);
        float y1 = (float)(quad.p1.y - cameraPos.y);
        float z1 = (float)(quad.p1.z - cameraPos.z);
        float x2 = (float)(quad.p2.x - cameraPos.x);
        float y2 = (float)(quad.p2.y - cameraPos.y);
        float z2 = (float)(quad.p2.z - cameraPos.z);
        float x3 = (float)(quad.p3.x - cameraPos.x);
        float y3 = (float)(quad.p3.y - cameraPos.y);
        float z3 = (float)(quad.p3.z - cameraPos.z);
        float x4 = (float)(quad.p4.x - cameraPos.x);
        float y4 = (float)(quad.p4.y - cameraPos.y);
        float z4 = (float)(quad.p4.z - cameraPos.z);
        buffer.vertex(entry, x1, y1, z1).color(quad.c1);
        buffer.vertex(entry, x2, y2, z2).color(quad.c2);
        buffer.vertex(entry, x3, y3, z3).color(quad.c3);
        buffer.vertex(entry, x4, y4, z4).color(quad.c4);
    }

    public static void drawCircle(MatrixStack matrix, LivingEntity lastTarget, float anim, float red, int baseColor1, int baseColor2) {
        double cs = MathUtils.interpolate((double)circleStep - 0.17, (double)circleStep);
        Vec3d target = MathUtils.interpolate((Entity)lastTarget);
        boolean canSee = Render3D.mc.player != null && Render3D.mc.player.canSee((Entity)lastTarget);
        float hitEffect = Math.min(red * 2.0f, 1.0f);
        float distanceMultiplier = 1.0f + (float)Math.sin((double)hitEffect * Math.PI) * 0.18f;
        int size = 64;
        float entityWidth = lastTarget.getWidth() * distanceMultiplier;
        float entityHeight = lastTarget.getHeight();
        double targetY = Render3D.smoothSinAnimation(cs) * (double)entityHeight;
        double targetY2 = Render3D.smoothSinAnimation(cs - 0.35) * (double)entityHeight;
        smoothY = Render3D.lerp(smoothY, targetY, 0.12);
        smoothY2 = Render3D.lerp(smoothY2, targetY2, 0.1);
        int color1 = ColorUtil.multRed(baseColor1, 1.0f + red * 125.0f);
        int color2 = ColorUtil.multRed(baseColor2, 1.0f + red * 125.0f);
        for (int i = 0; i < size; ++i) {
            float t = (float)i / (float)size;
            float tNext = (float)((i + 1) % size) / (float)size;
            float gradientT = (float)(0.5 - 0.5 * Math.cos((double)t * Math.PI * 2.0));
            float gradientTNext = (float)(0.5 - 0.5 * Math.cos((double)tNext * Math.PI * 2.0));
            int currentColor = ColorUtil.lerpColor(color1, color2, gradientT);
            int nextColor = ColorUtil.lerpColor(color1, color2, gradientTNext);
            int brightColor = ColorUtil.multAlpha(currentColor, 0.8f * anim);
            int brightColorNext = ColorUtil.multAlpha(nextColor, 0.8f * anim);
            int fadeColor = ColorUtil.multAlpha(currentColor, 0.0f);
            int fadeColorNext = ColorUtil.multAlpha(nextColor, 0.0f);
            Vec3d cosSin = MathUtils.cosSin(i, size, entityWidth);
            Vec3d nextCosSin = MathUtils.cosSin((i + 1) % size, size, entityWidth);
            Vec3d circlePoint = target.add(cosSin.x, smoothY, cosSin.z);
            Vec3d trailPoint = target.add(cosSin.x, smoothY2, cosSin.z);
            Vec3d nextCirclePoint = target.add(nextCosSin.x, smoothY, nextCosSin.z);
            Vec3d nextTrailPoint = target.add(nextCosSin.x, smoothY2, nextCosSin.z);
            Render3D.drawGradientQuad(circlePoint, nextCirclePoint, nextTrailPoint, trailPoint, brightColor, brightColorNext, fadeColorNext, fadeColor, canSee);
            Render3D.drawGradientQuad(trailPoint, nextTrailPoint, nextCirclePoint, circlePoint, fadeColor, fadeColorNext, brightColorNext, brightColor, canSee);
            int trailColorTop = ColorUtil.multAlpha(currentColor, 0.15f * anim);
            int trailColorBottom = ColorUtil.multAlpha(currentColor, 0.0f);
            Render3D.drawLineGradient(circlePoint, trailPoint, trailColorTop, trailColorBottom, 6.0f, canSee);
            int circleColor = ColorUtil.multAlpha(currentColor, 1.0f * anim);
            int circleColorNext = ColorUtil.multAlpha(nextColor, 1.0f * anim);
            Render3D.drawLineGradient(circlePoint, nextCirclePoint, circleColor, circleColorNext, 2.0f, canSee);
        }
    }

    public static void drawRadiusCircle(Vec3d center, float radius, int color) {
        if (Render3D.mc.player == null) {
            return;
        }
        double baseY = center.y;
        int fillColor = ColorUtil.multAlpha(color, 0.25f);
        int radiusInt = (int)Math.ceil(radius) + 1;
        for (int dx = -radiusInt; dx <= radiusInt; ++dx) {
            for (int dz = -radiusInt; dz <= radiusInt; ++dz) {
                boolean hasCornerInside = false;
                boolean hasCornerOutside = false;
                for (double ox = -0.5; ox <= 0.5; ox += 1.0) {
                    for (double oz = -0.5; oz <= 0.5; oz += 1.0) {
                        double cornerDist = Math.sqrt(((double)dx + ox) * ((double)dx + ox) + ((double)dz + oz) * ((double)dz + oz));
                        if (cornerDist <= (double)radius) {
                            hasCornerInside = true;
                            continue;
                        }
                        hasCornerOutside = true;
                    }
                }
                if (!hasCornerInside || !hasCornerOutside) continue;
                double x = center.x + (double)dx;
                double z = center.z + (double)dz;
                Box box = new Box(x - 0.5, baseY, z - 0.5, x + 0.5, baseY + 1.0, z + 0.5);
                Render3D.drawBoxWithCross(box, color, fillColor, 2.0f);
            }
        }
    }

    public static void drawBoxWithCross(Box box, int lineColor, int fillColor, float lineWidth) {
        double x1 = box.minX;
        double y1 = box.minY;
        double z1 = box.minZ;
        double x2 = box.maxX;
        double y2 = box.maxY;
        double z2 = box.maxZ;
        Render3D.drawQuad(new Vec3d(x1, y1, z1), new Vec3d(x2, y1, z1), new Vec3d(x2, y1, z2), new Vec3d(x1, y1, z2), fillColor, false);
        Render3D.drawQuad(new Vec3d(x1, y1, z1), new Vec3d(x1, y2, z1), new Vec3d(x2, y2, z1), new Vec3d(x2, y1, z1), fillColor, false);
        Render3D.drawQuad(new Vec3d(x2, y1, z1), new Vec3d(x2, y2, z1), new Vec3d(x2, y2, z2), new Vec3d(x2, y1, z2), fillColor, false);
        Render3D.drawQuad(new Vec3d(x1, y1, z2), new Vec3d(x2, y1, z2), new Vec3d(x2, y2, z2), new Vec3d(x1, y2, z2), fillColor, false);
        Render3D.drawQuad(new Vec3d(x1, y1, z1), new Vec3d(x1, y1, z2), new Vec3d(x1, y2, z2), new Vec3d(x1, y2, z1), fillColor, false);
        Render3D.drawQuad(new Vec3d(x1, y2, z1), new Vec3d(x1, y2, z2), new Vec3d(x2, y2, z2), new Vec3d(x2, y2, z1), fillColor, false);
        Render3D.drawLine(new Vec3d(x1, y1, z1), new Vec3d(x2, y1, z1), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x2, y1, z1), new Vec3d(x2, y1, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x2, y1, z2), new Vec3d(x1, y1, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x1, y1, z2), new Vec3d(x1, y1, z1), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x1, y1, z2), new Vec3d(x1, y2, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x1, y1, z1), new Vec3d(x1, y2, z1), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x2, y1, z2), new Vec3d(x2, y2, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x2, y1, z1), new Vec3d(x2, y2, z1), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x1, y2, z1), new Vec3d(x2, y2, z1), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x2, y2, z1), new Vec3d(x2, y2, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x2, y2, z2), new Vec3d(x1, y2, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x1, y2, z2), new Vec3d(x1, y2, z1), lineColor, lineWidth, false);
        int crossColor = ColorUtil.multAlpha(lineColor, 0.6f);
        float crossWidth = lineWidth * 0.8f;
        Render3D.drawLine(new Vec3d(x1, y1, z1), new Vec3d(x2, y1, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x2, y1, z1), new Vec3d(x1, y1, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x1, y2, z1), new Vec3d(x2, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x2, y2, z1), new Vec3d(x1, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x1, y1, z1), new Vec3d(x2, y2, z1), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x2, y1, z1), new Vec3d(x1, y2, z1), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x1, y1, z2), new Vec3d(x2, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x2, y1, z2), new Vec3d(x1, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x1, y1, z1), new Vec3d(x1, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x1, y1, z2), new Vec3d(x1, y2, z1), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x2, y1, z1), new Vec3d(x2, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x2, y1, z2), new Vec3d(x2, y2, z1), crossColor, crossWidth, false);
    }

    public static void drawBoxWithCrossFull(Box box, int lineColor, int fillColor, float lineWidth) {
        double x1 = box.minX;
        double y1 = box.minY;
        double z1 = box.minZ;
        double x2 = box.maxX;
        double y2 = box.maxY;
        double z2 = box.maxZ;
        Render3D.drawQuad(new Vec3d(x1, y1, z1), new Vec3d(x2, y1, z1), new Vec3d(x2, y1, z2), new Vec3d(x1, y1, z2), fillColor, false);
        Render3D.drawQuad(new Vec3d(x1, y1, z1), new Vec3d(x1, y2, z1), new Vec3d(x2, y2, z1), new Vec3d(x2, y1, z1), fillColor, false);
        Render3D.drawQuad(new Vec3d(x2, y1, z1), new Vec3d(x2, y2, z1), new Vec3d(x2, y2, z2), new Vec3d(x2, y1, z2), fillColor, false);
        Render3D.drawQuad(new Vec3d(x1, y1, z2), new Vec3d(x2, y1, z2), new Vec3d(x2, y2, z2), new Vec3d(x1, y2, z2), fillColor, false);
        Render3D.drawQuad(new Vec3d(x1, y1, z1), new Vec3d(x1, y1, z2), new Vec3d(x1, y2, z2), new Vec3d(x1, y2, z1), fillColor, false);
        Render3D.drawQuad(new Vec3d(x1, y2, z1), new Vec3d(x1, y2, z2), new Vec3d(x2, y2, z2), new Vec3d(x2, y2, z1), fillColor, false);
        Render3D.drawQuad(new Vec3d(x1, y1, z2), new Vec3d(x2, y1, z2), new Vec3d(x2, y1, z1), new Vec3d(x1, y1, z1), fillColor, false);
        Render3D.drawQuad(new Vec3d(x2, y1, z1), new Vec3d(x2, y2, z1), new Vec3d(x1, y2, z1), new Vec3d(x1, y1, z1), fillColor, false);
        Render3D.drawQuad(new Vec3d(x2, y1, z2), new Vec3d(x2, y2, z2), new Vec3d(x2, y2, z1), new Vec3d(x2, y1, z1), fillColor, false);
        Render3D.drawQuad(new Vec3d(x1, y2, z2), new Vec3d(x2, y2, z2), new Vec3d(x2, y1, z2), new Vec3d(x1, y1, z2), fillColor, false);
        Render3D.drawQuad(new Vec3d(x1, y2, z1), new Vec3d(x1, y2, z2), new Vec3d(x1, y1, z2), new Vec3d(x1, y1, z1), fillColor, false);
        Render3D.drawQuad(new Vec3d(x2, y2, z1), new Vec3d(x2, y2, z2), new Vec3d(x1, y2, z2), new Vec3d(x1, y2, z1), fillColor, false);
        Render3D.drawLine(new Vec3d(x1, y1, z1), new Vec3d(x2, y1, z1), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x2, y1, z1), new Vec3d(x2, y1, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x2, y1, z2), new Vec3d(x1, y1, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x1, y1, z2), new Vec3d(x1, y1, z1), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x1, y1, z2), new Vec3d(x1, y2, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x1, y1, z1), new Vec3d(x1, y2, z1), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x2, y1, z2), new Vec3d(x2, y2, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x2, y1, z1), new Vec3d(x2, y2, z1), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x1, y2, z1), new Vec3d(x2, y2, z1), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x2, y2, z1), new Vec3d(x2, y2, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x2, y2, z2), new Vec3d(x1, y2, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3d(x1, y2, z2), new Vec3d(x1, y2, z1), lineColor, lineWidth, false);
        int crossColor = ColorUtil.multAlpha(lineColor, 0.6f);
        float crossWidth = lineWidth * 0.8f;
        Render3D.drawLine(new Vec3d(x1, y1, z1), new Vec3d(x2, y1, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x2, y1, z1), new Vec3d(x1, y1, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x1, y2, z1), new Vec3d(x2, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x2, y2, z1), new Vec3d(x1, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x1, y1, z1), new Vec3d(x2, y2, z1), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x2, y1, z1), new Vec3d(x1, y2, z1), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x1, y1, z2), new Vec3d(x2, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x2, y1, z2), new Vec3d(x1, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x1, y1, z1), new Vec3d(x1, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x1, y1, z2), new Vec3d(x1, y2, z1), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x2, y1, z1), new Vec3d(x2, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3d(x2, y1, z2), new Vec3d(x2, y2, z1), crossColor, crossWidth, false);
    }

    public static void drawPlastShape(BlockPos playerPos, Vec3d smooth, int lineColor, int fillColor) {
        if (Render3D.mc.player == null) {
            return;
        }
        float yaw = MathHelper.wrapDegrees((float)Render3D.mc.player.getYaw());
        if (Math.abs(Render3D.mc.player.getPitch()) > 60.0f) {
            BlockPos blockPos = playerPos.up().offset(Render3D.mc.player.getFacing(), 3);
            Vec3d pos1 = Vec3d.of((Vec3i)blockPos.east(3).south(3).down()).add(smooth);
            Vec3d pos2 = Vec3d.of((Vec3i)blockPos.west(2).north(2).up()).add(smooth);
            Render3D.drawBoxWithCrossFull(new Box(pos1, pos2), lineColor, fillColor, 3.0f);
        } else if (yaw <= -157.5f || yaw >= 157.5f) {
            BlockPos blockPos = playerPos.north(3).up();
            Vec3d pos1 = Vec3d.of((Vec3i)blockPos.down(2).east(3)).add(smooth);
            Vec3d pos2 = Vec3d.of((Vec3i)blockPos.up(3).west(2).south(2)).add(smooth);
            Render3D.drawBoxWithCrossFull(new Box(pos1, pos2), lineColor, fillColor, 3.0f);
        } else if (yaw <= -112.5f) {
            Render3D.drawSidePlast(playerPos.east(5).south().down(), smooth, lineColor, fillColor, -1, true);
        } else if (yaw <= -67.5f) {
            BlockPos blockPos = playerPos.east(2).up();
            Vec3d pos1 = Vec3d.of((Vec3i)blockPos.down(2).south(3)).add(smooth);
            Vec3d pos2 = Vec3d.of((Vec3i)blockPos.up(3).north(2).east(2)).add(smooth);
            Render3D.drawBoxWithCrossFull(new Box(pos1, pos2), lineColor, fillColor, 3.0f);
        } else if (yaw <= -22.5f) {
            Render3D.drawSidePlast(playerPos.east(5).down(), smooth, lineColor, fillColor, 1, false);
        } else if ((double)yaw >= -22.5 && (double)yaw <= 22.5) {
            BlockPos blockPos = playerPos.south(2).up();
            Vec3d pos1 = Vec3d.of((Vec3i)blockPos.down(2).east(3)).add(smooth);
            Vec3d pos2 = Vec3d.of((Vec3i)blockPos.up(3).west(2).south(2)).add(smooth);
            Render3D.drawBoxWithCrossFull(new Box(pos1, pos2), lineColor, fillColor, 3.0f);
        } else if (yaw <= 67.5f) {
            Render3D.drawSidePlast(playerPos.west(4).down(), smooth, lineColor, fillColor, 1, true);
        } else if (yaw <= 112.5f) {
            BlockPos blockPos = playerPos.west(3).up();
            Vec3d pos1 = Vec3d.of((Vec3i)blockPos.down(2).south(3)).add(smooth);
            Vec3d pos2 = Vec3d.of((Vec3i)blockPos.up(3).north(2).east(2)).add(smooth);
            Render3D.drawBoxWithCrossFull(new Box(pos1, pos2), lineColor, fillColor, 3.0f);
        } else if (yaw <= 157.5f) {
            Render3D.drawSidePlast(playerPos.west(4).south().down(), smooth, lineColor, fillColor, -1, false);
        }
    }

    private static void drawSidePlast(BlockPos blockPos, Vec3d smooth, int lineColor, int fillColor, int i, boolean ff) {
        int f;
        Vec3d p2;
        Vec3d p1;
        int f2;
        Vec3d vec3d = Vec3d.of((Vec3i)blockPos).add(smooth);
        int crossColor = ColorUtil.multAlpha(lineColor, 0.6f);
        ArrayList<Vec3d> horizontalPoints = new ArrayList<Vec3d>();
        float x = ff ? (float)i : (float)(-i);
        Vec3d current = vec3d;
        horizontalPoints.add(current);
        current = current.add((double)x, 0.0, 0.0);
        horizontalPoints.add(current);
        for (f2 = 0; f2 < 4; ++f2) {
            current = current.add(0.0, 0.0, (double)i);
            horizontalPoints.add(current);
            current = current.add((double)x, 0.0, 0.0);
            horizontalPoints.add(current);
        }
        current = current.add(0.0, 0.0, (double)i);
        horizontalPoints.add(current);
        current = current.add((double)(x * -2.0f), 0.0, 0.0);
        horizontalPoints.add(current);
        for (f2 = 0; f2 < 3; ++f2) {
            current = current.add(0.0, 0.0, (double)(i * -1));
            horizontalPoints.add(current);
            current = current.add((double)(x * -1.0f), 0.0, 0.0);
            horizontalPoints.add(current);
        }
        current = current.add(0.0, 0.0, (double)(i * -2));
        horizontalPoints.add(current);
        for (int p = 0; p < horizontalPoints.size() - 1; ++p) {
            p1 = (Vec3d)horizontalPoints.get(p);
            p2 = (Vec3d)horizontalPoints.get(p + 1);
            Render3D.drawLine(p1, p2, lineColor, 2.0f, false);
            Render3D.drawLine(p1.add(0.0, 5.0, 0.0), p2.add(0.0, 5.0, 0.0), lineColor, 2.0f, false);
        }
        for (Vec3d point : horizontalPoints) {
            Render3D.drawLine(point, point.add(0.0, 5.0, 0.0), lineColor, 2.0f, false);
        }
        for (int p = 0; p < horizontalPoints.size() - 1; ++p) {
            p1 = (Vec3d)horizontalPoints.get(p);
            p2 = (Vec3d)horizontalPoints.get(p + 1);
            Vec3d p1Top = p1.add(0.0, 5.0, 0.0);
            Vec3d p2Top = p2.add(0.0, 5.0, 0.0);
            Render3D.drawQuad(p1, p2, p2Top, p1Top, fillColor, false);
            Render3D.drawQuad(p1Top, p2Top, p2, p1, fillColor, false);
            Render3D.drawLine(p1, p2Top, crossColor, 1.6f, false);
            Render3D.drawLine(p2, p1Top, crossColor, 1.6f, false);
        }
        current = vec3d;
        Render3D.drawQuad(current, current.add((double)x, 0.0, 0.0), current.add((double)x, 0.0, (double)(i * 2)), current.add(0.0, 0.0, (double)(i * 2)), fillColor, false);
        Render3D.drawQuad(current.add(0.0, 0.0, (double)(i * 2)), current.add((double)x, 0.0, (double)(i * 2)), current.add((double)x, 0.0, 0.0), current, fillColor, false);
        Render3D.drawLine(current, current.add((double)x, 0.0, (double)(i * 2)), crossColor, 1.6f, false);
        Render3D.drawLine(current.add((double)x, 0.0, 0.0), current.add(0.0, 0.0, (double)(i * 2)), crossColor, 1.6f, false);
        for (f = 0; f < 3; ++f) {
            current = current.add((double)x, 0.0, (double)i);
            Render3D.drawQuad(current, current.add((double)x, 0.0, 0.0), current.add((double)x, 0.0, (double)(i * 2)), current.add(0.0, 0.0, (double)(i * 2)), fillColor, false);
            Render3D.drawQuad(current.add(0.0, 0.0, (double)(i * 2)), current.add((double)x, 0.0, (double)(i * 2)), current.add((double)x, 0.0, 0.0), current, fillColor, false);
            Render3D.drawLine(current, current.add((double)x, 0.0, (double)(i * 2)), crossColor, 1.6f, false);
            Render3D.drawLine(current.add((double)x, 0.0, 0.0), current.add(0.0, 0.0, (double)(i * 2)), crossColor, 1.6f, false);
        }
        current = current.add((double)x, 0.0, (double)i);
        Render3D.drawQuad(current, current.add((double)x, 0.0, 0.0), current.add((double)x, 0.0, (double)i), current.add(0.0, 0.0, (double)i), fillColor, false);
        Render3D.drawQuad(current.add(0.0, 0.0, (double)i), current.add((double)x, 0.0, (double)i), current.add((double)x, 0.0, 0.0), current, fillColor, false);
        Render3D.drawLine(current, current.add((double)x, 0.0, (double)i), crossColor, 1.6f, false);
        Render3D.drawLine(current.add((double)x, 0.0, 0.0), current.add(0.0, 0.0, (double)i), crossColor, 1.6f, false);
        current = vec3d.add(0.0, 5.0, 0.0);
        Render3D.drawQuad(current, current.add(0.0, 0.0, (double)(i * 2)), current.add((double)x, 0.0, (double)(i * 2)), current.add((double)x, 0.0, 0.0), fillColor, false);
        Render3D.drawQuad(current.add((double)x, 0.0, 0.0), current.add((double)x, 0.0, (double)(i * 2)), current.add(0.0, 0.0, (double)(i * 2)), current, fillColor, false);
        Render3D.drawLine(current, current.add((double)x, 0.0, (double)(i * 2)), crossColor, 1.6f, false);
        Render3D.drawLine(current.add((double)x, 0.0, 0.0), current.add(0.0, 0.0, (double)(i * 2)), crossColor, 1.6f, false);
        for (f = 0; f < 3; ++f) {
            current = current.add((double)x, 0.0, (double)i);
            Render3D.drawQuad(current, current.add(0.0, 0.0, (double)(i * 2)), current.add((double)x, 0.0, (double)(i * 2)), current.add((double)x, 0.0, 0.0), fillColor, false);
            Render3D.drawQuad(current.add((double)x, 0.0, 0.0), current.add((double)x, 0.0, (double)(i * 2)), current.add(0.0, 0.0, (double)(i * 2)), current, fillColor, false);
            Render3D.drawLine(current, current.add((double)x, 0.0, (double)(i * 2)), crossColor, 1.6f, false);
            Render3D.drawLine(current.add((double)x, 0.0, 0.0), current.add(0.0, 0.0, (double)(i * 2)), crossColor, 1.6f, false);
        }
        current = current.add((double)x, 0.0, (double)i);
        Render3D.drawQuad(current, current.add(0.0, 0.0, (double)i), current.add((double)x, 0.0, (double)i), current.add((double)x, 0.0, 0.0), fillColor, false);
        Render3D.drawQuad(current.add((double)x, 0.0, 0.0), current.add((double)x, 0.0, (double)i), current.add(0.0, 0.0, (double)i), current, fillColor, false);
        Render3D.drawLine(current, current.add((double)x, 0.0, (double)i), crossColor, 1.6f, false);
        Render3D.drawLine(current.add((double)x, 0.0, 0.0), current.add(0.0, 0.0, (double)i), crossColor, 1.6f, false);
    }

    private static double lerp(double start, double end, double delta) {
        return start + (end - start) * delta;
    }

    public static void drawGradientQuad(Vec3d p1, Vec3d p2, Vec3d p3, Vec3d p4, int c1, int c2, int c3, int c4, boolean depth) {
        GradientQuad quad = new GradientQuad(p1, p2, p3, p4, c1, c2, c3, c4);
        if (depth) {
            GRADIENT_QUAD_DEPTH.add(quad);
        } else {
            GRADIENT_QUAD.add(quad);
        }
    }

    public static void drawLineGradient(Vec3d start, Vec3d end, int colorStart, int colorEnd, float width, boolean depth) {
        Line line = new Line(null, start, end, colorStart, colorEnd, width);
        if (depth) {
            LINE_DEPTH.add(line);
        } else {
            LINE.add(line);
        }
    }

    public static Vector3f getNormal(Vector3f start, Vector3f end) {
        Vector3f normal = new Vector3f((Vector3fc)start).sub((Vector3fc)end);
        float sqrt = MathHelper.sqrt((float)normal.lengthSquared());
        if (sqrt < 1.0E-4f) {
            return new Vector3f(0.0f, 1.0f, 0.0f);
        }
        return normal.div(sqrt);
    }

    public static void drawShape(BlockPos blockPos, VoxelShape voxelShape, int color, float width) {
        Render3D.drawShape(blockPos, voxelShape, color, width, true, false);
    }

    public static void drawShape(BlockPos blockPos, VoxelShape voxelShape, int color, float width, boolean fill, boolean depth) {
        List<Box> boxes = SHAPE_BOXES.computeIfAbsent(voxelShape, VoxelShape::getBoundingBoxes);
        boxes.forEach(box -> {
            Box offsetBox = box.offset(blockPos);
            Render3D.drawBox(offsetBox, color, width, true, fill, depth);
        });
    }

    public static void drawShapeAlternative(BlockPos blockPos, VoxelShape voxelShape, int color, float width, boolean fill, boolean depth) {
        Vec3d offset = Vec3d.of((Vec3i)blockPos);
        Pair<List<Box>, List<Line>> shapeData = SHAPE_OUTLINES.computeIfAbsent(voxelShape, shape -> {
            ArrayList lines = new ArrayList();
            shape.forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> lines.add(new Line(null, new Vec3d(minX, minY, minZ), new Vec3d(maxX, maxY, maxZ), 0, 0, 0.0f)));
            return new Pair((Object)shape.getBoundingBoxes(), lines);
        });
        if (fill) {
            shapeData.getLeft().forEach(box -> Render3D.drawBox(box.offset(offset), color, width, false, true, depth));
        }
        shapeData.getRight().forEach(line -> Render3D.drawLine(line.start().add(offset), line.end().add(offset), color, width, depth));
    }

    public static void drawBox(Box box, int color, float width) {
        Render3D.drawBox(box, color, width, true, true, false);
    }

    public static void drawBox(Box box, int color, float width, boolean line, boolean fill, boolean depth) {
        Render3D.drawBox(null, box, color, width, line, fill, depth);
    }

    public static void drawBox(MatrixStack.Entry entry, Box box, int color, float width, boolean line, boolean fill, boolean depth) {
        double x1 = box.minX;
        double y1 = box.minY;
        double z1 = box.minZ;
        double x2 = box.maxX;
        double y2 = box.maxY;
        double z2 = box.maxZ;
        if (fill) {
            int fillColor = ColorUtil.multAlpha(color, 0.3f);
            Render3D.drawQuad(entry, new Vec3d(x1, y1, z1), new Vec3d(x2, y1, z1), new Vec3d(x2, y1, z2), new Vec3d(x1, y1, z2), fillColor, depth);
            Render3D.drawQuad(entry, new Vec3d(x1, y1, z1), new Vec3d(x1, y2, z1), new Vec3d(x2, y2, z1), new Vec3d(x2, y1, z1), fillColor, depth);
            Render3D.drawQuad(entry, new Vec3d(x2, y1, z1), new Vec3d(x2, y2, z1), new Vec3d(x2, y2, z2), new Vec3d(x2, y1, z2), fillColor, depth);
            Render3D.drawQuad(entry, new Vec3d(x1, y1, z2), new Vec3d(x2, y1, z2), new Vec3d(x2, y2, z2), new Vec3d(x1, y2, z2), fillColor, depth);
            Render3D.drawQuad(entry, new Vec3d(x1, y1, z1), new Vec3d(x1, y1, z2), new Vec3d(x1, y2, z2), new Vec3d(x1, y2, z1), fillColor, depth);
            Render3D.drawQuad(entry, new Vec3d(x1, y2, z1), new Vec3d(x1, y2, z2), new Vec3d(x2, y2, z2), new Vec3d(x2, y2, z1), fillColor, depth);
        }
        if (line) {
            Render3D.drawLine(entry, x1, y1, z1, x2, y1, z1, color, width, depth);
            Render3D.drawLine(entry, x2, y1, z1, x2, y1, z2, color, width, depth);
            Render3D.drawLine(entry, x2, y1, z2, x1, y1, z2, color, width, depth);
            Render3D.drawLine(entry, x1, y1, z2, x1, y1, z1, color, width, depth);
            Render3D.drawLine(entry, x1, y1, z2, x1, y2, z2, color, width, depth);
            Render3D.drawLine(entry, x1, y1, z1, x1, y2, z1, color, width, depth);
            Render3D.drawLine(entry, x2, y1, z2, x2, y2, z2, color, width, depth);
            Render3D.drawLine(entry, x2, y1, z1, x2, y2, z1, color, width, depth);
            Render3D.drawLine(entry, x1, y2, z1, x2, y2, z1, color, width, depth);
            Render3D.drawLine(entry, x2, y2, z1, x2, y2, z2, color, width, depth);
            Render3D.drawLine(entry, x2, y2, z2, x1, y2, z2, color, width, depth);
            Render3D.drawLine(entry, x1, y2, z2, x1, y2, z1, color, width, depth);
        }
    }

    public static void drawLine(MatrixStack.Entry entry, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int color, float width, boolean depth) {
        Render3D.drawLine(entry, new Vec3d(minX, minY, minZ), new Vec3d(maxX, maxY, maxZ), color, color, width, depth);
    }

    public static void drawLine(Vec3d start, Vec3d end, int color, float width, boolean depth) {
        Render3D.drawLine(null, start, end, color, color, width, depth);
    }

    public static void drawLine(MatrixStack.Entry entry, Vec3d start, Vec3d end, int colorStart, int colorEnd, float width, boolean depth) {
        Line line = new Line(entry, start, end, colorStart, colorEnd, width);
        if (depth) {
            LINE_DEPTH.add(line);
        } else {
            LINE.add(line);
        }
    }

    public static void drawQuad(Vec3d x, Vec3d y, Vec3d w, Vec3d z, int color, boolean depth) {
        Render3D.drawQuad(null, x, y, w, z, color, depth);
    }

    public static void drawQuad(MatrixStack.Entry entry, Vec3d x, Vec3d y, Vec3d w, Vec3d z, int color, boolean depth) {
        Quad quad = new Quad(entry, x, y, w, z, color);
        if (depth) {
            QUAD_DEPTH.add(quad);
        } else {
            QUAD.add(quad);
        }
    }

    public static void resetCircleSmoothing() {
        smoothY = 0.0;
        smoothY2 = 0.0;
    }

    private Render3D() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void setLastWorldSpaceEntry(MatrixStack.Entry lastWorldSpaceEntry) {
        Render3D.lastWorldSpaceEntry = lastWorldSpaceEntry;
    }

    public static void setLastTickDelta(float lastTickDelta) {
        Render3D.lastTickDelta = lastTickDelta;
    }

    public static void setLastCameraPos(Vec3d lastCameraPos) {
        Render3D.lastCameraPos = lastCameraPos;
    }

    public static void setLastCameraRotation(Quaternionf lastCameraRotation) {
        Render3D.lastCameraRotation = lastCameraRotation;
    }

    static {
        smoothY = 0.0;
        smoothY2 = 0.0;
    }

    public record Line(MatrixStack.Entry entry, Vec3d start, Vec3d end, int colorStart, int colorEnd, float width) {
    }

    public record Quad(MatrixStack.Entry entry, Vec3d x, Vec3d y, Vec3d w, Vec3d z, int color) {
    }

    public record GradientQuad(Vec3d p1, Vec3d p2, Vec3d p3, Vec3d p4, int c1, int c2, int c3, int c4) {
    }
}

