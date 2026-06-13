package royale.screens.hud;

import java.awt.Color;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import royale.Initialization;
import royale.client.draggables.AbstractHudElement;
import royale.modules.module.ModuleRepository;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.util.render.BrandMarkRenderer;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.theme.ClientTheme;
import royale.util.tps.TPSCalculate;

public final class RoyaleHud extends AbstractHudElement {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final int MAX_TOASTS = 4;

    private final List<Toast> toasts = new ArrayList<>();
    private long lastTickTime = System.currentTimeMillis();
    private float wavePhase;
    private float healthTrail;
    private boolean bootstrapped;

    public RoyaleHud() {
        super("RoyaleHud", 12, 12, 320, 30, false);
    }

    @Override
    public boolean visible() {
        return true;
    }

    @Override
    public void tick() {
        long now = System.currentTimeMillis();
        float delta = Math.min(0.1F, (now - this.lastTickTime) / 1000.0F);
        this.lastTickTime = now;
        this.wavePhase += delta * 1.15F;
        this.toasts.removeIf(Toast::expired);

        if (!this.bootstrapped && this.mc.player != null && this.mc.world != null) {
            this.pushToast("Royale HUD", "In-game overlay online", ClientTheme.accent(), 2600L);
            this.pushToast("Royale HUD", "Press RSHIFT to move widgets", 0x63E7FF, 3200L);
            this.bootstrapped = true;
        }
    }

    public void pushToast(String title, String message, int accent, long durationMs) {
        this.toasts.add(new Toast(title, message, accent, durationMs));
        while (this.toasts.size() > MAX_TOASTS) {
            this.toasts.remove(0);
        }
    }

    @Override
    public void drawDraggable(DrawContext context, int alpha) {
        if (alpha <= 0 || this.mc == null || this.mc.getWindow() == null) {
            return;
        }

        float uiAlpha = clamp01(alpha / 255.0F);
        float screenW = getVirtualWidth();
        float screenH = getVirtualHeight();

        renderWatermark(18.0F, 16.0F, uiAlpha);
        renderCompass(screenW * 0.5F - 124.0F, 16.0F, 248.0F, uiAlpha);
        renderTargetHud(screenW - 264.0F, 16.0F, uiAlpha);
        renderModuleRail(18.0F, 84.0F, uiAlpha);
        renderStatsCard(18.0F, screenH - 112.0F, uiAlpha);
        renderKeystrokes(screenW * 0.5F - 108.0F, screenH - 176.0F, uiAlpha);
        renderToasts(screenW - 276.0F, screenH - 198.0F, uiAlpha);
        renderCrosshair(screenW * 0.5F, screenH * 0.5F, uiAlpha);
    }

    private void renderWatermark(float x, float y, float alpha) {
        String brand = "HUD";
        String client = "ROYALE MASTER";
        String user = this.mc.getSession() != null ? this.mc.getSession().getUsername() : "unknown";
        String fps = this.mc.getCurrentFps() + "fps";
        String tps = formatTps();
        String time = LocalTime.now().format(TIME_FORMAT);
        int accent = ClientTheme.accentWithAlpha(toAlpha(alpha));
        float brandWidth = Fonts.BOLD.getWidth(brand, 8.0F);
        float userWidth = Fonts.REGULARNEW.getWidth(user, 6.7F);
        float infoWidth = Fonts.REGULARNEW.getWidth(fps, 6.4F) + Fonts.REGULARNEW.getWidth(tps, 6.4F) + Fonts.REGULARNEW.getWidth(time, 6.4F) + 44.0F;
        float panelWidth = 36.0F + brandWidth + userWidth + infoWidth;

        Render2D.blur(x, y, panelWidth, 34.0F, 8.0F, 8.0F, 0x1A000000);
        Render2D.rect(x, y, panelWidth, 34.0F, rgba(0x0E1420, alpha), 14.0F);
        Render2D.outline(x, y, panelWidth, 34.0F, 1.0F, rgba(0xFFFFFF, alpha * 0.18F), 14.0F);
        BrandMarkRenderer.drawR(x + 15.0F, y + 17.0F, 13.0F, toAlpha(alpha));
        Fonts.BOLD.draw(brand, x + 30.0F, y + 13.0F, 8.0F, accent);
        Fonts.REGULARNEW.draw(client, x + 30.0F, y + 24.0F, 6.7F, rgba(0xF5F7FF, alpha));
        Fonts.REGULARNEW.draw(user, x + 30.0F + brandWidth + 10.0F, y + 24.0F, 6.5F, rgba(0xA7B0C9, alpha));

        float chipX = x + 30.0F + brandWidth + userWidth + 24.0F;
        chipX += drawStatChip(chipX, y + 8.0F, fps, 6.4F, alpha, 0x63E7FF);
        chipX += drawStatChip(chipX, y + 8.0F, tps, 6.4F, alpha, 0x7FEFBE);
        drawStatChip(chipX, y + 8.0F, time, 6.4F, alpha, 0xFFCF5A);
    }

