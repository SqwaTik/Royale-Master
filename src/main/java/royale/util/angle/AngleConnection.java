package royale.util.angle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import royale.IMinecraft;
import royale.events.api.EventHandler;
import royale.events.api.EventManager;
import royale.events.api.events.Event;
import royale.events.impl.PacketEvent;
import royale.events.impl.PlayerVelocityStrafeEvent;
import royale.events.impl.RotationUpdateEvent;
import royale.events.impl.TickEvent;
import royale.modules.module.ModuleStructure;
import royale.util.math.TaskPriority;
import royale.util.math.TaskProcessor;
public class AngleConnection
implements IMinecraft
{
public static AngleConnection INSTANCE = new AngleConnection(); private AngleConstructor lastRotationPlan;
public AngleConstructor getLastRotationPlan() {
return this.lastRotationPlan;
} public Angle currentAngle; private final TaskProcessor<AngleConstructor> rotationPlanTaskProcessor = new TaskProcessor(); private Angle previousAngle; public TaskProcessor<AngleConstructor> getRotationPlanTaskProcessor() { return this.rotationPlanTaskProcessor; }
public Angle getCurrentAngle() { return this.currentAngle; } public Angle getPreviousAngle() {
return this.previousAngle;
} private Angle serverAngle = Angle.DEFAULT; private Angle fakeAngle; public Angle getServerAngle() { return this.serverAngle; }
public Angle getFakeAngle() { return this.fakeAngle; } private boolean returning = false; public boolean isReturning() {
return this.returning;
}
public AngleConnection() {
EventManager.register(this);
}
public void setRotation(Angle value) {
if (value == null) {
this.previousAngle = (this.currentAngle != null) ? this.currentAngle : MathAngle.cameraAngle();
} else {
this.previousAngle = this.currentAngle;
} 
this.currentAngle = value;
}
public Angle getRotation() {
return (this.currentAngle != null) ? this.currentAngle : MathAngle.cameraAngle();
}
public Angle getFakeRotation() {
if (this.fakeAngle != null) {
return this.fakeAngle;
}
return (this.currentAngle != null) ? this.currentAngle : ((this.previousAngle != null) ? this.previousAngle : MathAngle.cameraAngle());
}
public void setFakeRotation(Angle angle) {
this.fakeAngle = angle;
}
public Angle getPreviousRotation() {
return (this.currentAngle != null && this.previousAngle != null) ? this.previousAngle : new Angle(mc.player.lastYaw, mc.player.lastPitch);
}
public Angle getMoveRotation() {
AngleConstructor rotationPlan = getCurrentRotationPlan();
return (this.currentAngle != null && rotationPlan != null && rotationPlan.isMoveCorrection()) ? this.currentAngle : MathAngle.cameraAngle();
}
public AngleConstructor getCurrentRotationPlan() {
return (this.rotationPlanTaskProcessor.fetchActiveTaskValue() != null) ? (AngleConstructor)this.rotationPlanTaskProcessor.fetchActiveTaskValue() : this.lastRotationPlan;
}
public void rotateTo(Angle.VecRotation vecRotation, LivingEntity entity, int reset, AngleConfig configurable, TaskPriority taskPriority, ModuleStructure provider) {
rotateTo(configurable.createRotationPlan(vecRotation.getAngle(), vecRotation.getVec(), (Entity)entity, reset), taskPriority, provider);
}
public void rotateTo(Angle angle, int reset, AngleConfig configurable, TaskPriority taskPriority, ModuleStructure provider) {
rotateTo(configurable.createRotationPlan(angle, angle.toVector(), null, reset), taskPriority, provider);
}
public void rotateTo(Angle angle, AngleConfig configurable, TaskPriority taskPriority, ModuleStructure provider) {
rotateTo(configurable.createRotationPlan(angle, angle.toVector(), null, 1), taskPriority, provider);
}
public void rotateTo(AngleConstructor plan, TaskPriority taskPriority, ModuleStructure provider) {
this.returning = false;
this.rotationPlanTaskProcessor.addTask(new TaskProcessor.Task(1, taskPriority.getPriority(), provider, plan));
}
public void update() {
AngleConstructor activePlan = getCurrentRotationPlan();
if (activePlan == null) {
if (this.currentAngle != null && this.returning) {
Angle cameraAngle = MathAngle.cameraAngle();
double diff = computeRotationDifference(this.currentAngle, cameraAngle);
if (diff < 0.5D) {
setRotation(null);
this.lastRotationPlan = null;
this.returning = false;
} else {
float speed = 0.25F;
float distanceFactor = Math.min(1.0F, (float)diff / 30.0F);
speed += 0.4F * distanceFactor;
float yawDiff = MathHelper.wrapDegrees(cameraAngle.getYaw() - this.currentAngle.getYaw());
float newYaw = this.currentAngle.getYaw() + yawDiff * speed;
float newPitch = MathHelper.lerp(speed, this.currentAngle.getPitch(), cameraAngle.getPitch());
setRotation((new Angle(newYaw, newPitch)).adjustSensitivity());
} 
} 
return;
} 
this.returning = false;
Angle clientAngle = MathAngle.cameraAngle();
if (this.lastRotationPlan != null) {
double differenceFromCurrentToPlayer = computeRotationDifference(this.serverAngle, clientAngle);
if (activePlan.getTicksUntilReset() <= this.rotationPlanTaskProcessor.tickCounter && differenceFromCurrentToPlayer < activePlan.getResetThreshold()) {
setRotation(null);
this.lastRotationPlan = null;
this.rotationPlanTaskProcessor.tickCounter = 0;
return;
} 
} 
Angle newAngle = activePlan.nextRotation((this.currentAngle != null) ? this.currentAngle : clientAngle, (this.rotationPlanTaskProcessor.fetchActiveTaskValue() == null)).adjustSensitivity();
setRotation(newAngle);
this.lastRotationPlan = activePlan;
this.rotationPlanTaskProcessor.tick(1);
}
public static double computeRotationDifference(Angle a, Angle b) {
return Math.hypot(Math.abs(computeAngleDifference(a.getYaw(), b.getYaw())), Math.abs(a.getPitch() - b.getPitch()));
}
public static float computeAngleDifference(float a, float b) {
return MathHelper.wrapDegrees(a - b);
}
private Vec3d fixVelocity(Vec3d currVelocity, Vec3d movementInput, float speed) {
if (this.currentAngle != null) {
float yaw = this.currentAngle.getYaw();
double d = movementInput.lengthSquared();
if (d < 1.0E-7D) {
return Vec3d.ZERO;
}
Vec3d vec3d = ((d > 1.0D) ? movementInput.normalize() : movementInput).multiply(speed);
float f = MathHelper.sin((yaw * 0.017453292F));
float g = MathHelper.cos((yaw * 0.017453292F));
return new Vec3d(vec3d.getX() * g - vec3d.getZ() * f, vec3d.getY(), vec3d.getZ() * g + vec3d.getX() * f);
} 
return currVelocity;
}
public void clear() {
this.rotationPlanTaskProcessor.activeTasks.clear();
}
public void startReturning() {}
public void reset() {
this.currentAngle = null;
this.previousAngle = null;
this.fakeAngle = null;
this.lastRotationPlan = null;
this.rotationPlanTaskProcessor.tickCounter = 0;
}
@EventHandler
public void onPlayerVelocityStrafe(PlayerVelocityStrafeEvent e) {
AngleConstructor currentRotationPlan = getCurrentRotationPlan();
if (currentRotationPlan != null && currentRotationPlan.isMoveCorrection()) {
e.setVelocity(fixVelocity(e.getVelocity(), e.getMovementInput(), e.getSpeed()));
}
}
@EventHandler
public void onTick(TickEvent e) {
EventManager.callEvent((Event)new RotationUpdateEvent((byte)0));
update();
EventManager.callEvent((Event)new RotationUpdateEvent((byte)2));
}
@EventHandler
public void onPacket(PacketEvent event) {
// Byte code:
//   0: aload_1
//   1: invokevirtual isCancelled : ()Z
//   4: ifne -> 136
//   7: aload_1
//   8: invokevirtual getPacket : ()Lnet/minecraft/Packet;
//   11: dup
//   12: invokestatic requireNonNull : (Ljava/lang/Object;)Ljava/lang/Object;
//   15: pop
//   16: astore #4
//   18: iconst_0
//   19: istore #5
//   21: aload #4
//   23: iload #5
//   25: <illegal opcode> typeSwitch : (Ljava/lang/Object;I)I
//   30: lookupswitch default -> 133, 0 -> 56, 1 -> 99
//   56: aload #4
//   58: checkcast net/minecraft/PlayerMoveC2SPacket
//   61: astore_2
//   62: aload_2
//   63: invokevirtual changesLook : ()Z
//   66: ifne -> 75
//   69: iconst_1
//   70: istore #5
//   72: goto -> 21
//   75: aload_0
//   76: new royale/util/angle/Angle
//   79: dup
//   80: aload_2
//   81: fconst_1
//   82: invokevirtual getYaw : (F)F
//   85: aload_2
//   86: fconst_1
//   87: invokevirtual getPitch : (F)F
//   90: invokespecial <init> : (FF)V
//   93: putfield serverAngle : Lroyale/util/angle/Angle;
//   96: goto -> 136
//   99: aload #4
//   101: checkcast net/minecraft/PlayerPositionLookS2CPacket
//   104: astore_3
//   105: aload_0
//   106: new royale/util/angle/Angle
//   109: dup
//   110: aload_3
//   111: invokevirtual change : ()Lnet/minecraft/EntityPosition;
//   114: invokevirtual yaw : ()F
//   117: aload_3
//   118: invokevirtual change : ()Lnet/minecraft/EntityPosition;
//   121: invokevirtual pitch : ()F
//   124: invokespecial <init> : (FF)V
//   127: putfield serverAngle : Lroyale/util/angle/Angle;
//   130: goto -> 136
//   133: goto -> 136
//   136: return
// Line number table:
//   Java source line number -> byte code offset
//   #208	-> 0
//   #209	-> 56
//   #210	-> 75
//   #211	-> 99
//   #212	-> 105
//   #214	-> 133
//   #216	-> 136
// Local variable table:
//   start	length	slot	name	descriptor
//   62	37	2	player	Lnet/minecraft/PlayerMoveC2SPacket;
//   105	28	3	player	Lnet/minecraft/PlayerPositionLookS2CPacket;
//   0	137	0	this	Lroyale/util/angle/AngleConnection;
//   0	137	1	event	Lroyale/events/impl/PacketEvent;
}
}


