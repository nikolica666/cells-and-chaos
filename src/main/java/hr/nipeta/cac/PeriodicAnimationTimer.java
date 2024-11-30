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

    @Getter
    private boolean playing;

    public void stopToExecuteThenRestart(Runnable runnable) {

        if (this.isPlaying()) {
            this.stop();
        }

        runnable.run();

        if (!this.isPlaying()) {
            this.start();
        }

    }

    @Override
    public void start() {
        super.start();
        this.playing = true;
    }

    @Override
    public void stop() {
        super.stop();
        this.playing = false;
    }

}