    private float drawStatChip(float x, float y, String label, float size, float alpha, int accentColor) {
        float width = Fonts.REGULARNEW.getWidth(label, size) + 18.0F;
        int fill = rgba(0xFFFFFF, alpha * 0.06F);
        int stroke = rgba(accentColor, alpha * 0.22F);
        Render2D.rect(x, y, width, 18.0F, fill, 9.0F);
        Render2D.outline(x, y, width, 18.0F, 1.0F, stroke, 9.0F);
        Fonts.REGULARNEW.drawCentered(label, x + width * 0.5F, y + 12.0F, size, rgba(0xF5F7FF, alpha));
        return width + 8.0F;
    }

    private void renderCompass(float x, float y, float width, float alpha) {
        int base = rgba(0x0D121C, alpha * 0.72F);
        int stroke = rgba(0xFFFFFF, alpha * 0.10F);
        Render2D.rect(x, y, width, 24.0F, base, 12.0F);
        Render2D.outline(x, y, width, 24.0F, 1.0F, stroke, 12.0F);
        Render2D.rect(x + width * 0.5F - 1.5F, y + 4.0F, 3.0F, 16.0F, ClientTheme.accentWithAlpha(toAlpha(alpha)), 2.0F);
        Fonts.BOLD.drawCentered("N", x + width * 0.5F, y + 16.0F, 7.0F, rgba(0xF5F7FF, alpha));
        Fonts.REGULARNEW.drawCentered("NW", x + width * 0.25F, y + 15.0F, 6.0F, rgba(0xA7B0C9, alpha));
        Fonts.REGULARNEW.drawCentered("NE", x + width * 0.75F, y + 15.0F, 6.0F, rgba(0xA7B0C9, alpha));
    }

