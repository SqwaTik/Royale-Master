package royale.util.animations;
public class OutBack
extends Animation
{
public double calculation(double value) {
double x = value / this.ms;
double c1 = 1.70158D;
double c3 = c1 + 1.0D;
return 1.0D + c3 * Math.pow(x - 1.0D, 3.0D) + c1 * Math.pow(x - 1.0D, 2.0D);
}
}


