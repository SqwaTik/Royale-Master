package royale.mixin;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.network.PendingUpdateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ClientWorld.class})
public interface ClientWorldAccessor {
  @Accessor("pendingUpdateManager")
  PendingUpdateManager getPendingUpdateManager();
}


