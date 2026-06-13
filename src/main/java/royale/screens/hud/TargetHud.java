package royale.screens.hud;

import java.awt.Color;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import royale.client.draggables.AbstractHudElement;
import royale.util.ColorUtil;
import royale.util.animations.Direction;
import royale.util.network.Network;
import royale.util.combat.CombatTargetPriority;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.render.font.SafeTextRenderer;

public class TargetHud
extends AbstractHudElement {
    private static final double ACTIVATION_RANGE = 3.0;
    private static final long HOLD_DURATION_MS = 3000L;
    private LivingEntity lastTarget;
    private LivingEntity heldTarget;
    private long holdUntilMs;
    private float healthAnimation = 0.0f;
    private float trailAnimation = 0.0f;
    private float absorptionAnimation = 0.0f;
    private float displayedHealth = 0.0f;
    private long lastUpdateTime = System.currentTimeMillis();
    private long startTime = System.currentTimeMillis();

    public TargetHud() {
        super("TargetHud", 10, 80, 112, 40, true);
    }

    @Override
    public boolean visible() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity resolvedTarget = this.resolveTarget();
        if (resolvedTarget != null) {
            if (this.lastTarget != resolvedTarget) {
                this.displayedHealth = this.getHealth(resolvedTarget);
            }
            this.lastTarget = resolvedTarget;
            this.startAnimation();
            return;
        }
        if (this.isChat(this.mc.currentScreen) && this.mc.player != null) {
            this.lastTarget = this.mc.player;
            this.startAnimation();
            return;
        }
        this.stopAnimation();
        if (this.scaleAnimation.isFinished(Direction.BACKWARDS)) {
            this.lastTarget = null;
        }
    }

    private LivingEntity resolveTarget() {
        if (this.mc.player == null || this.mc.world == null) {
            this.heldTarget = null;
            this.holdUntilMs = 0L;
            return null;
        }
        long now = System.currentTimeMillis();
        LivingEntity preferred = CombatTargetPriority.resolvePreferredTarget(ACTIVATION_RANGE * ACTIVATION_RANGE);
        if (preferred != null && this.hasDirectLineOfSight(preferred)) {
            this.heldTarget = preferred;
            this.holdUntilMs = now + HOLD_DURATION_MS;
            return preferred;
        }
        LivingEntity nearest = this.findNearestTargetInRange();
        if (nearest != null) {
            this.heldTarget = nearest;
            this.holdUntilMs = now + 3000L;
            return nearest;
        }
        if (this.heldTarget != null && now <= this.holdUntilMs && this.isUsableTarget(this.heldTarget) && this.hasDirectLineOfSight(this.heldTarget)) {
            return this.heldTarget;
        }
        this.heldTarget = null;
        this.holdUntilMs = 0L;
        return null;
    }

    private LivingEntity findNearestTargetInRange() {
        double maxDistSq;
        if (this.mc.player == null || this.mc.world == null) {
            return null;
        }
        double bestDistSq = maxDistSq = 9.0;
        PlayerEntity best = null;
        for (PlayerEntity player : this.mc.world.getPlayers()) {
            double distSq;
            if (player == this.mc.player || !this.isUsableTarget((LivingEntity)player) || player.isSpectator() || !this.hasDirectLineOfSight((LivingEntity)player) || !((distSq = this.mc.player.squaredDistanceTo((Entity)player)) <= bestDistSq)) continue;
            bestDistSq = distSq;
            best = player;
        }
        return best;
    }

    private boolean isUsableTarget(LivingEntity entity) {
        return entity != null && entity.isAlive() && !entity.isRemoved();
    }

    private boolean hasDirectLineOfSight(LivingEntity entity) {
        return this.mc.player != null && entity != null && this.mc.player.canSee((Entity)entity);
    }

    private float lerp(float current, float target, float deltaTime, float speed) {
        float factor = (float)(1.0 - Math.pow(0.001, deltaTime * speed));
        return current + (target - current) * factor;
    }

    private float snapToStep(float value, float step) {
        return (float)Math.round(value / step) * step;
    }

    private float getHealth(LivingEntity entity) {
        if (entity.isInvisible() && !Network.isSpookyTime() && !Network.isCopyTime()) {
            return entity.getMaxHealth();
        }
        return entity.getHealth();
    }

    private String getHealthString(float health) {
        if (this.lastTarget != null && this.lastTarget.isInvisible() && !Network.isSpookyTime() && !Network.isCopyTime()) {
            return "??";
        }
        if (health >= 100.0f) {
            return String.valueOf((int)health);
        }
        if (health >= 10.0f) {
            return String.format("%.1f", Float.valueOf(health));
        }
        return String.format("%.2f", Float.valueOf(health));
    }

    @Override
    public void drawDraggable(DrawContext context, int alpha) {
        if (alpha <= 0) {
            return;
        }
        if (this.lastTarget == null) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        float deltaTime = (float)(currentTime - this.lastUpdateTime) / 1000.0f;
        this.lastUpdateTime = currentTime;
        deltaTime = Math.min(deltaTime, 0.1f);
        float x = this.getX();
        float y = this.getY();
        this.setWidth(112);
        this.setHeight(40);
        float scaleAlpha = this.scaleAnimation.getOutput().floatValue();
        this.drawBackground(x, y, scaleAlpha);
        this.drawFace(x, y, scaleAlpha);
        this.drawContent(context, x, y, scaleAlpha, deltaTime);
    }

    private void drawBackground(float x, float y, float alpha) {
        int alphaInt = (int)(255.0f * alpha);
        Render2D.gradientRect(x + 2.0f, y + 2.0f, this.getWidth() - 4, this.getHeight() - 4, new int[]{new Color(52, 52, 52, alphaInt).getRGB(), new Color(22, 22, 22, alphaInt).getRGB(), new Color(52, 52, 52, alphaInt).getRGB(), new Color(22, 22, 22, alphaInt).getRGB()}, 6.0f);
        Render2D.outline(x + 2.0f, y + 2.0f, this.getWidth() - 4, this.getHeight() - 4, 0.35f, new Color(90, 90, 90, alphaInt).getRGB(), 5.0f);
        int blurTint = ColorUtil.rgba(0, 0, 0, 0);
        Render2D.blur(x + 2.0f, y + 2.0f, 1.0f, 1.0f, 0.0f, 7.0f, blurTint);
    }

    private void drawFace(float x, float y, float alpha) {
        EntityRenderer baseRenderer = this.mc.getEntityRenderDispatcher().getRenderer((Entity)this.lastTarget);
        if (!(baseRenderer instanceof LivingEntityRenderer)) {
            return;
        }
        LivingEntityRenderer renderer = (LivingEntityRenderer)baseRenderer;
        LivingEntityRenderState state = (LivingEntityRenderState)renderer.getAndUpdateRenderState((Entity)this.lastTarget, this.lastTickDelta);
        Identifier textureLocation = renderer.getTexture(state);
        float faceSize = 24.0f;
        float faceX = x + 9.0f;
        float faceY = y + 8.0f;
        float hurtPercent = this.lastTarget.hurtTime > 0 ? (float)this.lastTarget.hurtTime / 10.0f : 0.0f;
        int r = 255;
        int g = (int)(255.0f * (1.0f - hurtPercent));
        int b = (int)(255.0f * (1.0f - hurtPercent));
        int color = new Color(r, g, b, (int)(255.0f * alpha)).getRGB();
        float u0 = 0.125f;
        float v0 = 0.125f;
        float u1 = 0.25f;
        float v1 = 0.25f;
        Render2D.texture(textureLocation, faceX, faceY, faceSize, faceSize, u0, v0, u1, v1, color, 0.0f, 4.0f);
        float hatScale = 1.1f;
        float hatSize = faceSize * hatScale;
        float hatOffset = (hatSize - faceSize) / 2.0f;
        float hatU0 = 0.625f;
        float hatV0 = 0.125f;
        float hatU1 = 0.75f;
        float hatV1 = 0.25f;
        Render2D.texture(textureLocation, faceX - hatOffset, faceY - hatOffset, hatSize, hatSize, hatU0, hatV0, hatU1, hatV1, color, 0.0f, 4.0f);
    }

    private void drawContent(DrawContext context, float x, float y, float alpha, float deltaTime) {
        float absorptionPercent;
        float faceSize = 24.0f;
        float faceX = x + 9.0f;
        float contentX = faceX + faceSize + 6.0f;
        float nameY = y + 13.0f;
        float hp = this.getHealth(this.lastTarget);
        float maxHp = this.lastTarget.getMaxHealth();
        float absorp = this.lastTarget.getAbsorptionAmount();
        boolean isInvisible = this.lastTarget.isInvisible() && !Network.isSpookyTime() && !Network.isCopyTime();
        float targetDisplayHealth = isInvisible ? maxHp : hp + absorp;
        this.displayedHealth = this.lerp(this.displayedHealth, targetDisplayHealth, deltaTime, 5.0f);
        float snappedHealth = this.snapToStep(this.displayedHealth, 0.25f);
        String hpStr = this.getHealthString(snappedHealth);
        String name = this.lastTarget.getName().getString();
        float hpWidth = Fonts.BOLD.getWidth(hpStr, 5.5f);
        int nameColor = new Color(255, 255, 255, (int)(255.0f * alpha)).getRGB();
        SafeTextRenderer.draw(context, Fonts.TEST, name, contentX, nameY, 5.5f, nameColor);
        int hpColor = new Color(215, 215, 215, (int)(255.0f * alpha)).getRGB();
        Fonts.BOLD.draw(hpStr, x + (float)this.getWidth() - 10.0f - hpWidth, nameY, 5.5f, hpColor);
        float targetHealth = isInvisible ? 1.0f : hp / maxHp;
        this.healthAnimation = this.lerp(this.healthAnimation, targetHealth, deltaTime, 3.0f);
        if (targetHealth > this.trailAnimation) {
            this.trailAnimation = targetHealth;
        }
        this.trailAnimation = this.lerp(this.trailAnimation, targetHealth, deltaTime, 3.5f);
        float targetAbsorption = isInvisible ? 0.0f : absorp / maxHp;
        this.absorptionAnimation = this.lerp(this.absorptionAnimation, targetAbsorption, deltaTime, 3.0f);
        float barX = contentX;
        float barY = nameY + 12.0f;
        float barWidth = 64.0f;
        float barHeight = 4.0f;
        float barRadius = 2.0f;
        Render2D.rect(barX, barY, barWidth, barHeight, new Color(30, 30, 30, (int)(200.0f * alpha)).getRGB(), barRadius);
        float healthPercent = Math.max(0.0f, Math.min(1.0f, this.healthAnimation));
        float trailPercent = Math.max(0.0f, Math.min(1.0f, this.trailAnimation));
        if (trailPercent > healthPercent) {
            int trailColor = new Color(55, 55, 55, (int)(160.0f * alpha)).getRGB();
            Render2D.rect(barX, barY, barWidth * trailPercent, barHeight, trailColor, barRadius);
        }
        if (healthPercent > 0.01f) {
            long elapsed = System.currentTimeMillis() - this.startTime;
            int[] colors = this.buildHealthBarColors(alpha, elapsed, healthPercent);
            Render2D.gradientRect(barX, barY, barWidth * healthPercent, barHeight, colors, barRadius);
        }
        if ((absorptionPercent = Math.max(0.0f, Math.min(1.0f, this.absorptionAnimation))) > 0.01f && !Network.isFunTime()) {
            long elapsed = System.currentTimeMillis() - this.startTime;
            float waveSpeed = 1200.0f;
            float wavePhase = (float)(elapsed % (long)waveSpeed) / waveSpeed * (float)Math.PI * 2.0f;
            int[] goldColors = new int[4];
            for (int i = 0; i < 2; ++i) {
                float charWave = (float)Math.sin(wavePhase - (float)i * 1.5f);
                float waveFactor = (charWave + 1.0f) / 2.0f;
                int cr = 255;
                int cg = (int)(165.0f + 50.0f * waveFactor);
                int cb = 0;
                goldColors[i * 2] = new Color(cr, cg, cb, (int)(200.0f * alpha)).getRGB();
                goldColors[i * 2 + 1] = new Color(cr, cg, cb, (int)(200.0f * alpha)).getRGB();
            }
            Render2D.gradientRect(barX, barY, barWidth * absorptionPercent, barHeight, goldColors, barRadius);
        }
    }

    private int[] buildHealthBarColors(float alpha, long elapsed, float healthPercent) {
        int alphaInt = (int)(255.0f * alpha);
        float clampedHealth = Math.max(0.0f, Math.min(1.0f, healthPercent));
        float waveSpeed = 1300.0f;
        float wavePhase = (float)(elapsed % (long)waveSpeed) / waveSpeed * (float)Math.PI * 2.0f;
        int[] colors = new int[4];
        for (int i = 0; i < 2; ++i) {
            int color;
            float wave = (float)Math.sin(wavePhase - (float)i * 1.2f);
            float pulse = (wave + 1.0f) * 0.5f;
            colors[i * 2] = color = this.buildSmoothHealthColor(alphaInt, clampedHealth, pulse);
            colors[i * 2 + 1] = color;
        }
        return colors;
    }

    private int buildSmoothHealthColor(int alphaInt, float healthPercent, float pulse) {
        int lowR = 220;
        int lowG = 55;
        int lowB = 55;
        int highR = 70;
        int highG = 215;
        int highB = 95;
        float t = Math.max(0.0f, Math.min(1.0f, healthPercent));
        int baseR = (int)(lowR + (highR - lowR) * t);
        int baseG = (int)(lowG + (highG - lowG) * t);
        int baseB = (int)(lowB + (highB - lowB) * t);
        float brightness = 0.9f + pulse * 0.2f;
        int r = Math.min(255, Math.max(0, (int)(baseR * brightness)));
        int g = Math.min(255, Math.max(0, (int)(baseG * brightness)));
        int b = Math.min(255, Math.max(0, (int)(baseB * brightness)));
        return new Color(r, g, b, alphaInt).getRGB();
    }
}



