package royale.util.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.system.MemoryUtil;

public class TexturePipeline {
    private static final Identifier PIPELINE_ID = Identifier.of((String)"royale", (String)"pipeline/texture");
    private static final Identifier VERTEX_SHADER = Identifier.of((String)"royale", (String)"core/texture");
    private static final Identifier FRAGMENT_SHADER = Identifier.of((String)"royale", (String)"core/texture");
    private static final Vector3f MODEL_OFFSET = new Vector3f(0.0f, 0.0f, 0.0f);
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final float FIXED_GUI_SCALE = 2.0f;
    private static final RenderPipeline PIPELINE = RenderPipelines.register((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET}).withLocation(PIPELINE_ID).withVertexShader(VERTEX_SHADER).withFragmentShader(FRAGMENT_SHADER).withVertexFormat(VertexFormats.EMPTY, VertexFormat.DrawMode.TRIANGLES).withUniform("TextureData", UniformType.UNIFORM_BUFFER).withSampler("Sampler0").withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private static final int BUFFER_SIZE = 256;
    private GpuBuffer uniformBuffer;
    private GpuBuffer dummyVertexBuffer;
    private ByteBuffer dataBuffer;
    private boolean initialized = false;
    private boolean failed = false;

    private void ensureInitialized() {
        if (this.initialized) {
            return;
        }
        this.dataBuffer = MemoryUtil.memAlloc((int)256);
        ByteBuffer dummyData = MemoryUtil.memAlloc((int)4);
        dummyData.putInt(0);
        dummyData.flip();
        this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:texture_dummy_vertex", 32, dummyData);
        MemoryUtil.memFree((Buffer)dummyData);
        this.initialized = true;
    }

    public void drawTexture(Identifier textureId, float x, float y, float width, float height, float u0, float v0, float u1, float v1, int[] colors, float[] radii, float smoothness) {
        this.drawTexture(textureId, x, y, width, height, u0, v0, u1, v1, colors, radii, smoothness, 0.0f);
    }

