package royale.util.render.fakeplayer;
import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import royale.IMinecraft;
import royale.util.ColorUtil;
import royale.util.theme.ClientTheme;
public final class FakePlayerRenderer
implements IMinecraft {
private FakePlayerRenderer() {
throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
}
private static final float HEAD_SIZE = 0.5F;
private static final float BODY_WIDTH = 0.5F;
private static final float BODY_HEIGHT = 0.75F;
private static final float BODY_DEPTH = 0.25F;
private static final float ARM_WIDTH = 0.25F;
private static final float ARM_HEIGHT = 0.75F;
private static final float LEG_HEIGHT = 0.75F;
private static final float MODEL_CENTER_Y = 1.125F;
private static int currentAlpha = 255;
public static void render(Vec3d position, float alpha) {
if (mc.player == null || alpha <= 0.001F) {
return;
}
currentAlpha = (int)(Math.min(1.0F, Math.max(0.0F, alpha)) * 255.0F);
Vec3d camPos = mc.gameRenderer.getCamera().getCameraPos();
VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
MatrixStack stack = new MatrixStack();
GlStateManager._disableCull();
GlStateManager._enableBlend();
GlStateManager._blendFuncSeparate(770, 771, 1, 0);
stack.push();
stack.translate(position.x - camPos.x, position.y - camPos.y, position.z - camPos.z);
renderPlayerModel(stack, immediate);
stack.pop();
immediate.draw();
GlStateManager._disableBlend();
GlStateManager._enableCull();
}
private static void renderPlayerModel(MatrixStack stack, VertexConsumerProvider.Immediate immediate) {
Box leftLeg = new Box(-0.25D, 0.0D, -0.125D, 0.0D, 0.75D, 0.125D);
Box rightLeg = new Box(0.0D, 0.0D, -0.125D, 0.25D, 0.75D, 0.125D);
Box body = new Box(-0.25D, 0.75D, -0.125D, 0.25D, 1.5D, 0.125D);
Box head = new Box(-0.25D, 1.5D, -0.25D, 0.25D, 2.0D, 0.25D);
Box leftArm = new Box(-0.5D, 0.75D, -0.125D, -0.25D, 1.5D, 0.125D);
Box rightArm = new Box(0.25D, 0.75D, -0.125D, 0.5D, 1.5D, 0.125D);
Box[] bodyParts = { leftLeg, rightLeg, body, head, leftArm, rightArm };
VertexConsumer consumer = immediate.getBuffer(RenderLayers.debugFilledBox());
Matrix4f matrix = stack.peek().getPositionMatrix();
float centerX = 0.0F;
float centerY = 1.125F;
float centerZ = 0.0F;
float maxDist = 1.0F;
for (Box part : bodyParts) {
drawBoxWithVignette(matrix, consumer, part, centerX, centerY, centerZ, maxDist);
}
}
private static void drawBoxWithVignette(Matrix4f matrix, VertexConsumer consumer, Box box, float centerX, float centerY, float centerZ, float maxDist) {
float x1 = (float)box.minX;
float y1 = (float)box.minY;
float z1 = (float)box.minZ;
float x2 = (float)box.maxX;
float y2 = (float)box.maxY;
float z2 = (float)box.maxZ;
drawQuadVignette(matrix, consumer, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, centerX, centerY, centerZ, maxDist);
drawQuadVignette(matrix, consumer, x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1, centerX, centerY, centerZ, maxDist);
drawQuadVignette(matrix, consumer, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, centerX, centerY, centerZ, maxDist);
drawQuadVignette(matrix, consumer, x2, y1, z2, x2, y2, z2, x1, y2, z2, x1, y1, z2, centerX, centerY, centerZ, maxDist);
drawQuadVignette(matrix, consumer, x1, y1, z2, x1, y2, z2, x1, y2, z1, x1, y1, z1, centerX, centerY, centerZ, maxDist);
drawQuadVignette(matrix, consumer, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, centerX, centerY, centerZ, maxDist);
}
private static void drawQuadVignette(Matrix4f matrix, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float centerX, float centerY, float centerZ, float maxDist) {
consumer.vertex((Matrix4fc)matrix, x1, y1, z1)
.color(getVignetteColor(x1, y1, z1, centerX, centerY, centerZ, maxDist));
consumer.vertex((Matrix4fc)matrix, x2, y2, z2)
.color(getVignetteColor(x2, y2, z2, centerX, centerY, centerZ, maxDist));
consumer.vertex((Matrix4fc)matrix, x3, y3, z3)
.color(getVignetteColor(x3, y3, z3, centerX, centerY, centerZ, maxDist));
consumer.vertex((Matrix4fc)matrix, x4, y4, z4)
.color(getVignetteColor(x4, y4, z4, centerX, centerY, centerZ, maxDist));
}
private static int getVignetteColor(float x, float y, float z, float centerX, float centerY, float centerZ, float maxDist) {
float modelHalfWidth = 0.7F;
float modelHalfHeight = 1.0F;
float modelHalfDepth = 1.0F;
float dx = Math.abs(x - centerX) / modelHalfWidth;
float dy = Math.abs(y - centerY) / modelHalfHeight;
float dz = Math.abs(z - centerZ) / modelHalfDepth;
float t = Math.max(Math.max(dx, dy), dz);
t = Math.min(1.0F, t);
float colorIntensity = 0.6F;
t *= colorIntensity;
int clientColor = ColorUtil.setAlpha(ClientTheme.accent(), currentAlpha);
int white = ColorUtil.rgba(255, 255, 255, currentAlpha);
return ColorUtil.interpolateColor(white, clientColor, t);
}
public static void renderFromBox(Box playerBox, float alpha) {
double centerX = (playerBox.minX + playerBox.maxX) / 2.0D;
double bottomY = playerBox.minY;
double centerZ = (playerBox.minZ + playerBox.maxZ) / 2.0D;
render(new Vec3d(centerX, bottomY, centerZ), alpha);
}
}


