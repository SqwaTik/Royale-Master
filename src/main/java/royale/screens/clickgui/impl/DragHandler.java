package royale.screens.clickgui.impl;
import org.lwjgl.glfw.GLFW;
import royale.IMinecraft;
public class DragHandler
implements IMinecraft {
public void setOffsetX(float offsetX) {
this.offsetX = offsetX; } public void setOffsetY(float offsetY) { this.offsetY = offsetY; } public void setTargetOffsetX(float targetOffsetX) { this.targetOffsetX = targetOffsetX; } public void setTargetOffsetY(float targetOffsetY) { this.targetOffsetY = targetOffsetY; } public void setDragging(boolean dragging) { this.dragging = dragging; } public void setDragStartX(double dragStartX) { this.dragStartX = dragStartX; } public void setDragStartY(double dragStartY) { this.dragStartY = dragStartY; } public void setDragStartOffsetX(float dragStartOffsetX) { this.dragStartOffsetX = dragStartOffsetX; } public void setDragStartOffsetY(float dragStartOffsetY) { this.dragStartOffsetY = dragStartOffsetY; } public void setLastUpdateTime(long lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
private float offsetX = 0.0F; public float getOffsetX() { return this.offsetX; }
private float offsetY = 0.0F; public float getOffsetY() { return this.offsetY; }
private float targetOffsetX = 0.0F; public float getTargetOffsetX() { return this.targetOffsetX; }
private float targetOffsetY = 0.0F; public float getTargetOffsetY() { return this.targetOffsetY; }
private boolean dragging = false; public boolean isDragging() {
return this.dragging;
} private double dragStartX = 0.0D; public double getDragStartX() { return this.dragStartX; }
private double dragStartY = 0.0D; public double getDragStartY() { return this.dragStartY; }
private float dragStartOffsetX = 0.0F; public float getDragStartOffsetX() { return this.dragStartOffsetX; }
private float dragStartOffsetY = 0.0F; private static final float ANIMATION_SPEED = 10.0F; public float getDragStartOffsetY() { return this.dragStartOffsetY; }
private long lastUpdateTime = System.currentTimeMillis(); public long getLastUpdateTime() { return this.lastUpdateTime; }
public void update(double mouseX, double mouseY) {
long currentTime = System.currentTimeMillis();
float deltaTime = Math.min((float)(currentTime - this.lastUpdateTime) / 1000.0F, 0.1F);
this.lastUpdateTime = currentTime;
if (this.dragging) {
if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), 2) != 1) {
this.dragging = false;
} else {
this.targetOffsetX = this.dragStartOffsetX + (float)(mouseX - this.dragStartX);
this.targetOffsetY = this.dragStartOffsetY + (float)(mouseY - this.dragStartY);
this.offsetX = this.targetOffsetX;
this.offsetY = this.targetOffsetY;
} 
}
float diffX = this.targetOffsetX - this.offsetX;
float diffY = this.targetOffsetY - this.offsetY;
if (Math.abs(diffX) > 0.01F) {
this.offsetX += diffX * 10.0F * deltaTime;
} else {
this.offsetX = this.targetOffsetX;
} 
if (Math.abs(diffY) > 0.01F) {
this.offsetY += diffY * 10.0F * deltaTime;
} else {
this.offsetY = this.targetOffsetY;
} 
}
public boolean startDrag(double mouseX, double mouseY, float bgX, float bgY, int bgWidth, int bgHeight) {
if (mouseX >= bgX && mouseX <= (bgX + bgWidth) && mouseY >= bgY && mouseY <= (bgY + bgHeight)) {
this.dragging = true;
this.dragStartX = mouseX;
this.dragStartY = mouseY;
this.dragStartOffsetX = this.targetOffsetX;
this.dragStartOffsetY = this.targetOffsetY;
return true;
} 
return false;
}
public void reset() {
this.targetOffsetX = 0.0F;
this.targetOffsetY = 0.0F;
}
public void stopDrag() {
this.dragging = false;
}
public boolean isResetNeeded(int key, int mods) {
boolean ctrlMod = ((mods & 0x2) != 0);
boolean altMod = ((mods & 0x4) != 0);
boolean isCtrlKey = (key == 341 || key == 345);
boolean isAltKey = (key == 342 || key == 346);
return ((isCtrlKey && altMod) || (isAltKey && ctrlMod));
}
}


