package hr.nipeta.cac.collatz;

import hr.nipeta.cac.Main;
import hr.nipeta.cac.SceneBuilder;
import hr.nipeta.cac.collatz.rules.CollatzCell;
import hr.nipeta.cac.model.Coordinates;
import hr.nipeta.cac.model.gui.PeriodicAnimationTimer;
import hr.nipeta.cac.model.gui.PeriodicAnimationTimerGuiControl;
import hr.nipeta.cac.welcome.WelcomeSceneBuilder;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static hr.nipeta.cac.model.gui.SceneUtils.createButton;

@Slf4j
public class CollatzSceneBuilder extends SceneBuilder {

    private CollatzLogic collatzLogic;
    private Map<Long, CollatzSequenceDraw<Boolean>> collatzSequencesDraw;

    private PeriodicAnimationTimerGuiControl timerControl;

    private Canvas canvas;

    public CollatzSceneBuilder(Main main) {
        super(main);
    }

    @Override
    public Scene createContent() {

        collatzLogic = new CollatzLogic();
        collatzSequencesDraw = createCollatzSequences(5,1_000, new Coordinates<>(800.0, 600.0));

        timerControl = new PeriodicAnimationTimerGuiControl(
                PeriodicAnimationTimer.every(10).execute(this::drawCollatzSequences));

        canvas = new Canvas(1600, 1200);
        StackPane wrapper = new StackPane(canvas);
        wrapper.getStyleClass().add("bg-dark");

        Region parent = new VBox(10, mainMenu(), wrapper);
        parent.setPadding(new Insets(10));
        return new Scene(parent);

    }

    private Node mainMenu() {
        return horizontalMenu(
                timerControl.getStartButton(),
                timerControl.getStopButton(),
                timerControl.getDurationInput(),
                welcomeScreenButton()
        );
    }

    private Map<Long, CollatzSequenceDraw<Boolean>> createCollatzSequences(long start, long end, Coordinates<Double> startCoordinates) {

        long milli = System.currentTimeMillis();

        Map<Long, CollatzSequenceDraw<Boolean>> allSequences = new HashMap<>();

        double progressStep = 0.1d;

        for (long i = start; i <= end; i++) {

            List<CollatzCell<Boolean>> sequence = collatzLogic.createSequence(i, CollatzCell::new);
            Paint paint = palette.get((int)(i % 10));

            allSequences.put(i, new CollatzSequenceDraw<>(sequence, progressStep, startCoordinates, paint));

        }

        log.debug("created {} sequences in {}ms", allSequences.size(), (System.currentTimeMillis() - milli));

        return allSequences;

    }

    private void drawCollatzSequences() {

        long milli = System.currentTimeMillis();

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(1.0); // Line width

        double radius = 15;

        collatzSequencesDraw.forEach((index, sequenceDraw) -> {

            if (sequenceDraw.getCurrentCellIndex() >= sequenceDraw.getSequence().size()) {
                return;
            }

            CollatzCell<Boolean> cell = sequenceDraw.getSequence().get(sequenceDraw.getCurrentCellIndex());

            // Where are we
            Coordinates<Double> currentCoordinate = sequenceDraw.getCurrentCoordinate();
            double progress = sequenceDraw.getProgress();
            double progressStep = sequenceDraw.getProgressStep();
            double currentAngleDegrees = sequenceDraw.getCurrentAngleDegrees();

            // Draw new line segment

            double x1 = currentCoordinate.getX();
            double y1 = currentCoordinate.getY();

            double currentAngleRadians = Math.toRadians(currentAngleDegrees);
            double x2 = x1 + radius * progressStep * Math.cos(currentAngleRadians);
            double y2 = y1 - radius * progressStep * Math.sin(currentAngleRadians);

            gc.setStroke(sequenceDraw.getPaint());
            gc.strokeLine(x1, y1, x2, y2);

            sequenceDraw.setCurrentCoordinate(new Coordinates<>(x2, y2));

            // Update tracking
            progress += progressStep;
            sequenceDraw.setProgress(progress);

            if (Math.abs(progress - 1) < 0.000001) {

                gc.strokeOval(currentCoordinate.getX() - 1, currentCoordinate.getY() - 1, 2, 2);

                sequenceDraw.setProgress(0);
                sequenceDraw.setCurrentCellIndex(sequenceDraw.getCurrentCellIndex() + 1);

                if (cell.getProperty()) {
                    sequenceDraw.setCurrentAngleDegrees(currentAngleDegrees + 15);
                } else {
                    sequenceDraw.setCurrentAngleDegrees(currentAngleDegrees - 15);
                }


            }

        });

        log.debug("drew sequences in {}ms", (System.currentTimeMillis() - milli));

    }

    public static final List<Paint> palette;

    static {

        Color baseColor = Color.web("#4DB6AC"); // Base teal blue

        palette = Arrays.asList(
                baseColor,
                baseColor.deriveColor(0, 1.1, 0.9, 1),
                baseColor.deriveColor(10, 1, 1, 0.95),      // Slight hue shift, reduced opacity
                baseColor.deriveColor(20, 0.9, 1.1, 1),      // Slight hue shift, slightly brighter
                baseColor.deriveColor(-10, 1, 0.9, 1),      // Hue shift towards green, darker
                baseColor.interpolate(Color.WHITE, 0.2),    // Blended with white (pastel)
                baseColor.interpolate(Color.BLACK, 0.2),     // Blended with black (darker shade)
                baseColor.deriveColor(30, 1.1, 1, 1),     // Another hue variation
                baseColor.deriveColor(-30, 0.95, 0.95, 1),   // Shift toward blue-green, softer
                baseColor.deriveColor(5, 1.05, 1.05, 1)
        );
    }

    private Button welcomeScreenButton() {
        return createButton(
                "Main menu",
                e -> createScene(() -> new WelcomeSceneBuilder(main)));
    }

    @Data
    @AllArgsConstructor
    private static class CollatzSequenceDraw<T> {
        private List<CollatzCell<T>> sequence;
        private double progress;
        private double progressStep;
        private int currentCellIndex;
        private Coordinates<Double> currentCoordinate;
        private double currentAngleDegrees;
        private Paint paint;

        public CollatzSequenceDraw(List<CollatzCell<T>> sequence, double progressStep, Coordinates<Double> startCoordinates, Paint paint) {
            this(sequence, 0, progressStep, 0, startCoordinates, 45, paint);
        }

    }

}
