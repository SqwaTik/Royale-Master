package royale.modules.impl.misc;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionfc;
import royale.events.api.EventHandler;
import royale.events.impl.HandAnimationEvent;
import royale.events.impl.TickEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.util.Instance;
import royale.util.sounds.SoundManager;

public class Gazan67 extends ModuleStructure {
    private final SliderSettings volume = (new SliderSettings("Громкость", "Громкость звука 67"))
        .range(0.1F, 2.0F)
        .setValue(1.0F);

    private final SliderSettings duration = (new SliderSettings("Длительность", "Сколько секунд делать мем 67 руками"))
        .range(1.0F, 6.0F)
        .setValue(2.7F);

    private long startTime;
    private long lastSwingTime;

    public Gazan67() {
        super("67 Meme", "Проигрывает мем 67 и делает анимацию руками", ModuleCategory.RENDER);
        settings(new Setting[]{this.volume, this.duration});
    }

    public static Gazan67 getInstance() {
        return Instance.get(Gazan67.class);
    }

    @Override
    public void activate() {
        this.startTime = System.currentTimeMillis();
        this.lastSwingTime = 0L;
        SoundManager.playSoundDirect(SoundManager.GAZAN67, this.volume.getValue(), 1.0F);
        swingHands();
    }

    @Override
    public void deactivate() {
        this.startTime = 0L;
        this.lastSwingTime = 0L;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        long now = System.currentTimeMillis();
        if (this.startTime == 0L) {
            this.startTime = now;
        }

        if (now - this.startTime >= this.duration.getValue() * 1000.0F) {
            setState(false);
            return;
        }

        if (now - this.lastSwingTime >= 260L) {
            swingHands();
        }
    }

    @EventHandler
    public void onHandAnimation(HandAnimationEvent event) {
        if (mc.player == null || this.startTime == 0L) {
            return;
        }

        float elapsed = (System.currentTimeMillis() - this.startTime) / 1000.0F;
        float phase = elapsed * 6.7F;
        float wave = (float) Math.sin(phase * Math.PI * 2.0D);
        float bounce = (float) Math.sin(phase * Math.PI * 4.0D);
        int side = event.getHand() == Hand.MAIN_HAND ? 1 : -1;
        MatrixStack matrices = event.getMatrices();

        matrices.translate(side * (0.36F + 0.08F * wave), -0.42F + 0.10F * Math.abs(bounce), -0.64F);
        matrices.multiply((Quaternionfc) RotationAxis.POSITIVE_Y.rotationDegrees(side * (58.0F + 16.0F * wave)));
        matrices.multiply((Quaternionfc) RotationAxis.POSITIVE_Z.rotationDegrees(side * (24.0F + 34.0F * bounce)));
        matrices.multiply((Quaternionfc) RotationAxis.POSITIVE_X.rotationDegrees(-76.0F + 22.0F * wave));
        matrices.translate(0.0F, -0.10F, 0.08F);
        event.cancel();
    }

    private void swingHands() {
        this.lastSwingTime = System.currentTimeMillis();
        if (mc.player != null) {
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.swingHand(Hand.OFF_HAND);
        }
    }
}