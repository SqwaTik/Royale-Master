package royale.modules.impl.render;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class TrajectoryMath {
    private TrajectoryMath() {}

    static Data fromPlayer(PlayerEntity player, float tickDelta) {
        ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
        Settings settings = settingsFor(stack, player);
        if (settings == null) {
            stack = player.getStackInHand(Hand.OFF_HAND);
            settings = settingsFor(stack, player);
        }
        if (settings == null) {
            return null;
        }
        Vec3d look = player.getRotationVec(tickDelta).normalize();
        Vec3d start = player.getEyePos().add(look.multiply(0.32D));
        return new Data(start, look.multiply(settings.power()), settings.gravity(), settings.drag(), settings.scale());
    }

    static Data fromProjectile(ProjectileEntity projectile) {
        return new Data(new Vec3d(projectile.getX(), projectile.getY(), projectile.getZ()), projectile.getVelocity(), gravityForProjectile(projectile), 0.99D, 1.0D);
    }

    static Result calculate(net.minecraft.world.World world, Entity source, Data data, int maxTicks) {
        ArrayList<Vec3d> points = new ArrayList<>(Math.max(8, maxTicks));
        Vec3d position = data.start();
        Vec3d velocity = data.velocity();
        points.add(position);
        boolean hit = false;
        int ticks = 0;
        for (int i = 0; i < maxTicks; i++) {
            Vec3d next = position.add(velocity.multiply(data.scale()));
            BlockHitResult blockHit = world.raycast(new RaycastContext(position, next, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, source));
            if (blockHit != null && blockHit.getType() == HitResult.Type.BLOCK) {
                next = blockHit.getPos();
                hit = true;
            }
            points.add(next);
            ticks++;
            position = next;
            if (hit) {
                break;
            }
            velocity = velocity.multiply(data.drag()).subtract(0.0D, data.gravity(), 0.0D);
            if (position.y < world.getBottomY() - 4.0D || position.y > world.getBottomY() + 512.0D) {
                break;
            }
        }
        return new Result(points, position, ticks, hit);
    }

    static Settings settingsFor(ItemStack stack, PlayerEntity player) {
        if (stack == null || stack.isEmpty()) return null;
        Item item = stack.getItem();
        if (item == Items.SNOWBALL || item == Items.EGG || item == Items.ENDER_PEARL) return new Settings(1.5D, 0.03D, 0.99D, 1.0D);
        if (item == Items.SPLASH_POTION || item == Items.LINGERING_POTION || item == Items.EXPERIENCE_BOTTLE) return new Settings(0.75D, 0.05D, 0.99D, 0.85D);
        if (item == Items.TRIDENT) return new Settings(player.isUsingItem() ? 2.5D : 2.2D, 0.05D, 0.99D, 1.15D);
        if (item == Items.BOW) {
            double pull = bowPull(player);
            if (pull <= 0.08D && !player.isUsingItem()) pull = 1.0D;
            return new Settings(3.0D * pull, 0.05D, 0.99D, 1.2D);
        }
        if (item == Items.CROSSBOW) return new Settings(3.15D, 0.05D, 0.99D, 1.2D);
        if (item == Items.FISHING_ROD) return new Settings(1.25D, 0.04D, 0.92D, 0.8D);
        return null;
    }

    static Vec3d sample(List<Vec3d> points, double t) {
        if (points.isEmpty()) return Vec3d.ZERO;
        int index = Math.min(points.size() - 1, Math.max(0, (int) Math.floor(t * (points.size() - 1))));
        return points.get(index);
    }

    static String formatTicks(int ticks) {
        return String.format(Locale.US, "%.2fс", Math.max(0.05D, ticks / 20.0D));
    }

    private static double bowPull(PlayerEntity player) {
        int useTicks = Math.max(0, player.getItemUseTime());
        double pull = useTicks / 20.0D;
        pull = (pull * pull + pull * 2.0D) / 3.0D;
        return Math.min(1.0D, Math.max(0.0D, pull));
    }

    private static double gravityForProjectile(ProjectileEntity projectile) {
        String name = projectile.getType().toString().toLowerCase(Locale.ROOT);
        if (name.contains("potion") || name.contains("experience") || name.contains("arrow") || name.contains("trident")) return 0.05D;
        return 0.03D;
    }

    record Data(Vec3d start, Vec3d velocity, double gravity, double drag, double scale) {}
    record Settings(double power, double gravity, double drag, double scale) {}
    record Result(List<Vec3d> points, Vec3d landing, int ticks, boolean hit) {}
}
