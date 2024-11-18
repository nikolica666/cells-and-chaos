package hr.nik.gol;

import hr.nik.Main;
import hr.nik.SceneBuilder;
import hr.nik.welcome.WelcomeSceneBuilder;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GolSceneBuilder extends SceneBuilder {

    private static final int GRID_SIZE_X = 128;
    private static final int GRID_SIZE_Y = 80;

    private static GolLogic golLogic;

    private static final double RECT_SIZE = 12;
    private static final double RECT_BORDER_WIDTH = 2;
    private static final double RECT_TOTAL_SIZE = RECT_SIZE + RECT_BORDER_WIDTH;

    private int lastToggledScreenRow;
    private int lastToggledScreenCol;

    private TextField timelineDurationInput;
    private Timeline timeline;
    private boolean timelinePlaying;

    private GraphicsContext gc;

    public GolSceneBuilder(Main main) {
        super(main);
    }

    public Scene build() {

        golLogic = new GolLogic(GRID_SIZE_X, GRID_SIZE_Y, new GolClassicRules());

        double initialTimerDuration = 175;

        timeline = new Timeline(new KeyFrame(Duration.millis(initialTimerDuration), e -> onTimelineFrame()));
        timeline.setCycleCount(Timeline.INDEFINITE); // Repeat indefinitely

        timelineDurationInput = new TextField();
        timelineDurationInput.setPrefWidth(150);
        timelineDurationInput.setPromptText("" + initialTimerDuration);

        Region parent = new VBox(10, mainMenu(), golGridWrapped());
        parent.setPadding(new Insets(10));
        return new Scene(parent);

    }

    private void onTimelineFrame() {
        long milli = System.currentTimeMillis();
        evolveAndDrawGrid();
        log.debug("Evolved and drew {} cells in {}ms", GRID_SIZE_Y * GRID_SIZE_X, System.currentTimeMillis() - milli);
    }

    private void evolveAndDrawGrid() {
        evolve();
        drawGrid(gc);
    }

    private void evolve() {
        for (int row = 0; row < GRID_SIZE_Y; row++) {
            for (int col = 0; col < GRID_SIZE_X; col++) {
                GolCellState nextState = golLogic.calculateAndSetNextState(row, col);
            }
        }

        for (int row = 0; row < GRID_SIZE_Y; row++) {
            for (int col = 0; col < GRID_SIZE_X; col++) {
                golLogic.findCell(row, col).setCurrentStateToNextStateValue();
            }
        }

    }

    private Node mainMenu() {
        return horizontalMenu(
                startButton(),
                stopButton(),
                stepButton(),
                clearButton(),
                timelineDurationInput(),
                timelineDurationButton(),
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

            for (int row = 0; row < GRID_SIZE_Y; row++) {
                for (int col = 0; col < GRID_SIZE_X; col++) {
                    clearState(row, col);
                }
            }

            drawGrid(gc);

        });

    }

    private void clearState(int row, int col) {

        GolCellState clearState = GolCellState.DEAD;

        golLogic.setCellCurrentState(row, col, clearState);

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
            if (intInput < 30) {
                showAlertError("Min frequency is 30ms.");
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

    private Button welcomeScreenButton() {
        return createButton(
                "Main menu",
                e -> createScene(() -> new WelcomeSceneBuilder(main)));
    }

    private Node golGridWrapped() {
        StackPane canvasContainer = new StackPane(golGrid());
        return canvasContainer;
    }

    private Node golGrid() {

        Canvas canvas = new Canvas(GRID_SIZE_X * RECT_TOTAL_SIZE, GRID_SIZE_Y * RECT_TOTAL_SIZE);
        gc = canvas.getGraphicsContext2D();

        drawGrid(gc);

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> handleMousePressed(e, gc));
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> handleMouseDragged(e, gc));

        return new Pane(canvas);

    }

    private void handleMousePressed(MouseEvent e, GraphicsContext gc) {

        int col = (int)(e.getX() / RECT_TOTAL_SIZE);
        int row = (int)(e.getY() / RECT_TOTAL_SIZE);

        if (col >= 0 && col < GRID_SIZE_X && row >= 0 && row < GRID_SIZE_Y) {

            // We set "next enum" when mouse is clicked on screen
            GolCellState newCurrentState = golLogic.findCell(row, col).getCurrentState().next();

            // Set new current state to logical cell
            GolCell cell = golLogic.findCell(row, col);
            cell.setCurrentState(newCurrentState);

            drawCell(gc, cell);

            lastToggledScreenRow = row;
            lastToggledScreenCol = col;

        }

    }

    private void handleMouseDragged(MouseEvent e, GraphicsContext gc) {

        int col = (int)(e.getX() / RECT_TOTAL_SIZE);
        int row = (int)(e.getY() / RECT_TOTAL_SIZE);

        if (col >= 0 && col < GRID_SIZE_X && row >= 0 && row < GRID_SIZE_Y) {
            if (lastToggledScreenRow != row || lastToggledScreenCol != col) {

                // We set "next enum" when mouse is dragged on screen
                GolCellState newCurrentState = golLogic.findCell(row, col).getCurrentState().next();

                // Set new current state to logical cell
                golLogic.setCellCurrentState(row, col, newCurrentState);

                drawCell(gc, golLogic.findCell(row, col));

                lastToggledScreenRow = row;
                lastToggledScreenCol = col;

            }
        }

    }

    private void drawGrid(GraphicsContext gc) {

        //gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        for (int row = 0; row < GRID_SIZE_Y; row++) {
            for (int col = 0; col < GRID_SIZE_X; col++) {
                GolCell cell = golLogic.findCellSetDeadIfAbsent(row, col);
                if (cell.isStateChanged()) {
                    drawCell(gc, cell);
                }
            }
        }

//        GaussianBlur blur = new GaussianBlur(3);
//        gc.getCanvas().setEffect(blur);

    }

    private void drawCell(GraphicsContext gc, GolCell cell) {

        log.trace("Drawing cell {}", cell);

        double x = cell.getCoordinates().getX() * RECT_TOTAL_SIZE;
        double y = cell.getCoordinates().getY() * RECT_TOTAL_SIZE;

        GolCellState currentState = cell.getCurrentState();

        // Draw cell background
        gc.setFill(setFillBasedOnCellState(currentState));
        gc.fillRect(x, y, RECT_SIZE, RECT_SIZE);
//        gc.fillOval(x, y, RECT_SIZE, RECT_SIZE);
//        gc.fillRoundRect(x, y, RECT_SIZE, RECT_SIZE, 25 , 25);

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
