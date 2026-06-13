package royale.util.animations;
import royale.util.timer.TimerUtil;
public class GuiAnimation {
public GuiAnimation setMs(int ms) {
this.ms = ms; return this; } public GuiAnimation setValue(double value) { this.value = value; return this; }
public final TimerUtil counter = new TimerUtil();
protected int ms = 250;
protected double value = 1.0D;
protected Direction direction = Direction.FORWARDS;
public GuiAnimation reset() {
this.counter.resetCounter();
return this;
}
public boolean isDone() {
return this.counter.isReached(this.ms);
}
public boolean isFinished(Direction direction) {
return (this.direction == direction && isDone());
}
public Direction getDirection() {
return this.direction;
}
public GuiAnimation setDirection(Direction direction) {
if (this.direction != direction) {
this.direction = direction;
}
return this;
}
public Double getOutput() {
double progress = Math.min(1.0D, this.counter.getTime() / this.ms);
double eased = easeOutQuart(progress);
if (this.direction == Direction.FORWARDS) {
return Double.valueOf(eased * this.value);
}
return Double.valueOf((1.0D - eased) * this.value);
}
private double easeOutQuart(double x) {
return 1.0D - Math.pow(1.0D - x, 4.0D);
}
}


