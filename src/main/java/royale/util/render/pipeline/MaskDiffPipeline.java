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
public class MaskDiffPipeline {
private static final Identifier PIPELINE_ID = Identifier.of("royale", "pipeline/mask_diff");
private static final Identifier VERTEX_SHADER = Identifier.of("royale", "core/mask_diff");
private static final Identifier FRAGMENT_SHADER = Identifier.of("royale", "core/mask_diff");
private static final BlendFunction REPLACE_BLEND = new BlendFunction(SourceFactor.ONE, DestFactor.ZERO, SourceFactor.ONE, DestFactor.ZERO);
private static final RenderPipeline PIPELINE = RenderPipelines.register(
RenderPipeline.builder(new RenderPipeline.Snippet[] { RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET
}).withLocation(PIPELINE_ID)
.withVertexShader(VERTEX_SHADER)
.withFragmentShader(FRAGMENT_SHADER)
.withVertexFormat(VertexFormats.EMPTY, VertexFormat.DrawMode.TRIANGLES)
.withUniform("MaskData", UniformType.UNIFORM_BUFFER)
.withSampler("BeforeSampler")
.withSampler("AfterSampler")
.withSampler("DepthBeforeSampler")
.withSampler("DepthAfterSampler")
.withBlend(REPLACE_BLEND)
.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
.withDepthWrite(false)
.withCull(false)
.build());
private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
private static final int BUFFER_SIZE = 16;
private GpuBuffer uniformBuffer;
private GpuBuffer dummyVertexBuffer;
private ByteBuffer dataBuffer;
private boolean initialized = false;
private void ensureInitialized() {
if (this.initialized)
return; 
this.dataBuffer = MemoryUtil.memAlloc(16);
ByteBuffer dummyData = MemoryUtil.memAlloc(4);
dummyData.putInt(0);
dummyData.flip();
this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:mask_diff_dummy_vertex", 32, dummyData);
MemoryUtil.memFree(dummyData);
this.initialized = true;
}
public void createMask(GpuTextureView targetView, GpuTextureView beforeView, GpuTextureView afterView, GpuTextureView depthBeforeView, GpuTextureView depthAfterView, int width, int height) {
ensureInitialized();
CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
GpuSampler sampler = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
GpuSampler nearestSampler = RenderSystem.getSamplerCache().get(FilterMode.NEAREST);
prepareUniformData(width, height);
int size = this.dataBuffer.remaining();
if (this.uniformBuffer == null || this.uniformBuffer.size() < size) {
if (this.uniformBuffer != null) this.uniformBuffer.close(); 
this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:mask_diff_uniform", 136, size);
} 
encoder.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().write((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
RenderPass renderPass = encoder.createRenderPass(() -> "minecraft:mask_diff_pass", targetView, 
OptionalInt.of(0));
try { renderPass.setPipeline(PIPELINE);
renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
renderPass.bindTexture("BeforeSampler", beforeView, sampler);
renderPass.bindTexture("AfterSampler", afterView, sampler);
renderPass.bindTexture("DepthBeforeSampler", depthBeforeView, nearestSampler);
renderPass.bindTexture("DepthAfterSampler", depthAfterView, nearestSampler);
RenderSystem.bindDefaultUniforms(renderPass);
renderPass.setUniform("DynamicTransforms", dynamicTransforms);
renderPass.setUniform("MaskData", this.uniformBuffer);
renderPass.draw(0, 6);
if (renderPass != null) renderPass.close();  } catch (Throwable throwable) { if (renderPass != null)
try { renderPass.close(); }
catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }
throw throwable; }
} private void prepareUniformData(int width, int height) { this.dataBuffer.clear();
this.dataBuffer.putFloat(width);
this.dataBuffer.putFloat(height);
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


