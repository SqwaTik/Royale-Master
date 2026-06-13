package royale.modules.impl.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import royale.events.api.EventHandler;
import royale.events.impl.DrawEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.implement.ColorSetting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.util.Instance;
import royale.util.animations.Animation;
import royale.util.animations.Direction;
import royale.util.animations.EaseInOutQuad;
import royale.util.animations.Easings;
import royale.util.animations.SmoothAnimation;

public class Arrows
extends ModuleStructure {
    private static final Identifier ARROW_TEXTURE = Identifier.of((String)"royale", (String)"textures/world/arrow.png");
    public SliderSettings arrowsDistance = new SliderSettings("\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f", "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f \u043e\u0442 \u043f\u0440\u0438\u0446\u0435\u043b\u0430").range(1.0f, 20.0f).setValue(25.0f);
    public ColorSetting arrowColor = new ColorSetting("\u0426\u0432\u0435\u0442", "\u0426\u0432\u0435\u0442 \u0441\u0442\u0440\u0435\u043b\u043e\u043a").value(-7773880);
    private final SmoothAnimation animationStep = new SmoothAnimation();
    private final SmoothAnimation animatedYaw = new SmoothAnimation();
    private final SmoothAnimation animatedPitch = new SmoothAnimation();
    private final SmoothAnimation animatedCameraYaw = new SmoothAnimation();
    private final List<Arrow> playerList = new ArrayList<Arrow>();

    public static Arrows getInstance() {
        return Instance.get(Arrows.class);
    }

    public Arrows() {
        super("Arrows", "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u0441\u0442\u0440\u0435\u043b\u043a\u0438 \u0432 \u0441\u0442\u043e\u0440\u043e\u043d\u0443 \u0438\u0433\u0440\u043e\u043a\u043e\u0432", ModuleCategory.RENDER);
        this.settings(this.arrowsDistance, this.arrowColor);
    }

    @Override
    public void deactivate() {
        this.playerList.clear();
    }

    @EventHandler
    public void onDraw(DrawEvent event) {
        if (Arrows.mc.player == null || Arrows.mc.world == null) {
            return;
        }
        if (Arrows.mc.options.getPerspective() != Perspective.FIRST_PERSON) {
            return;
        }
        DrawContext context = event.getDrawContext();
        float partialTicks = event.getPartialTicks();
        this.animationStep.update();
        this.animatedYaw.update();
        this.animatedPitch.update();
        this.animatedCameraYaw.update();
        float size = 45.0f + this.arrowsDistance.getValue();
        if (Arrows.mc.currentScreen instanceof InventoryScreen) {
            size += 80.0f;
        }
        if (Arrows.mc.player.isSneaking()) {
            size -= 20.0f;
        }
        if (this.isMoving()) {
            size += 10.0f;
        }
        float strafeInput = Arrows.mc.player.input.getMovementInput().x;
        float forwardInput = Arrows.mc.player.input.getMovementInput().y;
        this.animatedYaw.run((double)(strafeInput * 5.0f), 0.75, Easings.EXPO_OUT);
        this.animatedPitch.run((double)(forwardInput * 5.0f), 0.75, Easings.EXPO_OUT);
        this.animatedCameraYaw.run(Arrows.mc.gameRenderer.getCamera().getYaw(), 0.75, Easings.EXPO_OUT, true);
        this.animationStep.run(size, 1.0, Easings.EXPO_OUT, false);
        ArrayList<Arrow> players = new ArrayList<Arrow>();
        for (Object player : Arrows.mc.world.getPlayers()) {
            Optional<Arrow> arrowConsumer = this.playerList.stream().filter(arg_0 -> Arrows.lambda$onDraw$0((AbstractClientPlayerEntity)player, arg_0)).findFirst();
            if (!this.isValidPlayer((PlayerEntity)player)) continue;
            Arrow arrow = new Arrow((PlayerEntity)player, arrowConsumer.map(a -> a.fadeAnimation).orElse(this.createFadeAnimation()));
            players.add(arrow);
        }
        ArrayList<Arrow> arrows = new ArrayList<Arrow>(this.playerList);
        arrows.removeIf(p -> players.stream().anyMatch(p2 -> p.player == p2.player));
        for (Arrow arrow : arrows) {
            arrow.fadeAnimation.setDirection(Direction.BACKWARDS);
            if (arrow.isDead()) continue;
            players.add(arrow);
        }
        this.playerList.clear();
        this.playerList.addAll(players);
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();
        float centerX = (float)screenWidth / 2.0f;
        float centerY = (float)screenHeight / 2.0f;
        for (Arrow arrow : this.playerList) {
            PlayerEntity player = arrow.player;
            arrow.updateAlpha();
            float animValue = arrow.getAlpha();
            if (animValue <= 0.001f || !this.isValidPlayer(player) && arrow.fadeAnimation.isDirection(Direction.FORWARDS)) continue;
            double playerX = player.lastRenderX + (player.getX() - player.lastRenderX) * (double)partialTicks - Arrows.mc.gameRenderer.getCamera().getCameraPos().x;
            double playerZ = player.lastRenderZ + (player.getZ() - player.lastRenderZ) * (double)partialTicks - Arrows.mc.gameRenderer.getCamera().getCameraPos().z;
            double cameraYaw = this.animatedCameraYaw.getValue();
            double cos = MathHelper.cos((double)((float)(cameraYaw * (Math.PI / 180))));
            double sin = MathHelper.sin((double)((float)(cameraYaw * (Math.PI / 180))));
            double rotY = -(playerZ * cos - playerX * sin);
            double rotX = -(playerX * cos + playerZ * sin);
            float angle = (float)(Math.atan2(rotY, rotX) * 180.0 / Math.PI);
            double x2 = this.animationStep.getValue() * (double)animValue * (double)MathHelper.cos((double)((float)Math.toRadians(angle))) + (double)centerX;
            double y2 = this.animationStep.getValue() * (double)animValue * (double)MathHelper.sin((double)((float)Math.toRadians(angle))) + (double)centerY;
            int color = this.applyAlpha(this.arrowColor.getColor(), animValue);
            this.drawArrow(context, (float)(x2 += this.animatedYaw.getValue()), (float)(y2 += this.animatedPitch.getValue()), angle, color, 1.0f);
        }
    }

    private Animation createFadeAnimation() {
        Animation anim = new EaseInOutQuad().setMs(200).setValue(1.0);
        anim.setDirection(Direction.FORWARDS);
        return anim;
    }

    private void drawArrow(DrawContext context, float x, float y, float angle, int color, float scale) {
        float size = 17.0f * scale;
        float halfSize = size / 2.0f;
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        context.getMatrices().rotate((float)Math.toRadians(angle));
        context.getMatrices().rotate((float)Math.toRadians(90.0));
        int intSize = (int)size;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, ARROW_TEXTURE, (int)(1.0f - halfSize), -5, 0.0f, 0.0f, intSize, intSize, intSize, intSize, color);
        context.getMatrices().popMatrix();
    }

    private int applyAlpha(int color, float alpha) {
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int a = (int)(alpha * 255.0f);
        return a << 24 | r << 16 | g << 8 | b;
    }

    private boolean isValidPlayer(PlayerEntity player) {
        return player != Arrows.mc.player && !player.isRemoved() && this.isActuallyVisible(player);
    }

    private boolean isMoving() {
        return Arrows.mc.player.input.getMovementInput().y != 0.0f || Arrows.mc.player.input.getMovementInput().x != 0.0f;
    }

    private boolean isActuallyVisible(PlayerEntity player) {
        return Arrows.mc.player != null && player != null && Arrows.mc.player.canSee((Entity)player);
    }

    private static /* synthetic */ boolean lambda$onDraw$0(AbstractClientPlayerEntity player, Arrow a) {
        return a.player == player;
    }

    private static class Arrow {
        final PlayerEntity player;
        final Animation fadeAnimation;
        float cachedAlpha = 0.0f;
        long lastAlphaUpdate = 0L;

        Arrow(PlayerEntity player, Animation fadeAnimation) {
            this.player = player;
            this.fadeAnimation = fadeAnimation;
        }

        void updateAlpha() {
            long now = System.currentTimeMillis();
            if (now - this.lastAlphaUpdate > 16L) {
                this.cachedAlpha = this.fadeAnimation.getOutput().floatValue();
                this.lastAlphaUpdate = now;
            }
        }

        float getAlpha() {
            return this.cachedAlpha;
        }

        boolean isDead() {
            return this.fadeAnimation.isDirection(Direction.BACKWARDS) && this.fadeAnimation.isDone() && this.cachedAlpha <= 0.0f;
        }
    }
}

