package royale.modules.impl.render;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.DataComponentTypes;
import org.joml.Vector4d;
import royale.events.api.EventHandler;
import royale.events.impl.DrawEvent;
import royale.events.impl.TickEvent;
import royale.events.impl.WorldLoadEvent;
import royale.events.impl.WorldRenderEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.BooleanSetting;
import royale.modules.module.setting.implement.ColorSetting;
import royale.modules.module.setting.implement.MultiSelectSetting;
import royale.modules.module.setting.implement.SelectSetting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.util.ColorUtil;
import royale.util.Instance;
import royale.util.math.Projection;
import royale.util.modules.esp.RwPrefix;
import royale.util.network.Network;
import royale.util.render.Render2D;
import royale.util.render.Render3D;
import royale.util.render.VisibilityUtil;
import royale.util.render.font.Fonts;
import royale.util.render.item.ItemRender;
import royale.util.repository.friend.FriendUtils;
import royale.util.string.PlayerInteractionHelper;
public class Esp extends ModuleStructure {
public static Esp getInstance() {
return (Esp)Instance.get(Esp.class);
}
private final Identifier TEXTURE = Identifier.of("royale", "textures/features/esp/container.png");
private final List<PlayerEntity> players = new ArrayList<>();
public final MultiSelectSetting entityType = (new MultiSelectSetting("Тип сущности", "Сущности, которые будут отображаться"))
.value(new String[] { "Player", "Item" }).selected(new String[] { "Player", "Item" });
private final MultiSelectSetting playerSetting = (new MultiSelectSetting("Настройки игрока", "Параметры отображения игроков"))
.value(new String[] { "Box", "Armor", "NameTags", "Hand Items"
}).selected(new String[] { "Box", "Armor", "NameTags", "Hand Items"
}).visible(() -> Boolean.valueOf(this.entityType.isSelected("Player")));
private final MultiSelectSetting itemSetting = (new MultiSelectSetting("Настройки предметов", "Параметры отображения предметов"))
.value(new String[] { "NameTags" }).selected(new String[] { "NameTags" })
.visible(() -> Boolean.valueOf(this.entityType.isSelected("Item")));
public final SelectSetting boxType = (new SelectSetting("Тип бокса", "Стиль рамки игрока"))
.value(new String[] { "Corner", "3D Box" }).selected("Corner")
.visible(() -> Boolean.valueOf(this.playerSetting.isSelected("Box")));
public final ColorSetting boxColor = (new ColorSetting("Цвет бокса", "Цвет выделения игрока"))
.value(-22016)
.visible(() -> Boolean.valueOf(this.playerSetting.isSelected("Box")));
public final ColorSetting friendColor = (new ColorSetting("Цвет друга", "Цвет выделения друзей"))
.value(-16711936)
.visible(() -> Boolean.valueOf(this.playerSetting.isSelected("Box")));
public final BooleanSetting flatBoxOutline = (new BooleanSetting("Контур", "Дополнительный контур для Corner-бокса"))
.visible(() -> Boolean.valueOf((this.playerSetting.isSelected("Box") && this.boxType.isSelected("Corner"))));
public final SliderSettings boxAlpha = (new SliderSettings("Прозрачность", "Прозрачность 3D-бокса"))
.setValue(1.0F).range(0.1F, 1.0F).visible(() -> Boolean.valueOf(this.boxType.isSelected("3D Box")));
private static final float DISTANCE = 128.0F;
private static final int GRAY_COLOR = -7829368;
private static final int WHITE_COLOR = -1;
public Esp() {
super("Boxes", "Подсветка игроков и предметов", ModuleCategory.RENDER);
settings(new Setting[] { (Setting)this.entityType, (Setting)this.playerSetting, (Setting)this.itemSetting, (Setting)this.boxType, (Setting)this.boxColor, (Setting)this.friendColor, (Setting)this.flatBoxOutline, (Setting)this.boxAlpha });
}
@EventHandler
public void onWorldLoad(WorldLoadEvent e) {
this.players.clear();
}
public void deactivate() {
this.players.clear();
}
@EventHandler
public void onTick(TickEvent e) {
this.players.clear();
if (mc.world != null) {
Objects.requireNonNull(this.players); mc.world.getPlayers().stream().filter(player -> (player != mc.player)).filter(player -> (player.getCustomName() == null || !player.getCustomName().getString().startsWith("Ghost_"))).forEach(this.players::add);
} 
}
@EventHandler
public void onWorldRender(WorldRenderEvent e) {
if (!this.entityType.isSelected("Player"))
return;  float tickDelta = e.getPartialTicks();
for (PlayerEntity player : this.players) {
if (player == null || player == mc.player || (
player.getCustomName() != null && player.getCustomName().getString().startsWith("Ghost_")) || 
!canRenderEntity((Entity)player) || 
!hasVisibleLine((Entity)player, tickDelta))
continue; 
double interpX = MathHelper.lerp(tickDelta, player.lastX, player.getX());
double interpY = MathHelper.lerp(tickDelta, player.lastY, player.getY());
double interpZ = MathHelper.lerp(tickDelta, player.lastZ, player.getZ());
Vec3d interpCenter = new Vec3d(interpX, interpY, interpZ);
float distance = (float)mc.gameRenderer.getCamera().getCameraPos().distanceTo(interpCenter);
if (distance < 1.0F || 
distance > 128.0F)
continue; 
boolean friend = FriendUtils.isFriend((Entity)player);
int baseColor = friend ? getFriendColor() : getClientColor();
int alpha = (int)(this.boxAlpha.getValue() * 255.0F);
int fillColor = baseColor & 0xFFFFFF | alpha << 24;
int outlineColor = baseColor | 0xFF000000;
if (this.boxType.isSelected("3D Box") && shouldRenderBoxes()) {
Box interpBox = player.getDimensions(player.getPose()).getBoxAt(interpX, interpY, interpZ);
Render3D.drawBox(interpBox, fillColor, 2.0F, true, true, true);
Render3D.drawBox(interpBox, outlineColor, 2.0F, true, false, true);
} 
} 
}
@EventHandler
public void onDraw(DrawEvent e) {
DrawContext context = e.getDrawContext();
float tickDelta = e.getPartialTicks();
float size = 5.5F;
if (this.entityType.isSelected("Player")) {
for (PlayerEntity player : this.players) {
if (player == null || player == mc.player || (
player.getCustomName() != null && player.getCustomName().getString().startsWith("Ghost_")) || 
!canRenderEntity((Entity)player) || 
!hasVisibleLine((Entity)player, tickDelta))
continue; 
Vector4d vec4d = Projection.getVector4D((Entity)player, tickDelta);
float distance = (float)mc.gameRenderer.getCamera().getCameraPos().distanceTo(player.getBoundingBox().getCenter());
boolean friend = FriendUtils.isFriend((Entity)player);
if (distance < 1.0F || 
distance > 128.0F || 
Projection.cantSee(vec4d))
continue; 
if (this.boxType.isSelected("Corner") && shouldRenderBoxes()) {
drawBox(friend, vec4d, player);
}
if (this.playerSetting.isSelected("Armor")) {
drawArmor(context, player, vec4d);
}
if (this.playerSetting.isSelected("Hand Items")) {
drawHands(context, player, vec4d, size);
}
if (this.playerSetting.isSelected("NameTags")) {
drawPlayerName(context, player, friend, Projection.centerX(vec4d), vec4d.y - 2.0D, size);
}
} 
}
List<Entity> entities = PlayerInteractionHelper.streamEntities().sorted(Comparator.comparing(ent -> { if (ent instanceof ItemEntity) { ItemEntity item = (ItemEntity)ent; if (item.getStack().getName().getContent().toString().equals("empty")); }  return Boolean.valueOf(false); })).toList();
for (Entity entity : entities) {
if (entity instanceof ItemEntity) { ItemEntity item = (ItemEntity)entity; if (!this.entityType.isSelected("Item") || 
!canRenderEntity((Entity)item) || 
!hasVisibleLine((Entity)item, tickDelta))
continue; 
if (!shouldRenderCustomItemLabels())
continue; 
Vector4d vec4d = Projection.getVector4D(entity, tickDelta);
ItemStack stack = item.getStack();
ContainerComponent compoundTag = (ContainerComponent)stack.get(DataComponentTypes.CONTAINER);
List<ItemStack> list = (compoundTag != null) ? compoundTag.stream().toList() : List.<ItemStack>of();
if (Projection.cantSee(vec4d))
continue; 
String text = item.getStack().getName().getString();
if (!list.isEmpty()) {
drawShulkerBox(context, stack, list, vec4d); continue;
} 
drawText(context, text, Projection.centerX(vec4d), vec4d.y, size); }
} 
}
private void drawPlayerName(DrawContext context, PlayerEntity player, boolean friend, double centerX, double startY, float size) {
String displayName;
StringBuilder extraInfo = new StringBuilder();
if (friend) extraInfo.append("[Friend] "); 
displayName = player.getDisplayName().getString();
RwPrefix.ParsedName parsed = RwPrefix.parseDisplayName(displayName);
String sphere = "";
ItemStack offHand = player.getStackInHand(Hand.OFF_HAND);
if (offHand.getItem().equals(Items.PLAYER_HEAD) || offHand.getItem().equals(Items.TOTEM_OF_UNDYING)) {
sphere = getSphere(offHand);
}
String prefixPart = "";
if (!parsed.prefix.isEmpty()) {
prefixPart = parsed.prefix + " ";
}
String namePart = parsed.name;
String clanPart = !parsed.clan.isEmpty() ? (" " + parsed.clan) : "";
String spherePart = sphere;
String extraPart = extraInfo.toString();
float hpValue = getHealth(player);
String healthPart = " " + getHealthString(hpValue) + "HP";
float extraWidth = extraPart.isEmpty() ? 0.0F : Fonts.TEST.getWidth(extraPart, size);
float prefixWidth = prefixPart.isEmpty() ? 0.0F : Fonts.TEST.getWidth(prefixPart, size);
float nameWidth = Fonts.TEST.getWidth(namePart, size);
float clanWidth = clanPart.isEmpty() ? 0.0F : Fonts.TEST.getWidth(clanPart, size);
float sphereWidth = spherePart.isEmpty() ? 0.0F : Fonts.TEST.getWidth(spherePart, size);
float healthWidth = Fonts.TEST.getWidth(healthPart, size);
float totalWidth = extraWidth + prefixWidth + nameWidth + clanWidth + sphereWidth + healthWidth;
float height = Fonts.TEST.getHeight(size);
float posX = (float)centerX - totalWidth / 2.0F;
float posY = (float)startY - height;
Render2D.rect(posX - 4.0F, posY - 1.25F, totalWidth + 8.0F, height + 2.0F, -2147483648, 2.0F);
float drawX = posX;
if (!extraPart.isEmpty()) {
Fonts.TEST.draw(extraPart, drawX, posY, size, friend ? getFriendColor() : -43691);
drawX += extraWidth;
} 
if (!prefixPart.isEmpty()) {
Fonts.TEST.draw(prefixPart, drawX, posY, size, -7829368);
drawX += prefixWidth;
} 
Fonts.TEST.draw(namePart, drawX, posY, size, -1);
drawX += nameWidth;
if (!clanPart.isEmpty()) {
Fonts.TEST.draw(clanPart, drawX, posY, size, -7829368);
drawX += clanWidth;
} 
if (!spherePart.isEmpty()) {
Fonts.TEST.draw(spherePart, drawX, posY, size, -7829368);
drawX += sphereWidth;
} 
Fonts.TEST.draw(healthPart, drawX, posY, size, getHealthColor(hpValue, player));
}
private void drawBox(boolean friend, Vector4d vec, PlayerEntity player) {
int client = friend ? getFriendColor() : getClientColor();
int black = Integer.MIN_VALUE;
float posX = (float)vec.x;
float posY = (float)vec.y;
float endPosX = (float)vec.z;
float endPosY = (float)vec.w;
float boxWidth = Math.max(2.0F, endPosX - posX);
float boxHeight = Math.max(2.0F, endPosY - posY);
float corner = Math.max(3.0F, Math.min(boxWidth, boxHeight) / 3.0F);
float line = 1.0F;
if (boxWidth < 8.0F || boxHeight < 8.0F) {
Render2D.outline(posX, posY, boxWidth, boxHeight, line, client);
return;
} 
Render2D.rect(posX, posY, corner, line, client);
Render2D.rect(posX, posY, line, corner, client);
Render2D.rect(posX, endPosY - corner, line, corner, client);
Render2D.rect(posX, endPosY - line, corner, line, client);
Render2D.rect(endPosX - corner, posY, corner, line, client);
Render2D.rect(endPosX - line, posY, line, corner, client);
Render2D.rect(endPosX - line, endPosY - corner, line, corner, client);
Render2D.rect(endPosX - corner, endPosY - line, corner, line, client);
if (this.flatBoxOutline.isValue()) {
float o = 1.0F;
Render2D.rect(posX - o, posY - o, corner + o, o, black);
Render2D.rect(posX - o, posY, o, corner + o, black);
Render2D.rect(posX - o, endPosY - corner - o, o, corner + o, black);
Render2D.rect(posX - o, endPosY, corner + o, o, black);
Render2D.rect(endPosX - corner, posY - o, corner + o, o, black);
Render2D.rect(endPosX, posY, o, corner + o, black);
Render2D.rect(endPosX, endPosY - corner - o, o, corner + o, black);
Render2D.rect(endPosX - corner, endPosY, corner + o, o, black);
} 
}
private void drawArmor(DrawContext context, PlayerEntity player, Vector4d vec) {
List<ItemStack> items = new ArrayList<>();
for (EquipmentSlot slot : EquipmentSlot.VALUES) {
ItemStack stack = player.getEquippedStack(slot);
if (!stack.isEmpty()) {
items.add(stack);
}
} 
float posX = (float)(Projection.centerX(vec) - (items.size() * 4.5F));
float posY = (float)(vec.y - 20.0D);
float offset = 0.0F;
for (ItemStack stack : items) {
ItemRender.drawItemWithContext(context, stack, posX + offset, posY, 0.5F, 1.0F);
offset += 11.0F;
} 
}
private void drawHands(DrawContext context, PlayerEntity player, Vector4d vec, float size) {
double posY = vec.w;
ItemStack mainHand = player.getStackInHand(Hand.MAIN_HAND);
ItemStack offHand = player.getStackInHand(Hand.OFF_HAND);
for (ItemStack stack : new ItemStack[] { mainHand, offHand }) {
if (!stack.isEmpty()) {
String text = stack.getName().getString();
posY += Fonts.TEST.getHeight(size) / 2.0D + 6.0D;
drawText(context, text, Projection.centerX(vec), posY, size);
} 
} 
}
private void drawShulkerBox(DrawContext context, ItemStack itemStack, List<ItemStack> stacks, Vector4d vec) {
int width = 176;
int height = 67;
int color = (((BlockItem)itemStack.getItem()).getBlock().getDefaultMapColor()).color | 0xFF000000;
float scale = 0.5F;
float scaledWidth = width * scale;
float scaledHeight = height * scale;
float drawX = (float)Projection.centerX(vec) - scaledWidth / 2.0F;
float drawY = (float)vec.y - scaledHeight - 2.0F;
Render2D.texture(this.TEXTURE, drawX, drawY, scaledWidth, scaledHeight, color);
Render2D.blur(drawX, drawY, 1.0F, 1.0F, 0.0F, 0.0F, ColorUtil.rgba(0, 0, 0, 0));
float itemScale = scale;
float itemStartX = drawX + 7.0F * scale;
float itemStartY = drawY + 6.0F * scale;
float itemSize = 18.0F * scale;
int col = 0;
int row = 0;
for (ItemStack stack : stacks) {
float itemX = itemStartX + col * itemSize;
float itemY = itemStartY + row * itemSize;
ItemRender.drawItemWithContext(context, stack, itemX, itemY, itemScale, 1.0F);
col++;
if (col >= 9) {
row++;
col = 0;
} 
} 
}
private void drawText(DrawContext context, String text, double startX, double startY, float size) {
String cleanText = RwPrefix.stripFormatting(text);
float width = Fonts.TEST.getWidth(cleanText, size);
float height = Fonts.TEST.getHeight(size);
float posX = (float)(startX - (width / 2.0F));
float posY = (float)startY - height;
Render2D.rect(posX - 4.0F, posY - 1.0F, width + 8.0F, height + 2.0F, -2147483648, 2.0F);
Fonts.TEST.draw(cleanText, posX, posY, size, -1);
}
private String getSphere(ItemStack stack) {
NbtComponent component = (NbtComponent)stack.get(DataComponentTypes.CUSTOM_DATA);
if (Network.isFunTime() && component != null) {
NbtCompound compound = component.copyNbt();
int tslevel = ((Integer)compound.getInt("tslevel").orElse(Integer.valueOf(0))).intValue();
if (tslevel != 0) {
String donItem = compound.getString("don-item").orElse("");
return " [" + donItem.replace("sphere-", "").toUpperCase() + "]";
} 
} 
return "";
}
private float getHealth(PlayerEntity player) {
return player.getHealth() + player.getAbsorptionAmount();
}
private String getHealthString(float hp) {
return String.format("%.1f", new Object[] { Float.valueOf(hp) }).replace(",", ".").replace(".0", "");
}
private int getHealthColor(float hp, PlayerEntity player) {
float maxHp = Math.max(1.0F, player.getMaxHealth() + player.getAbsorptionAmount());
float ratio = MathHelper.clamp(hp / maxHp, 0.0F, 1.0F);
return ColorUtil.interpolateColor(-43691, -11141291, ratio);
}
private int getFriendColor() {
return this.friendColor.getColorNoAlpha();
}
private int getClientColor() {
return this.boxColor.getColorNoAlpha();
}
private boolean canRenderEntity(Entity entity) {
if (mc.player == null || mc.world == null) {
return false;
}
return (entity != null && entity.isAlive() && !entity.isRemoved());
}
private boolean shouldRenderBoxes() {
return this.playerSetting.isSelected("Box");
}
public boolean shouldRenderCustomPlayerLabels() {
return (isState() && this.entityType.isSelected("Player") && this.playerSetting.isSelected("NameTags"));
}
public boolean shouldRenderCustomItemLabels() {
return (isState() && this.entityType.isSelected("Item") && this.itemSetting.isSelected("NameTags"));
}
private boolean hasVisibleLine(Entity entity, float tickDelta) {
if (mc.player == null || mc.world == null || mc.gameRenderer == null) {
return false;
}
Vec3d from = mc.gameRenderer.getCamera().getCameraPos();
Box interpolatedBox = getInterpolatedBox(entity, tickDelta);
Vec3d center = interpolatedBox.getCenter();
Vec3d eye = entity.getEyePos();
Vec3d top = new Vec3d(center.x, interpolatedBox.maxY, center.z);
double midY = (interpolatedBox.minY + interpolatedBox.maxY) * 0.5D;
if (VisibilityUtil.hasClearLine(mc, from, center)) {
return true;
}
if (VisibilityUtil.hasClearLine(mc, from, eye)) {
return true;
}
if (VisibilityUtil.hasClearLine(mc, from, top)) {
return true;
}
return (VisibilityUtil.hasClearLine(mc, from, new Vec3d(interpolatedBox.minX, midY, interpolatedBox.minZ)) || 
VisibilityUtil.hasClearLine(mc, from, new Vec3d(interpolatedBox.minX, midY, interpolatedBox.maxZ)) || 
VisibilityUtil.hasClearLine(mc, from, new Vec3d(interpolatedBox.maxX, midY, interpolatedBox.minZ)) || 
VisibilityUtil.hasClearLine(mc, from, new Vec3d(interpolatedBox.maxX, midY, interpolatedBox.maxZ)));
}
private Box getInterpolatedBox(Entity entity, float tickDelta) {
Vec3d lerpedPos = entity.getLerpedPos(tickDelta);
Vec3d currentPos = entity.getEntityPos();
return entity.getBoundingBox().offset(lerpedPos.subtract(currentPos));
}
}




