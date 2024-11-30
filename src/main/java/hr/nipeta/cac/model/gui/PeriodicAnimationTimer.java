package hr.nipeta.cac.model.gui;

import javafx.animation.AnimationTimer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class PeriodicAnimationTimer extends AnimationTimer {

    private long lastUpdate = 0;

    @Getter
    @Setter
    private long durationMs;

    /**
     * Useful flag so we know if this timer is currently active
     */
    @Getter
    private boolean playing;

    final private Runnable onTick;

    // TODO do we even need method inside timer? maybe it's ok just to start/stop it?
    private PeriodicAnimationTimer(long timerDurationMs, Runnable onTick) {
        this.durationMs = timerDurationMs;
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

        if (delta > durationMs) {
            lastUpdate = now;
            onTick.run();
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