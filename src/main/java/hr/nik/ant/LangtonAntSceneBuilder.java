package hr.nik.ant;

import hr.nik.Main;
import hr.nik.SceneBuilder;
import hr.nik.model.Coordinates;
import hr.nik.welcome.WelcomeSceneBuilder;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LangtonAntSceneBuilder extends SceneBuilder {

    private static final int GRID_SIZE_X = 256;
    private static final int GRID_SIZE_Y = 256;

    private static LangtonAntLogic antLogic;

    private static final double RECT_SIZE = 5;
    private static final double RECT_BORDER_WIDTH = 0;
    private static final double RECT_TOTAL_SIZE = RECT_SIZE + RECT_BORDER_WIDTH;

    private double timerDuration;
    private TextField timerDurationInput;
    private boolean timerPlaying;

    private AnimationTimer timer;

    private GraphicsContext gc;

    public LangtonAntSceneBuilder(Main main) {
        super(main);
    }

    @Override
    public Scene build() {

        antLogic = new LangtonAntLogic(GRID_SIZE_X, GRID_SIZE_Y);
        antLogic.init();

        timerDuration = 75;

        timerDurationInput = new TextField();
        timerDurationInput.setPrefWidth(150);
        timerDurationInput.setPromptText("" + timerDuration);

        timer = new AnimationTimer() {
            long lastUpdate = 0;

            @Override
            public void handle(long now) {

                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                double delta = (now - lastUpdate) / 1e6; // milliseconds since last frame

                if (delta > timerDuration) {
                    log.debug("Timer duration is {}ms", timerDuration);
                    lastUpdate = now;
                    evolveAndDraw();
                }

            }
        };

        Region parent = new VBox(10, mainMenu(), antGridWrapped());
        parent.setPadding(new Insets(10));
        return new Scene(parent);

    }

    private Node mainMenu() {
        return horizontalMenu(
                startButton(),
                stopButton(),
                stepButton(),
                timerDurationInput(),
                timerDurationButton(),
                welcomeScreenButton()
        );
    }

    private Button startButton() {
        return createButton("Start", event -> {
            if (!timerPlaying) {
                evolveAndDraw();
                timer.start();
                timerPlaying = true;
            }
        });
    }

    private Button stopButton() {
        return createButton("Stop", event -> {
            if (timerPlaying) {
                timer.stop();
                timerPlaying = false;
            }
        });
    }

    private Button stepButton() {
        return createButton("Step", event -> evolveAndDraw());
    }

    private Node timerDurationInput() {
        return onTextInputEnter(timerDurationInput, this::onTimerDurationInputSubmit);
    }

    private Button timerDurationButton() {
        return createButton("Set ms", event -> onTimerDurationInputSubmit());
    }

    private void onTimerDurationInputSubmit() {
        String input = timerDurationInput.getText();
        Integer msDuration = null;
        try {
            int intInput = Integer.parseInt(input);
            if (intInput < 20 || intInput > 20_000) {
                showAlertError("Frequency must be between 20ms and 20000ms.");
            } else {
                msDuration = intInput;
            }
        } catch (NumberFormatException ex) {
            showAlertError("Invalid number. Please enter a valid number.");
        }
        if (msDuration != null) {
            if (timerPlaying) {
                timer.stop();
                timerPlaying = false;
            }
            timerDuration = msDuration;
            timerDurationInput.setPromptText("" + msDuration);
            if (!timerPlaying) {
                timer.start();
                timerPlaying = true;
            }
        }
    }

    private Button welcomeScreenButton() {
        return createButton(
                "Main menu",
                e -> createScene(() -> new WelcomeSceneBuilder(main)));
    }

    private Node antGridWrapped() {
        return new Pane(antGrid());
    }

    private Node antGrid() {

        Canvas canvas = new Canvas(GRID_SIZE_X * RECT_TOTAL_SIZE, GRID_SIZE_Y * RECT_TOTAL_SIZE);
        gc = canvas.getGraphicsContext2D();

        drawGrid(gc);

        return canvas;

    }

    private void evolveAndDraw() {
        long milli = System.currentTimeMillis();
        antLogic.evolve();
        drawGrid(gc);
        log.debug("Evolved in {}ms", System.currentTimeMillis()- milli);
    }

    private void drawGrid(GraphicsContext gc) {

        // Clear the canvas before redrawing
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        for (int row = 0; row < GRID_SIZE_Y; row++) {
            for (int col = 0; col < GRID_SIZE_X; col++) {
                drawCell(gc, col, row);
            }
        }

        drawAnt(gc);

    }

    private void drawCell(GraphicsContext gc, int col, int row) {

        double x = col * RECT_TOTAL_SIZE;
        double y = row * RECT_TOTAL_SIZE;

        // Draw cell background
        gc.setFill(antLogic.isAlive(row, col) ? Color.DARKGREEN : Color.WHEAT);
        gc.fillRect(x, y, RECT_SIZE, RECT_SIZE);

    }

    private void drawAnt(GraphicsContext gc) {

        Polygon ant = new Polygon();
        ant.getPoints().addAll(
                -10.0, 0.0,  // Left side of arrowhead
                10.0, 0.0,   // Right side of arrowhead
                0.0, -20.0   // Tip of the arrow
        );

        Coordinates<Integer> coordinates = antLogic.getAntState().getCoordinates();
        double centerX = coordinates.getX() * RECT_TOTAL_SIZE + (RECT_TOTAL_SIZE / 2);
        double centerY = coordinates.getY() * RECT_TOTAL_SIZE + (RECT_TOTAL_SIZE / 2);

        ant.setTranslateX(centerX);
        ant.setTranslateY(centerY);

        ant.setRotate(antLogic.getAntState().getDirection().getDegrees());

        gc.setFill(Color.RED);
        gc.fill();

    }

}
