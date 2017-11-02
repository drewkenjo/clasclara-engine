package sys.util;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Every required seconds the flag will be up.
 * Created by gurjyan on 10/2/17.
 */
public class TimerFlag {
    private AtomicBoolean up = new AtomicBoolean(false);

    /**
     * Constructor.
     * Schedules a Timer to wake up every provided seconds.
     *
     * @param seconds period in seconds
     */
    public TimerFlag(int seconds) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new ChangeFlag(), 0, seconds * 1000);
    }

    /**
     * Resets the flag.
     */
    public void reset() {
        up.set(false);
    }

    /**
     * Checks id the flag is up.
     * @return true if time is up.
     */
    public boolean isUp() {
        return up.get();
    }

    private class ChangeFlag extends TimerTask {
        @Override
        public void run() {
            up.set(true);
        }
    }
}
