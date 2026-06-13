package royale.util.render;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.util.Window;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import royale.Initialization;
import royale.util.ColorUtil;
import royale.util.render.pipeline.Arc2D;
import royale.util.render.pipeline.ArcOutline2D;
public class Render2D
{
private static boolean inOverlayMode = false;
private static boolean savedDepthTest = false;
private static boolean savedDepthMask = false;
private static boolean savedBlend = false;
private static final Identifier BACKGROUND_TEXTURE = Identifier.of("royale", "textures/menu/backmenu.png");
private static final List<Runnable> OVERRIDE_TASKS = new ArrayList<>();
private static final float Z_OVERRIDE = 0.0F;
private static final float FIXED_GUI_SCALE = 2.0F;
public static int getFixedScaledWidth() {
Window window = MinecraftClient.getInstance().getWindow();
return (int)Math.ceil(window.getFramebufferWidth() / 2.0D);
}
public static int getFixedScaledHeight() {
Window window = MinecraftClient.getInstance().getWindow();
return (int)Math.ceil(window.getFramebufferHeight() / 2.0D);
}
public static float getFixedGuiScale() {
return 2.0F;
}
public static float getScaleMultiplier() {
MinecraftClient client = MinecraftClient.getInstance();
float currentScale = client.getWindow().getScaleFactor();
return 2.0F / currentScale;
}
public static void beginOverlay() {
inOverlayMode = true;
savedDepthTest = GL11.glIsEnabled(2929);
savedDepthMask = GL11.glGetBoolean(2930);
savedBlend = GL11.glIsEnabled(3042);
GL11.glDisable(2929);
GL11.glDepthMask(false);
GL11.glEnable(3042);
GL14.glBlendFuncSeparate(770, 771, 1, 771);
}
public static void endOverlay() {
if (savedDepthMask) {
GL11.glDepthMask(true);
}
if (savedDepthTest) {
GL11.glEnable(2929);
} else {
GL11.glDisable(2929);
} 
if (!savedBlend) {
GL11.glDisable(3042);
}
inOverlayMode = false;
}
public static void clearDepth() {
MinecraftClient client = MinecraftClient.getInstance();
if (client.getFramebuffer() != null) {
GL11.glClear(256);
}
}
public static void enableBlend() {
GL11.glEnable(3042);
GL14.glBlendFuncSeparate(770, 771, 1, 771);
}
public static void disableBlend() {
GL11.glDisable(3042);
}
public static void enableDepthTest() {
GL11.glEnable(2929);
}
public static void disableDepthTest() {
GL11.glDisable(2929);
}
public static void depthMask(boolean mask) {
GL11.glDepthMask(mask);
}
public static void backgroundImage(float opacity) {
backgroundImage(opacity, 1.0F);
}
public static void backgroundImage(float opacity, float zoom) {
int screenWidth = getFixedScaledWidth();
int screenHeight = getFixedScaledHeight();
float zoomedWidth = screenWidth * zoom;
float zoomedHeight = screenHeight * zoom;
float offsetX = (screenWidth - zoomedWidth) / 2.0F;
float offsetY = (screenHeight - zoomedHeight) / 2.0F;
int alpha = (int)(opacity * 255.0F);
int color = alpha << 24 | 0xFFFFFF;
texture(BACKGROUND_TEXTURE, offsetX, offsetY, zoomedWidth, zoomedHeight, color);
}
public static void backgroundImage(float x, float y, float width, float height, float opacity) {
int alpha = (int)(opacity * 255.0F);
int color = alpha << 24 | 0xFFFFFF;
texture(BACKGROUND_TEXTURE, x, y, width, height, color);
}
public static void rect(float x, float y, float width, float height, int color) {
int[] colors = ColorUtil.solid(color);
float[] radii = { 0.0F, 0.0F, 0.0F, 0.0F };
Initialization.getInstance().getManager().getRenderCore().getRectPipeline()
.drawRect(x, y, width, height, colors, radii);
}
public static void rect(float x, float y, float width, float height, int color, float radius) {
int[] colors = ColorUtil.solid(color);
float[] radii = { radius, radius, radius, radius };
Initialization.getInstance().getManager().getRenderCore().getRectPipeline()
.drawRect(x, y, width, height, colors, radii);
}
public static void rect(float x, float y, float width, float height, int color, float topLeft, float topRight, float bottomRight, float bottomLeft) {
int[] colors = ColorUtil.solid(color);
float[] radii = { topLeft, topRight, bottomRight, bottomLeft };
Initialization.getInstance().getManager().getRenderCore().getRectPipeline()
.drawRect(x, y, width, height, colors, radii);
}
public static void gradientRect(float x, float y, float width, float height, int[] colors, float radius) {
float[] radii = { radius, radius, radius, radius };
Initialization.getInstance().getManager().getRenderCore().getRectPipeline()
.drawRect(x, y, width, height, colors, radii);
}
public static void gradientRect(float x, float y, float width, float height, int[] colors, float topLeft, float topRight, float bottomRight, float bottomLeft) {
float[] radii = { topLeft, topRight, bottomRight, bottomLeft };
Initialization.getInstance().getManager().getRenderCore().getRectPipeline()
.drawRect(x, y, width, height, colors, radii);
}
public static void gradientRect9(float x, float y, float width, float height, int topLeft, int topCenter, int topRight, int leftCenter, int center, int rightCenter, int bottomLeft, int bottomCenter, int bottomRight, float radius) {
int[] colors = { topLeft, topCenter, topRight, leftCenter, center, rightCenter, bottomLeft, bottomCenter, bottomRight };
float[] radii = { radius, radius, radius, radius };
Initialization.getInstance().getManager().getRenderCore().getRectPipeline()
.drawRect(x, y, width, height, colors, radii);
}
public static void gradientRect9(float x, float y, float width, float height, int[] colors9, float radius) {
float[] radii = { radius, radius, radius, radius };
Initialization.getInstance().getManager().getRenderCore().getRectPipeline()
.drawRect(x, y, width, height, colors9, radii);
}
public static void gradientRect9(float x, float y, float width, float height, int[] colors9, float topLeft, float topRight, float bottomRight, float bottomLeft) {
float[] radii = { topLeft, topRight, bottomRight, bottomLeft };
Initialization.getInstance().getManager().getRenderCore().getRectPipeline()
.drawRect(x, y, width, height, colors9, radii);
}
public static void gradientRect9(float x, float y, float width, float height, int topLeft, int topCenter, int topRight, int leftCenter, int center, int rightCenter, int bottomLeft, int bottomCenter, int bottomRight, float radius, float innerBlur) {
int[] colors = { topLeft, topCenter, topRight, leftCenter, center, rightCenter, bottomLeft, bottomCenter, bottomRight };
float[] radii = { radius, radius, radius, radius };
Initialization.getInstance().getManager().getRenderCore().getRectPipeline()
.drawRect(x, y, width, height, colors, radii, innerBlur);
}
public static void gradientRect9(float x, float y, float width, float height, int[] colors9, float radius, float innerBlur) {
float[] radii = { radius, radius, radius, radius };
Initialization.getInstance().getManager().getRenderCore().getRectPipeline()
.drawRect(x, y, width, height, colors9, radii, innerBlur);
}
public static void outline(float x, float y, float width, float height, float thickness, int color) {
int[] colors = ColorUtil.solid8(color);
float[] thicknesses = { thickness, thickness, thickness, thickness, thickness, thickness, thickness, thickness };
float[] radii = { 0.0F, 0.0F, 0.0F, 0.0F };
Initialization.getInstance().getManager().getRenderCore().getOutlinePipeline()
.drawOutline(x, y, width, height, colors, thicknesses, radii, 1.0F);
}
public static void outline(float x, float y, float width, float height, float thickness, int color, float radius) {
int[] colors = ColorUtil.solid8(color);
float[] thicknesses = { thickness, thickness, thickness, thickness, thickness, thickness, thickness, thickness };
float[] radii = { radius, radius, radius, radius };
Initialization.getInstance().getManager().getRenderCore().getOutlinePipeline()
.drawOutline(x, y, width, height, colors, thicknesses, radii, 1.0F);
}
public static void outline(float x, float y, float width, float height, float thickness, int color, float topLeft, float topRight, float bottomRight, float bottomLeft) {
int[] colors = ColorUtil.solid8(color);
float[] thicknesses = { thickness, thickness, thickness, thickness, thickness, thickness, thickness, thickness };
float[] radii = { topLeft, topRight, bottomRight, bottomLeft };
Initialization.getInstance().getManager().getRenderCore().getOutlinePipeline()
.drawOutline(x, y, width, height, colors, thicknesses, radii, 1.0F);
}
public static void gradientOutline(float x, float y, float width, float height, float thickness, int[] colors, float radius) {
float[] thicknesses = { thickness, thickness, thickness, thickness, thickness, thickness, thickness, thickness };
float[] radii = { radius, radius, radius, radius };
Initialization.getInstance().getManager().getRenderCore().getOutlinePipeline()
.drawOutline(x, y, width, height, colors, thicknesses, radii, 1.0F);
}
public static void blur(float x, float y, float width, float height, float blurRadius, int tintColor) {
float[] radii = { 0.0F, 0.0F, 0.0F, 0.0F };
Initialization.getInstance().getManager().getRenderCore().getBlurPipeline()
.drawBlur(x, y, width, height, blurRadius, radii, tintColor);
}
public static void blur(float x, float y, float width, float height, float blurRadius, float cornerRadius, int tintColor) {
float[] radii = { cornerRadius, cornerRadius, cornerRadius, cornerRadius };
Initialization.getInstance().getManager().getRenderCore().getBlurPipeline()
.drawBlur(x, y, width, height, blurRadius, radii, tintColor);
}
public static void blur(float x, float y, float width, float height, float blurRadius, float topLeft, float topRight, float bottomRight, float bottomLeft, int tintColor) {
float[] radii = { topLeft, topRight, bottomRight, bottomLeft };
Initialization.getInstance().getManager().getRenderCore().getBlurPipeline()
.drawBlur(x, y, width, height, blurRadius, radii, tintColor);
}
public static void texture(Identifier id, float x, float y, float width, float height, int color) {
texture(id, x, y, width, height, 0.0F, 0.0F, 1.0F, 1.0F, color, 1.0F, 0.0F);
}
public static void texture(Identifier id, float x, float y, float width, float height, float smoothness, int color) {
texture(id, x, y, width, height, 0.0F, 0.0F, 1.0F, 1.0F, color, smoothness, 0.0F);
}
public static void texture(Identifier id, float x, float y, float width, float height, float smoothness, float radius, int color) {
texture(id, x, y, width, height, 0.0F, 0.0F, 1.0F, 1.0F, color, smoothness, radius);
}
public static void texture(Identifier id, float x, float y, float width, float height, float u0, float v0, float u1, float v1, int color) {
texture(id, x, y, width, height, u0, v0, u1, v1, color, 1.0F, 0.0F);
}
public static void texture(Identifier id, float x, float y, float width, float height, float u0, float v0, float u1, float v1, int color, float radius) {
texture(id, x, y, width, height, u0, v0, u1, v1, color, 1.0F, radius);
}
public static void texture(Identifier id, float x, float y, float width, float height, float u0, float v0, float u1, float v1, int color, float smoothness, float radius) {
int[] colors = { color, color, color, color };
float[] radii = { radius, radius, radius, radius };
Initialization.getInstance().getManager().getRenderCore().getTexturePipeline()
.drawTexture(id, x, y, width, height, u0, v0, u1, v1, colors, radii, smoothness);
}
public static void drawTexture(DrawContext context, Identifier id, float x, float y, float width, float height, float u, float v, float regionWidth, float regionHeight, float textureWidth, float textureHeight, int color) {
float u0 = u / textureWidth;
float v0 = v / textureHeight;
float u1 = (u + regionWidth) / textureWidth;
float v1 = (v + regionHeight) / textureHeight;
texture(id, x, y, width, height, u0, v0, u1, v1, color, 1.0F, 0.0F);
}
public static void drawTexture(DrawContext context, Identifier id, float x, float y, float width, float height, float u, float v, float regionWidth, float regionHeight, float textureWidth, float textureHeight, int color, float radius) {
float u0 = u / textureWidth;
float v0 = v / textureHeight;
float u1 = (u + regionWidth) / textureWidth;
float v1 = (v + regionHeight) / textureHeight;
texture(id, x, y, width, height, u0, v0, u1, v1, color, 1.0F, radius);
}
public static void drawSprite(Sprite sprite, float x, float y, float width, float height, int color) {
drawSprite(sprite, x, y, width, height, color, true);
}
public static void drawSprite(Sprite sprite, float x, float y, float width, float height, int color, boolean pixelPerfect) {
if (sprite == null || width == 0.0F || height == 0.0F)
return; 
float smoothness = pixelPerfect ? 1.0F : 0.0F;
texture(sprite.getAtlasId(), x, y, width, height, sprite
.getMinU(), sprite.getMinV(), sprite
.getMaxU(), sprite.getMaxV(), color, smoothness, 0.0F);
}
public static void drawSpriteSmooth(Sprite sprite, float x, float y, float width, float height, int color) {
drawSprite(sprite, x, y, width, height, color, false);
}
public static void drawFramebufferTexture(int textureId, float x, float y, float width, float height, float r, float g, float b, float a) {
int color = (int)(a * 255.0F) << 24 | (int)(r * 255.0F) << 16 | (int)(g * 255.0F) << 8 | (int)(b * 255.0F);
int[] colors = { color, color, color, color };
float[] radii = { 0.0F, 0.0F, 0.0F, 0.0F };
Initialization.getInstance().getManager().getRenderCore().getTexturePipeline()
.drawFramebufferTexture(textureId, x, y, width, height, colors, radii, a);
}
public static void glowOutline(float x, float y, float width, float height, float thickness, int color, float radius, float progress, float baseAlpha) {
float[] radii = { radius, radius, radius, radius };
Initialization.getInstance().getManager().getRenderCore().getGlowOutlinePipeline()
.drawGlowOutline(x, y, width, height, color, thickness, radii, progress, baseAlpha);
}
public static void glowOutline(float x, float y, float width, float height, float thickness, int color, float topLeft, float topRight, float bottomRight, float bottomLeft, float progress, float baseAlpha) {
float[] radii = { topLeft, topRight, bottomRight, bottomLeft };
Initialization.getInstance().getManager().getRenderCore().getGlowOutlinePipeline()
.drawGlowOutline(x, y, width, height, color, thickness, radii, progress, baseAlpha);
}
public static Matrix4f createProjection() {
int width = getFixedScaledWidth();
int height = getFixedScaledHeight();
return (new Matrix4f()).ortho(0.0F, width, height, 0.0F, -1000.0F, 1000.0F);
}
public static void arc(DrawContext context, float x, float y, float size, float thickness, float degree, float rotation, int color, boolean overrideContext) {
arc(createProjection(), x, y, size, thickness, degree, rotation, color, overrideContext);
}
public static void arc(DrawContext context, float x, float y, float size, float thickness, float degree, float rotation, boolean overrideContext, int... colors) {
arc(createProjection(), x, y, size, thickness, degree, rotation, overrideContext, colors);
}
public static void arc(Matrix4f matrix, float x, float y, float size, float thickness, float degree, float rotation, int color, boolean overrideContext) {
if (overrideContext) {
OVERRIDE_TASKS.add(() -> Arc2D.draw(matrix, x, y, size, thickness, degree, rotation, 0.0F, new int[] { color }));
return;
} 
Arc2D.draw(matrix, x, y, size, thickness, degree, rotation, 0.0F, new int[] { color });
}
public static void arc(Matrix4f matrix, float x, float y, float size, float thickness, float degree, float rotation, boolean overrideContext, int... colors) {
if (overrideContext) {
OVERRIDE_TASKS.add(() -> Arc2D.draw(matrix, x, y, size, thickness, degree, rotation, 0.0F, colors));
return;
} 
Arc2D.draw(matrix, x, y, size, thickness, degree, rotation, 0.0F, colors);
}
public static void arc(float x, float y, float size, float thickness, float degree, float rotation, int color) {
Arc2D.draw(createProjection(), x, y, size, thickness, degree, rotation, 0.0F, new int[] { color });
}
public static void arc(float x, float y, float size, float thickness, float degree, float rotation, int... colors) {
Arc2D.draw(createProjection(), x, y, size, thickness, degree, rotation, 0.0F, colors);
}
public static void arcOutline(float x, float y, float size, float arcThickness, float degree, float rotation, float outlineThickness, int fillColor, int outlineColor) {
ArcOutline2D.draw(createProjection(), x, y, size, arcThickness, degree, rotation, outlineThickness, fillColor, outlineColor, 0.0F);
}
public static void arcOutline(DrawContext context, float x, float y, float size, float arcThickness, float degree, float rotation, float outlineThickness, int fillColor, int outlineColor, boolean overrideContext) {
Matrix4f matrix = createProjection();
if (overrideContext) {
OVERRIDE_TASKS.add(() -> ArcOutline2D.draw(matrix, x, y, size, arcThickness, degree, rotation, outlineThickness, fillColor, outlineColor, 0.0F));
return;
} 
ArcOutline2D.draw(matrix, x, y, size, arcThickness, degree, rotation, outlineThickness, fillColor, outlineColor, 0.0F);
}
public static void arcOutline(Matrix4f matrix, float x, float y, float size, float arcThickness, float degree, float rotation, float outlineThickness, int fillColor, int outlineColor) {
ArcOutline2D.draw(matrix, x, y, size, arcThickness, degree, rotation, outlineThickness, fillColor, outlineColor, 0.0F);
}
public static void flushOverrideTasks() {
for (Runnable task : OVERRIDE_TASKS) {
task.run();
}
OVERRIDE_TASKS.clear();
}
public static boolean isInOverlayMode() {
return inOverlayMode;
}
public static void cleanup() {
OVERRIDE_TASKS.clear();
Arc2D.shutdown();
ArcOutline2D.shutdown();
}
}


