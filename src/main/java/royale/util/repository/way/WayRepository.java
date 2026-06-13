package royale.util.repository.way;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.Camera;
import royale.IMinecraft;
import royale.events.api.EventHandler;
import royale.events.api.EventManager;
import royale.events.impl.DrawEvent;
import royale.util.config.impl.way.WayConfig;
import royale.util.math.Projection;
import royale.util.render.Render2D;
import royale.util.render.font.Font;
import royale.util.render.font.Fonts;
public class WayRepository
implements IMinecraft
{
private static WayRepository instance;
private final List<Way> wayList = new ArrayList<>(); public List<Way> getWayList() { return this.wayList; }
public WayRepository() {
instance = this;
}
public static WayRepository getInstance() {
if (instance == null) {
instance = new WayRepository();
}
return instance;
}
public void init() {
EventManager.register(this);
WayConfig.getInstance().load();
}
public boolean isEmpty() {
return this.wayList.isEmpty();
}
public void addWay(String name, BlockPos pos, String server) {
this.wayList.add(new Way(name, pos, server));
}
public void addWayAndSave(String name, BlockPos pos, String server) {
addWay(name, pos, server);
WayConfig.getInstance().save();
}
public boolean hasWay(String name) {
return this.wayList.stream().anyMatch(way -> way.name().equalsIgnoreCase(name));
}
public Optional<Way> getWay(String name) {
return this.wayList.stream()
.filter(way -> way.name().equalsIgnoreCase(name))
.findFirst();
}
public void deleteWay(String name) {
this.wayList.removeIf(way -> way.name().equalsIgnoreCase(name));
}
public void deleteWayAndSave(String name) {
deleteWay(name);
WayConfig.getInstance().save();
}
public void clearList() {
this.wayList.clear();
}
public void clearListAndSave() {
clearList();
WayConfig.getInstance().save();
}
public int size() {
return this.wayList.size();
}
public List<String> getWayNames() {
return (List<String>)this.wayList.stream().map(Way::name).collect(Collectors.toList());
}
public List<String> getWayNamesForServer(String server) {
return (List<String>)this.wayList.stream()
.filter(way -> way.server().equalsIgnoreCase(server))
.map(Way::name)
.collect(Collectors.toList());
}
public void setWays(List<Way> ways) {
this.wayList.clear();
this.wayList.addAll(ways);
}
public String getCurrentServer() {
if (mc.getNetworkHandler() == null || mc.getNetworkHandler().getServerInfo() == null) {
return "";
}
return (mc.getNetworkHandler().getServerInfo()).address;
}
private boolean isInFrontOfCamera(Vec3d worldPos) {
Camera camera = mc.gameRenderer.getCamera();
if (camera == null || !camera.isReady()) return false;
Vec3d cameraPos = camera.getCameraPos();
Vec3d toPoint = worldPos.subtract(cameraPos);
float yaw = camera.getYaw();
float pitch = camera.getPitch();
double yawRad = Math.toRadians(yaw);
double pitchRad = Math.toRadians(pitch);
double lookX = -Math.sin(yawRad) * Math.cos(pitchRad);
double lookY = -Math.sin(pitchRad);
double lookZ = Math.cos(yawRad) * Math.cos(pitchRad);
Vec3d lookDir = new Vec3d(lookX, lookY, lookZ);
return (lookDir.dotProduct(toPoint) > 0.0D);
}
@EventHandler
public void onRender2D(DrawEvent event) {
if (isEmpty() || mc.player == null || mc.world == null)
return;  if (mc.getNetworkHandler() == null || mc.getNetworkHandler().getServerInfo() == null)
return; 
String currentServer = getCurrentServer();
for (Way way : this.wayList) {
if (!way.server().equalsIgnoreCase(currentServer))
continue; 
Vec3d wayVec = way.pos().toCenterPos();
if (!isInFrontOfCamera(wayVec))
continue; 
Vec3d screenPos = Projection.worldSpaceToScreenSpace(wayVec);
if (screenPos.z <= 0.0D || screenPos.z >= 1.0D)
continue; 
double distance = mc.player.getEntityPos().distanceTo(wayVec);
String text = way.name() + " - " + way.name() + "m";
Font font = Fonts.BOLD;
float fontSize = 6.0F;
float textWidth = font.getWidth(text, fontSize);
float textHeight = font.getHeight(fontSize);
float padding = 3.0F;
float x = (float)screenPos.x - textWidth / 2.0F;
float y = (float)screenPos.y - textHeight / 2.0F;
Render2D.rect(x - padding, y - padding + 0.5F, textWidth + padding * 2.0F, textHeight + padding * 2.0F, -535620843, 2.0F);
font.drawCentered(text, (float)screenPos.x, y + 1.0F, fontSize, -1);
} 
}
}


