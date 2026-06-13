package royale.modules.impl.render.worldparticles;
import net.minecraft.util.math.Vec3d;
public class ParticleSpawner
{
private static final double MIN_RADIUS = 3.0D;
private static final double MAX_RADIUS = 60.0D;
private static final double MAX_HEIGHT = 25.0D;
private static final double DESPAWN_DISTANCE = 65.0D;
public static Particle createParticle(Vec3d playerPos, Vec3d playerVelocity, double playerSpeed, long lifeTimeMs, Particle.ParticleType type) {
double radius = 3.0D + Math.random() * 57.0D;
double angle = Math.random() * Math.PI * 2.0D;
double spawnX = playerPos.x;
double spawnZ = playerPos.z;
if (playerSpeed > 0.05D && playerVelocity.horizontalLength() > 0.01D) {
Vec3d normalizedVelocity = playerVelocity.normalize();
double forwardAngle = Math.atan2(normalizedVelocity.z, normalizedVelocity.x);
double angleSpread = 1.2566370614359172D;
angle = forwardAngle + (Math.random() - 0.5D) * angleSpread * 2.0D;
double forwardOffset = radius * 0.7D * Math.min(playerSpeed * 8.0D, 1.0D);
spawnX += normalizedVelocity.x * forwardOffset;
spawnZ += normalizedVelocity.z * forwardOffset;
} 
double finalX = spawnX + Math.cos(angle) * radius;
double finalZ = spawnZ + Math.sin(angle) * radius;
double finalY = playerPos.y - 5.0D + Math.random() * 25.0D;
double mx = (Math.random() - 0.5D) * 0.08D;
double my = (Math.random() - 0.5D) * 0.02D;
double mz = (Math.random() - 0.5D) * 0.08D;
return (new Particle(finalX, finalY, finalZ, mx, my, mz, lifeTimeMs)).setType(type);
}
public static Particle createParticle(Vec3d playerPos, Vec3d playerVelocity, double playerSpeed, long lifeTimeMs) {
return createParticle(playerPos, playerVelocity, playerSpeed, lifeTimeMs, Particle.ParticleType.CUBE_3D);
}
public static int calculateSpawnDelay(double playerSpeed) {
int baseDelay = 40;
int actualDelay = baseDelay;
if (playerSpeed > 0.05D) {
double speedFactor = Math.min(playerSpeed * 5.0D, 4.0D);
actualDelay = (int)(baseDelay / (1.0D + speedFactor));
actualDelay = Math.max(actualDelay, 8);
} 
return actualDelay;
}
public static int calculateSpawnCount(double playerSpeed, int currentCount, int maxCount) {
int spawnCount = 1;
if (playerSpeed > 0.1D) {
spawnCount = Math.min(8, maxCount - currentCount);
spawnCount = Math.max(1, (int)(spawnCount * Math.min(playerSpeed * 5.0D, 1.0D)));
} 
return spawnCount;
}
public static double getDespawnDistance() {
return 65.0D;
}
public static double getDespawnDistanceSquared() {
return 4225.0D;
}
}


