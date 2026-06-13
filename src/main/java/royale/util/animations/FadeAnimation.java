package royale.util.animations;
public class FadeAnimation {
private final long duration;
private long startTime;
public long getDuration() { return this.duration; }
public long getStartTime() { return this.startTime; } public boolean isForwards() {
return this.forwards;
} private boolean forwards = true; private double value = 0.0D; public double getValue() { return this.value; }
private Easing easing = Easings.EXPO_OUT; public Easing getEasing() { return this.easing; }
public FadeAnimation(long durationMs) {
this.duration = durationMs;
this.startTime = System.currentTimeMillis();
}
public FadeAnimation(long durationMs, Easing easing) {
this.duration = durationMs;
this.startTime = System.currentTimeMillis();
this.easing = easing;
}
public void switchDirection(boolean forwards) {
if (this.forwards != forwards) {
long elapsed = System.currentTimeMillis() - this.startTime;
long remaining = this.duration - Math.min(elapsed, this.duration);
this.startTime = System.currentTimeMillis() - remaining;
this.forwards = forwards;
} 
}
public void setDirection(boolean forwards) {
this.forwards = forwards;
}
public void reset() {
this.startTime = System.currentTimeMillis();
this.value = this.forwards ? 0.0D : 1.0D;
}
public float get() {
long elapsed = System.currentTimeMillis() - this.startTime;
double progress = Math.min(elapsed / this.duration, 1.0D);
double easedProgress = this.easing.ease(progress);
if (this.forwards) {
this.value = easedProgress;
} else {
this.value = 1.0D - easedProgress;
} 
return (float)Math.max(0.0D, Math.min(1.0D, this.value));
}
public boolean isDone() {
return (System.currentTimeMillis() - this.startTime >= this.duration);
}
public boolean isFullyHidden() {
return (isDone() && !this.forwards);
}
public boolean isFullyVisible() {
return (isDone() && this.forwards);
}
}


