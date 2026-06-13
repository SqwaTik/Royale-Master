package royale.util.render.shader;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.MinecraftClient;
import royale.util.render.pipeline.GlassCompositePipeline;
import royale.util.render.pipeline.KawaseBlurPipeline;
import royale.util.render.pipeline.MaskDiffPipeline;
public class GlassHandsRenderer
{
private static GlassHandsRenderer instance;
private final MinecraftClient client;
private KawaseBlurPipeline kawaseBlur;
private GlassCompositePipeline glassComposite;
private MaskDiffPipeline maskDiff;
private GpuTexture sceneBeforeTexture;
private GpuTextureView sceneBeforeTextureView;
private GpuTexture sceneAfterTexture;
private GpuTextureView sceneAfterTextureView;
private GpuTexture depthBeforeTexture;
private GpuTextureView depthBeforeTextureView;
private GpuTexture depthAfterTexture;
private GpuTextureView depthAfterTextureView;
private GpuTexture maskTexture;
private GpuTextureView maskTextureView;
private int lastWidth = 0;
private int lastHeight = 0;
private boolean capturing = false;
private boolean enabled = false;
private boolean initialized = false;
private float blurRadius = 6.0F;
private int blurIterations = 4;
private float saturation = 1.0F;
private boolean reflect = true;
private int tintColor = 0;
private float tintIntensity = 0.1F;
private float edgeGlowIntensity = 0.3F;
public GlassHandsRenderer() {
this.client = MinecraftClient.getInstance();
instance = this;
}
public static GlassHandsRenderer getInstance() {
if (instance == null) {
instance = new GlassHandsRenderer();
}
return instance;
}
public static void resetInstance() {
if (instance != null) {
instance.close();
instance.initialized = false;
} 
}
private void ensureInitialized() {
if (this.initialized)
return; 
if (this.kawaseBlur != null) this.kawaseBlur.close(); 
if (this.glassComposite != null) this.glassComposite.close(); 
if (this.maskDiff != null) this.maskDiff.close();
this.kawaseBlur = new KawaseBlurPipeline();
this.glassComposite = new GlassCompositePipeline();
this.maskDiff = new MaskDiffPipeline();
this.lastWidth = 0;
this.lastHeight = 0;
this.initialized = true;
}
public void setEnabled(boolean enabled) {
this.enabled = enabled;
if (enabled) {
ensureInitialized();
}
}
public boolean isEnabled() {
return this.enabled;
}
public void setBlurRadius(float radius) {
this.blurRadius = radius;
}
public void setBlurIterations(int iterations) {
this.blurIterations = Math.max(1, Math.min(8, iterations));
}
public void setSaturation(float saturation) {
this.saturation = saturation;
}
public void setReflect(boolean reflect) {
this.reflect = reflect;
}
public void setTintColor(int color) {
this.tintColor = color;
}
public void setTintIntensity(float intensity) {
this.tintIntensity = intensity;
}
public void setEdgeGlowIntensity(float intensity) {
this.edgeGlowIntensity = intensity;
}
private void ensureTextures(int width, int height) {
if (width == this.lastWidth && height == this.lastHeight && this.sceneBeforeTexture != null)
return; 
cleanupTextures();
this.sceneBeforeTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_scene_before", 13, TextureFormat.RGBA8, width, height, 1, 1);
this.sceneBeforeTextureView = RenderSystem.getDevice().createTextureView(this.sceneBeforeTexture);
this.sceneAfterTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_scene_after", 13, TextureFormat.RGBA8, width, height, 1, 1);
this.sceneAfterTextureView = RenderSystem.getDevice().createTextureView(this.sceneAfterTexture);
this.depthBeforeTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_depth_before", 13, TextureFormat.DEPTH32, width, height, 1, 1);
this.depthBeforeTextureView = RenderSystem.getDevice().createTextureView(this.depthBeforeTexture);
this.depthAfterTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_depth_after", 13, TextureFormat.DEPTH32, width, height, 1, 1);
this.depthAfterTextureView = RenderSystem.getDevice().createTextureView(this.depthAfterTexture);
this.maskTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_mask", 13, TextureFormat.RGBA8, width, height, 1, 1);
this.maskTextureView = RenderSystem.getDevice().createTextureView(this.maskTexture);
this.lastWidth = width;
this.lastHeight = height;
}
private void cleanupTextures() {
if (this.sceneBeforeTextureView != null) {
this.sceneBeforeTextureView.close();
this.sceneBeforeTextureView = null;
} 
if (this.sceneBeforeTexture != null) {
this.sceneBeforeTexture.close();
this.sceneBeforeTexture = null;
} 
if (this.sceneAfterTextureView != null) {
this.sceneAfterTextureView.close();
this.sceneAfterTextureView = null;
} 
if (this.sceneAfterTexture != null) {
this.sceneAfterTexture.close();
this.sceneAfterTexture = null;
} 
if (this.depthBeforeTextureView != null) {
this.depthBeforeTextureView.close();
this.depthBeforeTextureView = null;
} 
if (this.depthBeforeTexture != null) {
this.depthBeforeTexture.close();
this.depthBeforeTexture = null;
} 
if (this.depthAfterTextureView != null) {
this.depthAfterTextureView.close();
this.depthAfterTextureView = null;
} 
if (this.depthAfterTexture != null) {
this.depthAfterTexture.close();
this.depthAfterTexture = null;
} 
if (this.maskTextureView != null) {
this.maskTextureView.close();
this.maskTextureView = null;
} 
if (this.maskTexture != null) {
this.maskTexture.close();
this.maskTexture = null;
} 
}
public void captureSceneBeforeHands() {
if (!this.enabled)
return; 
ensureInitialized();
Framebuffer fb = this.client.getFramebuffer();
if (fb == null || fb.getColorAttachment() == null)
return; 
int width = fb.textureWidth;
int height = fb.textureHeight;
ensureTextures(width, height);
CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
encoder.copyTextureToTexture(fb
.getColorAttachment(), this.sceneBeforeTexture, 0, 0, 0, 0, 0, width, height);
if (fb.getDepthAttachment() != null) {
encoder.copyTextureToTexture(fb
.getDepthAttachment(), this.depthBeforeTexture, 0, 0, 0, 0, 0, width, height);
}
this.capturing = true;
}
public void captureSceneAfterHands() {
if (!this.enabled || !this.capturing)
return; 
Framebuffer fb = this.client.getFramebuffer();
if (fb == null || fb.getColorAttachment() == null)
return; 
CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
encoder.copyTextureToTexture(fb
.getColorAttachment(), this.sceneAfterTexture, 0, 0, 0, 0, 0, this.lastWidth, this.lastHeight);
if (fb.getDepthAttachment() != null) {
encoder.copyTextureToTexture(fb
.getDepthAttachment(), this.depthAfterTexture, 0, 0, 0, 0, 0, this.lastWidth, this.lastHeight);
}
}
public void renderGlassEffect() {
if (!this.enabled || !this.capturing)
return; 
Framebuffer fb = this.client.getFramebuffer();
if (fb == null || fb.getColorAttachment() == null) {
this.capturing = false;
return;
} 
this.maskDiff.createMask(this.maskTextureView, this.sceneBeforeTextureView, this.sceneAfterTextureView, this.depthBeforeTextureView, this.depthAfterTextureView, this.lastWidth, this.lastHeight);
GpuTextureView blurredView = this.kawaseBlur.blur(this.sceneBeforeTexture, this.sceneBeforeTextureView, this.lastWidth, this.lastHeight, this.blurIterations, this.blurRadius);
if (blurredView == null) {
this.capturing = false;
return;
} 
this.glassComposite.composite(fb
.getColorAttachmentView(), this.sceneBeforeTextureView, blurredView, this.maskTextureView, this.lastWidth, this.lastHeight, this.saturation, this.reflect, this.tintColor, this.tintIntensity, this.edgeGlowIntensity);
this.capturing = false;
}
public boolean isCapturing() {
return this.capturing;
}
public void invalidate() {
cleanupTextures();
if (this.kawaseBlur != null) this.kawaseBlur.close(); 
if (this.glassComposite != null) this.glassComposite.close(); 
if (this.maskDiff != null) this.maskDiff.close(); 
this.kawaseBlur = null;
this.glassComposite = null;
this.maskDiff = null;
this.lastWidth = 0;
this.lastHeight = 0;
this.initialized = false;
this.capturing = false;
}
public void close() {
cleanupTextures();
if (this.kawaseBlur != null) {
this.kawaseBlur.close();
this.kawaseBlur = null;
} 
if (this.glassComposite != null) {
this.glassComposite.close();
this.glassComposite = null;
} 
if (this.maskDiff != null) {
this.maskDiff.close();
this.maskDiff = null;
} 
this.lastWidth = 0;
this.lastHeight = 0;
this.initialized = false;
}
}


