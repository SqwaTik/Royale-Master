package royale.util.color;

import java.awt.Color;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.ColorHelper;
import royale.util.math.MathUtils;

public final class ColorAssist {
    public static final int green = new Color(64, 255, 64).getRGB();
    public static final int yellow = new Color(255, 255, 64).getRGB();
    public static final int orange = new Color(255, 128, 32).getRGB();
    public static final int red = new Color(255, 64, 64).getRGB();
    private static final long CACHE_EXPIRATION_TIME = 60000L;
    private static final ConcurrentHashMap<ColorKey, CacheEntry> colorCache = new ConcurrentHashMap();
    private static final ScheduledExecutorService cacheCleaner = Executors.newScheduledThreadPool(1);
    private static final DelayQueue<CacheEntry> cleanupQueue = new DelayQueue();

    public static int colorForRectsCustom$() {
        return new Color(91, 63, 212, 255).getRGB();
    }

    public static int colorForRectsBlack$() {
        return new Color(26, 26, 26, 255).getRGB();
    }

    public static int colorForTextWhite$() {
        return new Color(255, 255, 255, 255).getRGB();
    }

    public static int colorForTextCustom$() {
        return new Color(130, 100, 210, 255).getRGB();
    }

    public static int red(int c) {
        return c >> 16 & 0xFF;
    }

    public static int green(int c) {
        return c >> 8 & 0xFF;
    }

    public static int blue(int c) {
        return c & 0xFF;
    }

    public static int alpha(int c) {
        return c >> 24 & 0xFF;
    }

    public static float redf(int c) {
        return (float)ColorAssist.red(c) / 255.0f;
    }

    public static float greenf(int c) {
        return (float)ColorAssist.green(c) / 255.0f;
    }

    public static float bluef(int c) {
        return (float)ColorAssist.blue(c) / 255.0f;
    }

    public static float alphaf(int c) {
        return (float)ColorAssist.alpha(c) / 255.0f;
    }

    public static int[] getRGB(int c) {
        return new int[]{ColorAssist.red(c), ColorAssist.green(c), ColorAssist.blue(c)};
    }

    public static int getColor(int brightness, int alpha) {
        return ColorAssist.getColor(brightness, brightness, brightness, alpha);
    }

    public static int getColor(int brightness, float alpha) {
        return ColorAssist.getColor(brightness, Math.round(alpha * 255.0f));
    }

    public static int getColor(int brightness) {
        return ColorAssist.getColor(brightness, brightness, brightness);
    }

    public static int applyOpacity(int hex, float opacity) {
        return ColorHelper.getArgb((int)((int)((float)ColorHelper.getAlpha((int)hex) * (opacity / 255.0f))), (int)ColorHelper.getRed((int)hex), (int)ColorHelper.getGreen((int)hex), (int)ColorHelper.getBlue((int)hex));
    }

    public static int calculateHuyDegrees(int divisor, int offset) {
        long currentTime = System.currentTimeMillis();
        long calculatedValue = (currentTime / (long)divisor + (long)offset) % 360L;
        return (int)calculatedValue;
    }

    public static int reAlphaInt(int color, int alpha) {
        return Math.clamp((long)alpha, 0, 255) << 24 | color & 0xFFFFFF;
    }

    public static int astolfo(int speed, int index, float saturation, float brightness, float alpha) {
        float hueStep = 90.0f;
        float basaHuy = ColorAssist.calculateHuyDegrees(speed, index);
        float huy = (basaHuy + (float)index * hueStep) % 360.0f;
        saturation = Math.clamp(saturation, 0.0f, 1.0f);
        brightness = Math.clamp(brightness, 0.0f, 1.0f);
        int rgb = Color.HSBtoRGB(huy /= 360.0f, saturation, brightness);
        int Ialpha = Math.max(0, Math.min(255, (int)(alpha * 255.0f)));
        return ColorAssist.reAlphaInt(rgb, Ialpha);
    }

    public static int toColor(String hexColor) {
        int argb = Integer.parseInt(hexColor.substring(1), 16);
        return ColorAssist.setAlpha(argb, 255);
    }

    public static int setAlpha(int color, int alpha) {
        return color & 0xFFFFFF | alpha << 24;
    }

    public static int interpolateColor(int color1, int color2, float amount) {
        amount = Math.min(1.0f, Math.max(0.0f, amount));
        int red1 = ColorAssist.getRed(color1);
        int green1 = ColorAssist.getGreen(color1);
        int blue1 = ColorAssist.getBlue(color1);
        int alpha1 = ColorAssist.getAlpha(color1);
        int red2 = ColorAssist.getRed(color2);
        int green2 = ColorAssist.getGreen(color2);
        int blue2 = ColorAssist.getBlue(color2);
        int alpha2 = ColorAssist.getAlpha(color2);
        int interpolatedRed = ColorAssist.interpolateInt(red1, red2, amount);
        int interpolatedGreen = ColorAssist.interpolateInt(green1, green2, amount);
        int interpolatedBlue = ColorAssist.interpolateInt(blue1, blue2, amount);
        int interpolatedAlpha = ColorAssist.interpolateInt(alpha1, alpha2, amount);
        return interpolatedAlpha << 24 | interpolatedRed << 16 | interpolatedGreen << 8 | interpolatedBlue;
    }

    public static Double interpolateD(double oldValue, double newValue, double interpolationValue) {
        return oldValue + (newValue - oldValue) * interpolationValue;
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return ColorAssist.interpolateD(oldValue, newValue, (float)interpolationValue).intValue();
    }

    public static int getRed(int hex) {
        return hex >> 16 & 0xFF;
    }

    public static int getGreen(int hex) {
        return hex >> 8 & 0xFF;
    }

