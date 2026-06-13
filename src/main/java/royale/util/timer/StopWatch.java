package royale.util.timer;
public class StopWatch
{
private long startTime;
public long getStartTime() {
return this.startTime;
}
public StopWatch() {
reset();
}
public boolean finished(double delay) {
return (System.currentTimeMillis() - delay >= this.startTime);
}
public boolean every(double delay) {
boolean finished = finished(delay);
if (finished) reset(); 
return finished;
}
public void reset() {
this.startTime = System.currentTimeMillis();
}
public long elapsedTime() {
return System.currentTimeMillis() - this.startTime;
}
public StopWatch setMs(long ms) {
this.startTime = System.currentTimeMillis() - ms;
return this;
}
}


