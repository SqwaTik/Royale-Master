package royale.util.render.pipeline;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;
public class BlurPipeline {
private static final Identifier PIPELINE_ID = Identifier.of("royale", "pipeline/blur");
private static final Identifier VERTEX_SHADER = Identifier.of("royale", "core/blur");
private static final Identifier FRAGMENT_SHADER = Identifier.of("royale", "core/blur");
private static final float FIXED_GUI_SCALE = 2.0F;
private static final RenderPipeline PIPELINE = RenderPipelines.register(
RenderPipeline.builder(new RenderPipeline.Snippet[] { RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET
}).withLocation(PIPELINE_ID)
.withVertexShader(VERTEX_SHADER)
.withFragmentShader(FRAGMENT_SHADER)
.withVertexFormat(VertexFormats.EMPTY, VertexFormat.DrawMode.TRIANGLES)
.withUniform("BlurData", UniformType.UNIFORM_BUFFER)
.withSampler("Sampler0")
.withBlend(BlendFunction.TRANSLUCENT)
.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
.withDepthWrite(false)
.withCull(false)
.build());
private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
private static final int BUFFER_SIZE = 128;
private GpuBuffer uniformBuffer;
private GpuBuffer dummyVertexBuffer;
private ByteBuffer dataBuffer;
private GpuTexture copyTexture;
private GpuTextureView copyTextureView;
private int lastWidth = 0;
private int lastHeight = 0;
private boolean initialized = false;
private int getFixedScaledWidth() {
MinecraftClient client = MinecraftClient.getInstance();
if (client == null || client.getWindow() == null) return 960; 
return (int)Math.ceil(client.getWindow().getFramebufferWidth() / 2.0D);
}
private int getFixedScaledHeight() {
MinecraftClient client = MinecraftClient.getInstance();
if (client == null || client.getWindow() == null) return 540; 
return (int)Math.ceil(client.getWindow().getFramebufferHeight() / 2.0D);
}
private void ensureInitialized() {
if (this.initialized)
return; 
this.dataBuffer = MemoryUtil.memAlloc(128);
ByteBuffer dummyData = MemoryUtil.memAlloc(4);
dummyData.putInt(0);
dummyData.flip();
this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:blur_dummy_vertex", 32, dummyData);
MemoryUtil.memFree(dummyData);
this.initialized = true;
}
private void ensureCopyTexture(int width, int height) {
if (this.copyTexture == null || this.lastWidth != width || this.lastHeight != height) {
if (this.copyTextureView != null) {
this.copyTextureView.close();
this.copyTextureView = null;
} 
if (this.copyTexture != null) {
this.copyTexture.close();
this.copyTexture = null;
} 
this.copyTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:blur_copy", 5, TextureFormat.RGBA8, width, height, 1, 1);
this.copyTextureView = RenderSystem.getDevice().createTextureView(this.copyTexture);
this.lastWidth = width;
this.lastHeight = height;
} 
}
public void drawBlur(float x, float y, float width, float height, float radius, float[] radii, int color) {
MinecraftClient client = MinecraftClient.getInstance();
if (client.getFramebuffer() == null)
return;  if (client.getFramebuffer().getColorAttachment() == null)
return; 
ensureInitialized();
int fbWidth = (client.getFramebuffer()).textureWidth;
int fbHeight = (client.getFramebuffer()).textureHeight;
ensureCopyTexture(fbWidth, fbHeight);
int fixedScreenWidth = getFixedScaledWidth();
int fixedScreenHeight = getFixedScaledHeight();
prepareUniformData(x, y, width, height, fixedScreenWidth, fixedScreenHeight, fbWidth, fbHeight, 2.0F, radius, radii, color);
CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
encoder.copyTextureToTexture(client
.getFramebuffer().getColorAttachment(), this.copyTexture, 0, 0, 0, 0, 0, fbWidth, fbHeight);
encoder.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().write((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
GpuSampler sampler = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
RenderPass renderPass = encoder.createRenderPass(() -> "minecraft:blur_pass", client
.getFramebuffer().getColorAttachmentView(), 
OptionalInt.empty(), client
.getFramebuffer().getDepthAttachmentView(), 
OptionalDouble.empty());
try {
renderPass.setPipeline(PIPELINE);
renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
renderPass.bindTexture("Sampler0", this.copyTextureView, sampler);
RenderSystem.bindDefaultUniforms(renderPass);
renderPass.setUniform("DynamicTransforms", dynamicTransforms);
renderPass.setUniform("BlurData", this.uniformBuffer);
renderPass.draw(0, 6);
if (renderPass != null) renderPass.close(); 
} catch (Throwable throwable) {
if (renderPass != null)
try {
renderPass.close();
} catch (Throwable throwable1) {
throwable.addSuppressed(throwable1);
}   throw throwable;
}  } private void prepareUniformData(float x, float y, float width, float height, float screenWidth, float screenHeight, int fbWidth, int fbHeight, float guiScale, float blurRadius, float[] radii, int color) { this.dataBuffer.clear();
this.dataBuffer.putFloat(x);
this.dataBuffer.putFloat(y);
this.dataBuffer.putFloat(width);
this.dataBuffer.putFloat(height);
this.dataBuffer.putFloat(screenWidth);
this.dataBuffer.putFloat(screenHeight);
this.dataBuffer.putFloat(guiScale);
this.dataBuffer.putFloat(blurRadius);
this.dataBuffer.putFloat(fbWidth);
this.dataBuffer.putFloat(fbHeight);
this.dataBuffer.putFloat(0.0F);
this.dataBuffer.putFloat(0.0F);
this.dataBuffer.putFloat(radii[0]);
this.dataBuffer.putFloat(radii[1]);
this.dataBuffer.putFloat(radii[2]);
this.dataBuffer.putFloat(radii[3]);
float a = (color >> 24 & 0xFF) / 255.0F;
float r = (color >> 16 & 0xFF) / 255.0F;
float g = (color >> 8 & 0xFF) / 255.0F;
float b = (color & 0xFF) / 255.0F;
this.dataBuffer.putFloat(r);
this.dataBuffer.putFloat(g);
this.dataBuffer.putFloat(b);
this.dataBuffer.putFloat(a);
this.dataBuffer.flip();
int size = this.dataBuffer.remaining();
if (this.uniformBuffer == null || this.uniformBuffer.size() < size) {
if (this.uniformBuffer != null) {
this.uniformBuffer.close();
}
this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:blur_uniform", 136, size);
}  }
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
MemoryUtil.memFree(this.dataBuffer);
this.dataBuffer = null;
} 
if (this.copyTextureView != null) {
this.copyTextureView.close();
this.copyTextureView = null;
} 
if (this.copyTexture != null) {
this.copyTexture.close();
this.copyTexture = null;
} 
this.lastWidth = 0;
this.lastHeight = 0;
this.initialized = false;
}
}


