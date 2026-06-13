package royale.util.render.shader;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import royale.util.render.font.FontRenderer;
import royale.util.render.font.Fonts;
import royale.util.render.pipeline.Arc2D;
import royale.util.render.pipeline.ArcOutline2D;
import royale.util.render.pipeline.BlurPipeline;
import royale.util.render.pipeline.GlassCompositePipeline;
import royale.util.render.pipeline.GlowOutlinePipeline;
import royale.util.render.pipeline.KawaseBlurPipeline;
import royale.util.render.pipeline.MaskDiffPipeline;
import royale.util.render.pipeline.OutlinePipeline;
import royale.util.render.pipeline.RectPipeline;
import royale.util.render.pipeline.TexturePipeline;
public class RenderCore {
private final RectPipeline rectPipeline;
private final OutlinePipeline outlinePipeline;
private final GlowOutlinePipeline glowOutlinePipeline;
private final TexturePipeline texturePipeline;
private final BlurPipeline blurPipeline;
private final KawaseBlurPipeline kawaseBlurPipeline;
public RenderCore() {
this.rectPipeline = new RectPipeline();
this.outlinePipeline = new OutlinePipeline();
this.glowOutlinePipeline = new GlowOutlinePipeline();
this.texturePipeline = new TexturePipeline();
this.blurPipeline = new BlurPipeline();
this.kawaseBlurPipeline = new KawaseBlurPipeline();
this.glassCompositePipeline = new GlassCompositePipeline();
this.glassHandsRenderer = new GlassHandsRenderer();
this.maskDiffPipeline = new MaskDiffPipeline();
this.fontRenderer = new FontRenderer();
}
private final GlassCompositePipeline glassCompositePipeline; private final GlassHandsRenderer glassHandsRenderer; private final FontRenderer fontRenderer; private final MaskDiffPipeline maskDiffPipeline; private boolean fontsLoaded = false; private boolean arcInitialized = false; private boolean arcOutlineInitialized = false;
private void ensureFontsLoaded() {
if (this.fontsLoaded)
return;  this.fontsLoaded = true;
this.fontRenderer.loadAllFonts(Fonts.getRegistry());
}
private void ensureArcInitialized() {
if (this.arcInitialized)
return;  this.arcInitialized = true;
Arc2D.init();
}
private void ensureArcOutlineInitialized() {
if (this.arcOutlineInitialized)
return;  this.arcOutlineInitialized = true;
ArcOutline2D.init();
}
public void setupOverlayState() {
GL11.glDisable(2929);
GL11.glDepthMask(false);
GL11.glEnable(3042);
GL14.glBlendFuncSeparate(770, 771, 1, 771);
}
public void restoreState() {
GL11.glDepthMask(true);
GL11.glEnable(2929);
}
public void clearDepthBuffer() {
GL11.glClear(256);
}
public void initArc() {
ensureArcInitialized();
}
public void initArcOutline() {
ensureArcOutlineInitialized();
}
public RectPipeline getRectPipeline() {
return this.rectPipeline;
}
public OutlinePipeline getOutlinePipeline() {
return this.outlinePipeline;
}
public GlowOutlinePipeline getGlowOutlinePipeline() {
return this.glowOutlinePipeline;
}
public TexturePipeline getTexturePipeline() {
return this.texturePipeline;
}
public BlurPipeline getBlurPipeline() {
return this.blurPipeline;
}
public KawaseBlurPipeline getKawaseBlurPipeline() {
return this.kawaseBlurPipeline;
}
public GlassCompositePipeline getGlassCompositePipeline() {
return this.glassCompositePipeline;
}
public GlassHandsRenderer getGlassHandsRenderer() {
return this.glassHandsRenderer;
}
public FontRenderer getFontRenderer() {
ensureFontsLoaded();
return this.fontRenderer;
}
public MaskDiffPipeline getMaskDiffPipeline() {
return this.maskDiffPipeline;
}
public MinecraftClient getClient() {
return MinecraftClient.getInstance();
}
public void close() {
runSafely(() -> this.rectPipeline.close());
runSafely(() -> this.outlinePipeline.close());
runSafely(() -> this.glowOutlinePipeline.close());
runSafely(() -> this.texturePipeline.close());
runSafely(() -> this.blurPipeline.close());
runSafely(() -> this.kawaseBlurPipeline.close());
runSafely(() -> this.glassCompositePipeline.close());
runSafely(() -> this.glassHandsRenderer.close());
runSafely(() -> this.maskDiffPipeline.close());
runSafely(() -> this.fontRenderer.close());
if (this.arcInitialized) {
try {
Arc2D.shutdown();
}
catch (Throwable ignored) {
}
this.arcInitialized = false;
}
if (this.arcOutlineInitialized) {
try {
ArcOutline2D.shutdown();
}
catch (Throwable ignored) {
}
this.arcOutlineInitialized = false;
}
this.fontsLoaded = false;
}
private static void runSafely(Runnable action) {
try {
action.run();
}
catch (Throwable ignored) {
}
}
}


