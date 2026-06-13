package royale.util.animations;
public class SmoothAnimation {
private long start;
private double duration;
private double fromValue;
public long getStart() { return this.start; } private double toValue; private double value; private double prevValue; public double getDuration() {
return this.duration; }
public double getFromValue() { return this.fromValue; }
public double getToValue() { return this.toValue; }
public void setValue(double value) { this.value = value; }
public double getValue() { return this.value; } public double getPrevValue() {
return this.prevValue;
} private Easing easing = Easings.EXPO_OUT; public Easing getEasing() { return this.easing; }
public SmoothAnimation run(double valueTo, double durationSeconds) {
return run(valueTo, durationSeconds, Easings.EXPO_OUT, false);
}
public SmoothAnimation run(double valueTo, double durationSeconds, Easing easing) {
return run(valueTo, durationSeconds, easing, false);
}
public SmoothAnimation run(double valueTo, double durationSeconds, boolean safe) {
return run(valueTo, durationSeconds, Easings.EXPO_OUT, safe);
}
public SmoothAnimation run(double valueTo, double durationSeconds, Easing easing, boolean safe) {
if (check(safe, valueTo)) {
return this;
}
this.easing = easing;
this.start = System.currentTimeMillis();
this.duration = durationSeconds * 1000.0D;
this.fromValue = this.value;
this.toValue = valueTo;
return this;
}
public boolean update() {
this.prevValue = this.value;
boolean alive = isAlive();
if (alive) {
double part = Math.min(1.0D, Math.max(0.0D, calculatePart()));
this.value = interpolate(this.fromValue, this.toValue, this.easing.ease(part));
} else {
this.start = 0L;
this.value = this.toValue;
} 
return alive;
}
public boolean isAlive() {
return !isFinished();
}
public boolean isFinished() {
return (calculatePart() >= 1.0D);
}
public double calculatePart() {
if (this.duration <= 0.0D) return 1.0D; 
return (System.currentTimeMillis() - this.start) / this.duration;
}
public boolean check(boolean safe, double valueTo) {
return (safe && isAlive() && (valueTo == this.fromValue || valueTo == this.toValue || valueTo == this.value));
}
public double interpolate(double start, double end, double pct) {
return start + (end - start) * pct;
}
public float get() {
return (float)this.value;
}
public float getPrev() {
return (float)this.prevValue;
}
public void set(double value) {
run(value, 1.0E-4D);
update();
this.value = value;
}
}


