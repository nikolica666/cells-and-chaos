package hr.nipeta.cac.gol;

import hr.nipeta.cac.Main;
import hr.nipeta.cac.PeriodicAnimationTimer;
import hr.nipeta.cac.SceneBuilder;
import hr.nipeta.cac.gol.count.NeighbourCountBox;
import hr.nipeta.cac.gol.count.NeighbourCountOpen;
import hr.nipeta.cac.gol.count.NeighbourCountWrap;
import hr.nipeta.cac.gol.model.GolCellState;
import hr.nipeta.cac.gol.rules.*;
import hr.nipeta.cac.model.IntCoordinates;
import hr.nipeta.cac.model.RectangularGrid;
import hr.nipeta.cac.welcome.WelcomeSceneBuilder;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import static java.lang.System.currentTimeMillis;

@Slf4j
public class GolSceneBuilder extends SceneBuilder {

    private RectangularGrid rectangularGrid;

    private GolLogic golLogic;

    private int lastToggledScreenRow;
    private int lastToggledScreenCol;

    private PeriodicAnimationTimer timer;
    private TextField timerDurationInput;

    private Canvas canvas;
    private double scaleFactor = 1.0;

    private TextField cellSizeInput;

    public GolSceneBuilder(Main main) {
        super(main);
    }

    @Override
    public Scene createContent() {

        rectangularGrid = RectangularGrid.of(100,200,10,1);

        golLogic = new GolLogic(rectangularGrid.getCols(), rectangularGrid.getRows(), new GolConwayRules(), new NeighbourCountWrap());

        timer = PeriodicAnimationTimer.every(125).execute(this::evolveAndDrawGrid);

        timerDurationInput = new TextField();
        timerDurationInput.setPrefWidth(150);
        timerDurationInput.setPromptText("" + timer.getTimerDurationMs());

        cellSizeInput = new TextField();
        cellSizeInput.setPrefWidth(150);
        cellSizeInput.setPromptText("" + rectangularGrid.getCellSize());

        Region parent = new VBox(10,
                mainMenu(),
                zoomableCanvas(
                        rectangularGrid.getCols() * rectangularGrid.getCellSizeWithBorder(),
                        rectangularGrid.getRows() * rectangularGrid.getCellSizeWithBorder()));
        parent.setPadding(new Insets(10));
        return new Scene(parent);

    }

    private void evolveAndDrawGrid() {
        long milli = System.currentTimeMillis();
        evolve();
        drawGrid(canvas.getGraphicsContext2D());
        log.debug("Evolved and drew in {}ms", currentTimeMillis() - milli);
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
                cellSizeInput(),
                welcomeScreenButton()
        );
    }

    private Node cellSizeInput() {
        TextInputControl textInputControl = onTextInputEnter(cellSizeInput, this::onCellSizeInputSubmit);
        textInputControl.setTooltip(createTooltip("Cell size"));
        return textInputControl;
    }

    private void onCellSizeInputSubmit() {

        Double newCellSize = parseCellSizeInput(cellSizeInput.getText());

        if (newCellSize == null) {
            return;
        }

        if (newCellSize.compareTo(rectangularGrid.getCellSize()) == 0) {
            return;
        }

        timer.stopToExecuteThenRestart(() -> {
            rectangularGrid.setCellSize(newCellSize);
            cellSizeInput.setPromptText("" + newCellSize);
            resizeAndRedrawCanvas();
        });

    }

    private void resizeAndRedrawCanvas() {

        canvas.setWidth(rectangularGrid.getCols() * rectangularGrid.getCellSizeWithBorder());
        canvas.setHeight(rectangularGrid.getRows() * rectangularGrid.getCellSizeWithBorder());
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        drawEmptyGrid(canvas.getGraphicsContext2D());

        drawGrid(canvas.getGraphicsContext2D());

    }

    private Double parseCellSizeInput(String input) {
        try {
            double doubleInput = Double.parseDouble(input);
            if (doubleInput < 1 || doubleInput > 127) {
                showAlertError("Cell size must be between 1 and 127.");
                return null;
            } else {
                return doubleInput;
            }
        } catch (NumberFormatException ex) {
            showAlertError("Invalid number. Please enter a valid number.");
            return null;
        }
    }

    private Button startButton() {
        return createButton("Start", event -> {
            if (!timer.isPlaying()) {
                evolveAndDrawGrid();
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
        return createButton("Step", event -> evolveAndDrawGrid());
    }

    private Node clearButton() {
        return createButton("Clear", event -> {

            if (timer.isPlaying()) {
                timer.stop();
            }

            golLogic.setAllDead();

            drawEmptyGrid(canvas.getGraphicsContext2D());

        });

    }

    private Node timelineDurationInput() {
        return onTextInputEnter(timerDurationInput, this::onTimelineDurationInputSubmit);
    }

    private Button timelineDurationButton() {
        return createButton("Set ms", event -> onTimelineDurationInputSubmit());
    }

    private void onTimelineDurationInputSubmit() {

        Integer msDuration = parseTimelineDurationInput(timerDurationInput.getText());

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
            if (intInput < 5 || intInput > 10_000) {
                showAlertError("Frequency must be between 5ms and 10000ms.");
                return null;
            } else {
                return intInput;
            }
        } catch (NumberFormatException ex) {
            showAlertError("Invalid number. Please enter a valid number.");
            return null;
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
        rulesSelector.setTooltip(createTooltip("Born/Survive rules (classical rule is 'Conway' B3/S23 - born when 3 live neighbours, survive if 2 or 3 live neighbours)"));
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
        neighbourCountSelector.setTooltip(createTooltip("Border type"));
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

        int col = (int)(e.getX() / rectangularGrid.getCellSizeWithBorder());
        int row = (int)(e.getY() / rectangularGrid.getCellSizeWithBorder());

        if (col >= 0 && col < rectangularGrid.getCols() && row >= 0 && row < rectangularGrid.getRows()) {
            toggleCell(row, col, gc);
        }

    }

    private void handleMouseDragged(MouseEvent e, GraphicsContext gc) {

        int col = (int)(e.getX() / rectangularGrid.getCellSizeWithBorder());
        int row = (int)(e.getY() / rectangularGrid.getCellSizeWithBorder());

        if (col >= 0 && col < rectangularGrid.getCols() && row >= 0 && row < rectangularGrid.getRows()) {
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

        for (int row = 0; row < rectangularGrid.getRows(); row++) {
            for (int col = 0; col < rectangularGrid.getCols(); col++) {
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

        log.debug("I drew {} of {} cells in {}ms", counter, rectangularGrid.getNumberOfCells(), (currentTimeMillis() - milli));

    }

    private void drawCell(GraphicsContext gc, IntCoordinates cell, Paint paint) {
        drawCell(gc, cell.getY(), cell.getX(), paint);
    }

    private void drawCell(GraphicsContext gc, int row, int col, Paint paint) {

        log.trace("Drawing cell row={}, col={} {}", row, col, paint);

        double x = col * rectangularGrid.getCellSizeWithBorder();
        double y = row * rectangularGrid.getCellSizeWithBorder();

        gc.setFill(paint);
        gc.fillRect(x, y, rectangularGrid.getCellSize(), rectangularGrid.getCellSize());

    }

    private void drawEmptyCell(GraphicsContext gc, int row, int col) {

        double x = col * rectangularGrid.getCellSizeWithBorder();
        double y = row * rectangularGrid.getCellSizeWithBorder();

        gc.setFill(setFillBasedOnCellState(GolCellState.DEAD));
        gc.fillRect(x, y, rectangularGrid.getCellSize(), rectangularGrid.getCellSize());

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
