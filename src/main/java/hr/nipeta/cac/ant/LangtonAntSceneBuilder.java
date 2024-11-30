package hr.nipeta.cac.ant;

import hr.nipeta.cac.Main;
import hr.nipeta.cac.PeriodicAnimationTimer;
import hr.nipeta.cac.SceneBuilder;
import hr.nipeta.cac.model.Coordinates;
import hr.nipeta.cac.model.IntCoordinates;
import hr.nipeta.cac.welcome.WelcomeSceneBuilder;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class LangtonAntSceneBuilder extends SceneBuilder {

    private static final int GRID_SIZE_X = 256;
    private static final int GRID_SIZE_Y = 128;

    private static LangtonAntLogic antLogic;

    private static final double RECT_SIZE = 11;
    private static final double RECT_BORDER_WIDTH = 0;
    private static final double RECT_TOTAL_SIZE = RECT_SIZE + RECT_BORDER_WIDTH;

    private String antRules;
    private TextField antRulesInput;

    private TextField timerDurationInput;

    private PeriodicAnimationTimer timer;

    private long counter;
    private Label counterLabel;

    private GraphicsContext gc;

    public LangtonAntSceneBuilder(Main main) {
        super(main);
    }

    @Override
    public Scene createContent() {

        // Classical rules are default (2 types of tiles, turn left / turn right)
        antRules = "RL";

        antLogic = new LangtonAntLogic(GRID_SIZE_X, GRID_SIZE_Y);
        antLogic.init(antRules);

        timer = PeriodicAnimationTimer.every(20).execute(this::evolveAndDraw);

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
                antRulesInput(),
                counterLabel(),
                welcomeScreenButton()
        );
    }

    private Button startButton() {
        return createButton("Start", event -> {
            if (!timer.isPlaying()) {
                evolveAndDraw();
                timer.start();
            }
        });
    }

    private Button stopButton() {
        return createButton("Stop", event -> {
            if (timer.isPlaying()) {
                timer.stop();
            }
        });
    }

    private Button stepButton() {
        return createButton("Step", event -> evolveAndDraw());
    }

    private Node timerDurationInput() {
        timerDurationInput = new TextField();
        timerDurationInput.setPrefWidth(150);
        timerDurationInput.setPromptText("" + timer.getTimerDurationMs());
        return onTextInputEnter(timerDurationInput, this::onTimerDurationInputSubmit);
    }

    private Button timerDurationButton() {
        return createButton("Set ms", event -> onTimerDurationInputSubmit());
    }

    private void onTimerDurationInputSubmit() {

        final Integer msDuration = parseTimelineDurationInput(timerDurationInput.getText());

        if (msDuration != null) {
            timer.stopToExecuteThenRestart(() -> {
                timer.setTimerDurationMs(msDuration);
                timerDurationInput.setPromptText("" + msDuration);
            });
        }

    }

    private Integer parseTimelineDurationInput(String input) {
        try {
            int intInput = Integer.parseInt(input);
            if (intInput < 20 || intInput > 30_000) {
                showAlertError("Frequency must be between 20ms and 30000ms.");
                return null;
            } else {
                return intInput;
            }
        } catch (NumberFormatException ex) {
            showAlertError("Invalid number. Please enter a valid number.");
            return null;
        }
    }

    private Node antRulesInput() {
        antRulesInput = new TextField();
        antRulesInput.setPrefWidth(250);
        antRulesInput.setPromptText(antRules);
        return onTextInputEnter(antRulesInput, this::onAntRulesInputSubmit);
    }

    private void onAntRulesInputSubmit() {
        String input = antRulesInput.getText();
        if (input == null) {
            return;
        }

        input = input.replaceAll("\\s+", "");

        if (!input.matches("^[LRCUlrcu]+$")) {
            showAlertError("Ant rules must be letters LRCU (or lrcu)");
        }

        if (input.length() > CELL_COLORS.size()) {
            showAlertError("Max number of rules is " + CELL_COLORS.size());
        }

        this.antRules = input;
        antLogic.init(antRules);
        drawGrid(gc);

    }

    private Label counterLabel() {
        counterLabel = new Label("Counter: " + counter);
        return counterLabel;
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
        counter++;
        counterLabel.setText("Counter: " + counter);
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

//        GaussianBlur blur = new GaussianBlur(3);
//        gc.getCanvas().setEffect(blur);

    }

    private static final List<Color> CELL_COLORS = Arrays.asList(
            Color.WHEAT,
            Color.DARKGREEN,
            Color.DARKORANGE,
            Color.DARKBLUE,
            Color.STEELBLUE,
            Color.BLACK,
            Color.CORAL,
            Color.CRIMSON,
            Color.AQUA,
            Color.FUCHSIA,
            Color.YELLOWGREEN,
            Color.VIOLET,
            Color.TAN,
            Color.TEAL
    );

    private void drawCell(GraphicsContext gc, int col, int row) {

        double x = col * RECT_TOTAL_SIZE;
        double y = row * RECT_TOTAL_SIZE;

        // Draw cell background

        gc.setFill(CELL_COLORS.get(antLogic.getTileIndex(row, col)));
//        gc.fillRect(x, y, RECT_SIZE, RECT_SIZE);
//        gc.fillRoundRect(x, y, RECT_SIZE, RECT_SIZE, RECT_SIZE/2, RECT_SIZE/2);
        gc.fillOval(x, y, RECT_SIZE - 2, RECT_SIZE - 2);

    }

    private void drawAnt(GraphicsContext gc) {

        IntCoordinates antCellCoordinates = antLogic.getCurrentStateCoordinates();
        double degrees = antLogic.getCurrentStateDirectionDegrees();

        Coordinates<Double> t1 = new Coordinates<>(antCellCoordinates.getX() * RECT_TOTAL_SIZE, (antCellCoordinates.getY() + 1) * RECT_TOTAL_SIZE);
        Coordinates<Double> t2 = new Coordinates<>((antCellCoordinates.getX() + 1) * RECT_TOTAL_SIZE, (antCellCoordinates.getY() + 1) * RECT_TOTAL_SIZE);
        Coordinates<Double> t3 = new Coordinates<>((antCellCoordinates.getX() + 0.5) * RECT_TOTAL_SIZE , (antCellCoordinates.getY() + 0.5) * RECT_TOTAL_SIZE);

        // Save current transformation state
        gc.save();
        gc.translate((antCellCoordinates.getX() + 0.5) * RECT_TOTAL_SIZE, (antCellCoordinates.getY() + 0.5) * RECT_TOTAL_SIZE);
        gc.rotate(degrees);
        gc.translate(- (antCellCoordinates.getX() + 0.5) * RECT_TOTAL_SIZE, - (antCellCoordinates.getY() + 0.5) * RECT_TOTAL_SIZE);

        drawTriangle(gc, t1, t2, t3);

        // Restore previous state to avoid affecting other drawings
        gc.restore();


    }

    private void drawTriangle(GraphicsContext gc, Coordinates<Double> t1, Coordinates<Double> t2 , Coordinates<Double> t3) {
        gc.setFill(Color.RED);
        gc.beginPath();
        gc.moveTo(t1.getX(), t1.getY()); // First vertex
        gc.lineTo(t2.getX(), t2.getY()); // Second vertex
        gc.lineTo(t3.getX(), t3.getY()); // Third vertex
        gc.closePath();
        gc.fill();
    }

}
