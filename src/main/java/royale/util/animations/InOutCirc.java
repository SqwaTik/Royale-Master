package royale.util.animations;
public class InOutCirc
extends Animation
{
public double calculation(double value) {
double x = value / this.ms;
return (x < 0.5D) ? ((
1.0D - Math.sqrt(1.0D - Math.pow(2.0D * x, 2.0D))) / 2.0D) : ((
Math.sqrt(1.0D - Math.pow(-2.0D * x + 2.0D, 2.0D)) + 1.0D) / 2.0D);
}
}


