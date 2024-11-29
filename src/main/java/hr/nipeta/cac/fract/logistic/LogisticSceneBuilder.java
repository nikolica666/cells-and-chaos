package hr.nipeta.cac.fract.logistic;

import hr.nipeta.cac.Main;
import hr.nipeta.cac.SceneBuilder;
import hr.nipeta.cac.ca.CellularAutomataLogic;
import hr.nipeta.cac.welcome.WelcomeSceneBuilder;
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
import lombok.extern.slf4j.Slf4j;

import javax.crypto.NoSuchPaddingException;
import java.util.List;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

@Slf4j
public class LogisticSceneBuilder extends SceneBuilder {

    private static final int GRID_SIZE_X = 2200;
    private static final int GRID_SIZE_Y = 1100;

    private static LogisticLogic logic;

    private static final double RECT_SIZE = 1;
    private static final double RECT_BORDER_WIDTH = 0;
    private static final double RECT_TOTAL_SIZE = RECT_SIZE + RECT_BORDER_WIDTH;

    private double timeLineDuration;
    private TextField timelineDurationInput;
    private boolean timelinePlaying;

    private AnimationTimer timer;

    private int rule;
    private TextField ruleInput;

    private GraphicsContext gc;

    public LogisticSceneBuilder(Main main) {
        super(main);
    }

    @Override
    public Scene createContent() {

        timeLineDuration = 3;

        timelineDurationInput = new TextField();
        timelineDurationInput.setPrefWidth(150);
        timelineDurationInput.setPromptText("" + timeLineDuration);

        timer = new AnimationTimer() {
            long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                double delta = (now - lastUpdate) / 1e6; // milliseconds since last frame

                if (delta > timeLineDuration) {
                    log.debug("Timer duration is {}ms", timeLineDuration);
                    lastUpdate = now;
                    evolveAndDraw();
                }

            }
        };

        ruleInput = new TextField();
        ruleInput.setPrefWidth(75);
        ruleInput.setPromptText("" + rule);

        Region parent = new VBox(10, mainMenu(), caGridWrapped());
        parent.setPadding(new Insets(10));
        return new Scene(parent);

    }

    private Node mainMenu() {
        return horizontalMenu(
                startButton(),
                stopButton(),
                stepButton(),
                timelineDurationInput(),
                timelineDurationButton(),
                welcomeScreenButton()
        );
    }

    private Button startButton() {
        return createButton("Start", event -> {
            if (!timelinePlaying) {
                evolveAndDraw();
                timer.start();
                timelinePlaying = true;
            }
        });
    }

    private Button stopButton() {
        return createButton("Stop", event -> stopTimeline());
    }

    private void stopTimeline() {
        if (timelinePlaying) {
            timer.stop();
            timelinePlaying = false;
        }
    }

    private Button stepButton() {
        return createButton("Step", event -> evolveAndDraw());
    }

    private Node timelineDurationInput() {
        return onTextInputEnter(timelineDurationInput, this::onTimelineDurationInputSubmit);
    }

    private Button timelineDurationButton() {
        return createButton("Set ms", event -> onTimelineDurationInputSubmit());
    }

    private void onTimelineDurationInputSubmit() {
        String input = timelineDurationInput.getText();
        Integer msDuration = null;
        try {
            int intInput = Integer.parseInt(input);
            if (intInput < 4 || intInput > 30_000) {
                showAlertError("Frequency must be between 4ms and 30000ms.");
            } else {
                msDuration = intInput;
            }
        } catch (NumberFormatException ex) {
            showAlertError("Invalid number. Please enter a valid number.");
        }
        if (msDuration != null) {
            if (timelinePlaying) {
                timer.stop();
                timelinePlaying = false;
            }
            timeLineDuration = msDuration;
            timelineDurationInput.setPromptText("" + timeLineDuration);
            if (!timelinePlaying) {
                timer.start();
                timelinePlaying = true;
            }
        }
    }

    private Button welcomeScreenButton() {
        return createButton(
                "Main menu",
                e -> createScene(() -> new WelcomeSceneBuilder(main)));
    }

    private Node caGridWrapped() {
        return caGrid();
    }

    private Node caGrid() {

        Canvas canvas = new Canvas(GRID_SIZE_X * RECT_TOTAL_SIZE, GRID_SIZE_Y * RECT_TOTAL_SIZE);
        gc = canvas.getGraphicsContext2D();

        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        gc.setFill(Color.WHEAT);
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        return new Pane(canvas);

    }

    private void drawCell(GraphicsContext gc, int col, int row) {

        double x = col * RECT_TOTAL_SIZE;
        double y = row * RECT_TOTAL_SIZE;

        // Draw cell background
        gc.setFill(Color.DARKGREEN);
        gc.fillRect(x, y, RECT_SIZE, RECT_SIZE);

    }

    private double minXAxis = 1.5;
    private double currentXAxis = minXAxis;
    private double stepXAxis = 0.00125;
    private double startingPopulation = 0.5;
    private double maxXAxis = 4;

    private double numberOfPoints = (maxXAxis - minXAxis) / stepXAxis;

    private int paddingX = 50;
    private int paddingY = 200;

    private double pixelsPerPoint = (GRID_SIZE_X - paddingX) / numberOfPoints;

    private int currentPoint() {return (int)((currentXAxis - minXAxis) / (maxXAxis - minXAxis) * numberOfPoints);}

    private void evolveAndDraw() {
        long milli = System.currentTimeMillis();

        logic = new LogisticLogic(currentXAxis, 1e-3);
        List<Double> doubles = logic.calculateAndDetectPeriodicity(startingPopulation, 7_000);

        drawGrid(gc, doubles);
        log.debug("Evolved in {}ms", System.currentTimeMillis()- milli);

        currentXAxis += stepXAxis;

        if (currentXAxis >= maxXAxis) {
            stopTimeline();
        }

    }

    private void drawGrid(GraphicsContext gc, List<Double> yAxisPoints) {

        gc.setFill(Color.RED);

        for (double yAxisPoint : yAxisPoints) {log.debug("current point is {}, pixels per point={}",currentPoint(), pixelsPerPoint);
            gc.fillOval(
                    paddingX + pixelsPerPoint * currentPoint(),
                    paddingY + yAxisPoint * 500,
                    2,2);
        }

    }

}
