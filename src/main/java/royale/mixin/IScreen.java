package royale.mixin;

import java.util.List;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.Selectable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({Screen.class})
public interface IScreen {
  @Accessor("drawables")
  List<Drawable> getDrawables();
  
  @Accessor("children")
  List<Element> getChildren();
  
  @Accessor("selectables")
  List<Selectable> getSelectables();
}


