package royale.util.animations;
public class Decelerate
extends Animation
{
public double calculation(double value) {
double x = value / this.ms;
return 1.0D - (x - 1.0D) * (x - 1.0D);
}
}


