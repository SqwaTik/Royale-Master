package royale.modules.impl.player;

import net.minecraft.util.math.MathHelper;
import royale.events.api.EventHandler;
import royale.events.impl.FovEvent;
import royale.events.impl.HotBarScrollEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.BindSetting;
import royale.modules.module.setting.implement.BooleanSetting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.util.math.OptionValueUtil;
import royale.util.string.PlayerInteractionHelper;

public class Zoom extends ModuleStructure {
    private final BindSetting zoomKey = (new BindSetting("Клавиша зума", "Удерживайте клавишу для зума")).setKey(67);
    private final SliderSettings baseFov = (new SliderSettings("FOV", "Базовый FOV при зуме"))
            .range(5.0F, 70.0F)
            .setValue(25.0F);
    private final BooleanSetting smooth = (new BooleanSetting("Плавность", "Плавный переход зума")).setValue(true);
    private final SliderSettings smoothSpeed = (new SliderSettings("Скорость плавности", "Скорость перехода"))
            .range(2.0F, 20.0F)
            .setValue(9.0F)
            .visible(() -> this.smooth.isValue());
    private final SliderSettings wheelStep = (new SliderSettings("Шаг колеса", "Шаг изменения FOV колесом"))
            .range(0.5F, 8.0F)
            .setValue(1.6F);

    private float zoomFov = 25.0F;
    private float smoothFov = 70.0F;
    private long lastFovUpdateNs = 0L;
    private boolean wasZooming = false;

    public Zoom() {
        super("Zoom", "Приближает обзор и поддерживает настройку колесом мыши", ModuleCategory.PLAYER);
        settings(new Setting[]{this.zoomKey, this.baseFov, this.smooth, this.smoothSpeed, this.wheelStep});
    }

    public void activate() {
        this.zoomFov = this.baseFov.getValue();
        this.smoothFov = getCurrentFov();
        this.lastFovUpdateNs = System.nanoTime();
        this.wasZooming = false;
        super.activate();
    }

    public void deactivate() {
        this.zoomFov = this.baseFov.getValue();
        this.smoothFov = getCurrentFov();
        this.lastFovUpdateNs = System.nanoTime();
        this.wasZooming = false;
        super.deactivate();
    }

    @EventHandler
    public void onScroll(HotBarScrollEvent event) {
        if (!isZoomActive()) {
            return;
        }

        double vertical = event.getVertical();
        if (Math.abs(vertical) < 0.001D) {
            return;
        }

        this.zoomFov = MathHelper.clamp(this.zoomFov - (float) vertical * this.wheelStep.getValue(), 2.0F, 70.0F);
        event.cancel();
    }

    @EventHandler
    public void onFov(FovEvent event) {
        if (mc == null || mc.options == null) {
            return;
        }

        float normalFov = getCurrentFov();
        boolean zoomActive = isZoomActive();

        if (!zoomActive && this.wasZooming) {
            this.zoomFov = this.baseFov.getValue();
        }
        this.wasZooming = zoomActive;

        float targetFov = zoomActive ? MathHelper.clamp(this.zoomFov, 2.0F, normalFov) : normalFov;

        if (!this.smooth.isValue()) {
            this.smoothFov = targetFov;
            this.lastFovUpdateNs = System.nanoTime();
        } else {
            this.smoothFov = animate(this.smoothFov, targetFov);
        }

        float clampedFov = MathHelper.clamp(this.smoothFov, 2.0F, normalFov);
        if (zoomActive || Math.abs(clampedFov - normalFov) > 0.0005F) {
            event.setFov(clampedFov);
            event.cancel();
        }

        if (!zoomActive && Math.abs(this.smoothFov - normalFov) <= 0.001F) {
            this.smoothFov = normalFov;
        }
    }

    private boolean isZoomActive() {
        return mc != null && mc.currentScreen == null && PlayerInteractionHelper.isKey(this.zoomKey);
    }

    private float getCurrentFov() {
        if (mc == null || mc.options == null) {
            return 70.0F;
        }
        return Math.max(2.0F, OptionValueUtil.toFloat(mc.options.getFov().getValue(), 70.0F));
    }

    private float animate(float current, float target) {
        if (Math.abs(current - target) <= 0.0005F) {
            this.lastFovUpdateNs = System.nanoTime();
            return target;
        }

        long now = System.nanoTime();
        if (this.lastFovUpdateNs == 0L) {
            this.lastFovUpdateNs = now;
        }

        float dt = (now - this.lastFovUpdateNs) / 1_000_000_000.0F;
        this.lastFovUpdateNs = now;
        dt = MathHelper.clamp(dt, 0.001F, 0.05F);

        float speedNorm = (this.smoothSpeed.getValue() - 2.0F) / 18.0F;
        float response = MathHelper.lerp(speedNorm, 6.0F, 28.0F);
        float factor = 1.0F - (float) Math.exp(-response * dt);
        factor = MathHelper.clamp(factor, 0.0001F, 0.95F);

        return MathHelper.lerp(factor, current, target);
    }
}