package royale.mixin;

import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import royale.events.api.EventManager;
import royale.events.impl.BlockBreakingEvent;
import royale.events.impl.ClickSlotEvent;
import royale.events.impl.UsingItemEvent;

@Mixin(value={ClientPlayerInteractionManager.class})
public class ClientPlayerInteractionManagerMixin {
    @Inject(method={"interactItem"}, at={@At(value="RETURN")})
    public void interactItemHook(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ActionResult.Success success;
        Object object = cir.getReturnValue();
        if (object instanceof ActionResult.Success && !(success = (ActionResult.Success)object).swingSource().equals((Object)ActionResult.SwingSource.CLIENT)) {
            UsingItemEvent event = new UsingItemEvent((byte)0);
            EventManager.callEvent(event);
        }
    }

    @Inject(method={"stopUsingItem"}, at={@At(value="HEAD")}, cancellable=true)
    public void stopUsingItemHook(CallbackInfo ci) {
        UsingItemEvent event = new UsingItemEvent((byte)2);
        EventManager.callEvent(event);
    }

    @Inject(method={"interactItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void gameModeHook(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        UsingItemEvent event = new UsingItemEvent((byte)-1);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method={"updateBlockBreakingProgress"}, at={@At(value="HEAD")})
    private void injectBlockBreaking(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        EventManager.callEvent(new BlockBreakingEvent(pos, direction));
    }

    @Inject(method={"clickSlot"}, at={@At(value="HEAD")}, cancellable=true)
    public void clickSlotHook(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo info) {
        ClickSlotEvent event = new ClickSlotEvent(syncId, slotId, button, actionType);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }
}

