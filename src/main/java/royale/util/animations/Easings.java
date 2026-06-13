package royale.util.animations;
public final class Easings { public static final Easing LINEAR; public static final Easing QUAD_OUT;
public static final Easing CUBIC_OUT;
public static final Easing EXPO_IN;
private Easings() { throw new UnsupportedOperationException("This is a utility class and cannot be instantiated"); } public static final Easing EXPO_OUT; public static final Easing EXPO_IN_OUT; public static final Easing SINE_OUT; public static final Easing BACK_OUT; static {
LINEAR = (value -> value);
QUAD_OUT = (value -> 1.0D - Math.pow(1.0D - value, 2.0D));
CUBIC_OUT = (value -> 1.0D - Math.pow(1.0D - value, 3.0D));
EXPO_IN = (value -> (value == 0.0D) ? 0.0D : Math.pow(2.0D, 10.0D * value - 10.0D));
EXPO_OUT = (value -> (value == 1.0D) ? 1.0D : (1.0D - Math.pow(2.0D, -10.0D * value)));
EXPO_IN_OUT = (value -> 
(value == 0.0D || value == 1.0D) ? value : ((value < 0.5D) ? (Math.pow(2.0D, 20.0D * value - 10.0D) / 2.0D) : ((2.0D - Math.pow(2.0D, -20.0D * value + 10.0D)) / 2.0D)));
SINE_OUT = (value -> Math.sin(value * Math.PI / 2.0D));
BACK_OUT = (value -> {
double c1 = 1.70158D;
double c3 = c1 + 1.0D;
return 1.0D + c3 * Math.pow(value - 1.0D, 3.0D) + c1 * Math.pow(value - 1.0D, 2.0D);
});
} }


