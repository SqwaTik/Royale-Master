package royale.util.interfaces;

import net.minecraft.client.gui.DrawContext;

public interface Component {
  void render(DrawContext paramclass_332, int paramInt1, int paramInt2, float paramFloat);
  
  void tick();
  
  boolean mouseClicked(double paramDouble1, double paramDouble2, int paramInt);
  
  boolean mouseReleased(double paramDouble1, double paramDouble2, int paramInt);
  
  boolean mouseDragged(double paramDouble1, double paramDouble2, int paramInt, double paramDouble3, double paramDouble4);
  
  boolean mouseScrolled(double paramDouble1, double paramDouble2, double paramDouble3);
  
  boolean keyPressed(int paramInt1, int paramInt2, int paramInt3);
  
  boolean charTyped(char paramChar, int paramInt);
  
  boolean isHover(double paramDouble1, double paramDouble2);
}


