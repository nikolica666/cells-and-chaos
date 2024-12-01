package hr.nipeta.cac.gol;

import hr.nipeta.cac.Main;
import hr.nipeta.cac.SceneBuilder;
import hr.nipeta.cac.gol.count.NeighbourCountBox;
import hr.nipeta.cac.gol.count.NeighbourCountOpen;
import hr.nipeta.cac.gol.count.NeighbourCountWrap;
import hr.nipeta.cac.gol.model.GolCellState;
import hr.nipeta.cac.gol.rules.*;
import hr.nipeta.cac.model.IntCoordinates;
import hr.nipeta.cac.model.RectangularGrid;
import hr.nipeta.cac.model.gui.PeriodicAnimationTimer;
import hr.nipeta.cac.model.gui.PeriodicAnimationTimerGuiControl;
import hr.nipeta.cac.welcome.WelcomeSceneBuilder;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import lombok.extern.slf4j.Slf4j;

import static hr.nipeta.cac.model.gui.SceneUtils.*;
import static java.lang.System.currentTimeMillis;

@Slf4j
public class GolSceneBuilder extends SceneBuilder {

    private RectangularGrid rectangularGrid;
    private PeriodicAnimationTimerGuiControl timerControl;

    private GolLogic logic;

    private int lastToggledScreenRow;
    private int lastToggledScreenCol;

    private Canvas canvas;
    private double scaleFactor = 1.0;

    private TextField cellSizeInput;

    public GolSceneBuilder(Main main) {
        super(main);
    }

    @Override
    public Scene createContent() {

        rectangularGrid = RectangularGrid.of(100,200,10,1);
        logic = new GolLogic(rectangularGrid.getCols(), rectangularGrid.getRows(), new GolConwayRules(), new NeighbourCountWrap());

        timerControl = new PeriodicAnimationTimerGuiControl(
                PeriodicAnimationTimer.every(125).execute(this::evolveAndDrawGrid));

        cellSizeInput = createInput("" + rectangularGrid.getCellSize(), 150, createTooltip("Cell size"), this::onCellSizeInputSubmit);

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
        logic.evolve();
    }

    private Node mainMenu() {
        return horizontalMenu(
                timerControl.getStartButton(),
                timerControl.getStopButton(),
                stepButton(),
                randomizeButton(),
                clearButton(),
                timerControl.getDurationInput(),
                rulesSelector(),
                neighbourCountSelector(),
                cellSizeInput,
                welcomeScreenButton()
        );
    }

    private void onCellSizeInputSubmit() {

        Double newCellSize = parseCellSizeInput(cellSizeInput.getText());

        if (newCellSize == null) {
            return;
        }

        if (newCellSize.compareTo(rectangularGrid.getCellSize()) == 0) {
            return;
        }

        timerControl.stopToExecuteThenRestart(() -> {
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

    private Button stepButton() {
        return createButton("Step", event -> evolveAndDrawGrid());
    }

    private Node clearButton() {
        return createButton("Clear", event -> {

            if (timerControl.getTimer().isPlaying()) {
                timerControl.getTimer().stop();
            }

            logic.setAllDead();

            drawEmptyGrid(canvas.getGraphicsContext2D());

        });

    }

    private Button randomizeButton() {
        return createButton("Randomize", event -> {

            logic.randomize();

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
                case "Conway (B3/S23)":
                    logic.setRules(new GolConwayRules());
                    break;
                case "Anneal (B4678/S35678)":
                    logic.setRules(new GolAnnealRules());
                    break;
                case "DayAndNight (B3678/S34678)":
                    logic.setRules(new GolDayAndNightRules());
                    break;
                case "Diamoeba (B35678/S5678)":
                    logic.setRules(new GolDiamoebaRules());
                    break;
                case "HighLife (B36/S23)":
                    logic.setRules(new GolHighLifeRules());
                    break;
                case "Seeds (B2/S)":
                    logic.setRules(new GolSeedsRules());
                    break;
                default:
                    boolean validInput = GolCustomRules.convertAndValidatePattern(selectedRule);
                    if (validInput) {
                        logic.setRules(new GolCustomRules(selectedRule));
                    } else {
                        showAlertError("Custom pattern must be in regex 'B[0-8]*/S[0-8]*' (e.g. 'B3/S23' or 'B278/S' or 'B024/S045')");
                    }

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
                    logic.setNeighbourCount(new NeighbourCountBox());
                    break;
                case "Open":
                    logic.setNeighbourCount(new NeighbourCountOpen());
                    break;
                case "Wrap":
                    logic.setNeighbourCount(new NeighbourCountWrap());
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
        GolCellState newState = logic.toggle(row, col);

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
        for (IntCoordinates cell : logic.getLiveCells()) {
            counter++;
            drawCell(gc, cell, setFillBasedOnCellState(GolCellState.ALIVE));
        }
        for (IntCoordinates cell : logic.getDeadCells()) {
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
