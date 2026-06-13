package royale.util.animations;
public class InOutBack
extends Animation
{
public double calculation(double value) {
double x = value / this.ms;
double c1 = 1.70158D;
double c2 = c1 * 1.525D;
return (x < 0.5D) ? (
Math.pow(2.0D * x, 2.0D) * ((c2 + 1.0D) * 2.0D * x - c2) / 2.0D) : ((
Math.pow(2.0D * x - 2.0D, 2.0D) * ((c2 + 1.0D) * (x * 2.0D - 2.0D) + c2) + 2.0D) / 2.0D);
}
}


