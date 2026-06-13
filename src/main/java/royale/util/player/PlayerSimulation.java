package royale.util.player;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import net.minecraft.util.PlayerInput;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.BlockView;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.block.LadderBlock;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluid;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import royale.IMinecraft;
import royale.util.move.MoveUtil;
public class PlayerSimulation
implements Simulation, IMinecraft
{
public final PlayerEntity player;
public final SimulatedPlayerInput input;
public Vec3d pos;
public Vec3d velocity;
public Box boundingBox;
public float yaw;
public float pitch;
public boolean sprinting;
public float fallDistance;
public int jumpingCooldown;
public boolean isJumping;
public boolean isFallFlying;
public boolean onGround;
public boolean horizontalCollision;
public boolean verticalCollision;
public boolean touchingWater;
public boolean isSwimming;
public boolean submergedInWater;
private final Object2DoubleMap<TagKey<Fluid>> fluidHeight;
private final HashSet<TagKey<Fluid>> submergedFluidTag;
private int simulatedTicks = 0;
private boolean clipLedged = false;
private static final double STEP_HEIGHT = 0.5D;
public PlayerSimulation(PlayerEntity player, SimulatedPlayerInput input, Vec3d pos, Vec3d velocity, Box boundingBox, float yaw, float pitch, boolean sprinting, float fallDistance, int jumpingCooldown, boolean isJumping, boolean isFallFlying, boolean onGround, boolean horizontalCollision, boolean verticalCollision, boolean touchingWater, boolean isSwimming, boolean submergedInWater, Object2DoubleMap<TagKey<Fluid>> fluidHeight, HashSet<TagKey<Fluid>> submergedFluidTag) {
this.player = player;
this.input = input;
this.pos = pos;
this.velocity = velocity;
this.boundingBox = boundingBox;
this.yaw = yaw;
this.pitch = pitch;
this.sprinting = sprinting;
this.fallDistance = fallDistance;
this.jumpingCooldown = jumpingCooldown;
this.isJumping = isJumping;
this.isFallFlying = isFallFlying;
this.onGround = onGround;
this.horizontalCollision = horizontalCollision;
this.verticalCollision = verticalCollision;
this.touchingWater = touchingWater;
this.isSwimming = isSwimming;
this.submergedInWater = submergedInWater;
this.fluidHeight = fluidHeight;
this.submergedFluidTag = submergedFluidTag;
}
public static PlayerSimulation simulateLocalPlayer(int ticks) {
PlayerSimulation simulatedPlayer = fromClientPlayer(SimulatedPlayerInput.fromClientPlayer(mc.player.input.playerInput));
for (int i = 0; i < ticks; i++) {
simulatedPlayer.tick();
}
return simulatedPlayer;
}
public static PlayerSimulation simulateOtherPlayer(PlayerEntity player, int ticks) {
PlayerSimulation simulatedPlayer = fromOtherPlayer(player, SimulatedPlayerInput.guessInput(player));
for (int i = 0; i < ticks; i++) {
simulatedPlayer.tick();
}
return simulatedPlayer;
}
public static PlayerSimulation fromClientPlayer(SimulatedPlayerInput input) {
ClientPlayerEntity player = mc.player;
return new PlayerSimulation((PlayerEntity)player, input, player
.getEntityPos(), player
.getVelocity(), player
.getBoundingBox(), player
.getYaw(), player
.getPitch(), player
.isSprinting(), (float)player.fallDistance, player.jumpingCooldown, player.jumping, player
.isGliding(), player
.isOnGround(), player.horizontalCollision, player.verticalCollision, player
.isTouchingWater(), player
.isSwimming(), player
.isSubmergedInWater(), (Object2DoubleMap<TagKey<Fluid>>)new Object2DoubleArrayMap(player.fluidHeight), new HashSet<>(player.submergedFluidTag));
}
public static PlayerSimulation fromOtherPlayer(PlayerEntity player, SimulatedPlayerInput input) {
return new PlayerSimulation(player, input, player
.getEntityPos(), player
.getEntityPos().subtract(new Vec3d(player.lastX, player.lastY, player.lastZ)), player
.getBoundingBox(), player
.getYaw(), player
.getPitch(), player
.isSprinting(), (float)player.fallDistance, player.jumpingCooldown, player.jumping, player
.isGliding(), player
.isOnGround(), player.horizontalCollision, player.verticalCollision, player
.isTouchingWater(), player
.isSwimming(), player
.isSubmergedInWater(), (Object2DoubleMap<TagKey<Fluid>>)new Object2DoubleArrayMap(player.fluidHeight), new HashSet<>(player.submergedFluidTag));
}
public Vec3d pos() {
return this.player.getEntityPos();
}
public void tick() {
this.simulatedTicks++;
this.clipLedged = false;
if (this.pos.y <= -70.0D) {
return;
}
this.input.update();
checkWaterState();
updateSubmergedInWaterState();
updateSwimming();
if (this.jumpingCooldown > 0) {
this.jumpingCooldown--;
}
this.isJumping = this.input.playerInput.jump();
double newX = this.velocity.x;
double newY = this.velocity.y;
double newZ = this.velocity.z;
if (Math.abs(this.velocity.x) < 0.003D) newX = 0.0D; 
if (Math.abs(this.velocity.y) < 0.003D) newY = 0.0D; 
if (Math.abs(this.velocity.z) < 0.003D) newZ = 0.0D; 
if (this.onGround) {
this.isFallFlying = false;
}
this.velocity = new Vec3d(newX, newY, newZ);
if (this.isJumping) {
double fluidLevel = isInLava() ? getFluidHeight(FluidTags.LAVA) : getFluidHeight(FluidTags.WATER);
boolean inWater = (isTouchingWater() && fluidLevel > 0.0D);
double swimHeight = getSwimHeight();
if (inWater && (!this.onGround || fluidLevel > swimHeight)) {
swimUpward(FluidTags.WATER);
} else if (isInLava() && (!this.onGround || fluidLevel > swimHeight)) {
swimUpward(FluidTags.LAVA);
} else if ((this.onGround || (inWater && fluidLevel <= swimHeight)) && this.jumpingCooldown == 0) {
jump();
if (this.player.equals(mc.player)) {
this.jumpingCooldown = 10;
}
} 
} 
float sidewaysSpeed = this.input.movementSideways * 0.98F;
float forwardSpeed = this.input.movementForward * 0.98F;
float upwardsSpeed = 0.0F;
if (hasStatusEffect(StatusEffects.SLOW_FALLING) || hasStatusEffect(StatusEffects.LEVITATION)) {
onLanding();
}
travel(new Vec3d(sidewaysSpeed, upwardsSpeed, forwardSpeed));
}
private void travel(Vec3d movementInput) {
if (this.isSwimming && !this.player.hasVehicle()) {
double g = (getRotationVector()).y;
double h = (g < -0.2D) ? 0.085D : 0.06D;
BlockPos posAbove = new BlockPos(MathHelper.floor(this.pos.x), MathHelper.floor(this.pos.y + 1.0D - 0.1D), MathHelper.floor(this.pos.z));
if (g <= 0.0D || this.input.playerInput.jump() || 
!this.player.getEntityWorld().getBlockState(posAbove).getFluidState().isEmpty()) {
this.velocity = this.velocity.add(0.0D, (g - this.velocity.y) * h, 0.0D);
}
} 
double beforeTravelVelocityY = this.velocity.y;
double d = 0.08D;
boolean falling = (this.velocity.y <= 0.0D);
if (this.velocity.y <= 0.0D && hasStatusEffect(StatusEffects.SLOW_FALLING)) {
d = 0.01D;
onLanding();
} 
if (isTouchingWater() && this.player.shouldSwimInFluids()) {
double e = this.pos.y;
float f = isSprinting() ? 0.9F : 0.8F;
float g = 0.02F;
float h = (float)getAttributeValue(EntityAttributes.WATER_MOVEMENT_EFFICIENCY);
if (!this.onGround) {
h *= 0.5F;
}
if (h > 0.0F) {
f += (0.54600006F - f) * h / 3.0F;
g += (getMovementSpeed() - g) * h / 3.0F;
} 
if (hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
f = 0.96F;
}
updateVelocity(g, movementInput);
move(this.velocity);
Vec3d tempVel = this.velocity;
if (this.horizontalCollision && isClimbing()) {
tempVel = new Vec3d(tempVel.x, 0.2D, tempVel.z);
}
this.velocity = tempVel.multiply(f, 0.8D, f);
Vec3d vec3d2 = this.player.applyFluidMovingSpeed(d, falling, this.velocity);
this.velocity = vec3d2;
if (this.horizontalCollision && doesNotCollide(vec3d2.x, vec3d2.y + 0.6D - this.pos.y + e, vec3d2.z)) {
this.velocity = new Vec3d(vec3d2.x, 0.3D, vec3d2.z);
}
} else if (isInLava() && this.player.shouldSwimInFluids()) {
double e = this.pos.y;
updateVelocity(0.02F, movementInput);
move(this.velocity);
if (getFluidHeight(FluidTags.LAVA) <= getSwimHeight()) {
this.velocity = this.velocity.multiply(0.5D, 0.8D, 0.5D);
this.velocity = this.player.applyFluidMovingSpeed(d, falling, this.velocity);
} else {
this.velocity = this.velocity.multiply(0.5D);
} 
if (!this.player.hasNoGravity()) {
this.velocity = this.velocity.add(0.0D, -d / 4.0D, 0.0D);
}
if (this.horizontalCollision && doesNotCollide(this.velocity.x, this.velocity.y + 0.6D - this.pos.y + e, this.velocity.z)) {
this.velocity = new Vec3d(this.velocity.x, 0.3D, this.velocity.z);
}
} else if (this.isFallFlying) {
Vec3d e = this.velocity;
if (e.y > -0.5D) {
this.fallDistance = 1.0F;
}
Vec3d vec3d3 = getRotationVector();
float f = this.pitch * 0.017453292F;
double g = Math.sqrt(vec3d3.x * vec3d3.x + vec3d3.z * vec3d3.z);
double horizontalSpeed = this.velocity.horizontalLength();
double i = vec3d3.length();
float j = MathHelper.cos(f);
j = (float)(j * j * Math.min(1.0D, i / 0.4D));
e = this.velocity.add(0.0D, d * (-1.0D + j * 0.75D), 0.0D);
if (e.y < 0.0D && g > 0.0D) {
double k = e.y * -0.1D * j;
e = e.add(vec3d3.x * k / g, k, vec3d3.z * k / g);
} 
if (f < 0.0F && g > 0.0D) {
double k = horizontalSpeed * -MathHelper.sin(f) * 0.04D;
e = e.add(-vec3d3.x * k / g, k * 3.2D, -vec3d3.z * k / g);
} 
if (g > 0.0D) {
e = e.add((vec3d3.x / g * horizontalSpeed - e.x) * 0.1D, 0.0D, (vec3d3.z / g * horizontalSpeed - e.z) * 0.1D);
}
this.velocity = e.multiply(0.99D, 0.98D, 0.99D);
move(this.velocity);
} else {
BlockPos blockPos = getVelocityAffectingPos();
float p = this.player.getEntityWorld().getBlockState(blockPos).getBlock().getSlipperiness();
float f = this.onGround ? (p * 0.91F) : 0.91F;
Vec3d vec3d6 = applyMovementInput(movementInput, p);
double q = vec3d6.y;
if (hasStatusEffect(StatusEffects.LEVITATION)) {
StatusEffectInstance levitation = getStatusEffect(StatusEffects.LEVITATION);
if (levitation != null) {
q += (0.05D * (levitation.getAmplifier() + 1) - vec3d6.y) * 0.2D;
}
} else if (this.player.getEntityWorld().isClient() && !this.player.getEntityWorld().isChunkLoaded(blockPos)) {
q = (this.pos.y > this.player.getEntityWorld().getBottomY()) ? -0.1D : 0.0D;
} else if (!this.player.hasNoGravity()) {
q -= d;
} 
if (this.player.hasNoDrag()) {
this.velocity = new Vec3d(vec3d6.x, q, vec3d6.z);
} else {
this.velocity = new Vec3d(vec3d6.x * f, q * 0.9800000190734863D, vec3d6.z * f);
} 
} 
if ((this.player.getAbilities()).flying && !this.player.hasVehicle()) {
this.velocity = new Vec3d(this.velocity.x, beforeTravelVelocityY * 0.6D, this.velocity.z);
onLanding();
} 
}
private Vec3d applyMovementInput(Vec3d movementInput, float slipperiness) {
updateVelocity(getMovementSpeed(slipperiness), movementInput);
this.velocity = applyClimbingSpeed(this.velocity);
move(this.velocity);
Vec3d result = this.velocity;
BlockPos posBlock = posToBlockPos(this.pos);
BlockState state = getState(posBlock);
if ((this.horizontalCollision || this.isJumping) && (
isClimbing() || (state != null && state.isOf(Blocks.POWDER_SNOW) && 
PowderSnowBlock.canWalkOnPowderSnow((Entity)this.player)))) {
result = new Vec3d(result.x, 0.2D, result.z);
}
return result;
}
private void updateVelocity(float speed, Vec3d movementInput) {
Vec3d vec = Entity.movementInputToVelocity(movementInput, speed, this.yaw);
this.velocity = this.velocity.add(vec);
}
private float getMovementSpeed(float slipperiness) {
return this.onGround ? (getMovementSpeed() * 0.21600002F / slipperiness * slipperiness * slipperiness) : 
getAirStrafingSpeed();
}
private float getAirStrafingSpeed() {
float speed = 0.02F;
if (this.input.playerInput.sprint()) {
return speed + 0.006F;
}
return speed;
}
private float getMovementSpeed() {
return 0.1F;
}
private void move(Vec3d movement) {
Vec3d modifiedMovement = movement;
modifiedMovement = adjustMovementForSneaking(modifiedMovement);
Vec3d adjustedMovement = adjustMovementForCollisions(modifiedMovement);
if (adjustedMovement.lengthSquared() > 1.0E-7D) {
this.pos = this.pos.add(adjustedMovement);
this.boundingBox = this.player.getDimensions(this.player.getPose()).getBoxAt(this.pos);
} 
boolean xCollision = !MathHelper.approximatelyEquals(movement.x, adjustedMovement.x);
boolean zCollision = !MathHelper.approximatelyEquals(movement.z, adjustedMovement.z);
this.horizontalCollision = (xCollision || zCollision);
this.verticalCollision = (movement.y != adjustedMovement.y);
this.onGround = (this.verticalCollision && movement.y < 0.0D);
if (!isTouchingWater()) {
checkWaterState();
}
if (this.onGround) {
onLanding();
} else if (movement.y < 0.0D) {
this.fallDistance -= (float)movement.y;
} 
Vec3d currentVel = this.velocity;
if (this.horizontalCollision || this.verticalCollision)
this
.velocity = new Vec3d(xCollision ? 0.0D : currentVel.x, this.onGround ? 0.0D : currentVel.y, zCollision ? 0.0D : currentVel.z); 
}
private Vec3d adjustMovementForCollisions(Vec3d movement) {
Vec3d adjusted;
Box box = (new Box(-0.3D, 0.0D, -0.3D, 0.3D, 1.8D, 0.3D)).offset(this.pos);
List<VoxelShape> collisionShapes = Collections.emptyList();
if (movement.lengthSquared() == 0.0D) {
adjusted = movement;
} else {
adjusted = Entity.adjustMovementForCollisions((Entity)this.player, movement, box, this.player.getEntityWorld(), collisionShapes);
} 
boolean xCollide = (movement.x != adjusted.x);
boolean yCollide = (movement.y != adjusted.y);
boolean zCollide = (movement.z != adjusted.z);
boolean stepPossible = (this.onGround || (yCollide && movement.y < 0.0D));
if (this.player.getStepHeight() > 0.0F && stepPossible && (xCollide || zCollide)) {
Vec3d stepAdjust = Entity.adjustMovementForCollisions((Entity)this.player, new Vec3d(movement.x, this.player
.getStepHeight(), movement.z), box, this.player
.getEntityWorld(), collisionShapes);
Vec3d stepOffset = Entity.adjustMovementForCollisions((Entity)this.player, new Vec3d(0.0D, this.player
.getStepHeight(), 0.0D), box
.stretch(movement.x, 0.0D, movement.z), this.player.getEntityWorld(), collisionShapes);
Vec3d combined = Entity.adjustMovementForCollisions((Entity)this.player, new Vec3d(movement.x, 0.0D, movement.z), box.offset(stepOffset), this.player.getEntityWorld(), collisionShapes).add(stepOffset);
if (stepOffset.y < this.player.getStepHeight() && combined.horizontalLengthSquared() > stepAdjust.horizontalLengthSquared()) {
stepAdjust = combined;
}
if (stepAdjust.horizontalLengthSquared() > adjusted.horizontalLengthSquared()) {
return stepAdjust.add(Entity.adjustMovementForCollisions((Entity)this.player, new Vec3d(0.0D, -stepAdjust.y + movement.y, 0.0D), box
.offset(stepAdjust), this.player.getEntityWorld(), collisionShapes));
}
} 
return adjusted;
}
private void onLanding() {
this.fallDistance = 0.0F;
}
public void jump() {
this.velocity = this.velocity.add(0.0D, getJumpVelocity() - this.velocity.y, 0.0D);
if (isSprinting()) {
float rad = (float)Math.toRadians(this.yaw);
this.velocity = this.velocity.add(-MathHelper.sin(rad) * 0.2D, 0.0D, MathHelper.cos(rad) * 0.2D);
} 
}
private Vec3d applyClimbingSpeed(Vec3d motion) {
if (!isClimbing()) {
return motion;
}
onLanding();
double clampedX = MathHelper.clamp(motion.x, -0.15000000596046448D, 0.15000000596046448D);
double clampedZ = MathHelper.clamp(motion.z, -0.15000000596046448D, 0.15000000596046448D);
double clampedY = Math.max(motion.y, -0.15000000596046448D);
if (clampedY < 0.0D && !getState(posToBlockPos(this.pos)).isOf(Blocks.SCAFFOLDING) && this.player.isHoldingOntoLadder()) {
clampedY = 0.0D;
}
return new Vec3d(clampedX, clampedY, clampedZ);
}
public boolean isClimbing() {
BlockPos posBlock = posToBlockPos(this.pos);
BlockState state = getState(posBlock);
if (state.isIn(BlockTags.CLIMBABLE))
return true; 
return (state.getBlock() instanceof TrapdoorBlock && canEnterTrapdoor(posBlock, state));
}
private boolean canEnterTrapdoor(BlockPos pos, BlockState state) {
if (!((Boolean)state.get((Property)TrapdoorBlock.OPEN)).booleanValue()) {
return false;
}
BlockState below = this.player.getEntityWorld().getBlockState(pos.down());
return (below.isOf(Blocks.LADDER) && ((Direction)below.get((Property)LadderBlock.FACING)).equals(state.get((Property)TrapdoorBlock.FACING)));
}
private Vec3d adjustMovementForSneaking(Vec3d movement) {
if (movement.y <= 0.0D && isStandingOnSurface()) {
double dx = movement.x;
double dz = movement.z;
double step = 0.05D;
while (dx != 0.0D && this.player.getEntityWorld().isSpaceEmpty((Entity)this.player, this.boundingBox.offset(dx, -0.5D, 0.0D))) {
if (dx < step && dx >= -step) {
dx = 0.0D;
break;
} 
dx += (dx > 0.0D) ? -step : step;
} 
while (dz != 0.0D && this.player.getEntityWorld().isSpaceEmpty((Entity)this.player, this.boundingBox.offset(0.0D, -0.5D, dz))) {
if (dz < step && dz >= -step) {
dz = 0.0D;
break;
} 
dz += (dz > 0.0D) ? -step : step;
} 
while (dx != 0.0D && dz != 0.0D && this.player.getEntityWorld().isSpaceEmpty((Entity)this.player, this.boundingBox.offset(dx, -0.5D, dz))) {
dx = (dx < step && dx >= -step) ? 0.0D : ((dx > 0.0D) ? (dx - step) : (dx + step));
if (dz < step && dz >= -step) {
dz = 0.0D;
break;
} 
dz += (dz > 0.0D) ? -step : step;
} 
if (movement.x != dx || movement.z != dz) {
this.clipLedged = true;
}
if (shouldClipAtLedge()) {
movement = new Vec3d(dx, movement.y, dz);
}
} 
return movement;
}
protected boolean shouldClipAtLedge() {
return (this.input.playerInput.sneak() || this.input.forceSafeWalk);
}
private boolean isStandingOnSurface() {
return (this.onGround || (this.fallDistance < 0.5D && 
!this.player.getEntityWorld().isSpaceEmpty((Entity)this.player, this.boundingBox.offset(0.0D, this.fallDistance - 0.5D, 0.0D))));
}
private boolean isSprinting() {
return this.sprinting;
}
private float getJumpVelocity() {
return 0.42F * getJumpVelocityMultiplier() + getJumpBoostVelocityModifier();
}
private float getJumpBoostVelocityModifier() {
if (hasStatusEffect(StatusEffects.JUMP_BOOST)) {
StatusEffectInstance boost = getStatusEffect(StatusEffects.JUMP_BOOST);
return 0.1F * (boost.getAmplifier() + 1);
} 
return 0.0F;
}
private float getJumpVelocityMultiplier() {
float multiplier1 = 0.0F;
Block block = getState(posToBlockPos(this.pos)).getBlock();
if (block != null) {
multiplier1 = block.getJumpVelocityMultiplier();
}
float multiplier2 = 0.0F;
Block block2 = getState(getVelocityAffectingPos()).getBlock();
if (block2 != null) {
multiplier2 = block2.getJumpVelocityMultiplier();
}
return (multiplier1 == 1.0F) ? multiplier2 : multiplier1;
}
private boolean doesNotCollide(double offsetX, double offsetY, double offsetZ) {
return doesNotCollide(this.boundingBox.offset(offsetX, offsetY, offsetZ));
}
private boolean doesNotCollide(Box box) {
return (this.player.getEntityWorld().isSpaceEmpty((Entity)this.player, box) && !this.player.getEntityWorld().containsFluid(box));
}
private void swimUpward(TagKey<Fluid> fluidTag) {
this.velocity = this.velocity.add(0.0D, 0.03999999910593033D, 0.0D);
}
private BlockPos getVelocityAffectingPos() {
return BlockPos.ofFloored(this.pos.x, this.boundingBox.minY - 0.5000001D, this.pos.z);
}
private double getSwimHeight() {
return (this.player.getStandingEyeHeight() < 0.4D) ? 0.0D : 0.4D;
}
private boolean isTouchingWater() {
return this.touchingWater;
}
public boolean isInLava() {
return (this.fluidHeight.getDouble(FluidTags.LAVA) > 0.0D);
}
private void checkWaterState() {
Entity vehicle = this.player.getVehicle();
if (vehicle instanceof AbstractBoatEntity) { AbstractBoatEntity boat = (AbstractBoatEntity)vehicle;
if (!boat.isSubmergedInWater()) {
this.touchingWater = false;
return;
}  }
if (updateMovementInFluid(FluidTags.WATER, 0.014D)) {
onLanding();
this.touchingWater = true;
} else {
this.touchingWater = false;
} 
}
private void updateSwimming() {
if (this.isSwimming) {
this.isSwimming = (isSprinting() && isTouchingWater() && !this.player.hasVehicle());
} else {
this
.isSwimming = (isSprinting() && isSubmergedInWater() && !this.player.hasVehicle() && this.player.getEntityWorld().getFluidState(posToBlockPos(this.pos)).isIn(FluidTags.WATER));
} 
}
private void updateSubmergedInWaterState() {
this.submergedInWater = this.submergedFluidTag.contains(FluidTags.WATER);
this.submergedFluidTag.clear();
double eyeLevel = getEyeY() - 0.1111111119389534D;
Entity vehicle = this.player.getVehicle();
if (vehicle instanceof AbstractBoatEntity) { AbstractBoatEntity boat = (AbstractBoatEntity)vehicle;
if (!boat.isSubmergedInWater() && 
(boat.getBoundingBox()).maxY >= eyeLevel && 
(boat.getBoundingBox()).minY <= eyeLevel) {
return;
} }
BlockPos posEye = BlockPos.ofFloored(this.pos.x, eyeLevel, this.pos.z);
FluidState fluidState = this.player.getEntityWorld().getFluidState(posEye);
double height = (posEye.getY() + fluidState.getHeight((BlockView)this.player.getEntityWorld(), posEye));
if (height > eyeLevel) {
this.submergedFluidTag.addAll(fluidState.streamTags().toList());
}
}
private double getEyeY() {
return this.pos.y + this.player.getStandingEyeHeight();
}
public boolean isSubmergedInWater() {
return (this.submergedInWater && isTouchingWater());
}
private double getFluidHeight(TagKey<Fluid> tag) {
return this.fluidHeight.getDouble(tag);
}
private boolean updateMovementInFluid(TagKey<Fluid> tag, double speed) {
if (isRegionUnloaded()) {
return false;
}
Box box = this.boundingBox.contract(0.001D);
int i = MathHelper.floor(box.minX);
int j = MathHelper.ceil(box.maxX);
int k = MathHelper.floor(box.minY);
int l = MathHelper.ceil(box.maxY);
int m = MathHelper.floor(box.minZ);
int n = MathHelper.ceil(box.maxZ);
double d = 0.0D;
boolean pushedByFluids = true;
boolean foundFluid = false;
Vec3d fluidVelocity = Vec3d.ZERO;
int count = 0;
BlockPos.Mutable mutable = new BlockPos.Mutable();
for (int p = i; p < j; p++) {
for (int q = k; q < l; q++) {
for (int r = m; r < n; r++) {
mutable.set(p, q, r);
FluidState fluidState = this.player.getEntityWorld().getFluidState((BlockPos)mutable);
if (fluidState.isIn(tag)) {
double e = (q + fluidState.getHeight((BlockView)this.player.getEntityWorld(), (BlockPos)mutable));
if (e >= box.minY) {
foundFluid = true;
d = Math.max(e - box.minY, d);
if (pushedByFluids) {
Vec3d vel = fluidState.getVelocity((BlockView)this.player.getEntityWorld(), (BlockPos)mutable);
if (d < 0.4D) {
vel = vel.multiply(d);
}
fluidVelocity = fluidVelocity.add(vel);
count++;
} 
} 
} 
} 
} 
} 
if (fluidVelocity.length() > 0.0D) {
if (count > 0) {
fluidVelocity = fluidVelocity.multiply(1.0D / count);
}
fluidVelocity = fluidVelocity.multiply(speed);
if (Math.abs(this.velocity.x) < 0.003D && Math.abs(this.velocity.z) < 0.003D && fluidVelocity
.length() < 0.0045D) {
fluidVelocity = fluidVelocity.normalize().multiply(0.0045D);
}
this.velocity = this.velocity.add(fluidVelocity);
} 
this.fluidHeight.put(tag, d);
return foundFluid;
}
private boolean isRegionUnloaded() {
Box box = this.boundingBox.expand(1.0D);
int i = MathHelper.floor(box.minX);
int j = MathHelper.ceil(box.maxX);
int k = MathHelper.floor(box.minZ);
int l = MathHelper.ceil(box.maxZ);
return !this.player.getEntityWorld().isRegionLoaded(i, k, j, l);
}
private Vec3d getRotationVector() {
return getRotationVector(this.pitch, this.yaw);
}
private Vec3d getRotationVector(float pitch, float yaw) {
float f = (float)(pitch * Math.PI / 180.0D);
float g = (float)(-yaw * Math.PI / 180.0D);
float h = MathHelper.cos(g);
float i = MathHelper.sin(g);
float j = MathHelper.cos(f);
float k = MathHelper.sin(f);
return new Vec3d((i * j), -k, (h * j));
}
public boolean hasStatusEffect(RegistryEntry<StatusEffect> effect) {
StatusEffectInstance instance = this.player.getStatusEffect(effect);
return (instance != null && instance.getDuration() >= this.simulatedTicks);
}
private StatusEffectInstance getStatusEffect(RegistryEntry<StatusEffect> effect) {
StatusEffectInstance instance = this.player.getStatusEffect(effect);
if (instance == null || instance.getDuration() < this.simulatedTicks) {
return null;
}
return instance;
}
public double getAttributeValue(RegistryEntry<EntityAttribute> attribute) {
return this.player.getAttributes().getValue(attribute);
}
public PlayerSimulation clone() {
return new PlayerSimulation(this.player, this.input, this.pos, this.velocity, this.boundingBox, this.yaw, this.pitch, this.sprinting, this.fallDistance, this.jumpingCooldown, this.isJumping, this.isFallFlying, this.onGround, this.horizontalCollision, this.verticalCollision, this.touchingWater, this.isSwimming, this.submergedInWater, (Object2DoubleMap<TagKey<Fluid>>)new Object2DoubleArrayMap(this.fluidHeight), new HashSet<>(this.submergedFluidTag));
}
public BlockPos posToBlockPos(Vec3d pos) {
return new BlockPos(MathHelper.floor(pos.x), MathHelper.floor(pos.y), MathHelper.floor(pos.z));
}
public BlockState getState(BlockPos pos) {
return this.player.getEntityWorld().getBlockState(pos);
}
public static class SimulatedPlayerInput extends Input {
public boolean forceSafeWalk = false;
public float movementForward;
public float movementSideways;
public PlayerInput playerInput;
public static final double MAX_WALKING_SPEED = 0.121D;
public SimulatedPlayerInput(PlayerInput input) {
this.playerInput = input;
}
public void update() {
if (this.playerInput.forward() != this.playerInput.backward()) {
this.movementForward = this.playerInput.forward() ? 1.0F : -1.0F;
} else {
this.movementForward = 0.0F;
} 
if (this.playerInput.left() == this.playerInput.right()) {
this.movementSideways = 0.0F;
} else {
this.movementSideways = this.playerInput.left() ? 1.0F : -1.0F;
} 
if (this.playerInput.sneak()) {
this.movementSideways *= 0.3F;
this.movementForward *= 0.3F;
} 
}
public String toString() {
return "SimulatedPlayerInput(forwards={" + this.playerInput.forward() + "}, backwards={" + this.playerInput.backward() + "}, left={" + this.playerInput
.left() + "}, right={" + this.playerInput.right() + "}, jumping={" + this.playerInput.jump() + "}, sprinting=" + this.playerInput
.sprint() + ", slowDown=" + this.playerInput.sneak() + ")";
}
public static SimulatedPlayerInput fromClientPlayer(PlayerInput input) {
return new SimulatedPlayerInput(input);
}
public static SimulatedPlayerInput guessInput(PlayerEntity entity) {
Vec3d velocity = entity.getEntityPos().subtract(new Vec3d(entity.lastX, entity.lastY, entity.lastZ));
double horizontalVelocity = velocity.horizontalLengthSquared();
PlayerInput input = new PlayerInput(false, false, false, false, !entity.isOnGround(), entity.isSneaking(), (horizontalVelocity >= 0.014641D));
if (horizontalVelocity > 0.0025000000000000005D) {
double velocityAngle = MoveUtil.getDegreesRelativeToView(velocity, entity.getYaw());
double wrappedAngle = MathHelper.wrapDegrees(velocityAngle);
input = MoveUtil.getDirectionalInputForDegrees(input, wrappedAngle);
} 
return new SimulatedPlayerInput(input);
}
}
}


