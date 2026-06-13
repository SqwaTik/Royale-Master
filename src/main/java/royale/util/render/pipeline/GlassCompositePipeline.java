package royale.util.render.pipeline;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;
public class GlassCompositePipeline {
private static final Identifier PIPELINE_ID = Identifier.of("royale", "pipeline/glass_composite");
private static final Identifier VERTEX_SHADER = Identifier.of("royale", "core/glass_composite");
private static final Identifier FRAGMENT_SHADER = Identifier.of("royale", "core/glass_composite");
private static final BlendFunction REPLACE_BLEND = new BlendFunction(SourceFactor.ONE, DestFactor.ZERO, SourceFactor.ONE, DestFactor.ZERO);
private static final RenderPipeline PIPELINE = RenderPipelines.register(
RenderPipeline.builder(new RenderPipeline.Snippet[] { RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET
}).withLocation(PIPELINE_ID)
.withVertexShader(VERTEX_SHADER)
.withFragmentShader(FRAGMENT_SHADER)
.withVertexFormat(VertexFormats.EMPTY, VertexFormat.DrawMode.TRIANGLES)
.withUniform("GlassData", UniformType.UNIFORM_BUFFER)
.withSampler("SceneSampler")
.withSampler("BlurSampler")
.withSampler("MaskSampler")
.withBlend(REPLACE_BLEND)
.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
.withDepthWrite(false)
.withCull(false)
.build());
private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
private static final int BUFFER_SIZE = 64;
private GpuBuffer uniformBuffer;
private GpuBuffer dummyVertexBuffer;
private ByteBuffer dataBuffer;
private boolean initialized = false;
private void ensureInitialized() {
if (this.initialized)
return; 
this.dataBuffer = MemoryUtil.memAlloc(64);
ByteBuffer dummyData = MemoryUtil.memAlloc(4);
dummyData.putInt(0);
dummyData.flip();
this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:glass_composite_dummy_vertex", 32, dummyData);
MemoryUtil.memFree(dummyData);
this.initialized = true;
}
public void composite(GpuTextureView targetView, GpuTextureView sceneView, GpuTextureView blurView, GpuTextureView maskView, int width, int height, float saturation, boolean reflect, int tintColor, float tintIntensity, float edgeGlowIntensity) {
ensureInitialized();
prepareUniformData(width, height, saturation, reflect, tintColor, tintIntensity, edgeGlowIntensity);
int size = this.dataBuffer.remaining();
if (this.uniformBuffer == null || this.uniformBuffer.size() < size) {
if (this.uniformBuffer != null) this.uniformBuffer.close(); 
this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:glass_composite_uniform", 136, size);
} 
CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
encoder.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().write((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
GpuSampler linearSampler = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
RenderPass renderPass = encoder.createRenderPass(() -> "minecraft:glass_composite_pass", targetView, 
OptionalInt.empty());
try { renderPass.setPipeline(PIPELINE);
renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
renderPass.bindTexture("SceneSampler", sceneView, linearSampler);
renderPass.bindTexture("BlurSampler", blurView, linearSampler);
renderPass.bindTexture("MaskSampler", maskView, linearSampler);
RenderSystem.bindDefaultUniforms(renderPass);
renderPass.setUniform("DynamicTransforms", dynamicTransforms);
renderPass.setUniform("GlassData", this.uniformBuffer);
renderPass.draw(0, 6);
if (renderPass != null) renderPass.close();  }
catch (Throwable throwable) { if (renderPass != null)
try { renderPass.close(); }
catch (Throwable throwable1)
{ throwable.addSuppressed(throwable1); }
throw throwable; }
} private void prepareUniformData(int width, int height, float saturation, boolean reflect, int tintColor, float tintIntensity, float edgeGlowIntensity) { this.dataBuffer.clear();
this.dataBuffer.putFloat(width);
this.dataBuffer.putFloat(height);
this.dataBuffer.putFloat(saturation);
this.dataBuffer.putFloat(reflect ? 1.0F : 0.0F);
float a = (tintColor >> 24 & 0xFF) / 255.0F;
float r = (tintColor >> 16 & 0xFF) / 255.0F;
float g = (tintColor >> 8 & 0xFF) / 255.0F;
float b = (tintColor & 0xFF) / 255.0F;
this.dataBuffer.putFloat(r);
this.dataBuffer.putFloat(g);
this.dataBuffer.putFloat(b);
this.dataBuffer.putFloat(a);
this.dataBuffer.putFloat(tintIntensity);
this.dataBuffer.putFloat(edgeGlowIntensity);
this.dataBuffer.putFloat(0.0F);
this.dataBuffer.putFloat(0.0F);
this.dataBuffer.flip(); }
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
this.initialized = false;
}
}


