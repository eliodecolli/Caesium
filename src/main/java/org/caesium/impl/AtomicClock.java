package org.caesium.impl;

import org.caesium.base.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.function.Consumer;

/**
 * The main implementation of an Atomic Clock unit.
 * Used as the underlying time-keeping mechanism and only source of truth related to time in the core Caesium Task Scheduler.
 */
@Service
public class AtomicClock implements Clock {
    // a tick rate is the delay between each consecutive clock ticks (in seconds)
    // ex: if tick rate is set to 1, the main clock loop will run every second
    private int tickRate;

    // the callback to invoke in each clock tick
    private Consumer<Long> callback;

    // the thread used to run the time keeping loop
    private final Thread mainThread;

    // timestamp of the last tick
    private long lastTick;

    // used to lock invocations inside a synchronized block
    private final Object lock;

    private static final Logger log = LoggerFactory.getLogger(AtomicClock.class);

    /**
     * Creates a new instance of an Atomic Clock.
     */
    public AtomicClock() {
        this.lastTick = 0;
        this.lock = new Object();

        this.mainThread = new Thread(this::tick);
    }

    private void tick() {
        while (true) {
            long currentTime = Instant.now().getEpochSecond();

            // we do this inside a synchronized block in order to avoid concurrency issues
            synchronized (lock) {
                long elapsed = currentTime - lastTick;

                // if enough time has passed (meaning either the desired tick rate or more), run the callback
                if (elapsed >= tickRate) {
                    callback.accept(currentTime);
                }

                lastTick = currentTime;
            }

            try {
                // recalculate the "now" after the procedure is done
                // the idea is that if we have a tick rate of 4 seconds, but the last tick was 1 second late
                // the next one should happen after 3 seconds to resync the clock
                long elapsed = Instant.now().getEpochSecond() - lastTick;
                long remaining = tickRate - elapsed;

                // convert the value inside sleep to milliseconds
                Thread.sleep(Math.max(0, remaining) * 1000L);
            }
            catch (InterruptedException ex) {
                log.error("[x] atomic clock has received interrupt signal");
                break;
            }
        }

        log.info("[x] atomic clock stopped");
    }

    /**
     * Starts the Atomic Clock.
     * @param tickRate The delay between each clock tick. Represented in seconds.
     * @param callback The callback function to invoke per tick.
     */
    public void start(int tickRate, Consumer<Long> callback) {
        this.tickRate = tickRate;
        this.callback = callback;
        mainThread.start();
    }

    /**
     * Stops the running clock.
     */
    public void stop() {
        mainThread.interrupt();
    }
}
