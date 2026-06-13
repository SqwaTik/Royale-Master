package royale.util.math;

import java.util.ArrayDeque;
import java.util.Deque;

public class FrameRateCounter {
    public static final FrameRateCounter INSTANCE = new FrameRateCounter();

    private final Deque<Long> records = new ArrayDeque<>();
    private int fps = 1;

    public void recordFrame() {
        long now = System.currentTimeMillis();
        long cutoff = now - 1000L;

        this.records.addLast(now);
        while (!this.records.isEmpty() && this.records.peekFirst() < cutoff) {
            this.records.removeFirst();
        }

        this.fps = Math.max(1, this.records.size());
    }

    public int getFps() {
        return this.fps;
    }
}
