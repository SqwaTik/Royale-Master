package royale.modules.impl.combat;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionfc;
import royale.events.api.EventHandler;
import royale.events.api.types.Priority;
import royale.events.impl.HandAnimationEvent;
import royale.events.impl.TickEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.util.Instance;
import royale.util.sounds.SoundManager;

public class Gazan67 extends ModuleStructure {
    private final SliderSettings volume = (new SliderSettings("Громкость", "Громкость песни 67"))
        .range(0.1F, 2.0F)
        .setValue(1.0F);

    private long startTime;
    private long lastSwingTime;
    private SoundInstance loopSound;

    public Gazan67() {
        super("67", "Проигрывает песню 67 на повторе и по очереди поднимает руки", ModuleCategory.COMBAT);
        settings(new Setting[]{this.volume});
    }

    public static Gazan67 getInstance() {
        return Instance.get(Gazan67.class);
    }

    @Override
    public void activate() {
        this.startTime = System.currentTimeMillis();
        this.lastSwingTime = 0L;
        stopLoopSound();
        this.loopSound = SoundManager.playLoopingDirect(SoundManager.GAZAN67, this.volume.getValue(), 1.0F);
        swingHands();
    }

    @Override
    public void deactivate() {
        stopLoopSound();
        this.startTime = 0L;
        this.lastSwingTime = 0L;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        long now = System.currentTimeMillis();
        if (this.startTime == 0L) {
            this.startTime = now;
        }

        if (this.loopSound == null || !mc.getSoundManager().isPlaying(this.loopSound)) {
            this.loopSound = SoundManager.playLoopingDirect(SoundManager.GAZAN67, this.volume.getValue(), 1.0F);
        }

        if (now - this.lastSwingTime >= 450L) {
            swingHands();
        }
    }

    @EventHandler(Priority.LOWEST)
    public void onHandAnimation(HandAnimationEvent event) {
        if (mc.player == null || this.startTime == 0L) {
            return;
        }

        applyArmAnimation(event.getMatrices(), event.getHand());
        event.cancel();
    }

    public void applyArmAnimation(MatrixStack matrices, Hand hand) {
        float elapsed = (System.currentTimeMillis() - this.startTime) / 1000.0F;
        float cycle = 0.86F;
        float progress = (elapsed % cycle) / cycle;
        boolean mainTurn = progress < 0.5F;
        float local = mainTurn ? progress * 2.0F : (progress - 0.5F) * 2.0F;
        boolean activeHand = hand == (mainTurn ? Hand.MAIN_HAND : Hand.OFF_HAND);
        float lift = activeHand ? smoothLift(local) : 0.0F;
        float rest = activeHand ? 0.0F : 0.05F;
        int side = hand == Hand.MAIN_HAND ? 1 : -1;

        matrices.translate(side * (0.46F - lift * 0.04F), -0.68F + lift * 0.72F + rest, -0.82F + lift * 0.18F);
        matrices.multiply((Quaternionfc) RotationAxis.POSITIVE_Y.rotationDegrees(side * (46.0F - lift * 18.0F)));
        matrices.multiply((Quaternionfc) RotationAxis.POSITIVE_Z.rotationDegrees(side * (14.0F + lift * 92.0F)));
        matrices.multiply((Quaternionfc) RotationAxis.POSITIVE_X.rotationDegrees(-78.0F + lift * 112.0F));
        matrices.translate(0.0F, -0.14F, 0.08F);
    }

    private float smoothLift(float progress) {
        float clamped = Math.max(0.0F, Math.min(1.0F, progress));
        return (float) Math.sin(clamped * Math.PI);
    }

    private void swingHands() {
        this.lastSwingTime = System.currentTimeMillis();
        if (mc.player != null) {
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.swingHand(Hand.OFF_HAND);
        }
    }

    private void stopLoopSound() {
        if (this.loopSound != null) {
            SoundManager.stopSound(this.loopSound);
            this.loopSound = null;
        }
    }
}