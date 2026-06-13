package royale.util.inventory;

import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface ItemSearcher {
  boolean matches(ItemStack paramclass_1799);
}


