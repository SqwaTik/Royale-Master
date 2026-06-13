package royale.util;

import it.unimi.dsi.fastutil.chars.Char2IntArrayMap;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.ColorHelper;
import org.joml.Vector4i;
import org.lwjgl.opengl.GL11;
import royale.util.math.MathUtils;

public final class ColorUtil {
    public static final int green = new Color(64, 255, 64).getRGB();
    public static final int yellow = new Color(255, 255, 64).getRGB();
    public static final int orange = new Color(255, 128, 32).getRGB();
    public static final int red = new Color(255, 64, 64).getRGB();
    private static final long CACHE_EXPIRATION_TIME = 60000L;
    private static final ConcurrentHashMap<ColorKey, CacheEntry> colorCache = new ConcurrentHashMap();
    private static final ScheduledExecutorService cacheCleaner = Executors.newScheduledThreadPool(1);
    private static final DelayQueue<CacheEntry> cleanupQueue = new DelayQueue();
    public static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)\u00a7[0-9a-f-or]");
    public static Char2IntArrayMap colorCodes = new Char2IntArrayMap(){
        {
            this.put('0', 0);
            this.put('1', 170);
            this.put('2', 43520);
            this.put('3', 43690);
            this.put('4', 0xAA0000);
            this.put('5', 0xAA00AA);
            this.put('6', 0xFFAA00);
            this.put('7', 0xAAAAAA);
            this.put('8', 0x555555);
            this.put('9', 0x5555FF);
            this.put('A', 0x55FF55);
            this.put('B', 0x55FFFF);
            this.put('C', 0xFF5555);
            this.put('D', 0xFF55FF);
            this.put('E', 0xFFFF55);
            this.put('F', 0xFFFFFF);
        }
    };
    public static final int RED;
    public static final int GREEN;
    public static final int BLUE;
    public static final int YELLOW;
    public static final int WHITE;
    public static final int BLACK;
    public static final int HALF_BLACK;
    public static final int LIGHT_RED;

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

    public static int applyAlpha(int color, int alpha) {
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int originalAlpha = color >> 24 & 0xFF;
        int newAlpha = originalAlpha * alpha / 255;
        return newAlpha << 24 | r << 16 | g << 8 | b;
    }

    public static int applyAlpha(int color, float alpha) {
        return ColorUtil.applyAlpha(color, (int)(alpha * 255.0f));
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
        return (float)ColorUtil.red(c) / 255.0f;
    }

    public static float greenf(int c) {
        return (float)ColorUtil.green(c) / 255.0f;
    }

    public static float bluef(int c) {
        return (float)ColorUtil.blue(c) / 255.0f;
    }

    public static float alphaf(int c) {
        return (float)ColorUtil.alpha(c) / 255.0f;
    }

    public static int[] getRGBA(int c) {
        return new int[]{ColorUtil.red(c), ColorUtil.green(c), ColorUtil.blue(c), ColorUtil.alpha(c)};
    }

    public static int[] getRGB(int c) {
        return new int[]{ColorUtil.red(c), ColorUtil.green(c), ColorUtil.blue(c)};
    }

    public static float[] getRGBAf(int c) {
        return new float[]{ColorUtil.redf(c), ColorUtil.greenf(c), ColorUtil.bluef(c), ColorUtil.alphaf(c)};
    }

    public static float[] getRGBf(int c) {
        return new float[]{ColorUtil.redf(c), ColorUtil.greenf(c), ColorUtil.bluef(c)};
    }

    public static int getColor(float red, float green, float blue, float alpha) {
        return ColorUtil.getColor(Math.round(red * 255.0f), Math.round(green * 255.0f), Math.round(blue * 255.0f), Math.round(alpha * 255.0f));
    }

    public static int getColor(int red, int green, int blue, float alpha) {
        return ColorUtil.getColor(red, green, blue, Math.round(alpha * 255.0f));
    }

    public static int getColor(float red, float green, float blue) {
        return ColorUtil.getColor(red, green, blue, 1.0f);
    }

    public static int getColor(int brightness, int alpha) {
        return ColorUtil.getColor(brightness, brightness, brightness, alpha);
    }

    public static int getColor(int brightness, float alpha) {
        return ColorUtil.getColor(brightness, Math.round(alpha * 255.0f));
    }

    public static int getColor(int brightness) {
        return ColorUtil.getColor(brightness, brightness, brightness);
    }

    public static int replAlpha(int color, int alpha) {
        return ColorUtil.getColor(ColorUtil.red(color), ColorUtil.green(color), ColorUtil.blue(color), alpha);
    }

    public static int replAlpha(int color, float alpha) {
        return ColorUtil.getColor(ColorUtil.red(color), ColorUtil.green(color), ColorUtil.blue(color), alpha);
    }

    public static int multAlpha(int color, float percent01) {
        return ColorUtil.getColor(ColorUtil.red(color), ColorUtil.green(color), ColorUtil.blue(color), Math.round((float)ColorUtil.alpha(color) * percent01));
    }

    public static int applyOpacity(int hex, int percent) {
        return ColorUtil.applyOpacity(hex, 2.55f * (float)Math.min(percent, 100));
    }

    public static int applyOpacity(int hex, float opacity) {
        return ColorHelper.getArgb((int)((int)((float)ColorHelper.getAlpha((int)hex) * (opacity / 255.0f))), (int)ColorHelper.getRed((int)hex), (int)ColorHelper.getGreen((int)hex), (int)ColorHelper.getBlue((int)hex));
    }

    public static int lerp(float value, int from, int to) {
        return ColorHelper.lerp((float)value, (int)from, (int)to);
    }

    public static int pixelColor(int x, int y) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
        GL11.glReadPixels((int)x, (int)y, (int)1, (int)1, (int)6408, (int)5121, (ByteBuffer)byteBuffer);
        return ColorHelper.getArgb((int)ColorUtil.getRed(byteBuffer.get()), (int)ColorUtil.getGreen(byteBuffer.get()), (int)ColorUtil.getBlue(byteBuffer.get()));
    }

    public static int MathUtilHuyDegrees(int divisor, int offset) {
        long currentTime = System.currentTimeMillis();
        long MathUtildValue = (currentTime / (long)divisor + (long)offset) % 360L;
        return (int)MathUtildValue;
    }

    public static int reAlphaInt(int color, int alpha) {
        return Math.clamp((long)alpha, 0, 255) << 24 | color & 0xFFFFFF;
    }

    public static int astolfo(int speed, int index, float saturation, float brightness, float alpha) {
        float hueStep = 90.0f;
        float basaHuy = ColorUtil.MathUtilHuyDegrees(speed, index);
        float huy = (basaHuy + (float)index * hueStep) % 360.0f;
        saturation = Math.clamp(saturation, 0.0f, 1.0f);
        brightness = Math.clamp(brightness, 0.0f, 1.0f);
        int rgb = Color.HSBtoRGB(huy /= 360.0f, saturation, brightness);
        int Ialpha = Math.max(0, Math.min(255, (int)(alpha * 255.0f)));
        return ColorUtil.reAlphaInt(rgb, Ialpha);
    }

    public static int rgb(int r, int g, int b) {
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    public static int toColor(String hexColor) {
        int argb = Integer.parseInt(hexColor.substring(1), 16);
        return ColorUtil.setAlpha(argb, 255);
    }

    public static int setAlpha(int color, int alpha) {
        return color & 0xFFFFFF | alpha << 24;
    }

    public static int gradient(int start, int end, int index, int speed) {
        int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
        angle = (angle > 180 ? 360 - angle : angle) + 180;
        int color = ColorUtil.interpolate(start, end, Math.clamp((float)angle / 180.0f - 1.0f, 0.0f, 1.0f));
        float[] hs = ColorUtil.rgba(color);
        float[] hsb = Color.RGBtoHSB((int)(hs[0] * 255.0f), (int)(hs[1] * 255.0f), (int)(hs[2] * 255.0f), null);
        hsb[1] = hsb[1] * 1.5f;
        hsb[1] = Math.min(hsb[1], 1.0f);
        return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
    }

    public static int gradient(int speed, int index, int ... colors) {
        int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
        angle = (angle > 180 ? 360 - angle : angle) + 180;
        int colorIndex = (int)((float)angle / 360.0f * (float)colors.length);
        if (colorIndex == colors.length) {
            --colorIndex;
        }
        int color1 = colors[colorIndex];
        int color2 = colors[colorIndex == colors.length - 1 ? 0 : colorIndex + 1];
        return ColorUtil.interpolate(color1, color2, (float)angle / 360.0f * (float)colors.length - (float)colorIndex);
    }

    public static int interpolateColor(int color1, int color2, float amount) {
        amount = Math.min(1.0f, Math.max(0.0f, amount));
        int red1 = ColorUtil.getRed(color1);
        int green1 = ColorUtil.getGreen(color1);
        int blue1 = ColorUtil.getBlue(color1);
        int alpha1 = ColorUtil.getAlpha(color1);
        int red2 = ColorUtil.getRed(color2);
        int green2 = ColorUtil.getGreen(color2);
        int blue2 = ColorUtil.getBlue(color2);
        int alpha2 = ColorUtil.getAlpha(color2);
        int interpolatedRed = ColorUtil.interpolateInt(red1, red2, amount);
        int interpolatedGreen = ColorUtil.interpolateInt(green1, green2, amount);
        int interpolatedBlue = ColorUtil.interpolateInt(blue1, blue2, amount);
        int interpolatedAlpha = ColorUtil.interpolateInt(alpha1, alpha2, amount);
        return interpolatedAlpha << 24 | interpolatedRed << 16 | interpolatedGreen << 8 | interpolatedBlue;
    }

    public static Double interpolateD(double oldValue, double newValue, double interpolationValue) {
        return oldValue + (newValue - oldValue) * interpolationValue;
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return ColorUtil.interpolateD(oldValue, newValue, (float)interpolationValue).intValue();
    }

    public static int lerpColor(int c1, int c2, float t) {
        int a1 = c1 >> 24 & 0xFF;
        int r1 = c1 >> 16 & 0xFF;
        int g1 = c1 >> 8 & 0xFF;
        int b1 = c1 & 0xFF;
        int a2 = c2 >> 24 & 0xFF;
        int r2 = c2 >> 16 & 0xFF;
        int g2 = c2 >> 8 & 0xFF;
        int b2 = c2 & 0xFF;
        int a = (int)((float)a1 + (float)(a2 - a1) * t);
        int r = (int)((float)r1 + (float)(r2 - r1) * t);
        int g = (int)((float)g1 + (float)(g2 - g1) * t);
        int b = (int)((float)b1 + (float)(b2 - b1) * t);
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static int withAlpha(int color, int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        return color & 0xFFFFFF | alpha << 24;
    }

    public static int getRed(int hex) {
        return hex >> 16 & 0xFF;
    }

    public static int withAlphahud(int color, int alpha) {
        return alpha << 24 | color & 0xFFFFFF;
    }

    public static int withAlpha(int color, float alpha) {
        return ColorUtil.withAlphahud(color, (int)(alpha * 255.0f));
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

    public static int multColor(int colorStart, int colorEnd, float progress) {
        return ColorUtil.getColor(Math.round((float)ColorUtil.red(colorStart) * (ColorUtil.redf(colorEnd) * progress)), Math.round((float)ColorUtil.green(colorStart) * (ColorUtil.greenf(colorEnd) * progress)), Math.round((float)ColorUtil.blue(colorStart) * (ColorUtil.bluef(colorEnd) * progress)), Math.round((float)ColorUtil.alpha(colorStart) * (ColorUtil.alphaf(colorEnd) * progress)));
    }

    public static int multRed(int colorStart, int colorEnd, float progress) {
        return ColorUtil.getColor(Math.round((float)ColorUtil.red(colorStart) * (ColorUtil.redf(colorEnd) * progress)), Math.round((float)ColorUtil.green(colorStart) * (ColorUtil.greenf(colorEnd) * progress)), Math.round((float)ColorUtil.blue(colorStart) * (ColorUtil.bluef(colorEnd) * progress)), Math.round((float)ColorUtil.alpha(colorStart) * (ColorUtil.alphaf(colorEnd) * progress)));
    }

    public static int multDark(int color, float percent01) {
        return ColorUtil.getColor(Math.round((float)ColorUtil.red(color) * percent01), Math.round((float)ColorUtil.green(color) * percent01), Math.round((float)ColorUtil.blue(color) * percent01), ColorUtil.alpha(color));
    }

    public static int multBright(int color, float percent01) {
        return ColorUtil.getColor(Math.min(255, Math.round((float)ColorUtil.red(color) / percent01)), Math.min(255, Math.round((float)ColorUtil.green(color) / percent01)), Math.min(255, Math.round((float)ColorUtil.blue(color) / percent01)), ColorUtil.alpha(color));
    }

    public static int overCol(int color1, int color2, float percent01) {
        float percent = MathHelper.clamp((float)percent01, (float)0.0f, (float)1.0f);
        return ColorUtil.getColor(MathHelper.lerp((float)percent, (int)ColorUtil.red(color1), (int)ColorUtil.red(color2)), MathHelper.lerp((float)percent, (int)ColorUtil.green(color1), (int)ColorUtil.green(color2)), MathHelper.lerp((float)percent, (int)ColorUtil.blue(color1), (int)ColorUtil.blue(color2)), MathHelper.lerp((float)percent, (int)ColorUtil.alpha(color1), (int)ColorUtil.alpha(color2)));
    }

    public static Vector4i multRedAndAlpha(Vector4i color, float red, float alpha) {
        return new Vector4i(ColorUtil.multRedAndAlpha(color.x, red, alpha), ColorUtil.multRedAndAlpha(color.y, red, alpha), ColorUtil.multRedAndAlpha(color.w, red, alpha), ColorUtil.multRedAndAlpha(color.z, red, alpha));
    }

    public static int multRedAndAlpha(int color, float red, float alpha) {
        return ColorUtil.getColor(ColorUtil.red(color), Math.min(255, Math.round((float)ColorUtil.green(color) / red)), Math.min(255, Math.round((float)ColorUtil.blue(color) / red)), Math.round((float)ColorUtil.alpha(color) * alpha));
    }

    public static int multRed(int color, float percent01) {
        return ColorUtil.getColor(ColorUtil.red(color), Math.min(255, Math.round((float)ColorUtil.green(color) / percent01)), Math.min(255, Math.round((float)ColorUtil.blue(color) / percent01)), ColorUtil.alpha(color));
    }

    public static int multGreen(int color, float percent01) {
        return ColorUtil.getColor(Math.min(255, Math.round((float)ColorUtil.green(color) / percent01)), ColorUtil.green(color), Math.min(255, Math.round((float)ColorUtil.blue(color) / percent01)), ColorUtil.alpha(color));
    }

    public static int rgba(int red, int green, int blue, int alpha) {
        return ColorUtil.getColor(red, green, blue, alpha);
    }

    public static int[] genGradientForText(int color1, int color2, int length) {
        int[] gradient = new int[length];
        for (int i = 0; i < length; ++i) {
            float pc = (float)i / (float)(length - 1);
            gradient[i] = ColorUtil.overCol(color1, color2, pc);
        }
        return gradient;
    }

    public static float[] rgba(int color) {
        return new float[]{(float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f, (float)(color >> 24 & 0xFF) / 255.0f};
    }

    public static int interpolate(int start, int end, float value) {
        float[] startColor = ColorUtil.rgba(start);
        float[] endColor = ColorUtil.rgba(end);
        return ColorUtil.rgba((int)MathUtils.interpolate(startColor[0] * 255.0f, endColor[0] * 255.0f, value), (int)MathUtils.interpolate(startColor[1] * 255.0f, endColor[1] * 255.0f, value), (int)MathUtils.interpolate(startColor[2] * 255.0f, endColor[2] * 255.0f, value), (int)MathUtils.interpolate(startColor[3] * 255.0f, endColor[3] * 255.0f, value));
    }

    public static int[] solid(int color) {
        return new int[]{color, color, color, color, color, color, color, color, color};
    }

    public static int[] solid8(int color) {
        return new int[]{color, color, color, color, color, color, color, color};
    }

    public static int rainbow(int speed, int index, float saturation, float brightness, float opacity) {
        int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
        float hue = (float)angle / 360.0f;
        int color = Color.HSBtoRGB(hue, saturation, brightness);
        return ColorUtil.getColor(ColorUtil.red(color), ColorUtil.green(color), ColorUtil.blue(color), Math.round(opacity * 255.0f));
    }

    public static int fade(int speed, int index, int first, int second) {
        int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
        angle = angle >= 180 ? 360 - angle : angle;
        return ColorUtil.overCol(first, second, (float)angle / 180.0f);
    }

    public static int rgbaFloat(float r, float g, float b, float a) {
        return (int)(MathHelper.clamp((float)a, (float)0.0f, (float)1.0f) * 255.0f) << 24 | (int)(MathHelper.clamp((float)r, (float)0.0f, (float)1.0f) * 255.0f) << 16 | (int)(MathHelper.clamp((float)g, (float)0.0f, (float)1.0f) * 255.0f) << 8 | (int)(MathHelper.clamp((float)b, (float)0.0f, (float)1.0f) * 255.0f);
    }

    public static int getColor(int red, int green, int blue, int alpha) {
        ColorKey key = new ColorKey(red, green, blue, alpha);
        CacheEntry cacheEntry = colorCache.computeIfAbsent(key, k -> {
            CacheEntry newEntry = new CacheEntry((ColorKey)k, ColorUtil.computeColor(red, green, blue, alpha), 60000L);
            cleanupQueue.offer(newEntry);
            return newEntry;
        });
        return cacheEntry.getColor();
    }

    public static int getColor(int red, int green, int blue) {
        return ColorUtil.getColor(red, green, blue, 255);
    }

    private static int computeColor(int red, int green, int blue, int alpha) {
        return MathHelper.clamp((int)alpha, (int)0, (int)255) << 24 | MathHelper.clamp((int)red, (int)0, (int)255) << 16 | MathHelper.clamp((int)green, (int)0, (int)255) << 8 | MathHelper.clamp((int)blue, (int)0, (int)255);
    }

    public static String formatting(int color) {
        return "\u23cf" + color + "\u23cf";
    }

    public static int darkenColor(int color, float factor) {
        int a = color >> 24 & 0xFF;
        int r = (int)((float)(color >> 16 & 0xFF) * factor);
        int g = (int)((float)(color >> 8 & 0xFF) * factor);
        int b = (int)((float)(color & 0xFF) * factor);
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static int lightenColor(int color, float factor) {
        int a = color >> 24 & 0xFF;
        int r = Math.min(255, (int)((float)(color >> 16 & 0xFF) * factor));
        int g = Math.min(255, (int)((float)(color >> 8 & 0xFF) * factor));
        int b = Math.min(255, (int)((float)(color & 0xFF) * factor));
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static int hsvToRgb(float h, float s, float v) {
        float b;
        float g;
        float r;
        float c = v * s;
        float x = c * (1.0f - Math.abs(h * 6.0f % 2.0f - 1.0f));
        float m = v - c;
        if (h < 0.16666667f) {
            r = c;
            g = x;
            b = 0.0f;
        } else if (h < 0.33333334f) {
            r = x;
            g = c;
            b = 0.0f;
        } else if (h < 0.5f) {
            r = 0.0f;
            g = c;
            b = x;
        } else if (h < 0.6666667f) {
            r = 0.0f;
            g = x;
            b = c;
        } else if (h < 0.8333333f) {
            r = x;
            g = 0.0f;
            b = c;
        } else {
            r = c;
            g = 0.0f;
            b = x;
        }
        int ri = (int)((r + m) * 255.0f);
        int gi = (int)((g + m) * 255.0f);
        int bi = (int)((b + m) * 255.0f);
        return 0xFF000000 | ri << 16 | gi << 8 | bi;
    }

    public static int hsvToRgb(float h, float s, float v, float alpha) {
        int rgb = ColorUtil.hsvToRgb(h, s, v);
        int a = (int)(alpha * 255.0f);
        return a << 24 | rgb & 0xFFFFFF;
    }

    public static String removeFormatting(String text) {
        return text == null || text.isEmpty() ? null : FORMATTING_CODE_PATTERN.matcher(text).replaceAll("");
    }

    public static int getMainGuiColor() {
        return new Color(20, 20, 24, 255).getRGB();
    }

    public static int getGuiRectColor(float alpha) {
        return ColorUtil.multAlpha(new Color(0x1A1A1F).getRGB(), alpha);
    }

    public static int getGuiRectColor2(float alpha) {
        return ColorUtil.multAlpha(new Color(1973798).getRGB(), alpha);
    }

    public static int getRect(float alpha) {
        return ColorUtil.multAlpha(new Color(0, 0, 0, 228).getRGB(), alpha);
    }

    public static int getRectDarker(float alpha) {
        return ColorUtil.multAlpha(new Color(0x18181E).getRGB(), alpha);
    }

    public static int getText(float alpha) {
        return ColorUtil.multAlpha(ColorUtil.getText(), alpha);
    }

    public static int getText() {
        return new Color(255, 255, 255, 255).getRGB();
    }

    public static int getText2() {
        return new Color(175, 175, 175, 255).getRGB();
    }

    public static int getFriendColor() {
        return new Color(0x55FF55).getRGB();
    }

    public static int getOutline(float alpha, float bright) {
        return ColorUtil.multBright(ColorUtil.multAlpha(ColorUtil.getOutline(), alpha), bright);
    }

    public static int getOutline(float alpha) {
        return ColorUtil.multAlpha(ColorUtil.getOutline(), alpha);
    }

    public static int getOutline() {
        return new Color(3618630).getRGB();
    }

    private ColorUtil() {
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
        RED = ColorUtil.getColor(255, 0, 0);
        GREEN = ColorUtil.getColor(0, 255, 0);
        BLUE = ColorUtil.getColor(0, 0, 255);
        YELLOW = ColorUtil.getColor(255, 255, 0);
        WHITE = ColorUtil.getColor(255);
        BLACK = ColorUtil.getColor(0);
        HALF_BLACK = ColorUtil.getColor(0, 0.5f);
        LIGHT_RED = ColorUtil.getColor(255, 85, 85);
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

    public static class IntColor {
        public static float[] rgb(int color) {
            return new float[]{(float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f, (float)(color >> 24 & 0xFF) / 255.0f};
        }

        public static int rgba(int r, int g, int b, int a) {
            return a << 24 | r << 16 | g << 8 | b;
        }

        public static int rgb(int r, int g, int b) {
            return 0xFF000000 | r << 16 | g << 8 | b;
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
    }
}

