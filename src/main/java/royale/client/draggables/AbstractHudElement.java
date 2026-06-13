package royale.client.draggables;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import royale.util.animations.Animation;
import royale.util.animations.Decelerate;
import royale.util.animations.Direction;
public abstract class AbstractHudElement implements HudElement {
protected int x;
protected int y;
protected int width;
protected int height;
protected String name;
protected boolean enabled = true;
protected boolean draggable = true;
protected final MinecraftClient mc = MinecraftClient.getInstance();
protected final Animation scaleAnimation = (new Decelerate()).setMs(300).setValue(1.0D);
protected float lastTickDelta = 0.0F;
public AbstractHudElement(String name, int x, int y, int width, int height, boolean draggable) {
this.name = name;
this.x = x;
this.y = y;
this.width = width;
this.height = height;
this.draggable = draggable;
}
public void render(DrawContext context, float tickDelta) {
if (!visible())
return; 
this.lastTickDelta = tickDelta;
this.scaleAnimation.update();
int alpha = (int)(this.scaleAnimation.getOutput().floatValue() * 255.0F);
if (alpha <= 0)
return; 
drawDraggable(context, alpha);
}
public abstract void drawDraggable(DrawContext paramclass_332, int paramInt);
public void tick() {}
public boolean visible() {
return true;
}
public void startAnimation() {
this.scaleAnimation.setDirection(Direction.FORWARDS);
}
public void stopAnimation() {
this.scaleAnimation.setDirection(Direction.BACKWARDS);
}
protected boolean isChat(Screen screen) {
return screen instanceof net.minecraft.client.gui.screen.ChatScreen;
}
public boolean isDraggable() {
return this.draggable;
}
public float getLastTickDelta() {
return this.lastTickDelta;
}
public boolean isEnabled() {
return this.enabled;
}
public void setEnabled(boolean enabled) {
this.enabled = enabled;
}
public String getName() {
return this.name;
}
public int getX() {
return this.x;
}
public int getY() {
return this.y;
}
public void setX(int x) {
this.x = x;
}
public void setY(int y) {
this.y = y;
}
public int getWidth() {
return this.width;
}
public int getHeight() {
return this.height;
}
public void setWidth(int width) {
this.width = width;
}
public void setHeight(int height) {
this.height = height;
}
}