    private void renderTargetHud(float x, float y, float alpha) {
        PlayerEntity target = resolveTargetPlayer();
        if (target == null) {
            return;
        }

        String name = target.getName().getString();
        float health = Math.max(0.0F, target.getHealth());
        float maxHealth = Math.max(1.0F, target.getMaxHealth());
        float ratio = Math.max(0.0F, Math.min(1.0F, health / maxHealth));
        this.healthTrail += (ratio - this.healthTrail) * 0.16F;
        float distance = this.mc.player != null ? (float)Math.sqrt(this.mc.player.squaredDistanceTo(target)) : 0.0F;
        String distanceLabel = String.format(Locale.ROOT, "%.1f blocks", Float.valueOf(distance));
        String healthLabel = String.format(Locale.ROOT, "%.1f / %.0f HP", Float.valueOf(health), Float.valueOf(maxHealth));
        String initials = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase(Locale.ROOT);
        float width = 244.0F;
        float height = 96.0F;

        Render2D.blur(x, y, width, height, 10.0F, 12.0F, 0x22000000);
        Render2D.rect(x, y, width, height, rgba(0x101826, alpha * 0.78F), 18.0F);
        Render2D.outline(x, y, width, height, 1.0F, rgba(0xFFFFFF, alpha * 0.10F), 18.0F);
        Render2D.rect(x, y, width, 34.0F, rgba(0xF63D3D, alpha * 0.95F), 18.0F, 18.0F, 0.0F, 0.0F);
        Render2D.rect(x, y + 34.0F, width, 62.0F, rgba(0x353C4A, alpha * 0.82F), 0.0F, 0.0F, 18.0F, 18.0F);

        Render2D.rect(x + 14.0F, y + 18.0F, 54.0F, 54.0F, rgba(0x0C0F16, alpha * 0.72F), 27.0F);
        Render2D.outline(x + 14.0F, y + 18.0F, 54.0F, 54.0F, 1.0F, rgba(0x000000, alpha * 0.32F), 27.0F);
        Render2D.glowOutline(x + 14.0F, y + 18.0F, 54.0F, 54.0F, 1.0F, ClientTheme.accent(), 27.0F, 0.9F, alpha * 0.10F);
        Fonts.BOLD.drawCentered(initials, x + 41.0F, y + 53.0F, 16.0F, rgba(0xF5F7FF, alpha));

        Fonts.BOLD.draw(name, x + 76.0F, y + 48.0F, 10.0F, rgba(0xF5F7FF, alpha));
        Fonts.REGULARNEW.draw(distanceLabel, x + 76.0F, y + 61.0F, 6.5F, rgba(0xA7B0C9, alpha));
        Fonts.REGULARNEW.draw(healthLabel, x + 76.0F, y + 73.0F, 6.5F, rgba(0xDDE6FF, alpha));

        float barX = x + 76.0F;
        float barY = y + 79.0F;
        float barWidth = width - 92.0F;
        Render2D.rect(barX, barY, barWidth, 5.0F, rgba(0xFFFFFF, alpha * 0.07F), 2.5F);
        Render2D.gradientRect(barX, barY, barWidth * this.healthTrail, 5.0F,
                new int[] {
                    rgba(0x63E7FF, alpha),
                    rgba(0x7F7CFF, alpha),
                    rgba(0x7FEFBE, alpha),
                    rgba(0x63E7FF, alpha)
                },
                2.5F);
    }

    private void renderModuleRail(float x, float y, float alpha) {
        List<ModuleStructure> active = collectActiveModules();
        if (active.isEmpty()) {
            active = sampleModules();
        }

        int visibleRows = Math.min(active.size(), 6);
        float width = 182.0F;
        float height = 30.0F + visibleRows * 22.0F;
        Render2D.blur(x, y, width, height, 8.0F, 12.0F, 0x18000000);
        Render2D.rect(x, y, width, height, rgba(0x0E1420, alpha * 0.74F), 16.0F);
        Render2D.outline(x, y, width, height, 1.0F, rgba(0xFFFFFF, alpha * 0.08F), 16.0F);
        Fonts.BOLD.draw("ACTIVE MODULES", x + 14.0F, y + 18.0F, 7.0F, rgba(0xF5F7FF, alpha));
        Fonts.REGULARNEW.draw("live overlay state", x + 14.0F, y + 28.0F, 5.8F, rgba(0xA7B0C9, alpha));

        float rowY = y + 36.0F;
        for (int i = 0; i < visibleRows; ++i) {
            ModuleStructure module = active.get(i);
            int categoryAccent = categoryAccent(module.getCategory(), alpha);
            Render2D.rect(x + 12.0F, rowY, width - 24.0F, 16.0F, rgba(0xFFFFFF, alpha * 0.04F), 7.0F);
            Render2D.rect(x + 14.0F, rowY + 4.0F, 2.4F, 8.0F, categoryAccent, 1.2F);
            Fonts.REGULARNEW.draw(module.getName(), x + 22.0F, rowY + 5.0F, 6.2F, rgba(0xF5F7FF, alpha));
            Fonts.REGULARNEW.draw(module.getCategory().name(), x + width - 56.0F, rowY + 5.0F, 5.0F, rgba(0xA7B0C9, alpha));
            rowY += 22.0F;
        }
    }

