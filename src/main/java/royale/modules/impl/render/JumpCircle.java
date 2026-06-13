package royale.modules.impl.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import royale.events.api.EventHandler;
import royale.events.impl.JumpEvent;
import royale.events.impl.WorldRenderEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.implement.BooleanSetting;
import royale.modules.module.setting.implement.ColorSetting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.util.ColorUtil;
import royale.util.render.clientpipeline.ClientPipelines;
import royale.util.timer.StopWatch;

public class JumpCircle
extends ModuleStructure {
    private final List<Circle> circles = new ArrayList<Circle>();
    private final Identifier circleTexture = Identifier.of((String)"royale", (String)"images/circle/circle.png");
    private final Identifier glowTexture = Identifier.of((String)"royale", (String)"images/particle/glow.png");
    private final SliderSettings maxSize = new SliderSettings("\u041c\u0430\u043a\u0441. \u0440\u0430\u0437\u043c\u0435\u0440", "\u041c\u0430\u043a\u0441\u0438\u043c\u0430\u043b\u044c\u043d\u044b\u0439 \u0440\u0430\u0437\u043c\u0435\u0440 \u043a\u0440\u0443\u0433\u0430 \u043f\u0440\u044b\u0436\u043a\u0430").setValue(2.0f).range(1.0f, 2.0f);
    private final SliderSettings speed = new SliderSettings("\u0414\u043b\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c", "\u0412\u0440\u0435\u043c\u044f \u043e\u0442\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u044f \u043a\u0440\u0443\u0433\u0430 \u043f\u043e\u0441\u043b\u0435 \u043f\u0440\u044b\u0436\u043a\u0430").setValue(2000.0f).range(1000.0f, 2000.0f);
    private final BooleanSetting glow = new BooleanSetting("\u0421\u0432\u0435\u0447\u0435\u043d\u0438\u0435", "\u0414\u043e\u0431\u0430\u0432\u043b\u044f\u0435\u0442 \u043c\u044f\u0433\u043a\u043e\u0435 \u0441\u0432\u0435\u0447\u0435\u043d\u0438\u0435 \u0432\u043e\u043a\u0440\u0443\u0433 \u043a\u0440\u0443\u0433\u0430").setValue(true);
    private final ColorSetting color1 = new ColorSetting("\u0426\u0432\u0435\u0442 1", "\u041f\u0435\u0440\u0432\u044b\u0439 \u0446\u0432\u0435\u0442 \u0433\u0440\u0430\u0434\u0438\u0435\u043d\u0442\u0430").value(ColorUtil.getColor(137, 97, 72, 255));
    private final ColorSetting color2 = new ColorSetting("\u0426\u0432\u0435\u0442 2", "\u0412\u0442\u043e\u0440\u043e\u0439 \u0446\u0432\u0435\u0442 \u0433\u0440\u0430\u0434\u0438\u0435\u043d\u0442\u0430").value(ColorUtil.getColor(255, 255, 255, 255));
    private static final int SEGMENTS = 64;

    public JumpCircle() {
        super("JumpCircle", "\u0420\u0438\u0441\u0443\u0435\u0442 \u0430\u043d\u0438\u043c\u0438\u0440\u043e\u0432\u0430\u043d\u043d\u044b\u0439 \u043a\u0440\u0443\u0433 \u0432 \u0442\u043e\u0447\u043a\u0435 \u043f\u0440\u044b\u0436\u043a\u0430", ModuleCategory.RENDER);
        this.settings(this.maxSize, this.speed, this.glow, this.color1, this.color2);
    }

    @EventHandler
    public void onJump(JumpEvent event) {
        if (JumpCircle.mc.player == null || event.getPlayer() != JumpCircle.mc.player) {
            return;
        }
        Vec3d pos = new Vec3d(JumpCircle.mc.player.getX(), Math.floor(JumpCircle.mc.player.getY()) + 0.001, JumpCircle.mc.player.getZ());
        this.circles.add(new Circle(pos, new StopWatch()));
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        long maxTime = (long)this.speed.getValue();
        Iterator<Circle> iterator = this.circles.iterator();
        while (iterator.hasNext()) {
            Circle circle = iterator.next();
            if (circle.timer.elapsedTime() <= maxTime) continue;
            iterator.remove();
        }
        if (this.circles.isEmpty()) {
            return;
        }
        MatrixStack matrices = e.getStack();
        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        Vec3d cameraPos = JumpCircle.mc.gameRenderer.getCamera().getCameraPos();
        for (Circle circle : this.circles) {
            this.renderSingleCircle(matrices, immediate, circle, cameraPos);
        }
        immediate.draw();
    }

    private void renderSingleCircle(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, Circle circle, Vec3d cameraPos) {
        float alpha;
        float maxTime;
        float lifeTime = circle.timer.elapsedTime();
        float progress = Math.min(lifeTime / (maxTime = this.speed.getValue()), 1.0f);
        if (progress >= 1.0f) {
            return;
        }
        float easedProgress = this.bounceOut(progress);
        float scale = easedProgress * this.maxSize.getValue();
        float fadeInDuration = 0.15f;
        float glowStart = 0.65f;
        float fadeOutStart = 0.85f;
        if (progress < fadeInDuration) {
            alpha = progress / fadeInDuration;
        } else if (progress >= fadeOutStart) {
            float fadeOutProgress = (progress - fadeOutStart) / (1.0f - fadeOutStart);
            alpha = 1.0f - fadeOutProgress;
            if (progress > glowStart) {
                float glowProgress = (progress - glowStart) / (fadeOutStart - glowStart);
                float glowPulse = (float)(Math.sin((double)glowProgress * Math.PI * 3.0) * 0.3 + 0.3);
                alpha += glowPulse * (1.0f - fadeOutProgress);
            }
        } else if (progress > glowStart) {
            float glowProgress = (progress - glowStart) / (fadeOutStart - glowStart);
            float glowPulse = (float)(Math.sin((double)glowProgress * Math.PI * 3.0) * 0.3 + 0.3);
            alpha = 1.0f + glowPulse;
        } else {
            alpha = 1.0f;
        }
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));
        float rotationOffset = lifeTime / 1000.0f * 0.5f * 360.0f;
        Vec3d circlePos = circle.pos();
        if (this.glow.isValue()) {
            this.renderGradientGlow(matrices, immediate, circlePos, scale, alpha * 0.1f, rotationOffset, cameraPos);
        }
        this.renderGradientCircle(matrices, immediate, circlePos, scale, alpha, rotationOffset, cameraPos);
    }

    private void renderGradientCircle(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, Vec3d pos, float size, float alpha, float rotationOffset, Vec3d cameraPos) {
        VertexConsumer buffer = immediate.getBuffer(ClientPipelines.BLOOM_ESP.apply(this.circleTexture));
        matrices.push();
        float x = (float)(pos.x - cameraPos.x);
        float y = (float)(pos.y - cameraPos.y);
        float z = (float)(pos.z - cameraPos.z);
        matrices.translate(x, y, z);
        matrices.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float radius = size / 2.0f;
        int c1 = this.color1.getColor();
        int c2 = this.color2.getColor();
        for (int i = 0; i < 64; ++i) {
            float angle1 = (float)(Math.PI * 2 * (double)i / 64.0);
            float angle2 = (float)(Math.PI * 2 * (double)(i + 1) / 64.0);
            float t = (float)i / 64.0f;
            float tNext = (float)(i + 1) / 64.0f;
            float adjustedT = (t + rotationOffset / 360.0f) % 1.0f;
            float adjustedTNext = (tNext + rotationOffset / 360.0f) % 1.0f;
            int currentColor = this.getGradientColor(c1, c2, adjustedT, alpha);
            int nextColor = this.getGradientColor(c1, c2, adjustedTNext, alpha);
            float x1 = (float)(Math.cos(angle1) * (double)radius);
            float z1 = (float)(Math.sin(angle1) * (double)radius);
            float x2 = (float)(Math.cos(angle2) * (double)radius);
            float z2 = (float)(Math.sin(angle2) * (double)radius);
            float u1 = (float)(0.5 + 0.5 * Math.cos(angle1));
            float v1 = (float)(0.5 + 0.5 * Math.sin(angle1));
            float u2 = (float)(0.5 + 0.5 * Math.cos(angle2));
            float v2 = (float)(0.5 + 0.5 * Math.sin(angle2));
            int centerColor = ColorUtil.lerpColor(currentColor, nextColor, 0.5f);
            buffer.vertex((Matrix4fc)matrix, 0.0f, 0.0f, 0.0f).texture(0.5f, 0.5f).color(centerColor);
            buffer.vertex((Matrix4fc)matrix, x1, z1, 0.0f).texture(u1, v1).color(currentColor);
            buffer.vertex((Matrix4fc)matrix, x2, z2, 0.0f).texture(u2, v2).color(nextColor);
            buffer.vertex((Matrix4fc)matrix, x2, z2, 0.0f).texture(u2, v2).color(nextColor);
        }
        matrices.pop();
    }

    private void renderGradientGlow(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, Vec3d pos, float scale, float alpha, float rotationOffset, Vec3d cameraPos) {
        int c1 = this.color1.getColor();
        int c2 = this.color2.getColor();
        for (int layer = 0; layer < 3; ++layer) {
            float layerScale = scale * (1.3f + (float)layer * 0.4f);
            float layerAlpha = alpha * (0.35f - (float)layer * 0.1f);
            this.renderGlowLayer(matrices, immediate, pos, layerScale, layerAlpha, rotationOffset, c1, c2, cameraPos);
        }
        float coreAlpha = alpha * 0.2f;
        int coreColor1 = ColorUtil.multAlpha(c1, coreAlpha);
        int coreColor2 = ColorUtil.multAlpha(c2, coreAlpha);
        int mixedCore = ColorUtil.lerpColor(coreColor1, coreColor2, 0.5f);
        this.renderTexturedQuad(matrices, immediate, pos, scale * 2.5f, mixedCore, this.glowTexture, cameraPos);
    }

    private void renderGlowLayer(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, Vec3d pos, float size, float alpha, float rotationOffset, int c1, int c2, Vec3d cameraPos) {
        VertexConsumer buffer = immediate.getBuffer(ClientPipelines.BLOOM_ESP.apply(this.glowTexture));
        int glowSegments = 16;
        float radius = size / 2.0f;
        for (int i = 0; i < glowSegments; ++i) {
            float angle = (float)(Math.PI * 2 * (double)i / (double)glowSegments);
            float t = (float)i / (float)glowSegments;
            float adjustedT = (t + rotationOffset / 360.0f) % 1.0f;
            int glowColor = this.getGradientColor(c1, c2, adjustedT, alpha);
            float glowX = (float)(pos.x + Math.cos(angle) * (double)radius * (double)0.8f);
            float glowZ = (float)(pos.z + Math.sin(angle) * (double)radius * (double)0.8f);
            Vec3d glowPos = new Vec3d((double)glowX, pos.y, (double)glowZ);
            float glowSize = size * 0.4f;
            this.renderTexturedQuadAtPos(matrices, buffer, glowPos, glowSize, glowColor, cameraPos);
        }
    }

    private void renderTexturedQuadAtPos(MatrixStack matrices, VertexConsumer buffer, Vec3d pos, float size, int color, Vec3d cameraPos) {
        matrices.push();
        float x = (float)(pos.x - cameraPos.x);
        float y = (float)(pos.y - cameraPos.y);
        float z = (float)(pos.z - cameraPos.z);
        matrices.translate(x, y, z);
        matrices.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float half = size / 2.0f;
        buffer.vertex((Matrix4fc)matrix, -half, -half, 0.0f).texture(0.0f, 0.0f).color(color);
        buffer.vertex((Matrix4fc)matrix, half, -half, 0.0f).texture(1.0f, 0.0f).color(color);
        buffer.vertex((Matrix4fc)matrix, half, half, 0.0f).texture(1.0f, 1.0f).color(color);
        buffer.vertex((Matrix4fc)matrix, -half, half, 0.0f).texture(0.0f, 1.0f).color(color);
        matrices.pop();
    }

    private int getGradientColor(int c1, int c2, float t, float alpha) {
        float gradientT = t <= 0.5f ? t * 2.0f : (1.0f - t) * 2.0f;
        int color = ColorUtil.lerpColor(c1, c2, gradientT);
        return ColorUtil.multAlpha(color, alpha);
    }

    private void renderTexturedQuad(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, Vec3d pos, float size, int color, Identifier texture, Vec3d cameraPos) {
        VertexConsumer buffer = immediate.getBuffer(ClientPipelines.BLOOM_ESP.apply(texture));
        matrices.push();
        float x = (float)(pos.x - cameraPos.x);
        float y = (float)(pos.y - cameraPos.y);
        float z = (float)(pos.z - cameraPos.z);
        matrices.translate(x, y, z);
        matrices.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float half = size / 2.0f;
        buffer.vertex((Matrix4fc)matrix, -half, -half, 0.0f).texture(0.0f, 0.0f).color(color);
        buffer.vertex((Matrix4fc)matrix, half, -half, 0.0f).texture(1.0f, 0.0f).color(color);
        buffer.vertex((Matrix4fc)matrix, half, half, 0.0f).texture(1.0f, 1.0f).color(color);
        buffer.vertex((Matrix4fc)matrix, -half, half, 0.0f).texture(0.0f, 1.0f).color(color);
        matrices.pop();
    }

    private float bounceOut(float value) {
        float n1 = 7.5625f;
        float d1 = 2.75f;
        if (value < 1.0f / d1) {
            return n1 * value * value;
        }
        if (value < 2.0f / d1) {
            return n1 * (value -= 1.5f / d1) * value + 0.75f;
        }
        if (value < 2.5f / d1) {
            return n1 * (value -= 2.25f / d1) * value + 0.9375f;
        }
        return n1 * (value -= 2.625f / d1) * value + 0.984375f;
    }

    public record Circle(Vec3d pos, StopWatch timer) {
    }
}

