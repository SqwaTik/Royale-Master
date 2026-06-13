package royale.util.interfaces;
import royale.IMinecraft;
public abstract class AbstractComponent
implements Component, IMinecraft, ResizableMovable {
public float x;
public float y;
public double scroll = 0.0D; public float width; public float height;
public double smoothedScroll = 0.0D;
public ResizableMovable position(float x, float y) {
this.x = x;
this.y = y;
return this;
}
public ResizableMovable size(float width, float height) {
this.width = width;
this.height = height;
return this;
}
public void tick() {}
public boolean mouseClicked(double mouseX, double mouseY, int button) {
return false;
}
public boolean mouseReleased(double mouseX, double mouseY, int button) {
return false;
}
public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
return false;
}
public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
return false;
}
public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
return false;
}
public boolean charTyped(char chr, int modifiers) {
return false;
}
public boolean isHover(double mouseX, double mouseY) {
return false;
}
}