    private void renderStatsCard(float x, float y, float alpha) {
        String fps = this.mc.getCurrentFps() + " fps";
        String tps = formatTps();
        String time = LocalTime.now().format(TIME_FORMAT);
        String pos = this.mc.player != null
                ? String.format(Locale.ROOT, "%d %d %d",
                    Integer.valueOf(this.mc.player.getBlockX()),
                    Integer.valueOf(this.mc.player.getBlockY()),
                    Integer.valueOf(this.mc.player.getBlockZ()))
                : "0 0 0";

        float width = 204.0F;
        float height = 96.0F;
        Render2D.blur(x, y, width, height, 8.0F, 12.0F, 0x18000000);
        Render2D.rect(x, y, width, height, rgba(0x101826, alpha * 0.74F), 16.0F);
        Render2D.outline(x, y, width, height, 1.0F, rgba(0xFFFFFF, alpha * 0.08F), 16.0F);
        Fonts.BOLD.draw("STATISTICS", x + 14.0F, y + 18.0F, 7.0F, rgba(0xF5F7FF, alpha));
        Fonts.REGULARNEW.draw("position / timing", x + 14.0F, y + 28.0F, 5.8F, rgba(0xA7B0C9, alpha));

        drawStatRow(x + 14.0F, y + 40.0F, "FPS", fps, 0x63E7FF, alpha);
        drawStatRow(x + 14.0F, y + 54.0F, "TPS", tps, 0x7FEFBE, alpha);
        drawStatRow(x + 14.0F, y + 68.0F, "TIME", time, 0xFFCF5A, alpha);
        drawStatRow(x + 14.0F, y + 82.0F, "POS", pos, 0xFF7F93, alpha);

        Render2D.rect(x + 132.0F, y + 16.0F, 56.0F, 56.0F, rgba(0xFFFFFF, alpha * 0.04F), 14.0F);
        Render2D.gradientRect(x + 139.0F, y + 26.0F, 42.0F, 4.0F,
                new int[] { rgba(0x63E7FF, alpha), rgba(0x7F7CFF, alpha), rgba(0x63E7FF, alpha), rgba(0x7FEFBE, alpha) }, 2.0F);
        Render2D.gradientRect(x + 139.0F, y + 37.0F, 42.0F, 4.0F,
                new int[] { rgba(0x7FEFBE, alpha), rgba(0x63E7FF, alpha), rgba(0x7F7CFF, alpha), rgba(0x7FEFBE, alpha) }, 2.0F);
        Render2D.gradientRect(x + 139.0F, y + 48.0F, 42.0F, 4.0F,
                new int[] { rgba(0xFF7F93, alpha), rgba(0xFFCF5A, alpha), rgba(0xFF7F93, alpha), rgba(0x63E7FF, alpha) }, 2.0F);
    }

    private void renderKeystrokes(float x, float y, float alpha) {
        boolean wDown = this.mc.options.forwardKey.isPressed();
        boolean aDown = this.mc.options.leftKey.isPressed();
        boolean sDown = this.mc.options.backKey.isPressed();
        boolean dDown = this.mc.options.rightKey.isPressed();
        boolean spaceDown = this.mc.options.jumpKey.isPressed();
        float width = 220.0F;
        float height = 110.0F;

        Render2D.blur(x, y, width, height, 8.0F, 12.0F, 0x18000000);
        Render2D.rect(x, y, width, height, rgba(0x0E1420, alpha * 0.74F), 16.0F);
        Render2D.outline(x, y, width, height, 1.0F, rgba(0xFFFFFF, alpha * 0.08F), 16.0F);
        Fonts.BOLD.draw("MOVEMENT", x + 14.0F, y + 18.0F, 7.0F, rgba(0xF5F7FF, alpha));
        Fonts.REGULARNEW.draw("w a s d / jump", x + 14.0F, y + 28.0F, 5.8F, rgba(0xA7B0C9, alpha));

        drawKey(x + 80.0F, y + 40.0F, 26.0F, 22.0F, "W", wDown, alpha);
        drawKey(x + 52.0F, y + 64.0F, 26.0F, 22.0F, "A", aDown, alpha);
        drawKey(x + 80.0F, y + 64.0F, 26.0F, 22.0F, "S", sDown, alpha);
        drawKey(x + 108.0F, y + 64.0F, 26.0F, 22.0F, "D", dDown, alpha);
        drawKey(x + 52.0F, y + 90.0F, 82.0F, 14.0F, "SPACE", spaceDown, alpha);
    }

