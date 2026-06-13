package royale.modules.impl.combat;

import net.minecraft.client.MinecraftClient;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;

public class ShiftAnim extends ModuleStructure {
    private static ShiftAnim INSTANCE;
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public ShiftAnim() {
        super("ShiftAnim", "Убирает плавную анимацию приседа при удержании Shift", ModuleCategory.COMBAT);
        INSTANCE = this;
    }

    public static ShiftAnim getInstance() {
        return INSTANCE;
    }

    public boolean isActiveOnShift() {
        return this.isState()
                && this.mc.player != null
                && this.mc.options != null
                && !this.mc.player.isSpectator()
                && (this.mc.options.sneakKey.isPressed() || this.mc.player.isSneaking());
    }
}