    public static int getBlue(int hex) {
        return hex & 0xFF;
    }

    public static int getAlpha(int hex) {
        return hex >> 24 & 0xFF;
    }

    public static int overCol(int color1, int color2, float percent01) {
        float percent = MathHelper.clamp((float)percent01, (float)0.0f, (float)1.0f);
        return ColorAssist.getColor(MathHelper.lerp((float)percent, (int)ColorAssist.red(color1), (int)ColorAssist.red(color2)), MathHelper.lerp((float)percent, (int)ColorAssist.green(color1), (int)ColorAssist.green(color2)), MathHelper.lerp((float)percent, (int)ColorAssist.blue(color1), (int)ColorAssist.blue(color2)), MathHelper.lerp((float)percent, (int)ColorAssist.alpha(color1), (int)ColorAssist.alpha(color2)));
    }

    public static int multRedAndAlpha(int color, float red, float alpha) {
        return ColorAssist.getColor(ColorAssist.red(color), Math.min(255, Math.round((float)ColorAssist.green(color) / red)), Math.min(255, Math.round((float)ColorAssist.blue(color) / red)), Math.round((float)ColorAssist.alpha(color) * alpha));
    }

    public static int rgba(int red, int green, int blue, int alpha) {
        return ColorAssist.getColor(red, green, blue, alpha);
    }

    public static int[] genGradientForText(int color1, int color2, int length) {
        int[] gradient = new int[length];
        for (int i = 0; i < length; ++i) {
            float pc = (float)i / (float)(length - 1);
            gradient[i] = ColorAssist.overCol(color1, color2, pc);
        }
        return gradient;
    }

    public static float[] rgba(int color) {
        return new float[]{(float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f, (float)(color >> 24 & 0xFF) / 255.0f};
    }

    public static int interpolate(int start, int end, float value) {
        float[] startColor = ColorAssist.rgba(start);
        float[] endColor = ColorAssist.rgba(end);
        return ColorAssist.rgba((int)MathUtils.interpolate(startColor[0] * 255.0f, endColor[0] * 255.0f, value), (int)MathUtils.interpolate(startColor[1] * 255.0f, endColor[1] * 255.0f, value), (int)MathUtils.interpolate(startColor[2] * 255.0f, endColor[2] * 255.0f, value), (int)MathUtils.interpolate(startColor[3] * 255.0f, endColor[3] * 255.0f, value));
    }

    public static int getColor(int red, int green, int blue, int alpha) {
        ColorKey key = new ColorKey(red, green, blue, alpha);
        CacheEntry cacheEntry = colorCache.computeIfAbsent(key, k -> {
            CacheEntry newEntry = new CacheEntry((ColorKey)k, ColorAssist.computeColor(red, green, blue, alpha), 60000L);
            cleanupQueue.offer(newEntry);
            return newEntry;
        });
        return cacheEntry.getColor();
    }

    public static int getColor(int red, int green, int blue) {
        return ColorAssist.getColor(red, green, blue, 255);
    }

    private static int computeColor(int red, int green, int blue, int alpha) {
        return MathHelper.clamp((int)alpha, (int)0, (int)255) << 24 | MathHelper.clamp((int)red, (int)0, (int)255) << 16 | MathHelper.clamp((int)green, (int)0, (int)255) << 8 | MathHelper.clamp((int)blue, (int)0, (int)255);
    }

    public static String formatting(int color) {
        return "\u23cf" + color + "\u23cf";
    }

    private ColorAssist() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    static {
        cacheCleaner.scheduleWithFixedDelay(() -> {
            CacheEntry entry = (CacheEntry)cleanupQueue.poll();
            while (entry != null) {
                if (entry.isExpired()) {
                    colorCache.remove(entry.getKey());
                }
                entry = (CacheEntry)cleanupQueue.poll();
            }
        }, 0L, 1L, TimeUnit.SECONDS);
    }

    private static class ColorKey {
        final int red;
        final int green;
        final int blue;
        final int alpha;

        public int getRed() {
            return this.red;
        }

        public int getGreen() {
            return this.green;
        }

        public int getBlue() {
            return this.blue;
        }

        public int getAlpha() {
            return this.alpha;
        }

        public ColorKey(int red, int green, int blue, int alpha) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof ColorKey)) {
                return false;
            }
            ColorKey other = (ColorKey)o;
            if (!other.canEqual(this)) {
                return false;
            }
            if (this.getRed() != other.getRed()) {
                return false;
            }
            if (this.getGreen() != other.getGreen()) {
                return false;
            }
            if (this.getBlue() != other.getBlue()) {
                return false;
            }
            return this.getAlpha() == other.getAlpha();
        }

        protected boolean canEqual(Object other) {
            return other instanceof ColorKey;
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            result = result * 59 + this.getRed();
            result = result * 59 + this.getGreen();
            result = result * 59 + this.getBlue();
            result = result * 59 + this.getAlpha();
            return result;
        }
    }

    private static class CacheEntry
    implements Delayed {
        private final ColorKey key;
        private final int color;
        private final long expirationTime;

        CacheEntry(ColorKey key, int color, long ttl) {
            this.key = key;
            this.color = color;
            this.expirationTime = System.currentTimeMillis() + ttl;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long delay = this.expirationTime - System.currentTimeMillis();
            return unit.convert(delay, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            if (other instanceof CacheEntry) {
                return Long.compare(this.expirationTime, ((CacheEntry)other).expirationTime);
            }
            return 0;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > this.expirationTime;
        }

        public ColorKey getKey() {
            return this.key;
        }

        public int getColor() {
            return this.color;
        }

        public long getExpirationTime() {
            return this.expirationTime;
        }
    }
}

