package royale.util.performance;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public final class BuiltinOptimizer {
    private static final long LOW_PERF_WINDOW_MS = 7000L;
    private static final long SEVERE_WINDOW_MS = 12000L;
    private static final double FRAME_SPIKE_MS = 350.0D;
    private static final double SEVERE_SPIKE_MS = 900.0D;

    private static volatile long lowPerfUntilMs = 0L;
    private static volatile long severePerfUntilMs = 0L;
    private static volatile long lastTickNano = 0L;

    private BuiltinOptimizer() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void tick(MinecraftClient mc) {
        long nowMs = System.currentTimeMillis();
        long nowNs = System.nanoTime();

        long prev = lastTickNano;
        lastTickNano = nowNs;
        if (prev != 0L) {
            double frameMs = (nowNs - prev) / 1_000_000.0D;
            if (frameMs >= SEVERE_SPIKE_MS) {
                severePerfUntilMs = Math.max(severePerfUntilMs, nowMs + SEVERE_WINDOW_MS);
                lowPerfUntilMs = Math.max(lowPerfUntilMs, nowMs + SEVERE_WINDOW_MS);
            } else if (frameMs >= FRAME_SPIKE_MS) {
                lowPerfUntilMs = Math.max(lowPerfUntilMs, nowMs + LOW_PERF_WINDOW_MS);
            }
        }

        if (mc == null) {
            return;
        }

        int fps = mc.getCurrentFps();
        if (fps > 0 && fps <= 20) {
            severePerfUntilMs = Math.max(severePerfUntilMs, nowMs + 5000L);
            lowPerfUntilMs = Math.max(lowPerfUntilMs, nowMs + 7000L);
        } else if (fps > 0 && fps <= 35) {
            lowPerfUntilMs = Math.max(lowPerfUntilMs, nowMs + 4000L);
        }

        // NVIDIA Instant Replay save hotkeys can cause short heavy stalls.
        if (mc.getWindow() != null) {
            long handle = mc.getWindow().getHandle();
            if (handle != 0L) {
                boolean alt = GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS
                        || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;
                boolean replaySaveKey = GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_GRAVE_ACCENT) == GLFW.GLFW_PRESS
                        || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_F10) == GLFW.GLFW_PRESS;
                if (alt && replaySaveKey) {
                    lowPerfUntilMs = Math.max(lowPerfUntilMs, nowMs + 6000L);
                }
            }
        }
    }

    public static void onWorldLeft(MinecraftClient mc) {
        lowPerfUntilMs = 0L;
        severePerfUntilMs = 0L;
        lastTickNano = 0L;
    }

    public static int getDynamicParticleCap(int configuredCap) {
        if (isSevereMode()) {
            return Math.max(60, Math.min(configuredCap, 160));
        }
        if (isLowPerfMode()) {
            return Math.max(90, Math.min(configuredCap, 280));
        }
        return configuredCap;
    }

    public static int getDynamicWorldParticleCap(int configuredCap) {
        if (isSevereMode()) {
            return Math.max(50, Math.min(configuredCap, 120));
        }
        if (isLowPerfMode()) {
            return Math.max(80, Math.min(configuredCap, 220));
        }
        return configuredCap;
    }

    public static int getDecorativeSpawnBudget(int configuredPerTick) {
        if (isSevereMode()) {
            return 0;
        }
        if (isLowPerfMode()) {
            return Math.max(1, configuredPerTick / 3);
        }
        return configuredPerTick;
    }

    public static boolean shouldReduceDecorativeParticles() {
        return isLowPerfMode();
    }

    public static boolean shouldSuspendDecorativeParticles() {
        return isSevereMode();
    }

    public static boolean shouldCullDistantPlayer(double squaredDistanceToCamera) {
        if (isSevereMode()) {
            return squaredDistanceToCamera > 120.0D * 120.0D;
        }
        if (isLowPerfMode()) {
            return squaredDistanceToCamera > 170.0D * 170.0D;
        }
        return false;
    }

    public static boolean shouldCullStorageBlockEntity(BlockEntityType<?> type, double squaredDistanceToCamera) {
        if (isSevereMode()) {
            return squaredDistanceToCamera > 48.0D * 48.0D;
        }
        if (isLowPerfMode()) {
            return squaredDistanceToCamera > 72.0D * 72.0D;
        }
        return false;
    }

    public static boolean shouldCullDistantItemEntity(double squaredDistanceToCamera) {
        if (isSevereMode()) {
            return squaredDistanceToCamera > 32.0D * 32.0D;
        }
        if (isLowPerfMode()) {
            return squaredDistanceToCamera > 48.0D * 48.0D;
        }
        return false;
    }

    public static int getProjectileScanBudget(int configuredBudget) {
        if (isSevereMode()) {
            return Math.max(4, configuredBudget / 4);
        }
        if (isLowPerfMode()) {
            return Math.max(8, configuredBudget / 2);
        }
        return configuredBudget;
    }

    private static boolean isLowPerfMode() {
        return System.currentTimeMillis() < lowPerfUntilMs;
    }

    private static boolean isSevereMode() {
        return System.currentTimeMillis() < severePerfUntilMs;
    }
}
