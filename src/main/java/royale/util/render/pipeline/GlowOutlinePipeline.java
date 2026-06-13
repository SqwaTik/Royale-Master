package royale.util.render.pipeline;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
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
import org.lwjgl.system.MemoryUtil;
public class GlowOutlinePipeline {
private static final Identifier PIPELINE_ID = Identifier.of("royale", "pipeline/glow_outline");
private static final Identifier VERTEX_SHADER = Identifier.of("royale", "core/glow_outline");
private static final Identifier FRAGMENT_SHADER = Identifier.of("royale", "core/glow_outline");
private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
private static final float FIXED_GUI_SCALE = 2.0F;
private static final RenderPipeline PIPELINE = RenderPipelines.register(
RenderPipeline.builder(new RenderPipeline.Snippet[] { RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET
}).withLocation(PIPELINE_ID)
.withVertexShader(VERTEX_SHADER)
.withFragmentShader(FRAGMENT_SHADER)
.withVertexFormat(VertexFormats.EMPTY, VertexFormat.DrawMode.TRIANGLES)
.withUniform("GlowOutlineData", UniformType.UNIFORM_BUFFER)
.withBlend(BlendFunction.TRANSLUCENT)
.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
.withDepthWrite(false)
.withCull(false)
.build());
private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
private static final int BUFFER_SIZE = 128;
private GpuBuffer uniformBuffer;
private GpuBuffer dummyVertexBuffer;
private ByteBuffer dataBuffer;
private boolean initialized = false;
private void ensureInitialized() {
if (this.initialized)
return; 
this.dataBuffer = MemoryUtil.memAlloc(128);
ByteBuffer dummyData = MemoryUtil.memAlloc(4);
dummyData.putInt(0);
dummyData.flip();
this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:glow_outline_dummy_vertex", 32, dummyData);
MemoryUtil.memFree(dummyData);
this.initialized = true;
}
public void drawGlowOutline(float x, float y, float width, float height, int color, float thickness, float[] radii, float progress, float baseAlpha) {
MinecraftClient client = MinecraftClient.getInstance();
if (client.getFramebuffer() == null)
return; 
ensureInitialized();
int framebufferWidth = client.getWindow().getFramebufferWidth();
int framebufferHeight = client.getWindow().getFramebufferHeight();
float fixedScreenWidth = framebufferWidth / 2.0F;
float fixedScreenHeight = framebufferHeight / 2.0F;
prepareUniformData(x, y, width, height, fixedScreenWidth, fixedScreenHeight, 2.0F, color, thickness, radii, progress, baseAlpha);
uploadAndDraw(client);
}
private void prepareUniformData(float x, float y, float width, float height, float screenWidth, float screenHeight, float guiScale, int color, float thickness, float[] radii, float progress, float baseAlpha) {
this.dataBuffer.clear();
this.dataBuffer.putFloat(x);
this.dataBuffer.putFloat(y);
this.dataBuffer.putFloat(width);
this.dataBuffer.putFloat(height);
this.dataBuffer.putFloat(screenWidth);
this.dataBuffer.putFloat(screenHeight);
this.dataBuffer.putFloat(guiScale);
this.dataBuffer.putFloat(thickness);
this.dataBuffer.putFloat(radii[0]);
this.dataBuffer.putFloat(radii[1]);
this.dataBuffer.putFloat(radii[2]);
this.dataBuffer.putFloat(radii[3]);
this.dataBuffer.putFloat(progress);
this.dataBuffer.putFloat(baseAlpha);
this.dataBuffer.putFloat(0.0F);
this.dataBuffer.putFloat(0.0F);
float a = (color >> 24 & 0xFF) / 255.0F;
float r = (color >> 16 & 0xFF) / 255.0F;
float g = (color >> 8 & 0xFF) / 255.0F;
float b = (color & 0xFF) / 255.0F;
this.dataBuffer.putFloat(r);
this.dataBuffer.putFloat(g);
this.dataBuffer.putFloat(b);
this.dataBuffer.putFloat(a);
this.dataBuffer.flip();
}
private void uploadAndDraw(MinecraftClient client) {
int size = this.dataBuffer.remaining();
if (this.uniformBuffer == null || this.uniformBuffer.size() < size) {
if (this.uniformBuffer != null) {
this.uniformBuffer.close();
}
this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:glow_outline_uniform", 136, size);
} 
CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
encoder.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().write((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
RenderPass renderPass = encoder.createRenderPass(() -> "minecraft:glow_outline_pass", client
.getFramebuffer().getColorAttachmentView(), 
OptionalInt.empty(), client
.getFramebuffer().getDepthAttachmentView(), 
OptionalDouble.empty());
try { renderPass.setPipeline(PIPELINE);
renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
RenderSystem.bindDefaultUniforms(renderPass);
renderPass.setUniform("DynamicTransforms", dynamicTransforms);
renderPass.setUniform("GlowOutlineData", this.uniformBuffer);
renderPass.draw(0, 6);
if (renderPass != null) renderPass.close();  } catch (Throwable throwable) { if (renderPass != null)
try { renderPass.close(); }
catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }
throw throwable; }
} public void close() { if (this.uniformBuffer != null) {
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
this.initialized = false; }
}