    private void drawKey(float x, float y, float width, float height, String label, boolean active, float alpha) {
        int fill = active ? rgba(0x7F7CFF, alpha * 0.40F) : rgba(0xFFFFFF, alpha * 0.05F);
        int stroke = active ? rgba(0x7F7CFF, alpha * 0.48F) : rgba(0xFFFFFF, alpha * 0.08F);
        Render2D.rect(x, y, width, height, fill, 8.0F);
        Render2D.outline(x, y, width, height, 1.0F, stroke, 8.0F);
        Fonts.BOLD.drawCentered(label, x + width * 0.5F, y + height * 0.70F, label.length() > 2 ? 5.0F : 7.0F, rgba(0xF5F7FF, alpha));
    }

    private void renderToasts(float x, float y, float alpha) {
        long now = System.currentTimeMillis();
        float offsetY = 0.0F;
        for (Toast toast : this.toasts) {
            float enter = clamp01((now - toast.createdAt) / 220.0F);
            float leave = clamp01((toast.createdAt + toast.durationMs - now) / 220.0F);
            float anim = Math.min(enter, leave);
            if (anim <= 0.01F) {
                continue;
            }

            float slide = (1.0F - enter) * 10.0F;
            float width = 260.0F;
            float height = 18.0F;
            float drawX = x + slide;
            float drawY = y + offsetY;
            int fill = rgba(0x0B1018, alpha * anim * 0.92F);
            int stroke = rgba(toast.accent, alpha * anim * 0.30F);

            Render2D.blur(drawX, drawY, width, height, 8.0F, 8.0F, 0x18000000);
            Render2D.rect(drawX, drawY, width, height, fill, 8.0F);
            Render2D.outline(drawX, drawY, width, height, 1.0F, stroke, 8.0F);
            Render2D.rect(drawX + 2.0F, drawY + 2.0F, 3.0F, 14.0F, toast.accent, 1.5F);
            Fonts.BOLD.draw(toast.title, drawX + 10.0F, drawY + 7.0F, 5.5F, rgba(0xF5F7FF, alpha * anim));
            Fonts.REGULARNEW.draw(toast.message, drawX + 82.0F, drawY + 7.0F, 5.2F, rgba(0xA7B0C9, alpha * anim));
            offsetY += 22.0F;
        }
    }

    private void renderCrosshair(float centerX, float centerY, float alpha) {
        int cross = ClientTheme.accentWithAlpha(toAlpha(alpha));
        Render2D.rect(centerX - 1.0F, centerY - 1.0F, 2.0F, 2.0F, cross, 1.0F);
        Render2D.rect(centerX - 10.0F, centerY - 0.5F, 5.0F, 1.0F, rgba(0xF5F7FF, alpha * 0.40F), 0.5F);
        Render2D.rect(centerX + 5.0F, centerY - 0.5F, 5.0F, 1.0F, rgba(0xF5F7FF, alpha * 0.40F), 0.5F);
        Render2D.rect(centerX - 0.5F, centerY - 10.0F, 1.0F, 5.0F, rgba(0xF5F7FF, alpha * 0.40F), 0.5F);
        Render2D.rect(centerX - 0.5F, centerY + 5.0F, 1.0F, 5.0F, rgba(0xF5F7FF, alpha * 0.40F), 0.5F);
    }

    private void drawStatRow(float x, float y, String label, String value, int accentColor, float alpha) {
        Fonts.REGULARNEW.draw(label, x, y, 5.5F, rgba(0xA7B0C9, alpha));
        Fonts.BOLD.draw(value, x + 44.0F, y, 5.5F, rgba(0xF5F7FF, alpha));
        Render2D.rect(x + 35.0F, y + 1.0F, 5.0F, 5.0F, accentColor, 2.5F);
    }

    private float getVirtualWidth() {
        return this.mc != null && this.mc.getWindow() != null
                ? this.mc.getWindow().getFramebufferWidth() / 2.0F
                : 960.0F;
    }

