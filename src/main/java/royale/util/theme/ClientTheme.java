package royale.util.theme;
import royale.modules.impl.misc.Client;
import royale.util.ColorUtil;
public final class ClientTheme
{
public static int accent() {
return Client.getResolvedClientColor();
}
public static int accentWithAlpha(int alpha) {
return ColorUtil.setAlpha(accent(), clamp(alpha));
}
public static int blendWithAccent(int color, float amount) {
return ColorUtil.interpolateColor(color, accent(), clamp01(amount));
}
public static int blendWithAccentAndAlpha(int color, float amount, int alpha) {
return ColorUtil.setAlpha(blendWithAccent(color, amount), clamp(alpha));
}
private static int clamp(int value) {
return Math.max(0, Math.min(255, value));
}
private static float clamp01(float value) {
return Math.max(0.0F, Math.min(1.0F, value));
}
}


