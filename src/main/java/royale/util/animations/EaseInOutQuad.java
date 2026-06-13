package royale.util.animations;
public class EaseInOutQuad
extends Animation
{
public double calculation(double value) {
double x = value / this.ms;
return (x < 0.5D) ? (2.0D * x * x) : (1.0D - Math.pow(-2.0D * x + 2.0D, 2.0D) / 2.0D);
}
}


