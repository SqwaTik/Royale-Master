package royale.modules.impl.combat;
import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import royale.events.api.EventHandler;
import royale.events.impl.TickEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.implement.BooleanSetting;
import royale.modules.module.setting.implement.SelectSetting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.util.string.chat.ChatMessage;
import royale.util.timer.StopWatch;

public class TapeMouse
extends ModuleStructure {
    private final SelectSetting modeClick = new SelectSetting("\u0422\u0438\u043f \u043a\u043b\u0438\u043a\u0430", "\u041a\u0430\u043a\u043e\u0439 \u043a\u043b\u0438\u043a \u0432\u044b\u043f\u043e\u043b\u043d\u044f\u0442\u044c \u0430\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438").value("\u041b\u0435\u0432\u0430\u044f \u043a\u043d\u043e\u043f\u043a\u0430", "\u041f\u0440\u0430\u0432\u0430\u044f \u043a\u043d\u043e\u043f\u043a\u0430").selected("\u041b\u0435\u0432\u0430\u044f \u043a\u043d\u043e\u043f\u043a\u0430");
    private final SliderSettings delayForClick = new SliderSettings("\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430", "\u0418\u043d\u0442\u0435\u0440\u0432\u0430\u043b \u043c\u0435\u0436\u0434\u0443 \u043a\u043b\u0438\u043a\u0430\u043c\u0438").range(1.0f, 15.0f).setValue(1.0f);
    private final BooleanSetting saveItems = new BooleanSetting("\u0421\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0438\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432", "\u041e\u0442\u043a\u043b\u044e\u0447\u0430\u0442\u044c \u043c\u043e\u0434\u0443\u043b\u044c \u043f\u0440\u0438 \u043f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u0438 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430 1").setValue(false);
    private final StopWatch delay = new StopWatch();

    public TapeMouse() {
        super("TapeMouse", "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u043f\u0440\u043e\u0436\u0438\u043c\u0430\u0435\u0442 \u041b\u041a\u041c \u0438\u043b\u0438 \u041f\u041a\u041c", ModuleCategory.COMBAT);
        this.settings(this.modeClick, this.delayForClick, this.saveItems);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (TapeMouse.mc.player == null || TapeMouse.mc.world == null) {
            return;
        }
        if (TapeMouse.mc.currentScreen != null) {
            return;
        }
        if (this.saveItems.isValue() && this.shouldProtectCurrentItem()) {
            this.setState(false);
            ChatMessage.brandmessage("TapeMouse \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d: \u043d\u0438\u0437\u043a\u0430\u044f \u043f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430 (1)");
            return;
        }
        long delayMs = (long)(this.delayForClick.getValue() * 300.0f);
        if (!this.delay.finished(delayMs)) {
            return;
        }
        this.performClick();
        this.delay.reset();
    }

    private boolean shouldProtectCurrentItem() {
        ItemStack stack = TapeMouse.mc.player.getMainHandStack();
        if (stack == null || stack.isEmpty() || !stack.isDamageable()) {
            return false;
        }
        int remainingDurability = stack.getMaxDamage() - stack.getDamage();
        return remainingDurability <= 1;
    }
    private void performClick() {
        if (this.modeClick.isSelected("\u041b\u0435\u0432\u0430\u044f \u043a\u043d\u043e\u043f\u043a\u0430")) {
            this.leftClick();
        } else {
            this.rightClick();
        }
    }
    private void leftClick() {
        if (TapeMouse.mc.interactionManager == null) {
            return;
        }
        if (TapeMouse.mc.targetedEntity != null) {
            TapeMouse.mc.interactionManager.attackEntity((PlayerEntity)TapeMouse.mc.player, TapeMouse.mc.targetedEntity);
            TapeMouse.mc.player.swingHand(Hand.MAIN_HAND);
        } else if (TapeMouse.mc.crosshairTarget != null) {
            mc.doAttack();
        }
    }
    private void rightClick() {
        if (TapeMouse.mc.interactionManager == null) {
            return;
        }
        mc.doItemUse();
    }

    public SelectSetting getModeClick() {
        return this.modeClick;
    }

    public SliderSettings getDelayForClick() {
        return this.delayForClick;
    }

    public BooleanSetting getSaveItems() {
        return this.saveItems;
    }

    public StopWatch getDelay() {
        return this.delay;
    }
}

