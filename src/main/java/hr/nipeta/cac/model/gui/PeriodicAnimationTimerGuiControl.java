package hr.nipeta.cac.model.gui;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import lombok.Getter;

import static hr.nipeta.cac.model.gui.SceneUtils.*;

@Getter
public class PeriodicAnimationTimerGuiControl extends Region {

    private PeriodicAnimationTimer timer;

    private Button startButton;
    private Button stopButton;

    private TextField durationInput;

    public PeriodicAnimationTimerGuiControl(PeriodicAnimationTimer timer) {
        this.timer = timer;
        this.startButton = createButton("Start", event -> {
            if (!timer.isPlaying()) {
                //evolveAndDrawGrid(); // TODO do we even need method inside timer?
                timer.start();
            }
        });
        this.stopButton = createButton("Stop", event -> {
            if (timer.isPlaying()) {
                timer.stop();
            }
        });
        this.durationInput = createInput(
                timer.getDurationMs(),
                150,
                createTooltip("Frequency in milliseconds (press Enter to change)"),
                this::onTimelineDurationInputSubmit
        );
    }

    private void onTimelineDurationInputSubmit() {

        Integer msDuration = parseTimelineDurationInput(durationInput.getText());

        if (msDuration != null) {
            stopToExecuteThenRestart(() -> {
                timer.setDurationMs(msDuration);
                durationInput.setPromptText("" + msDuration);
            });
        }

    }

    private Integer parseTimelineDurationInput(String input) {
        try {
            int intInput = Integer.parseInt(input);
            if (intInput < 3 || intInput > 10_000) {
                showAlertError("Frequency must be between 3ms and 10000ms.");
                return null;
            } else {
                return intInput;
            }
        } catch (NumberFormatException ex) {
            showAlertError("Invalid number. Please enter a valid number.");
            return null;
        }
    }

    /**
     * <ol>
     *     <li>If {@link PeriodicAnimationTimer#isPlaying() timer is playing} at the beginning of this method,
     *     {@link PeriodicAnimationTimer#stop() stop it}</li>
     *     <li>Execute the input parameter {@code runnable}</li>
     *     <li>If timer was {@link PeriodicAnimationTimer#isPlaying() playing} at the <b>beginning</b> of this method,
     *     {@link PeriodicAnimationTimer#start() start it}</li>
     * </ol>
     *
     * @param runnable void method with no arguments we'd like to execute
     */
    public void stopToExecuteThenRestart(Runnable runnable) {

        final boolean wasPlaying = this.timer.isPlaying();

        if (this.timer.isPlaying()) {
            this.timer.stop();
        }

        runnable.run();

        if (!this.timer.isPlaying() && wasPlaying) {
            this.timer.start();
        }

    }

}