    public void drawTexture(Identifier textureId, float x, float y, float width, float height, float u0, float v0, float u1, float v1, int[] colors, float[] radii, float smoothness, float rotation) {
        if (this.failed) {
            return;
        }
        int textureGlId;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getFramebuffer() == null) {
            return;
        }
        AbstractTexture texture = client.getTextureManager().getTexture(textureId);
        if (texture == null) {
            return;
        }
        try {
            GpuTexture gpuTexture = texture.getGlTexture();
            if (gpuTexture == null) {
                return;
            }
            textureGlId = this.getTextureGlId(gpuTexture);
            if (textureGlId <= 0) {
                return;
            }
        }
        catch (Exception e) {
            return;
        }
        try {
            this.ensureInitialized();
            int framebufferWidth = client.getWindow().getFramebufferWidth();
            int framebufferHeight = client.getWindow().getFramebufferHeight();
            float fixedScreenWidth = (float)framebufferWidth / 2.0f;
            float fixedScreenHeight = (float)framebufferHeight / 2.0f;
            this.prepareUniformData(x, y, width, height, u0, v0, u1, v1, fixedScreenWidth, fixedScreenHeight, 2.0f, colors, radii, smoothness, rotation);
            this.uploadAndDraw(client, textureGlId);
        } catch (Throwable ignored) {
            this.failed = true;
        }
    }

    public void drawFramebufferTexture(int textureId, float x, float y, float width, float height, int[] colors, float[] radii, float alpha) {
        if (this.failed) {
            return;
        }
        if (textureId <= 0) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getFramebuffer() == null) {
            return;
        }
        try {
            this.ensureInitialized();
            int framebufferWidth = client.getWindow().getFramebufferWidth();
            int framebufferHeight = client.getWindow().getFramebufferHeight();
            float fixedScreenWidth = (float)framebufferWidth / 2.0f;
            float fixedScreenHeight = (float)framebufferHeight / 2.0f;
            this.prepareUniformData(x, y, width, height, 0.0f, 0.0f, 1.0f, 1.0f, fixedScreenWidth, fixedScreenHeight, 2.0f, colors, radii, 1.0f, 0.0f);
            this.uploadAndDraw(client, textureId);
        } catch (Throwable ignored) {
            this.failed = true;
        }
    }

    private void prepareUniformData(float x, float y, float w, float h, float u0, float v0, float u1, float v1, float screenWidth, float screenHeight, float guiScale, int[] colors, float[] radii, float smoothness, float rotation) {
        this.dataBuffer.clear();
        this.dataBuffer.putFloat(screenWidth);
        this.dataBuffer.putFloat(screenHeight);
        this.dataBuffer.putFloat(smoothness);
        this.dataBuffer.putFloat(guiScale);
        this.dataBuffer.putFloat(x);
        this.dataBuffer.putFloat(y);
        this.dataBuffer.putFloat(w);
        this.dataBuffer.putFloat(h);
        this.dataBuffer.putFloat(u0);
        this.dataBuffer.putFloat(v0);
        this.dataBuffer.putFloat(u1);
        this.dataBuffer.putFloat(v1);
        this.dataBuffer.putFloat(radii[0]);
        this.dataBuffer.putFloat(radii[1]);
        this.dataBuffer.putFloat(radii[2]);
        this.dataBuffer.putFloat(radii[3]);
        float rotationRadians = (float)Math.toRadians(rotation);
        this.dataBuffer.putFloat(rotationRadians);
        this.dataBuffer.putFloat(0.0f);
        this.dataBuffer.putFloat(0.0f);
        this.dataBuffer.putFloat(0.0f);
        for (int i = 0; i < 4; ++i) {
            int color = i < colors.length ? colors[i] : colors[colors.length - 1];
            float a = (float)(color >> 24 & 0xFF) / 255.0f;
            float r = (float)(color >> 16 & 0xFF) / 255.0f;
            float g = (float)(color >> 8 & 0xFF) / 255.0f;
            float b = (float)(color & 0xFF) / 255.0f;
            this.dataBuffer.putFloat(r);
            this.dataBuffer.putFloat(g);
            this.dataBuffer.putFloat(b);
            this.dataBuffer.putFloat(a);
        }
        this.dataBuffer.flip();
    }

    private void uploadAndDraw(MinecraftClient client, int textureGlId) {
        int size = this.dataBuffer.remaining();
        if (this.uniformBuffer == null || this.uniformBuffer.size() < (long)size) {
            if (this.uniformBuffer != null) {
                this.uniformBuffer.close();
            }
            this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:texture_uniform", 136, (long)size);
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().write((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
        GL13.glActiveTexture((int)33984);
        GL11.glBindTexture((int)3553, (int)textureGlId);
        try (RenderPass renderPass = encoder.createRenderPass(() -> "minecraft:texture_pass", client.getFramebuffer().getColorAttachmentView(), OptionalInt.empty(), client.getFramebuffer().getDepthAttachmentView(), OptionalDouble.empty());){
            renderPass.setPipeline(PIPELINE);
            renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
            RenderSystem.bindDefaultUniforms((RenderPass)renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setUniform("TextureData", this.uniformBuffer);
            renderPass.draw(0, 6);
        }
        GL11.glBindTexture((int)3553, (int)0);
    }

    private int getTextureGlId(GpuTexture gpuTexture) {
        try {
            Field field = gpuTexture.getClass().getDeclaredField("id");
            field.setAccessible(true);
            return field.getInt(gpuTexture);
        }
        catch (Exception e1) {
            try {
                Field field = gpuTexture.getClass().getDeclaredField("glId");
                field.setAccessible(true);
                return field.getInt(gpuTexture);
            }
            catch (Exception e2) {
                try {
                    for (Field f : gpuTexture.getClass().getDeclaredFields()) {
                        if (f.getType() != Integer.TYPE) continue;
                        f.setAccessible(true);
                        int value = f.getInt(gpuTexture);
                        if (value <= 0) continue;
                        return value;
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                return 0;
            }
        }
    }

    public void flush() {
    }

    public void close() {
        if (this.uniformBuffer != null) {
            this.uniformBuffer.close();
            this.uniformBuffer = null;
        }
        if (this.dummyVertexBuffer != null) {
            this.dummyVertexBuffer.close();
            this.dummyVertexBuffer = null;
        }
        if (this.dataBuffer != null) {
            MemoryUtil.memFree((Buffer)this.dataBuffer);
            this.dataBuffer = null;
        }
        this.initialized = false;
        this.failed = false;
    }
}

