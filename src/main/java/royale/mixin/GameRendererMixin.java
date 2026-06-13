package royale.mixin;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.RenderTickCounter;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import royale.Initialization;
import royale.client.draggables.Drag;
import royale.events.api.EventManager;
import royale.events.api.events.Event;
import royale.events.impl.FovEvent;
import royale.events.impl.WorldRenderEvent;
import royale.modules.impl.render.Hud;
import royale.modules.impl.render.NoRender;
import royale.screens.clickgui.ClickGui;
import royale.util.math.FrameRateCounter;
import royale.util.render.Render3D;
@Mixin({GameRenderer.class})
public abstract class GameRendererMixin
{
@Shadow
@Final
private MinecraftClient client;
@Shadow
@Final
private Camera camera;
@Shadow
@Final
GuiRenderState guiState;
@Shadow
@Final
private GuiRenderer guiRenderer;
@Shadow
@Final
private FogRenderer fogRenderer;
@Unique
private final MatrixStack matrices = new MatrixStack();
@Shadow
protected abstract void bobView(MatrixStack paramclass_4587, float paramFloat);
@Shadow
protected abstract void tiltViewWhenHurt(MatrixStack paramclass_4587, float paramFloat);
@Shadow
public abstract float getFov(Camera paramclass_4184, float paramFloat, boolean paramBoolean);
@Inject(method = {"close"}, at = {@At("RETURN")})
private void onClose(CallbackInfo ci) {
if (Initialization.getInstance() != null && Initialization.getInstance().getManager() != null && Initialization.getInstance().getManager().getRenderCore() != null) {
try {
Initialization.getInstance().getManager().getRenderCore().close();
}
catch (Throwable ignored) {
}
}
}
@Inject(method = {"getFov"}, at = {@At("RETURN")}, cancellable = true)
private void hookGetFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
FovEvent event = new FovEvent();
EventManager.callEvent((Event)event);
if (event.isCancelled()) {
cir.setReturnValue(event.getFov());
}
}
@Inject(method = {"renderWorld"}, at = {@At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = {"ldc=hand"})})
public void hookWorldRender(RenderTickCounter tickCounter, CallbackInfo ci, @Local(ordinal = 0) Matrix4f projection, @Local(ordinal = 1) Matrix4f view, @Local(ordinal = 0) float tickDelta, @Local MatrixStack matrixStack) {
if (this.client.world == null || this.client.player == null)
return; 
MatrixStack worldSpaceStack = new MatrixStack();
worldSpaceStack.multiply((Quaternionfc)RotationAxis.POSITIVE_X.rotationDegrees(this.camera.getPitch()));
worldSpaceStack.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees(this.camera.getYaw() + 180.0F));
Render3D.lastProjMat.set((Matrix4fc)(Object)this.client.gameRenderer.getBasicProjectionMatrix(getFov(this.camera, tickDelta, true)));
Render3D.lastModMat.set((Matrix4fc)RenderSystem.getModelViewMatrix());
Render3D.lastWorldSpaceMatrix.set((Matrix4fc)worldSpaceStack.peek().getPositionMatrix());
Render3D.setLastWorldSpaceEntry(matrixStack.peek());
Render3D.setLastTickDelta(tickDelta);
Render3D.setLastCameraPos(this.camera.getCameraPos());
Render3D.setLastCameraRotation(new Quaternionf((Quaternionfc)(Object)this.camera.getRotation()));
Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
modelViewStack.pushMatrix().mul((Matrix4fc)view);
this.matrices.push();
tiltViewWhenHurt(this.matrices, this.camera.getLastTickProgress());
if (((Boolean)(Object)this.client.options.getBobView().getValue()).booleanValue()) {
bobView(this.matrices, this.camera.getLastTickProgress());
}
modelViewStack.mul((Matrix4fc)(Object)this.matrices.peek().getPositionMatrix().invert(new Matrix4f()));
this.matrices.pop();
WorldRenderEvent event = new WorldRenderEvent(matrixStack, tickDelta);
EventManager.callEvent((Event)event);
Render3D.onWorldRender(event);
modelViewStack.popMatrix();
}
@Inject(method = {"tiltViewWhenHurt"}, at = {@At("HEAD")}, cancellable = true)
private void onTiltViewWhenHurt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
NoRender noRender = NoRender.getInstance();
if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected("Damage")) {
ci.cancel();
}
}
@ModifyExpressionValue(method = {"renderWorld"}, at = {@At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F", ordinal = 0)})
private float onNauseaDistortion(float original) {
NoRender noRender = NoRender.getInstance();
if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected("Nausea")) {
return 0.0F;
}
return original;
}
@Inject(method = {"render"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", shift = At.Shift.AFTER)})
private void afterGuiRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
FrameRateCounter.INSTANCE.recordFrame();
if (this.client.world == null || this.client.player == null)
return;  if (isLoadingScreen(this.client.currentScreen))
return;  if (this.client.getOverlay() != null)
return;  if (!shouldRenderOnTop(this.client.currentScreen))
return; 
this.guiState.clear();
int mouseX = (int)this.client.mouse.getScaledX(this.client.getWindow());
int mouseY = (int)this.client.mouse.getScaledY(this.client.getWindow());
float tickDelta = tickCounter.getTickProgress(false);
DrawContext context = new DrawContext(this.client, this.guiState, mouseX, mouseY);
Hud hud = Hud.getInstance();
boolean isChatScreen = this.client.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen;
if (hud != null && hud.isState() && isChatScreen) {
Drag.onDraw(context, mouseX, mouseY, tickDelta, true);
} 
Screen Screen = this.client.currentScreen; if (Screen instanceof ClickGui) { ClickGui clickGui = (ClickGui)Screen;
clickGui.renderOverlay(context, tickCounter); }
this.guiRenderer.render(this.fogRenderer.getFogBuffer(FogRenderer.FogType.NONE));
}
@Unique
private boolean shouldRenderOnTop(Screen screen) {
if (screen == null) return false; 
if (screen instanceof ClickGui) return true; 
if (screen instanceof net.minecraft.client.gui.screen.ChatScreen) return true; 
return false;
}
@Unique
private boolean isLoadingScreen(Screen screen) {
if (screen == null) return false; 
String className = screen.getClass().getSimpleName().toLowerCase();
String fullName = screen.getClass().getName().toLowerCase();
if (className.contains("loading")) return true; 
if (className.contains("progress")) return true; 
if (className.contains("connecting")) return true; 
if (className.contains("downloading")) return true; 
if (className.contains("terrain")) return true; 
if (className.contains("generating")) return true; 
if (className.contains("saving")) return true; 
if (className.contains("reload")) return true; 
if (className.contains("resource")) return true; 
if (className.contains("pack")) return true; 
if (fullName.contains("mojang")) return true; 
return false;
}
}




