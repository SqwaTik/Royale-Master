package royale.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import royale.IMinecraft;
import royale.events.api.EventHandler;
import royale.events.impl.WorldRenderEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.implement.ColorSetting;
import royale.modules.module.setting.implement.SelectSetting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.util.animations.Animation;
import royale.util.animations.Direction;
import royale.util.animations.OutBack;
import royale.util.render.Render3D;
import royale.util.render.VisibilityUtil;
import royale.util.render.clientpipeline.ClientPipelines;

public class TargetESP
extends ModuleStructure {
    private static TargetESP instance;
    private static final double TARGET_RANGE = 6.0;
    private static final long HOLD_DURATION_MS = 2500L;
    private Animation espAnim = new OutBack().setMs(300).setValue(1.0);
    private SelectSetting mode = new SelectSetting("\u0420\u0435\u0436\u0438\u043c", "\u0412\u0438\u0437\u0443\u0430\u043b\u044c\u043d\u044b\u0439 \u0441\u0442\u0438\u043b\u044c \u043e\u0442\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u044f \u0446\u0435\u043b\u0438").value("\u0420\u043e\u043c\u0431", "\u041f\u0440\u0438\u0437\u0440\u0430\u043a", "\u0426\u0435\u043f\u044c", "\u041a\u0440\u0438\u0441\u0442\u0430\u043b\u043b\u044b", "\u041a\u0440\u0443\u0433").selected("\u0420\u043e\u043c\u0431");
    private SliderSettings speed = new SliderSettings("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c", "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u0438 \u044d\u0444\u0444\u0435\u043a\u0442\u0430").range(0.1f, 3.0f);
    private SliderSettings crystalRotationSpeed = new SliderSettings("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0432\u0440\u0430\u0449\u0435\u043d\u0438\u044f \u043a\u0440\u0438\u0441\u0442\u0430\u043b\u043b\u043e\u0432", "\u0414\u043e\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044c\u043d\u0430\u044f \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0432\u0440\u0430\u0449\u0435\u043d\u0438\u044f \u0434\u043b\u044f \u0440\u0435\u0436\u0438\u043c\u0430 \u041a\u0440\u0438\u0441\u0442\u0430\u043b\u043b\u044b").range(0.1f, 2.0f).visible(() -> this.mode.isSelected("\u041a\u0440\u0438\u0441\u0442\u0430\u043b\u043b\u044b"));
    private ColorSetting color1 = new ColorSetting("\u0426\u0432\u0435\u0442 1", "\u041e\u0441\u043d\u043e\u0432\u043d\u043e\u0439 \u0446\u0432\u0435\u0442 \u044d\u0444\u0444\u0435\u043a\u0442\u0430").setColor(new Color(255, 101, 57, 255).getRGB());
    private ColorSetting color2 = new ColorSetting("\u0426\u0432\u0435\u0442 2", "\u0412\u0442\u043e\u0440\u043e\u0439 \u0446\u0432\u0435\u0442 \u0433\u0440\u0430\u0434\u0438\u0435\u043d\u0442\u0430").setColor(new Color(255, 50, 150, 255).getRGB());
    private ColorSetting color3 = new ColorSetting("\u0426\u0432\u0435\u0442 3", "\u0414\u043e\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044c\u043d\u044b\u0439 \u0446\u0432\u0435\u0442 \u0434\u043b\u044f \u0440\u0435\u0436\u0438\u043c\u0430 \u041f\u0440\u0438\u0437\u0440\u0430\u043a").setColor(new Color(150, 50, 255, 255).getRGB()).visible(() -> this.mode.isSelected("\u041f\u0440\u0438\u0437\u0440\u0430\u043a"));
    private Vec3d smoothedPos = null;
    private LivingEntity lastTarget = null;
    private float hurtProgress = 0.0f;
    private LivingEntity heldTarget = null;
    private long holdUntilMs = 0L;
    private LivingEntity lastRenderedTarget = null;
    private final List<Crystal> crystalList = new ArrayList<Crystal>();
    private float rotationAngle = 0.0f;
    private long lastFrameTime = System.currentTimeMillis();
    private static final float TARGET_FPS = 60.0f;
    private static final float TARGET_FRAME_TIME = 16.666666f;

    public static TargetESP getInstance() {
        return instance;
    }

    public TargetESP() {
        super("TargetEsp", "\u041f\u043e\u0434\u0441\u0432\u0435\u0447\u0438\u0432\u0430\u0435\u0442 \u0442\u0435\u043a\u0443\u0449\u0443\u044e \u0446\u0435\u043b\u044c \u0432 \u0431\u043e\u044e", ModuleCategory.RENDER);
        instance = this;
        this.speed.setValue(1.0f);
        this.crystalRotationSpeed.setValue(0.5f);
        this.settings(this.mode, this.speed, this.crystalRotationSpeed, this.color1, this.color2, this.color3);
    }

    @Override
    public void deactivate() {
        this.smoothedPos = null;
        this.lastTarget = null;
        this.heldTarget = null;
        this.holdUntilMs = 0L;
        this.hurtProgress = 0.0f;
        this.crystalList.clear();
        this.rotationAngle = 0.0f;
        this.espAnim.setDirection(Direction.BACKWARDS);
        Render3D.resetCircleSmoothing();
    }

    private float getDeltaTime() {
        long currentTime = System.currentTimeMillis();
        float deltaMs = currentTime - this.lastFrameTime;
        this.lastFrameTime = currentTime;
        deltaMs = Math.max(1.0f, Math.min(deltaMs, 100.0f));
        return deltaMs / 16.666666f;
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent e) {
        float deltaTime = this.getDeltaTime();
        LivingEntity target = this.resolveTarget();
        if (target == null) {
            this.smoothedPos = null;
            this.lastTarget = null;
            this.espAnim.setDirection(Direction.BACKWARDS);
            Render3D.resetCircleSmoothing();
            return;
        }
        this.espAnim.setDirection(Direction.FORWARDS);
        float alpha = this.espAnim.getOutput().floatValue();
        if (alpha <= 0.01f) {
            return;
        }
        float hurtDecay = 0.1f * deltaTime;
        this.hurtProgress = target.hurtTime > 0 ? (float)target.hurtTime / 10.0f : Math.max(0.0f, this.hurtProgress - hurtDecay);
        Render3D.updateTargetEsp(deltaTime * this.speed.getValue());
        if (this.mode.isSelected("\u041a\u0440\u0443\u0433")) {
            this.renderCircle(e.getStack(), target, alpha);
            return;
        }
        MatrixStack stack = e.getStack();
        VertexConsumerProvider.Immediate provider = mc.getBufferBuilders().getEntityVertexConsumers();
        Vec3d camPos = TargetESP.mc.gameRenderer.getCamera().getCameraPos();
        float partialTicks = e.getPartialTicks();
        Vec3d targetPos = target.getLerpedPos(partialTicks);
        if (this.lastTarget != target || this.smoothedPos == null) {
            this.smoothedPos = targetPos;
            this.lastTarget = target;
        } else {
            float smoothingFactor = Math.min(1.0f, partialTicks * (1.5f * this.speed.getValue()));
            double dx = targetPos.x - this.smoothedPos.x;
            double dy = targetPos.y - this.smoothedPos.y;
            double dz = targetPos.z - this.smoothedPos.z;
            this.smoothedPos = new Vec3d(this.smoothedPos.x + dx * (double)smoothingFactor, this.smoothedPos.y + dy * (double)smoothingFactor, this.smoothedPos.z + dz * (double)smoothingFactor);
        }
        stack.push();
        stack.translate(this.smoothedPos.x - camPos.x, this.smoothedPos.y - camPos.y, this.smoothedPos.z - camPos.z);
        if (this.mode.isSelected("\u0420\u043e\u043c\u0431")) {
            this.renderRhomb(stack, (VertexConsumerProvider)provider, target, alpha);
        } else if (this.mode.isSelected("\u041f\u0440\u0438\u0437\u0440\u0430\u043a")) {
            this.renderGhost(stack, (VertexConsumerProvider)provider, target, alpha);
        } else if (this.mode.isSelected("\u0426\u0435\u043f\u044c")) {
            this.renderChain(stack, (VertexConsumerProvider)provider, target, alpha);
        } else if (this.mode.isSelected("\u041a\u0440\u0438\u0441\u0442\u0430\u043b\u043b\u044b")) {
            if (this.crystalList.isEmpty() || this.lastRenderedTarget != target) {
                this.createCrystals();
                this.lastRenderedTarget = target;
            }
            this.renderCrystals(stack, (VertexConsumerProvider)provider, target, alpha, deltaTime);
        }
        provider.draw();
        stack.pop();
    }

    private void renderCircle(MatrixStack stack, LivingEntity target, float alpha) {
        int baseColor1 = this.color1.getColor();
        int baseColor2 = this.color2.getColor();
        if (this.hurtProgress > 0.0f) {
            baseColor1 = this.lerpColor(baseColor1, -65536, this.hurtProgress);
            baseColor2 = this.lerpColor(baseColor2, -65536, this.hurtProgress);
        }
        Render3D.drawCircle(stack, target, alpha, this.hurtProgress, baseColor1, baseColor2);
    }

    private void renderChain(MatrixStack stack, VertexConsumerProvider provider, LivingEntity target, float alpha) {
        VertexConsumer consumer = provider.getBuffer(ClientPipelines.CHAIN_ESP.apply(Identifier.of((String)"royale", (String)"images/world/chain.png")));
        float animationSpeed = this.speed.getValue();
        float animValue = (float)(System.currentTimeMillis() % 360000L) / 1000.0f * 60.0f * animationSpeed;
        float gradusX = (float)(20.0 * Math.min(1.0 + Math.sin(Math.toRadians(animValue)), 1.0));
        float gradusZ = (float)(20.0 * (Math.min(1.0 + Math.sin(Math.toRadians(animValue)), 2.0) - 1.0));
        float width = target.getWidth() * 3.0f;
        int linksStep = 18;
        int totalAngle = 720;
        float chainSizeVal = 8.0f;
        float down = 1.5f;
        float chainScale = 0.5f;
        int alphaVal = MathHelper.clamp((int)((int)(alpha * 128.0f)), (int)0, (int)128);
        int baseColor1 = this.color1.getColor();
        int baseColor2 = this.color2.getColor();
        if (this.hurtProgress > 0.0f) {
            baseColor1 = this.lerpColor(baseColor1, -65536, this.hurtProgress);
            baseColor2 = this.lerpColor(baseColor2, -65536, this.hurtProgress);
        }
        int c1 = this.withAlpha(baseColor1, alphaVal);
        int c2 = this.withAlpha(baseColor2, alphaVal);
        float rotationValue = (float)(System.currentTimeMillis() % 720000L) / 1000.0f * 30.0f * animationSpeed;
        for (int chain = 0; chain < 2; ++chain) {
            float val = 1.2f - 0.5f * (chain == 0 ? 1.0f : 0.9f);
            stack.push();
            stack.translate(0.0f, target.getHeight() / 2.0f, 0.0f);
            stack.scale(chainScale, chainScale, chainScale);
            stack.multiply((Quaternionfc)RotationAxis.POSITIVE_Z.rotationDegrees(chain == 0 ? gradusX : -gradusX));
            stack.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(chain == 0 ? gradusZ : -gradusZ));
            float x = 0.0f;
            float y = -0.5f;
            float z = 0.0f;
            Matrix4f matrix = stack.peek().getPositionMatrix();
            int modif = linksStep / 2;
            for (int i = 0; i < totalAngle; i += modif) {
                float offsetX = (chain == 0 ? gradusX : -gradusX) / 100.0f;
                float offsetZ = (chain == 0 ? -gradusZ : gradusZ) / 100.0f;
                float prevSin = (float)((double)(x + offsetX) + Math.sin(Math.toRadians((float)(i - modif) + rotationValue)) * (double)width * (double)val);
                float prevCos = (float)((double)(z + offsetZ) + Math.cos(Math.toRadians((float)(i - modif) + rotationValue)) * (double)width * (double)val);
                float sin = (float)((double)(x + offsetX) + Math.sin(Math.toRadians((float)i + rotationValue)) * (double)width * (double)val);
                float cos = (float)((double)(z + offsetZ) + Math.cos(Math.toRadians((float)i + rotationValue)) * (double)width * (double)val);
                float u0 = 0.0027777778f * (float)(i - modif) * chainSizeVal;
                float u1 = 0.0027777778f * (float)i * chainSizeVal;
                consumer.vertex((Matrix4fc)matrix, prevSin, y, prevCos).texture(u0, 0.0f).color(c1);
                consumer.vertex((Matrix4fc)matrix, sin, y, cos).texture(u1, 0.0f).color(c1);
                consumer.vertex((Matrix4fc)matrix, sin, y + down, cos).texture(u1, 0.99f).color(c2);
                consumer.vertex((Matrix4fc)matrix, prevSin, y + down, prevCos).texture(u0, 0.99f).color(c2);
            }
            stack.pop();
        }
    }

    private void renderRhomb(MatrixStack stack, VertexConsumerProvider provider, LivingEntity target, float alpha) {
        VertexConsumer consumer = provider.getBuffer(ClientPipelines.ROMB_ESP.apply(Identifier.of((String)"royale", (String)"images/world/cube.png")));
        Quaternionf camRot = TargetESP.mc.gameRenderer.getCamera().getRotation();
        stack.translate(0.0f, target.getHeight() / 2.0f, 0.0f);
        stack.multiply((Quaternionfc)camRot);
        float timeRotation = (float)(System.currentTimeMillis() % 6283L) / 1000.0f * this.speed.getValue();
        stack.multiply((Quaternionfc)RotationAxis.POSITIVE_Z.rotationDegrees((float)Math.sin(timeRotation) * 360.0f));
        float size = 0.5f;
        stack.scale(size, size, 1.0f);
        int c1 = this.withAlpha(this.color1.getColor(), (int)(255.0f * alpha));
        int c2 = this.withAlpha(this.color2.getColor(), (int)(255.0f * alpha));
        Vector3f[] quad = new Vector3f[]{new Vector3f(-1.0f, -1.0f, 0.0f), new Vector3f(-1.0f, 1.0f, 0.0f), new Vector3f(1.0f, 1.0f, 0.0f), new Vector3f(1.0f, -1.0f, 0.0f)};
        MatrixStack.Entry m = stack.peek();
        consumer.vertex(m, quad[0].x, quad[0].y, 0.0f).texture(0.0f, 0.0f).color(c2);
        consumer.vertex(m, quad[1].x, quad[1].y, 0.0f).texture(0.0f, 1.0f).color(c1);
        consumer.vertex(m, quad[2].x, quad[2].y, 0.0f).texture(1.0f, 1.0f).color(c2);
        consumer.vertex(m, quad[3].x, quad[3].y, 0.0f).texture(1.0f, 0.0f).color(c1);
    }

    private void renderGhost(MatrixStack stack, VertexConsumerProvider consumers, LivingEntity target, float alpha) {
        VertexConsumer consumer = consumers.getBuffer(ClientPipelines.GHOSTS_ESP.apply(Identifier.of((String)"royale", (String)"images/particle/ghost-glow.png")));
        stack.translate(0.0f, target.getHeight() * 0.5f, 0.0f);
        this.particle(stack, consumer, (sin, cos) -> new Vec3d(sin, cos, -cos), alpha, 0);
        this.particle(stack, consumer, (sin, cos) -> new Vec3d(-sin, sin, -cos), alpha, 1);
        this.particle(stack, consumer, (sin, cos) -> new Vec3d(-sin, -sin, cos), alpha, 2);
    }

    private void particle(MatrixStack stack, VertexConsumer consumer, Transformation transformation, float alpha, int colorIndex) {
        double radius = 0.7f;
        double distance = 11.0;
        float particleSize = 0.5f;
        int alphaFactor = 15;
        float animationSpeed = this.speed.getValue();
        long elapsed = System.currentTimeMillis();
        int baseColor = switch (colorIndex) {
            case 0 -> this.color1.getColor();
            case 1 -> this.color2.getColor();
            default -> this.color3.getColor();
        };
        int i = 0;
        while ((float)i < 40.0f * alpha) {
            stack.push();
            double angle = 0.15 * ((double)elapsed * 0.5 * (double)animationSpeed - (double)i * distance) / 30.0;
            double sin = Math.sin(angle) * radius;
            double cos = Math.cos(angle) * radius;
            Vec3d trans = transformation.make(sin, cos);
            stack.translate(trans.x, trans.y, trans.z);
            stack.multiply((Quaternionfc)TargetESP.mc.gameRenderer.getCamera().getRotation());
            float spinRotation = (float)elapsed * 0.1f * animationSpeed - (float)i * 10.0f;
            stack.multiply((Quaternionfc)RotationAxis.POSITIVE_Z.rotationDegrees(spinRotation));
            stack.translate(particleSize / 2.0f, particleSize / 2.0f, 0.0f);
            float x = (float)i / 40.0f;
            int lerpedColor = this.lerpColor(baseColor, this.getNextColor(colorIndex), x);
            int c1 = this.withAlpha(lerpedColor, (int)((float)(255 - i * alphaFactor) * alpha));
            int c2 = this.withAlpha(lerpedColor, (int)((float)(255 - i * alphaFactor) * alpha));
            MatrixStack.Entry m = stack.peek();
            consumer.vertex(m, 0.0f, -particleSize, 0.0f).texture(0.0f, 0.0f).color(c2);
            consumer.vertex(m, -particleSize, -particleSize, 0.0f).texture(0.0f, 1.0f).color(c1);
            consumer.vertex(m, -particleSize, 0.0f, 0.0f).texture(1.0f, 1.0f).color(c2);
            consumer.vertex(m, 0.0f, 0.0f, 0.0f).texture(1.0f, 0.0f).color(c1);
            stack.pop();
            ++i;
        }
    }

    private void createCrystals() {
        this.crystalList.clear();
        this.crystalList.add(new Crystal(new Vec3d(0.0, 0.85, 0.8), new Vec3d(-49.0, 0.0, 40.0)));
        this.crystalList.add(new Crystal(new Vec3d(0.2, 0.85, -0.675), new Vec3d(35.0, 0.0, -30.0)));
        this.crystalList.add(new Crystal(new Vec3d(0.6, 1.35, 0.6), new Vec3d(-30.0, 0.0, 35.0)));
        this.crystalList.add(new Crystal(new Vec3d(-0.74, 1.05, 0.4), new Vec3d(-25.0, 0.0, -30.0)));
        this.crystalList.add(new Crystal(new Vec3d(0.74, 0.95, -0.4), new Vec3d(0.0, 0.0, 0.0)));
        this.crystalList.add(new Crystal(new Vec3d(-0.475, 0.85, -0.375), new Vec3d(30.0, 0.0, -25.0)));
        this.crystalList.add(new Crystal(new Vec3d(0.0, 1.35, -0.6), new Vec3d(45.0, 0.0, 0.0)));
        this.crystalList.add(new Crystal(new Vec3d(0.85, 0.7, 0.1), new Vec3d(-30.0, 0.0, 30.0)));
        this.crystalList.add(new Crystal(new Vec3d(-0.7, 1.35, -0.3), new Vec3d(0.0, 0.0, 0.0)));
        this.crystalList.add(new Crystal(new Vec3d(-0.3, 1.35, 0.55), new Vec3d(0.0, 0.0, 0.0)));
        this.crystalList.add(new Crystal(new Vec3d(-0.5, 0.7, 0.7), new Vec3d(0.0, 0.0, 0.0)));
        this.crystalList.add(new Crystal(new Vec3d(0.5, 0.7, 0.7), new Vec3d(0.0, 0.0, 0.0)));
        this.crystalList.add(new Crystal(new Vec3d(-0.7, 0.75, 0.0), new Vec3d(0.0, 0.0, 0.0)));
        this.crystalList.add(new Crystal(new Vec3d(-0.2, 0.65, -0.7), new Vec3d(0.0, 0.0, 0.0)));
    }

    private void renderCrystals(MatrixStack stack, VertexConsumerProvider provider, LivingEntity target, float alpha, float deltaTime) {
        if (target == null || this.crystalList.isEmpty()) {
            return;
        }
        this.rotationAngle += this.crystalRotationSpeed.getValue() * this.speed.getValue() * deltaTime;
        this.rotationAngle %= 360.0f;
        stack.push();
        stack.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees(this.rotationAngle));
        int baseColor = this.color1.getColor();
        if (this.hurtProgress > 0.0f) {
            baseColor = this.lerpColor(baseColor, -65536, this.hurtProgress);
        }
        for (Crystal crystal : this.crystalList) {
            crystal.render(stack, provider, alpha, baseColor, this.speed.getValue());
        }
        stack.pop();
    }

    private int darkenColor(int color, float factor) {
        int a = color >> 24 & 0xFF;
        int r = (int)((float)(color >> 16 & 0xFF) * factor);
        int g = (int)((float)(color >> 8 & 0xFF) * factor);
        int b = (int)((float)(color & 0xFF) * factor);
        return a << 24 | r << 16 | g << 8 | b;
    }

    private int lightenColor(int color, float factor) {
        int a = color >> 24 & 0xFF;
        int r = Math.min(255, (int)((float)(color >> 16 & 0xFF) * factor));
        int g = Math.min(255, (int)((float)(color >> 8 & 0xFF) * factor));
        int b = Math.min(255, (int)((float)(color & 0xFF) * factor));
        return a << 24 | r << 16 | g << 8 | b;
    }

    private int getNextColor(int colorIndex) {
        return switch (colorIndex) {
            case 0 -> this.color2.getColor();
            case 1 -> this.color3.getColor();
            default -> this.color1.getColor();
        };
    }

    private int lerpColor(int c1, int c2, float t) {
        int a1 = c1 >> 24 & 0xFF;
        int r1 = c1 >> 16 & 0xFF;
        int g1 = c1 >> 8 & 0xFF;
        int b1 = c1 & 0xFF;
        int a2 = c2 >> 24 & 0xFF;
        int r2 = c2 >> 16 & 0xFF;
        int g2 = c2 >> 8 & 0xFF;
        int b2 = c2 & 0xFF;
        int a = (int)((float)a1 + (float)(a2 - a1) * t);
        int r = (int)((float)r1 + (float)(r2 - r1) * t);
        int g = (int)((float)g1 + (float)(g2 - g1) * t);
        int b = (int)((float)b1 + (float)(b2 - b1) * t);
        return a << 24 | r << 16 | g << 8 | b;
    }

    private int withAlpha(int color, int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        return color & 0xFFFFFF | alpha << 24;
    }

    private LivingEntity resolveTarget() {
        long now = System.currentTimeMillis();
        LivingEntity nearest = this.findNearestTargetInRange();
        if (nearest != null) {
            this.heldTarget = nearest;
            this.holdUntilMs = now + 2500L;
            return nearest;
        }
        if (this.heldTarget != null && now <= this.holdUntilMs && this.isUsableTarget(this.heldTarget)) {
            return this.heldTarget;
        }
        this.heldTarget = null;
        this.holdUntilMs = 0L;
        return null;
    }

    private LivingEntity findNearestTargetInRange() {
        if (TargetESP.mc.player == null || TargetESP.mc.world == null) {
            return null;
        }
        double maxDistSq = 36.0;
        double bestDistSq = Double.MAX_VALUE;
        PlayerEntity best = null;
        for (PlayerEntity player : TargetESP.mc.world.getPlayers()) {
            double distSq;
            if (!this.isUsableTarget((LivingEntity)player) || !((distSq = TargetESP.mc.player.squaredDistanceTo((Entity)player)) <= maxDistSq) || !(distSq < bestDistSq)) continue;
            bestDistSq = distSq;
            best = player;
        }
        return best;
    }

    private boolean isUsableTarget(LivingEntity target) {
        if (target == null || !target.isAlive() || target.isRemoved()) {
            return false;
        }
        if (target instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)target;
            if (TargetESP.mc.player != null && player == TargetESP.mc.player) {
                return false;
            }
            if (player.isSpectator()) {
                return false;
            }
        }
        return this.hasVisibleLine(target);
    }

    private boolean hasVisibleLine(LivingEntity target) {
        if (TargetESP.mc.player == null || TargetESP.mc.world == null || TargetESP.mc.gameRenderer == null) {
            return false;
        }

        Vec3d from = TargetESP.mc.gameRenderer.getCamera().getCameraPos();
        Vec3d center = target.getBoundingBox().getCenter();
        if (VisibilityUtil.hasClearLine(TargetESP.mc, from, center)) {
            return true;
        }

        return VisibilityUtil.hasClearLine(TargetESP.mc, from, target.getEyePos());
    }

    @FunctionalInterface
    private static interface Transformation {
        public Vec3d make(double var1, double var3);
    }

    private class Crystal {
        private final Vec3d position;
        private final Vec3d rotation;
        private final float rotationSpeed;

        public Crystal(Vec3d position, Vec3d rotation) {
            this.position = position;
            this.rotation = rotation;
            this.rotationSpeed = 0.5f + (float)(Math.random() * 1.5);
        }

        public void render(MatrixStack stack, VertexConsumerProvider provider, float alpha, int baseColor, float speedMultiplier) {
            stack.push();
            stack.translate(this.position.x, this.position.y, this.position.z);
            float timeSeconds = (float)(System.currentTimeMillis() % 31416L) / 1000.0f;
            float pulsation = 1.0f + (float)(Math.sin(timeSeconds * 2.0f) * (double)0.1f);
            stack.scale(pulsation, pulsation, pulsation);
            float selfRotation = (float)(System.currentTimeMillis() % 36000L) / 100.0f * this.rotationSpeed * speedMultiplier;
            stack.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees((float)this.rotation.x));
            stack.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees((float)this.rotation.y + selfRotation));
            stack.multiply((Quaternionfc)RotationAxis.POSITIVE_Z.rotationDegrees((float)this.rotation.z));
            float userAlpha = 0.3f;
            VertexConsumer filledConsumer = provider.getBuffer(ClientPipelines.CRYSTAL_FILLED);
            this.drawFilledCrystal(stack, filledConsumer, baseColor, userAlpha * 0.85f, alpha);
            VertexConsumer glowConsumer = provider.getBuffer(ClientPipelines.CRYSTAL_GLOW);
            stack.push();
            stack.scale(1.15f, 1.15f, 1.15f);
            this.drawFilledCrystal(stack, glowConsumer, baseColor, userAlpha * 0.25f, alpha);
            stack.pop();
            stack.push();
            stack.scale(1.3f, 1.3f, 1.3f);
            this.drawFilledCrystal(stack, glowConsumer, baseColor, userAlpha * 0.1f, alpha);
            stack.pop();
            this.drawBloomEffect(stack, provider, baseColor, alpha);
            stack.pop();
        }

        private void drawFilledCrystal(MatrixStack stack, VertexConsumer consumer, int baseColor, float alphaMultiplier, float anim) {
            Vector3f v2;
            Vector3f v1;
            int i;
            float s = 0.05f;
            float h_prism = s * 1.0f;
            float h_pyramid = s * 1.5f;
            int numSides = 8;
            ArrayList<Vector3f> topVertices = new ArrayList<Vector3f>();
            ArrayList<Vector3f> bottomVertices = new ArrayList<Vector3f>();
            for (int i2 = 0; i2 < numSides; ++i2) {
                float angle = (float)(Math.PI * 2 * (double)i2 / (double)numSides);
                float x = (float)((double)s * Math.cos(angle));
                float z = (float)((double)s * Math.sin(angle));
                topVertices.add(new Vector3f(x, h_prism / 2.0f, z));
                bottomVertices.add(new Vector3f(x, -h_prism / 2.0f, z));
            }
            Vector3f vTop = new Vector3f(0.0f, h_prism / 2.0f + h_pyramid, 0.0f);
            Vector3f vBottom = new Vector3f(0.0f, -h_prism / 2.0f - h_pyramid, 0.0f);
            int finalAlpha = (int)(alphaMultiplier * 255.0f * anim);
            int finalColor = TargetESP.this.withAlpha(baseColor, finalAlpha);
            int darkerColor = TargetESP.this.withAlpha(TargetESP.this.darkenColor(baseColor, 0.7f), finalAlpha);
            int lighterColor = TargetESP.this.withAlpha(TargetESP.this.lightenColor(baseColor, 1.2f), finalAlpha);
            Matrix4f matrix = stack.peek().getPositionMatrix();
            for (i = 0; i < numSides; ++i) {
                v1 = (Vector3f)bottomVertices.get(i);
                v2 = (Vector3f)bottomVertices.get((i + 1) % numSides);
                Vector3f v3 = (Vector3f)topVertices.get((i + 1) % numSides);
                Vector3f v4 = (Vector3f)topVertices.get(i);
                int sideColor = i % 2 == 0 ? finalColor : darkerColor;
                this.drawQuadFilled(matrix, consumer, v1, v2, v3, v4, sideColor);
            }
            for (i = 0; i < numSides; ++i) {
                v1 = (Vector3f)topVertices.get(i);
                v2 = (Vector3f)topVertices.get((i + 1) % numSides);
                int pyramidColor = i % 2 == 0 ? lighterColor : finalColor;
                this.drawTriangleFilled(matrix, consumer, vTop, v2, v1, pyramidColor);
            }
            for (i = 0; i < numSides; ++i) {
                v1 = (Vector3f)bottomVertices.get(i);
                v2 = (Vector3f)bottomVertices.get((i + 1) % numSides);
                int pyramidColor = i % 2 == 0 ? darkerColor : finalColor;
                this.drawTriangleFilled(matrix, consumer, vBottom, v1, v2, pyramidColor);
            }
        }

        private void drawTriangleFilled(Matrix4f matrix, VertexConsumer consumer, Vector3f v1, Vector3f v2, Vector3f v3, int color) {
            consumer.vertex((Matrix4fc)matrix, v1.x, v1.y, v1.z).color(color);
            consumer.vertex((Matrix4fc)matrix, v2.x, v2.y, v2.z).color(color);
            consumer.vertex((Matrix4fc)matrix, v3.x, v3.y, v3.z).color(color);
            consumer.vertex((Matrix4fc)matrix, v3.x, v3.y, v3.z).color(color);
        }

        private void drawQuadFilled(Matrix4f matrix, VertexConsumer consumer, Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, int color) {
            consumer.vertex((Matrix4fc)matrix, v1.x, v1.y, v1.z).color(color);
            consumer.vertex((Matrix4fc)matrix, v2.x, v2.y, v2.z).color(color);
            consumer.vertex((Matrix4fc)matrix, v3.x, v3.y, v3.z).color(color);
            consumer.vertex((Matrix4fc)matrix, v4.x, v4.y, v4.z).color(color);
        }

        private void drawBloomEffect(MatrixStack stack, VertexConsumerProvider provider, int baseColor, float anim) {
            Matrix4f matrix;
            float angle;
            int i;
            VertexConsumer bloomConsumer = provider.getBuffer(ClientPipelines.BLOOM_ESP.apply(Identifier.of((String)"royale", (String)"images/particle/glow.png")));
            int bloomAlpha = (int)(18.0f * anim);
            int bloomColor = TargetESP.this.withAlpha(baseColor, bloomAlpha);
            float bloomSize = 0.75f;
            Quaternionf camRot = IMinecraft.mc.gameRenderer.getCamera().getRotation();
            int segments = 6;
            for (i = 0; i < segments; ++i) {
                stack.push();
                angle = 360.0f / (float)segments * (float)i;
                stack.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees(angle));
                stack.multiply((Quaternionfc)camRot);
                matrix = stack.peek().getPositionMatrix();
                bloomConsumer.vertex((Matrix4fc)matrix, -bloomSize / 2.0f, -bloomSize / 2.0f, 0.0f).texture(0.0f, 1.0f).color(bloomColor);
                bloomConsumer.vertex((Matrix4fc)matrix, bloomSize / 2.0f, -bloomSize / 2.0f, 0.0f).texture(1.0f, 1.0f).color(bloomColor);
                bloomConsumer.vertex((Matrix4fc)matrix, bloomSize / 2.0f, bloomSize / 2.0f, 0.0f).texture(1.0f, 0.0f).color(bloomColor);
                bloomConsumer.vertex((Matrix4fc)matrix, -bloomSize / 2.0f, bloomSize / 2.0f, 0.0f).texture(0.0f, 0.0f).color(bloomColor);
                stack.pop();
            }
            for (i = 0; i < segments; ++i) {
                stack.push();
                angle = 360.0f / (float)segments * (float)i;
                stack.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
                stack.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees(angle));
                stack.multiply((Quaternionfc)camRot);
                matrix = stack.peek().getPositionMatrix();
                bloomConsumer.vertex((Matrix4fc)matrix, -bloomSize / 2.0f, -bloomSize / 2.0f, 0.0f).texture(0.0f, 1.0f).color(bloomColor);
                bloomConsumer.vertex((Matrix4fc)matrix, bloomSize / 2.0f, -bloomSize / 2.0f, 0.0f).texture(1.0f, 1.0f).color(bloomColor);
                bloomConsumer.vertex((Matrix4fc)matrix, bloomSize / 2.0f, bloomSize / 2.0f, 0.0f).texture(1.0f, 0.0f).color(bloomColor);
                bloomConsumer.vertex((Matrix4fc)matrix, -bloomSize / 2.0f, bloomSize / 2.0f, 0.0f).texture(0.0f, 0.0f).color(bloomColor);
                stack.pop();
            }
        }
    }
}


