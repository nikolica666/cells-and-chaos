package hr.nipeta.cac.gol;

import static java.lang.System.currentTimeMillis;

import hr.nipeta.cac.Main;
import hr.nipeta.cac.SceneBuilder;
import hr.nipeta.cac.gol.count.NeighbourCountBox;
import hr.nipeta.cac.gol.count.NeighbourCountOpen;
import hr.nipeta.cac.gol.count.NeighbourCountWrap;
import hr.nipeta.cac.gol.model.GolCellState;
import hr.nipeta.cac.gol.rules.*;
import hr.nipeta.cac.model.IntCoordinates;
import hr.nipeta.cac.welcome.WelcomeSceneBuilder;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GolSceneBuilder extends SceneBuilder {

    private static final int GRID_SIZE_X = 450;
    private static final int GRID_SIZE_Y = 225;

    private static GolLogic golLogic;

    private static final double RECT_SIZE = 4;
    private static final double RECT_BORDER_WIDTH = 1;
    private static final double RECT_TOTAL_SIZE = RECT_SIZE + RECT_BORDER_WIDTH;

    private int lastToggledScreenRow;
    private int lastToggledScreenCol;

    private TextField timelineDurationInput;
    private Timeline timeline;
    private boolean timelinePlaying;

    private Canvas canvas;
    private double scaleFactor = 1.0;

    public GolSceneBuilder(Main main) {
        super(main);
    }

    public Scene build() {

        golLogic = new GolLogic(GRID_SIZE_X, GRID_SIZE_Y, new GolConwayRules(), new NeighbourCountWrap());

        double initialTimerDuration = 125;

        timeline = new Timeline(new KeyFrame(Duration.millis(initialTimerDuration), e -> onTimelineFrame()));
        timeline.setCycleCount(Timeline.INDEFINITE); // Repeat indefinitely

        timelineDurationInput = new TextField();
        timelineDurationInput.setPrefWidth(150);
        timelineDurationInput.setPromptText("" + initialTimerDuration);

        Region parent = new VBox(10, mainMenu(), zoomableCanvas(GRID_SIZE_X * RECT_TOTAL_SIZE, GRID_SIZE_Y * RECT_TOTAL_SIZE));
        parent.setPadding(new Insets(10));
        return new Scene(parent);

    }

    private void onTimelineFrame() {
        long milli = currentTimeMillis();
        evolveAndDrawGrid();
        log.debug("Evolved and drew in {}ms", currentTimeMillis() - milli);
    }

    private void evolveAndDrawGrid() {
        evolve();
        drawGrid(canvas.getGraphicsContext2D());
    }

    private void evolve() {
        golLogic.evolve();
    }

    private Node mainMenu() {
        return horizontalMenu(
                startButton(),
                stopButton(),
                stepButton(),
                randomizeButton(),
                clearButton(),
                timelineDurationInput(),
                timelineDurationButton(),
                rulesSelector(),
                neighbourCountSelector(),
                welcomeScreenButton()
        );
    }

    private Button startButton() {
        return createButton("Start", event -> {
            if (!timelinePlaying) {
                evolveAndDrawGrid();
                timeline.play();
                timelinePlaying = true;
            }
        });
    }

    private Button stopButton() {
        return createButton("Stop", event -> {
            if (timelinePlaying) {
                timeline.stop();
                timelinePlaying = false;
            }
        });
    }

    private Button stepButton() {
        return createButton("Step", event -> evolveAndDrawGrid());
    }

    private Node clearButton() {
        return createButton("Clear", event -> {

            if (timelinePlaying) {
                timeline.stop();
                timelinePlaying = false;
            }

            golLogic.setAllDead();

            drawEmptyGrid(canvas.getGraphicsContext2D());

        });

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
            if (intInput < 5) {
                showAlertError("Min frequency is 5ms.");
            } else if (intInput > 30_000) {
                showAlertError("Max frequency is 30000ms.");
            } else {
                msDuration = intInput;
            }
        } catch (NumberFormatException ex) {
            showAlertError("Invalid number. Please enter a valid number.");
        }
        if (msDuration != null) {
            if (timelinePlaying) {
                timeline.stop();
            }
            timelineDurationInput.setPromptText("" + msDuration);
            timeline.getKeyFrames().setAll(new KeyFrame(Duration.millis(msDuration), e -> onTimelineFrame()));
            if (timelinePlaying) {
                timeline.play();
            }
        }
    }

    private Button randomizeButton() {
        return createButton("Randomize", event -> {

            golLogic.randomize();

            drawGrid(canvas.getGraphicsContext2D());

        });
    }

    private ComboBox<String> rulesSelector() {

        ComboBox<String> rulesSelector = new ComboBox<>();
        rulesSelector.getItems().addAll("Conway", "Anneal", "DayAndNight", "Diamoeba", "HighLife", "Seeds");
        rulesSelector.setValue("Conway"); // Default selection
        rulesSelector.setPrefWidth(250);
        rulesSelector.getEditor().setContextMenu(null); // Preventing popup when typing in editor field
        rulesSelector.setEditable(true);

        Tooltip tooltip = new Tooltip("Born/Survive rules (classical rule is 'Conway' B3/S23 - born when 3 live neighbours, survive if 2 or 3 live neighbours)");
        tooltip.setShowDelay(Duration.millis(0));
        tooltip.setHideDelay(Duration.millis(0));

        rulesSelector.setTooltip(tooltip);
        rulesSelector.setOnAction(e -> {
            String selectedRule = rulesSelector.getValue();

            switch (selectedRule) {
                case "Conway":
                    golLogic.setRules(new GolConwayRules());
                    break;
                case "Anneal":
                    golLogic.setRules(new GolAnnealRules());
                    break;
                case "DayAndNight":
                    golLogic.setRules(new GolDayAndNightRules());
                    break;
                case "Diamoeba":
                    golLogic.setRules(new GolDiamoebaRules());
                    break;
                case "HighLife":
                    golLogic.setRules(new GolHighLifeRules());
                    break;
                case "Seeds":
                    golLogic.setRules(new GolSeedsRules());
                    break;
                default:
                    boolean validInput = GolCustomRules.convertAndValidatePattern(selectedRule);
                    if (!validInput) {
                        showAlertError("Custom pattern must be in regex 'B[0-8]*/S[0-8]*' (e.g. 'B3/S23' or 'B278/S' or 'B024/S045')");
                    }
                    golLogic.setRules(new GolCustomRules(selectedRule));
            }
        });

        return rulesSelector;

    }

    private ComboBox<String> neighbourCountSelector() {

        ComboBox<String> neighbourCountSelector = new ComboBox<>();
        neighbourCountSelector.getItems().addAll("Box", "Open", "Wrap");
        neighbourCountSelector.setValue("Wrap"); // Default selection
        neighbourCountSelector.setPrefWidth(150);

        Tooltip tooltip = new Tooltip("Border type");
        tooltip.setShowDelay(Duration.millis(0));
        tooltip.setHideDelay(Duration.millis(0));

        neighbourCountSelector.setTooltip(tooltip);
        neighbourCountSelector.setOnAction(e -> {
            String selectedRule = neighbourCountSelector.getValue();
            switch (selectedRule) {
                case "Box":
                    golLogic.setNeighbourCount(new NeighbourCountBox());
                    break;
                case "Open":
                    golLogic.setNeighbourCount(new NeighbourCountOpen());
                    break;
                case "Wrap":
                    golLogic.setNeighbourCount(new NeighbourCountWrap());
                    break;
            }
        });

        return neighbourCountSelector;

    }

    private Button welcomeScreenButton() {
        return createButton(
                "Main menu",
                e -> createScene(() -> new WelcomeSceneBuilder(main)));
    }

    private Node zoomableCanvas(double canvasWidth, double canvasHeight) {

        initCanvasGrid(canvasWidth, canvasHeight);

        Pane canvasContainer = new Pane(canvas);
/*
        // Add zoom functionality
        canvasContainer.setOnScroll((ScrollEvent event) -> {
            double zoomFactor = 1.1; // Define zoom speed
            if (event.getDeltaY() < 0) {
                zoomFactor = 1 / zoomFactor; // Zoom out
            }
            double newScaleFactor = scaleFactor * zoomFactor;
            if (newScaleFactor >= 0.5 && newScaleFactor <= 5.0) { // Limits: 50% to 500%
                scaleFactor = newScaleFactor;
                canvasContainer.getTransforms().clear(); // Clear previous transforms
                Scale scale = new Scale(scaleFactor, scaleFactor, event.getX(), event.getY());
                canvasContainer.getTransforms().add(scale);
            }

        });
*/
        return canvasContainer;

    }

    private void initCanvasGrid(double canvasWidth, double canvasHeight) {

        canvas = new Canvas(canvasWidth, canvasHeight);

        drawEmptyGrid(canvas.getGraphicsContext2D());

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> handleMousePressed(e, canvas.getGraphicsContext2D()));
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> handleMouseDragged(e, canvas.getGraphicsContext2D()));

    }

    private void handleMousePressed(MouseEvent e, GraphicsContext gc) {

        int col = (int)(e.getX() / RECT_TOTAL_SIZE);
        int row = (int)(e.getY() / RECT_TOTAL_SIZE);

        if (col >= 0 && col < GRID_SIZE_X && row >= 0 && row < GRID_SIZE_Y) {
            toggleCell(row, col, gc);
        }

    }

    private void handleMouseDragged(MouseEvent e, GraphicsContext gc) {

        int col = (int)(e.getX() / RECT_TOTAL_SIZE);
        int row = (int)(e.getY() / RECT_TOTAL_SIZE);

        if (col >= 0 && col < GRID_SIZE_X && row >= 0 && row < GRID_SIZE_Y) {
            if (lastToggledScreenRow != row || lastToggledScreenCol != col) {
                toggleCell(row, col, gc);
            }
        }

    }

    private void toggleCell(int row, int col, GraphicsContext gc) {

        lastToggledScreenRow = row;
        lastToggledScreenCol = col;

        // We set "next enum" when mouse is dragged on screen
        GolCellState newState = golLogic.toggle(row, col);

        drawCell(gc, row, col, setFillBasedOnCellState(newState));

    }

    private void drawEmptyGrid(GraphicsContext gc) {

        // No need to clear canvas (not sure)

        for (int row = 0; row < GRID_SIZE_Y; row++) {
            for (int col = 0; col < GRID_SIZE_X; col++) {
                drawEmptyCell(gc, row, col);
            }
        }

    }

    private void drawGrid(GraphicsContext gc) {

        long milli = currentTimeMillis();

        // No need to clear canvas (not sure)

        int counter = 0;

        // Needs refactoring
        for (IntCoordinates cell : golLogic.getLiveCells()) {
            counter++;
            drawCell(gc, cell, setFillBasedOnCellState(GolCellState.ALIVE));
        }
        for (IntCoordinates cell : golLogic.getDeadCells()) {
            counter++;
            drawCell(gc, cell, setFillBasedOnCellState(GolCellState.DEAD));
        }

        log.debug("I drew {} of {} cells in {}ms", counter, GRID_SIZE_Y * GRID_SIZE_X, (currentTimeMillis() - milli));

    }

    private void drawCell(GraphicsContext gc, IntCoordinates cell, Paint paint) {
        drawCell(gc, cell.getY(), cell.getX(), paint);
    }

    private void drawCell(GraphicsContext gc, int row, int col, Paint paint) {

        log.trace("Drawing cell row={}, col={} {}", row, col, paint);

        double x = col * RECT_TOTAL_SIZE;
        double y = row * RECT_TOTAL_SIZE;

        gc.setFill(paint);
        gc.fillRect(x, y, RECT_SIZE, RECT_SIZE);

    }

    private void drawEmptyCell(GraphicsContext gc, int row, int col) {

        double x = col * RECT_TOTAL_SIZE;
        double y = row * RECT_TOTAL_SIZE;

        gc.setFill(setFillBasedOnCellState(GolCellState.DEAD));
        gc.fillRect(x, y, RECT_SIZE, RECT_SIZE);

    }

    private Paint setFillBasedOnCellState(GolCellState state) {
        switch (state) {
            case ALIVE -> {
                return Color.DARKGREEN;
            }
            case DEAD -> {
                return Color.WHEAT;
            }
            default -> throw new RuntimeException("Cannot set fill color for unexpected state " + state);
        }
    }

}
