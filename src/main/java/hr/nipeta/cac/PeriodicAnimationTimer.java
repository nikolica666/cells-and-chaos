package hr.nipeta.cac;

import javafx.animation.AnimationTimer;
import javafx.util.Duration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class PeriodicAnimationTimer extends AnimationTimer {

    private long lastUpdate = 0;

    @Getter
    @Setter
    private long timerDurationMs;

    /**
     * Useful flag so we know if this timer is currently active
     */
    @Getter
    private boolean playing;

    final private Runnable onTick;

    private PeriodicAnimationTimer(long timerDurationMs, Runnable onTick) {
        this.timerDurationMs = timerDurationMs;
        this.onTick = onTick;
    }

    public static PeriodicAnimationTimerBuildMs every(long miliSeconds) {
        return new PeriodicAnimationTimerBuildMs(miliSeconds);
    }

    @AllArgsConstructor
    public static class PeriodicAnimationTimerBuildMs {
        private long duration;
        public PeriodicAnimationTimer execute(Runnable runnable) {
            return new PeriodicAnimationTimer(duration, runnable);
        }
    }

    @Override
    public void handle(long now) {

        if (lastUpdate == 0) {
            lastUpdate = now;
            return;
        }

        double delta = (now - lastUpdate) / 1e6; // milliseconds since last frame

        if (delta > timerDurationMs) {
            lastUpdate = now;
            onTick.run();
        }

    }

    /**
     * <ol>
     *     <li>If this is {@link #playing} at the beginning of this method, {@link #stop() stop it}</li>
     *     <li>Execute the input parameter {@code runnable}</li>
     *     <li>If this was {@link #playing} at the <b>beginning</b> of this method, {@link #start() start it}</li>
     * </ol>
     *
     * @param runnable void method with no arguments we'd like to execute
     */
    public void stopToExecuteThenRestart(Runnable runnable) {

        final boolean wasPlaying = this.isPlaying();

        if (this.isPlaying()) {
            this.stop();
        }

        runnable.run();

        if (!this.isPlaying() && wasPlaying) {
            this.start();
        }

    }

    /**
     * <p>Calls {@link AnimationTimer#start()} and sets {@link #playing} to <b>true</b> (below is inherited JavaDoc)</p>
     *
     * {@inheritDoc}
     */
    @Override
    public void start() {
        super.start();
        this.playing = true;
    }

    /**
     * <p>Calls {@link AnimationTimer#stop()} and sets {@link #playing} to <b>false</b> (below is inherited JavaDoc)</p>
     *
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        super.stop();
        this.playing = false;
    }

}