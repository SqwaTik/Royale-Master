package royale.modules.impl.render.particles;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import royale.IMinecraft;
import royale.modules.impl.render.worldparticles.ParticleRenderer;
import royale.util.ColorUtil;
import royale.util.animations.Animation;
import royale.util.animations.Direction;
import royale.util.animations.EaseInOutQuad;
import royale.util.render.clientpipeline.ClientPipelines;

public class Particle3D
implements IMinecraft {
    private static final ParticleMode[] RANDOM_MODES = new ParticleMode[]{ParticleMode.CUBES, ParticleMode.CROWN, ParticleMode.CUBE_BLAST, ParticleMode.DOLLAR, ParticleMode.HEART, ParticleMode.LIGHTNING, ParticleMode.LINE, ParticleMode.RHOMBUS, ParticleMode.SNOWFLAKE, ParticleMode.STAR, ParticleMode.STAR_ALT, ParticleMode.TRIANGLE};
    private static final Identifier TEXTURE_CROWN = Identifier.of((String)"royale", (String)"textures/world/crown.png");
    private static final Identifier TEXTURE_CUBE_BLAST = Identifier.of((String)"royale", (String)"textures/world/cubeblast1.png");
    private static final Identifier TEXTURE_DOLLAR = Identifier.of((String)"royale", (String)"textures/world/dollar.png");
    private static final Identifier TEXTURE_HEART = Identifier.of((String)"royale", (String)"textures/world/heart.png");
    private static final Identifier TEXTURE_LIGHTNING = Identifier.of((String)"royale", (String)"textures/world/lightning.png");
    private static final Identifier TEXTURE_LINE = Identifier.of((String)"royale", (String)"textures/world/line.png");
    private static final Identifier TEXTURE_RHOMBUS = Identifier.of((String)"royale", (String)"textures/world/rhombus.png");
    private static final Identifier TEXTURE_SNOWFLAKE = Identifier.of((String)"royale", (String)"textures/world/snowflake.png");
    private static final Identifier TEXTURE_STAR = Identifier.of((String)"royale", (String)"textures/world/star.png");
    private static final Identifier TEXTURE_STAR_ALT = Identifier.of((String)"royale", (String)"textures/world/star1.png");
    private static final Identifier TEXTURE_TRIANGLE = Identifier.of((String)"royale", (String)"textures/world/triangle.png");
    private static final Identifier GLOW_BLOOM = Identifier.of((String)"royale", (String)"textures/world/dashbloom.png");
    private static final Identifier GLOW_BLOOM_SAMPLE = Identifier.of((String)"royale", (String)"textures/world/dashbloomsample.png");
    private double x;
    private double y;
    private double z;
    private double lastX;
    private double lastY;
    private double lastZ;
    private double velocityX;
    private double velocityY;
    private double velocityZ;
    private long start;
    private float phase;
    private int color;
    private float scale;
    private long lifeTimeMs;
    private float rotation;
    private Animation fadeInAnimation;
    private Animation fadeOutAnimation;
    private float cachedAlpha = 0.0f;
    private long lastAlphaUpdate = 0L;
    private boolean fadingOut = false;
    private float gravityStrength = 0.04f;
    private float velocityMultiplier = 0.98f;
    private boolean collidesWithWorld = true;
    private ParticleMode actualMode = ParticleMode.CUBES;
    private GlowMode glowMode = GlowMode.BOTH;
    private boolean spinning = true;

    public Particle3D(Vec3d pos, Vec3d velocity, int color, float scale, float maxAgeSeconds) {
        this.start = System.currentTimeMillis();
        this.phase = (float)(Math.random() * 100.0);
        this.rotation = (float)(Math.random() * 360.0);
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.lastX = pos.x;
        this.lastY = pos.y;
        this.lastZ = pos.z;
        this.velocityX = velocity.x;
        this.velocityY = velocity.y;
        this.velocityZ = velocity.z;
        this.color = color;
        this.scale = scale;
        this.lifeTimeMs = (long)(maxAgeSeconds * 1000.0f);
        this.fadeInAnimation = new EaseInOutQuad().setMs(150).setValue(1.0);
        this.fadeInAnimation.setDirection(Direction.FORWARDS);
        this.fadeOutAnimation = new EaseInOutQuad().setMs(250).setValue(1.0);
        this.fadeOutAnimation.setDirection(Direction.FORWARDS);
    }

    public Particle3D setGravity(float gravity) {
        this.gravityStrength = gravity;
        return this;
    }

    public Particle3D setVelocityMultiplier(float mult) {
        this.velocityMultiplier = mult;
        return this;
    }

    public Particle3D setCollision(boolean collision) {
        this.collidesWithWorld = collision;
        return this;
    }

    public Particle3D setMode(ParticleMode mode) {
        this.actualMode = mode == ParticleMode.RANDOM ? RANDOM_MODES[ThreadLocalRandom.current().nextInt(RANDOM_MODES.length)] : mode;
        return this;
    }

    public Particle3D setGlowMode(GlowMode glowMode) {
        this.glowMode = glowMode;
        return this;
    }

    public Particle3D setSpinning(boolean spinning) {
        this.spinning = spinning;
        return this;
    }

    public void update() {
        boolean checkCollision;
        long now = System.currentTimeMillis();
        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;
        this.velocityY -= (double)this.gravityStrength;
        double velocityMagSq = this.velocityX * this.velocityX + this.velocityY * this.velocityY + this.velocityZ * this.velocityZ;
        boolean bl = checkCollision = this.collidesWithWorld && Particle3D.mc.world != null && velocityMagSq > 1.0E-6;
        if (checkCollision) {
            if (this.isHit(this.x + this.velocityX, this.y, this.z)) {
                this.velocityX *= -0.8;
            } else {
                this.x += this.velocityX;
            }
            if (this.isHit(this.x, this.y + this.velocityY, this.z)) {
                this.velocityX *= 0.999;
                this.velocityZ *= 0.999;
                this.velocityY *= -0.7;
            } else {
                this.y += this.velocityY;
            }
            if (this.isHit(this.x, this.y, this.z + this.velocityZ)) {
                this.velocityZ *= -0.8;
            } else {
                this.z += this.velocityZ;
            }
        } else {
            this.x += this.velocityX;
            this.y += this.velocityY;
            this.z += this.velocityZ;
        }
        this.velocityX *= (double)this.velocityMultiplier;
        this.velocityZ *= (double)this.velocityMultiplier;
        if (Math.abs(this.velocityX) < 1.0E-5) {
            this.velocityX = 0.0;
        }
        if (Math.abs(this.velocityY) < 1.0E-5) {
            this.velocityY = 0.0;
        }
        if (Math.abs(this.velocityZ) < 1.0E-5) {
            this.velocityZ = 0.0;
        }
        if (this.spinning) {
            this.rotation += 2.0f;
        }
        if (!this.fadingOut && now - this.start > this.lifeTimeMs) {
            this.fadingOut = true;
            this.fadeOutAnimation.setDirection(Direction.BACKWARDS);
        }
        if (now - this.lastAlphaUpdate > 16L) {
            this.cachedAlpha = this.fadingOut ? this.fadeOutAnimation.getOutput().floatValue() : this.fadeInAnimation.getOutput().floatValue();
            this.lastAlphaUpdate = now;
        }
    }

    private boolean isHit(double px, double py, double pz) {
        if (Particle3D.mc.world == null) {
            return false;
        }
        BlockPos pos = BlockPos.ofFloored((double)px, (double)py, (double)pz);
        return Particle3D.mc.world.getBlockState(pos).isFullCube((BlockView)Particle3D.mc.world, pos);
    }

    public boolean isDead() {
        return this.fadingOut && this.cachedAlpha <= 0.0f;
    }

    public double distanceSquaredTo(Vec3d target) {
        double dx = this.x - target.x;
        double dy = this.y - target.y;
        double dz = this.z - target.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public float getAlpha() {
        return this.cachedAlpha;
    }

    private Identifier getTexture() {
        return switch (this.actualMode.ordinal()) {
            case 1 -> TEXTURE_CROWN;
            case 2 -> TEXTURE_CUBE_BLAST;
            case 3 -> TEXTURE_DOLLAR;
            case 4 -> TEXTURE_HEART;
            case 5 -> TEXTURE_LIGHTNING;
            case 6 -> TEXTURE_LINE;
            case 7 -> TEXTURE_RHOMBUS;
            case 8 -> TEXTURE_SNOWFLAKE;
            case 9 -> TEXTURE_STAR;
            case 10 -> TEXTURE_STAR_ALT;
            case 11 -> TEXTURE_TRIANGLE;
            default -> null;
        };
    }

    public void render(MatrixStack matrices, VertexConsumerProvider immediate, float glowSize, float partialTicks) {
        float alpha = this.getAlpha();
        if (alpha <= 0.0f) {
            return;
        }
        Vec3d cameraPos = Particle3D.mc.gameRenderer.getCamera().getCameraPos();
        float cameraYaw = Particle3D.mc.gameRenderer.getCamera().getYaw();
        float cameraPitch = Particle3D.mc.gameRenderer.getCamera().getPitch();
        double interpX = MathHelper.lerp((double)partialTicks, (double)this.lastX, (double)this.x);
        double interpY = MathHelper.lerp((double)partialTicks, (double)this.lastY, (double)this.y);
        double interpZ = MathHelper.lerp((double)partialTicks, (double)this.lastZ, (double)this.z);
        float relX = (float)(interpX - cameraPos.x);
        float relY = (float)(interpY - cameraPos.y);
        float relZ = (float)(interpZ - cameraPos.z);
        if (this.actualMode == ParticleMode.CUBES) {
            this.renderCube(matrices, immediate, relX, relY, relZ, alpha, glowSize, cameraYaw, cameraPitch);
        } else {
            this.renderTextured(matrices, immediate, relX, relY, relZ, alpha, glowSize, cameraYaw, cameraPitch);
        }
    }

    private void renderCube(MatrixStack matrices, VertexConsumerProvider immediate, float relX, float relY, float relZ, float alpha, float glowSize, float cameraYaw, float cameraPitch) {
        long now = System.currentTimeMillis();
        float rotationAnim = (float)(now % 9000L) / 9000.0f * 360.0f;
        float alpha02 = alpha * 0.2f;
        float alpha04 = alpha * 0.4f;
        int glowCol = ColorUtil.multAlpha(this.color, alpha);
        float size = this.scale * 0.25f;
        float cubeGlow1 = size * glowSize;
        float cubeGlow2 = size * (glowSize / 3.0f);
        float rotY = rotationAnim + this.phase;
        float rotX = rotationAnim * 0.5f;
        matrices.push();
        matrices.translate(relX, relY, relZ);
        matrices.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees(rotY));
        matrices.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(rotX));
        Matrix4f mat = matrices.peek().getPositionMatrix();
        ParticleRenderer.drawCube(immediate.getBuffer(ParticleRenderer.getQuadsLayer()), mat, ColorUtil.multAlpha(this.color, alpha02), size);
        ParticleRenderer.drawLines(immediate.getBuffer(ParticleRenderer.getLinesLayer()), mat, ColorUtil.multAlpha(this.color, alpha04), size);
        matrices.pop();
        matrices.push();
        matrices.translate(relX, relY, relZ);
        matrices.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees(-cameraYaw));
        matrices.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(cameraPitch));
        Matrix4f gMat = matrices.peek().getPositionMatrix();
        this.renderGlowEffect(immediate, gMat, glowCol, alpha, cubeGlow1, cubeGlow2);
        matrices.pop();
    }

    private void renderTextured(MatrixStack matrices, VertexConsumerProvider immediate, float relX, float relY, float relZ, float alpha, float glowSize, float cameraYaw, float cameraPitch) {
        Identifier texture = this.getTexture();
        if (texture == null) {
            return;
        }
        int glowCol = ColorUtil.multAlpha(this.color, alpha);
        float size = this.scale * 0.5f;
        int r = glowCol >> 16 & 0xFF;
        int g = glowCol >> 8 & 0xFF;
        int b = glowCol & 0xFF;
        int a = (int)(255.0f * alpha);
        matrices.push();
        matrices.translate(relX, relY, relZ);
        matrices.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees(-cameraYaw));
        matrices.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(cameraPitch));
        if (this.spinning) {
            matrices.multiply((Quaternionfc)RotationAxis.POSITIVE_Z.rotationDegrees(this.rotation));
        }
        Matrix4f mat = matrices.peek().getPositionMatrix();
        RenderLayer layer = ClientPipelines.WORLD_PARTICLES_GLOW.apply(texture);
        VertexConsumer buffer = immediate.getBuffer(layer);
        float half = size / 2.0f;
        buffer.vertex((Matrix4fc)mat, -half, -half, 0.0f).texture(0.0f, 0.0f).color(r, g, b, a);
        buffer.vertex((Matrix4fc)mat, -half, half, 0.0f).texture(0.0f, 1.0f).color(r, g, b, a);
        buffer.vertex((Matrix4fc)mat, half, half, 0.0f).texture(1.0f, 1.0f).color(r, g, b, a);
        buffer.vertex((Matrix4fc)mat, half, -half, 0.0f).texture(1.0f, 0.0f).color(r, g, b, a);
        matrices.pop();
        matrices.push();
        matrices.translate(relX, relY, relZ);
        matrices.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees(-cameraYaw));
        matrices.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(cameraPitch));
        Matrix4f gMat = matrices.peek().getPositionMatrix();
        float glowSizePrimary = size * glowSize * 0.5f;
        float glowSizeSecondary = size * glowSize * 0.2f;
        this.renderGlowEffect(immediate, gMat, glowCol, alpha, glowSizePrimary, glowSizeSecondary);
        matrices.pop();
    }

    private void renderGlowEffect(VertexConsumerProvider immediate, Matrix4f matrix, int color, float alpha, float sizePrimary, float sizeSecondary) {
        switch (this.glowMode.ordinal()) {
            case 0: {
                RenderLayer layerBloom = ClientPipelines.WORLD_PARTICLES_GLOW.apply(GLOW_BLOOM);
                ParticleRenderer.drawGlow(immediate.getBuffer(layerBloom), matrix, color, (int)(80.0f * alpha), sizePrimary);
                break;
            }
            case 1: {
                RenderLayer layerSample = ClientPipelines.WORLD_PARTICLES_GLOW.apply(GLOW_BLOOM_SAMPLE);
                ParticleRenderer.drawGlow(immediate.getBuffer(layerSample), matrix, color, (int)(140.0f * alpha), sizeSecondary);
                break;
            }
            case 2: {
                RenderLayer layerBloom = ClientPipelines.WORLD_PARTICLES_GLOW.apply(GLOW_BLOOM);
                RenderLayer layerSample = ClientPipelines.WORLD_PARTICLES_GLOW.apply(GLOW_BLOOM_SAMPLE);
                ParticleRenderer.drawGlow(immediate.getBuffer(layerBloom), matrix, color, (int)(80.0f * alpha), sizePrimary);
                ParticleRenderer.drawGlow(immediate.getBuffer(layerSample), matrix, color, (int)(140.0f * alpha), sizeSecondary);
            }
        }
    }

    public static enum ParticleMode {
        CUBES,
        CROWN,
        CUBE_BLAST,
        DOLLAR,
        HEART,
        LIGHTNING,
        LINE,
        RHOMBUS,
        SNOWFLAKE,
        STAR,
        STAR_ALT,
        TRIANGLE,
        RANDOM;

    }

    public static enum GlowMode {
        BLOOM,
        BLOOM_SAMPLE,
        BOTH;

    }
}