    private float getVirtualHeight() {
        return this.mc != null && this.mc.getWindow() != null
                ? this.mc.getWindow().getFramebufferHeight() / 2.0F
                : 540.0F;
    }

    private PlayerEntity resolveTargetPlayer() {
        if (this.mc.player == null || this.mc.world == null) {
            return null;
        }

        PlayerEntity best = null;
        double bestDistanceSq = Double.MAX_VALUE;
        for (PlayerEntity player : this.mc.world.getPlayers()) {
            if (player == this.mc.player || !player.isAlive() || player.isRemoved() || player.isSpectator()) {
                continue;
            }

            double distanceSq = this.mc.player.squaredDistanceTo(player);
            if (distanceSq < bestDistanceSq) {
                bestDistanceSq = distanceSq;
                best = player;
            }
        }

        return best != null ? best : this.mc.player;
    }

    private ModuleRepository safeRepository() {
        try {
            if (Initialization.getInstance() == null || Initialization.getInstance().getManager() == null) {
                return null;
            }
            return Initialization.getInstance().getManager().getModuleRepository();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private List<ModuleStructure> collectActiveModules() {
        ModuleRepository repository = safeRepository();
        List<ModuleStructure> active = new ArrayList<>();
        if (repository == null) {
            return active;
        }

        for (ModuleStructure module : repository.modules()) {
            if (module != null && module.isState()) {
                active.add(module);
            }
        }

        active.sort(Comparator.comparingDouble(module -> -Fonts.REGULARNEW.getWidth(module.getName(), 6.2F)));
        return active;
    }

    private List<ModuleStructure> sampleModules() {
        List<ModuleStructure> sample = new ArrayList<>();
        sample.add(new DemoModule("Aim Assist", ModuleCategory.COMBAT));
        sample.add(new DemoModule("Auto Totem", ModuleCategory.COMBAT));
        sample.add(new DemoModule("Speed", ModuleCategory.MOVEMENT));
        sample.add(new DemoModule("Full Bright", ModuleCategory.RENDER));
        sample.add(new DemoModule("Auto Armor", ModuleCategory.PLAYER));
        sample.add(new DemoModule("Notifications", ModuleCategory.MISC));
        return sample;
    }

    private int categoryAccent(ModuleCategory category, float alpha) {
        int rgb;
        switch (category) {
            case COMBAT:
                rgb = 0xFF7F93;
                break;
            case MOVEMENT:
                rgb = 0x63E7FF;
                break;
            case RENDER:
                rgb = 0x7F7CFF;
                break;
            case PLAYER:
                rgb = 0x7FEFBE;
                break;
            case MISC:
                rgb = 0xFFCF5A;
                break;
            case MODS:
                rgb = 0xFF9F6D;
                break;
            case AUTOBUY:
                rgb = 0xF55C96;
                break;
            default:
                rgb = 0x7F7CFF;
                break;
        }

        return rgba(rgb, alpha);
    }

    private String formatTps() {
        float tps = 20.0F;
        if (TPSCalculate.getInstance() != null) {
            tps = TPSCalculate.getInstance().getTpsRounded();
        }
        return String.format(Locale.ROOT, "%.1f TPS", Float.valueOf(tps));
    }

    private static int rgba(int rgb, float alpha) {
        int a = Math.max(0, Math.min(255, Math.round(alpha * 255.0F)));
        return (rgb & 0x00FFFFFF) | (a << 24);
    }

    private static int toAlpha(float alpha) {
        return Math.max(0, Math.min(255, Math.round(alpha * 255.0F)));
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static final class Toast {
        private final String title;
        private final String message;
        private final int accent;
        private final long createdAt;
        private final long durationMs;

        private Toast(String title, String message, int accent, long durationMs) {
            this.title = title;
            this.message = message;
            this.accent = accent;
            this.createdAt = System.currentTimeMillis();
            this.durationMs = Math.max(400L, durationMs);
        }

        private boolean expired() {
            return System.currentTimeMillis() > this.createdAt + this.durationMs;
        }
    }

    private static final class DemoModule extends ModuleStructure {
        private DemoModule(String name, ModuleCategory category) {
            super(name, category);
        }
    }
}
