package royale.util.animations;
import royale.util.timer.TimerUtil;
public class Animation implements AnimationCalculation {
public Animation setMs(int ms) {
this.ms = ms; return this; } public Animation setValue(double value) { this.value = value; return this; }
public final TimerUtil counter = new TimerUtil();
protected int ms;
protected double value;
protected Direction direction = Direction.FORWARDS;
public void reset() {
this.counter.resetCounter();
}
public void update() {}
public boolean isDone() {
return this.counter.isReached(this.ms);
}
public boolean isFinished(Direction direction) {
return (this.direction == direction && isDone());
}
public Direction getDirection() {
return this.direction;
}
public void setDirection(Direction direction) {
if (this.direction != direction) {
this.direction = direction;
adjustTimer();
} 
}
public boolean isDirection(Direction direction) {
return (this.direction == direction);
}
private void adjustTimer() {
this.counter.setTime(
System.currentTimeMillis() - this.ms - Math.min(this.ms, this.counter.getTime()));
}
public Double getOutput() {
double time = (1.0D - calculation(this.counter.getTime())) * this.value;
return Double.valueOf((this.direction == Direction.FORWARDS) ? 
endValue() : (
isDone() ? 0.0D : time));
}
protected double endValue() {
return isDone() ? 
this.value : (
calculation(this.counter.getTime()) * this.value);
}
}


