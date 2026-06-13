package royale.client.draggables;
import net.minecraft.client.gui.DrawContext;
import royale.events.impl.PacketEvent;
public interface HudElement {
void render(DrawContext paramclass_332, float paramFloat);
void tick();
default void onPacket(PacketEvent e) {}
boolean isEnabled();
void setEnabled(boolean paramBoolean);
String getName();
int getX();
int getY();
void setX(int paramInt);
void setY(int paramInt);
int getWidth();
int getHeight();
void setWidth(int paramInt);
void setHeight(int paramInt);
default float getRoundingRadius() {
return 4.0F;
}
default boolean visible() {
return true;
}
default boolean mouseClicked(double mouseX, double mouseY, int button) {
return false;
}
default boolean mouseReleased(double mouseX, double mouseY, int button) {
return false;
}
}


