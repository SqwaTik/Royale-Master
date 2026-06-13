package royale.modules.impl.combat;

import net.minecraft.item.ItemStack;
import net.minecraft.component.DataComponentTypes;
import royale.events.api.EventHandler;
import royale.events.impl.TickEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.implement.SliderSettings;

public class AutoEat
extends ModuleStructure {
    private final SliderSettings hungerThreshold = new SliderSettings("\u041f\u043e\u0440\u043e\u0433 \u0433\u043e\u043b\u043e\u0434\u0430", "\u041f\u0440\u0438 \u043a\u0430\u043a\u043e\u043c \u0443\u0440\u043e\u0432\u043d\u0435 \u0433\u043e\u043b\u043e\u0434\u0430 \u043d\u0430\u0447\u0438\u043d\u0430\u0442\u044c \u0435\u0441\u0442\u044c").range(1.0f, 20.0f).setValue(14.0f);
    private boolean eatingByModule = false;

    public AutoEat() {
        super("AutoEat", "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u0435\u0441\u0442 \u043f\u0440\u0438 \u0433\u043e\u043b\u043e\u0434\u0435, \u0435\u0441\u043b\u0438 \u0438\u0433\u0440\u043e\u043a \u043d\u0435 \u0434\u0432\u0438\u0433\u0430\u0435\u0442\u0441\u044f", ModuleCategory.COMBAT);
        this.settings(this.hungerThreshold);
    }

    @Override
    public void activate() {
        this.eatingByModule = false;
    }

    @Override
    public void deactivate() {
        this.stopEating();
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (AutoEat.mc.player == null || AutoEat.mc.world == null) {
            return;
        }
        if (AutoEat.mc.currentScreen != null) {
            this.stopEating();
            return;
        }
        if (this.isMoving()) {
            this.stopEating();
            return;
        }
        if (!this.shouldEatNow()) {
            this.stopEating();
            return;
        }
        this.startEating();
    }

    private boolean shouldEatNow() {
        if (AutoEat.mc.player.isDead()) {
            return false;
        }
        if ((float)AutoEat.mc.player.getHungerManager().getFoodLevel() > this.hungerThreshold.getValue()) {
            return false;
        }
        return this.hasFoodInHands();
    }

    private boolean hasFoodInHands() {
        return this.isFood(AutoEat.mc.player.getMainHandStack()) || this.isFood(AutoEat.mc.player.getOffHandStack());
    }

    private boolean isFood(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.get(DataComponentTypes.FOOD) != null;
    }

    private boolean isMoving() {
        return AutoEat.mc.player.input.getMovementInput().x != 0.0f || AutoEat.mc.player.input.getMovementInput().y != 0.0f;
    }

    private void startEating() {
        if (!this.eatingByModule) {
            this.eatingByModule = true;
        }
        AutoEat.mc.options.useKey.setPressed(true);
    }

    private void stopEating() {
        if (this.eatingByModule) {
            AutoEat.mc.options.useKey.setPressed(false);
            this.eatingByModule = false;
        }
    }
}

