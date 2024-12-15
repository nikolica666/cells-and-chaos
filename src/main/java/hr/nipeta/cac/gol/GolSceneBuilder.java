package hr.nipeta.cac.gol;

import hr.nipeta.cac.Main;
import hr.nipeta.cac.SceneBuilder;
import hr.nipeta.cac.gol.count.NeighbourCountFactory;
import hr.nipeta.cac.gol.count.NeighbourCountWrap;
import hr.nipeta.cac.gol.file.parser.GolFileParserFactory;
import hr.nipeta.cac.gol.file.parser.GolFileParserResult;
import hr.nipeta.cac.gol.model.GolCellState;
import hr.nipeta.cac.gol.rules.GolConwayRules;
import hr.nipeta.cac.gol.rules.GolCustomRules;
import hr.nipeta.cac.gol.rules.GolRules;
import hr.nipeta.cac.model.IntCoordinates;
import hr.nipeta.cac.model.RectangularGrid;
import hr.nipeta.cac.model.gui.PercentLabelGuiControl;
import hr.nipeta.cac.model.gui.PeriodicAnimationTimer;
import hr.nipeta.cac.model.gui.PeriodicAnimationTimerGuiControl;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static hr.nipeta.cac.model.gui.SceneUtils.*;
import static java.lang.System.currentTimeMillis;

@Slf4j
public class GolSceneBuilder extends SceneBuilder {

    private static final Map<GolCellState, Color> DEFAULT_COLORS = Map.of(
            GolCellState.DEAD, Color.WHEAT,
            GolCellState.ALIVE, Color.DARKGREEN);

    private Map<GolCellState, Color> colors = new HashMap<>(DEFAULT_COLORS);

    private RectangularGrid rectangularGrid;

    private PeriodicAnimationTimerGuiControl timerControl;

    private GolLogic logic;

    private int lastToggledScreenRow;
    private int lastToggledScreenCol;

    private Group canvasContainer;
    private Canvas canvas;
    private double scaleFactor = 1.0;

    private TextField cellSizeInput;
    private ColorPicker liveColorPicker;
    private ColorPicker deadColorPicker;

    private PercentLabelGuiControl livePercentLabel;

    public GolSceneBuilder(Main main) {
        super(main);
    }

    @Override
    public Scene createContent() {

        // Get the primary screen
        javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();

        // Get the visual bounds (usable screen area, excluding taskbar, etc.)
        javafx.geometry.Rectangle2D visualBounds = screen.getVisualBounds();

        double cellSize = 10;
        double cellBorder = 1;

        rectangularGrid = RectangularGrid.of(
                (int) ((visualBounds.getHeight() - 200) / (cellSize + cellBorder)),
                (int) ((visualBounds.getWidth() - 100) / (cellSize + cellBorder)),
                cellSize,
                cellBorder);
        logic = new GolLogic(rectangularGrid.getCols(), rectangularGrid.getRows(), new GolConwayRules(), new NeighbourCountWrap());

        timerControl = PeriodicAnimationTimerGuiControl.of(PeriodicAnimationTimer.every(125).execute(this::evolveAndDrawGrid));

        livePercentLabel = PercentLabelGuiControl.of("Alive ");

        Region parent = new VBox(10,
                mainMenu(),
                zoomableCanvas(
                        rectangularGrid.getCols() * rectangularGrid.getCellSizeWithBorder(),
                        rectangularGrid.getRows() * rectangularGrid.getCellSizeWithBorder()),
                statistics());
        parent.setPadding(new Insets(10));
        return new Scene(parent);

    }

    private Node statistics() {
        return horizontalMenu(
                livePercentLabel
        );
    }

    private Node patternsPopup() {

        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox popupContent = new VBox(10);
        popupContent.setPadding(new Insets(10));
        popupContent.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 5px; -fx-background-radius: 5px;");

        Label promptLabel = new Label("Choose an option:");

        popupContent.getChildren().addAll(promptLabel, testScrollSelection(), patternFileUpload());
        popupContent.setAlignment(Pos.CENTER);

        popup.getContent().add(popupContent);

        return createButton("Patterns", e -> popup.show(main.getPrimaryStage()));

    }

    private Node patternFileUpload() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload Initial State");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("RLE Files", "*.rle"),
                new FileChooser.ExtensionFilter("Plaintext Files", "*.cells"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Button uploadButton = new Button("Upload Pattern");
        uploadButton.setOnAction(e -> {

            File selectedFile = fileChooser.showOpenDialog(main.getPrimaryStage());
            if (selectedFile == null) {
                return;
            }

            try {

                GolFileParserResult parseResult = GolFileParserFactory.parse(selectedFile);

                logic.setAllDead();
                drawEmptyGrid(canvas.getGraphicsContext2D());

                List<int[]> liveCells = parseResult.getLiveCells();
                int startX = rectangularGrid.getCols() / 2 - liveCells.getFirst().length / 2;
                int startY = rectangularGrid.getRows() / 2 - liveCells.size() / 2;

                for (int rowIndex = 0; rowIndex < liveCells.size(); rowIndex++) {
                    int[] row = liveCells.get(rowIndex);
                    for (int colIndex = 0; colIndex < row.length; colIndex++) {
                        if (row[colIndex] == 1) {
                            GolCellState newState = logic.toggle(startY + rowIndex, startX + colIndex);
                            drawCell(canvas.getGraphicsContext2D(), startY + rowIndex, startX + colIndex, colors.get(newState));
                        }
                    }
                }

                livePercentLabel.set((double) logic.getLiveCells().size() / rectangularGrid.getNumberOfCells());

                // TODO could use custom exception to be more precise on GUI when something's wrong
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                showAlertError("Error reading file:" + ex.getMessage());
            }

        });

        VBox layout = new VBox(10, uploadButton);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        return layout;

    }

    private Node displayPopup() {

        Button showPopupButton = new Button("Display");

        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox popupContent = new VBox(10);
        popupContent.setPadding(new Insets(10));
        popupContent.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 5px; -fx-background-radius: 5px;");

        cellSizeInput = createInput("" + rectangularGrid.getCellSize(), 150, createTooltip("Cell size"), this::onCellSizeInputSubmit);

        liveColorPicker = new ColorPicker(colors.get(GolCellState.ALIVE));
        liveColorPicker.setOnAction(event -> {
            colors.put(GolCellState.ALIVE, liveColorPicker.getValue());
            drawGridLiveCells(canvas.getGraphicsContext2D());
        });
        deadColorPicker = new ColorPicker(colors.get(GolCellState.DEAD));
        deadColorPicker.setOnAction(event -> {
            colors.put(GolCellState.DEAD, deadColorPicker.getValue());
            drawGridDeadAndEmptyCells(canvas.getGraphicsContext2D());
        });

        Button defaultColorsButton = createButton("Default colors", e -> {
            colors = new HashMap<>(DEFAULT_COLORS);
            drawGridLiveCells(canvas.getGraphicsContext2D());
            drawGridDeadAndEmptyCells(canvas.getGraphicsContext2D());
        });

        popupContent.getChildren().addAll(cellSizeInput, liveColorPicker, deadColorPicker, defaultColorsButton);
        popupContent.setAlignment(Pos.CENTER);

        // Add content to the Popup
        popup.getContent().add(popupContent);

        showPopupButton.setOnAction(e -> popup.show(main.getPrimaryStage()));

        return showPopupButton;

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

    private Node testScrollSelection() {

        VBox content = new VBox(10);

        try (InputStream inputStream = GolSceneBuilder.class.getResourceAsStream("/hr/nipeta/cac/gol/patterns/lexicon.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {



        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (int i = 1; i <= 90; i++) {
            TextFlow item = new TextFlow(new Text("Custom Item " + i));
            item.setStyle("-fx-padding: 10px; -fx-background-color: lightgray; -fx-border-color: black;");
            content.getChildren().add(item);
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(300, 400); // Set preferred size
        scrollPane.setMaxSize(300, 400);  // Limi

        return scrollPane;
    }

    private void evolveAndDrawGrid() {
        long milli = System.currentTimeMillis();
        evolve();
        drawGrid(canvas.getGraphicsContext2D());
        livePercentLabel.set((double) logic.getLiveCells().size() / rectangularGrid.getNumberOfCells());
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
                randomizeInput(),
                clearButton(),
                timerControl.getDurationInput(),
                rulesSelector(),
                neighbourCountSelector(),
                patternsPopup(),
                displayPopup(),
                createSceneChangePopupButton()
        );
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
            livePercentLabel.reset();

        });

    }

    private Node randomizeInput() {
        return createInput("Randomize", 150, createTooltip("Type in percentage of live cells (just a number) then press Enter"), (input) -> {
            Double percentAlive = parseRandomizeInput(input.getText());
            if (percentAlive != null) {
                logic.randomize(percentAlive);
                drawEmptyGrid(canvas.getGraphicsContext2D());
                drawGridLiveCells(canvas.getGraphicsContext2D());
                livePercentLabel.set((double) logic.getLiveCells().size() / rectangularGrid.getNumberOfCells());
            }
        });
    }

    private Double parseRandomizeInput(String input) {
        try {
            double doubleInput = Double.parseDouble(input) / 100;
            if (doubleInput < 0 || doubleInput > 1) {
                showAlertError("Percent must be between 0 and 100.");
                return null;
            } else {
                return doubleInput;
            }
        } catch (NumberFormatException ex) {
            showAlertError("Invalid number. Please enter a valid number.");
            return null;
        }
    }

    private ComboBox<String> rulesSelector() {

        ComboBox<String> rulesSelector = new ComboBox<>();
        rulesSelector.getItems().addAll(GolRules.GAME_OF_LIFE_RULE_LABELS);
        rulesSelector.setValue("Conway (B3/S23)"); // Default selection
        rulesSelector.setPrefWidth(300);
        rulesSelector.getEditor().setContextMenu(null); // Preventing popup when typing in editor field
        rulesSelector.setEditable(true);
        rulesSelector.setTooltip(
                createTooltip("""
                        Born/Survive rules e.g. B2/S45 = Born if 2 neighbours, Survive if 4 or 5\r\n
                        (classical rule is 'Conway' B3/S23 - born when 3 live neighbours,\s
                        survive if 2 or 3 live neighbours)\r\n
                        It is possible to type in custom rule"""));
        rulesSelector.setOnAction(e -> {

            String selectedRuleLabel = rulesSelector.getValue();

            if (selectedRuleLabel == null) {
                return;
            }

            // If user selected rule from dropdown
            if (rulesSelector.getItems().contains(selectedRuleLabel)) {
                logic.setRules(GolRules.fromLabel(selectedRuleLabel));
            }
            // If user typed in rule
            else {
                boolean validInput = GolCustomRules.validatePattern(selectedRuleLabel);
                if (validInput) {
                    logic.setRules(new GolCustomRules(selectedRuleLabel));
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
            String selectedNeighbourCount = neighbourCountSelector.getValue();
            logic.setNeighbourCount(NeighbourCountFactory.from(selectedNeighbourCount));
        });

        return neighbourCountSelector;

    }

    private Node zoomableCanvas(double canvasWidth, double canvasHeight) {

        initCanvasGrid(canvasWidth, canvasHeight);

        canvasContainer = new Group(canvas);

        // Add zoom functionality
        canvas.setOnScroll((ScrollEvent event) -> {
            log.debug("ScrollEvent {}", event);

            if (event.getDeltaY() == 0) {
                log.debug("Ignoring ScrollEvent because deltaY is zero");
                return;
            }

            double zoomFactor = 1.1; // Define zoom speed
            if (event.getDeltaY() < 0) {
                zoomFactor = 1 / zoomFactor; // Zoom out
            }
            double newScaleFactor = scaleFactor * zoomFactor;
            log.debug("newScaleFactor={}, scaleFactor={}", newScaleFactor, scaleFactor);
            if (newScaleFactor >= 0.5 && newScaleFactor <= 5.0) { // Limits: 50% to 500%
                scaleFactor = newScaleFactor;

                canvas.getTransforms().clear(); // Clear previous transforms
                Scale scale = new Scale(scaleFactor, scaleFactor, event.getX(), event.getY());
                canvas.getTransforms().add(scale);
                
            }

            event.consume();

        });

        canvas.setClip(new Rectangle(canvas.getWidth(), canvas.getHeight()));
        canvas.widthProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setClip(new Rectangle(canvas.getWidth(), canvas.getHeight()));
        });
        canvas.heightProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setClip(new Rectangle(canvas.getWidth(), canvas.getHeight()));
        });


        return canvasContainer;

    }

    private void initCanvasGrid(double canvasWidth, double canvasHeight) {

        canvas = new Canvas(canvasWidth, canvasHeight);

        drawEmptyGrid(canvas.getGraphicsContext2D());

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> handleMousePressed(e, canvas.getGraphicsContext2D()));
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> handleMouseDragged(e, canvas.getGraphicsContext2D()));

    }

    final double[] lastMouseX = {0};
    final double[] lastMouseY = {0};

    private void handleMousePressed(MouseEvent e, GraphicsContext gc) {

        if (e.isPrimaryButtonDown()) {
            int col = (int)(e.getX() / rectangularGrid.getCellSizeWithBorder());
            int row = (int)(e.getY() / rectangularGrid.getCellSizeWithBorder());

            if (col >= 0 && col < rectangularGrid.getCols() && row >= 0 && row < rectangularGrid.getRows()) {
                toggleCell(row, col, gc);
            }
        } else if (e.isSecondaryButtonDown()) {
            lastMouseX[0] = e.getSceneX();
            lastMouseY[0] = e.getSceneY();
        }

    }

    private void handleMouseDragged(MouseEvent e, GraphicsContext gc) {

        if (e.isPrimaryButtonDown()) {
            int col = (int)(e.getX() / rectangularGrid.getCellSizeWithBorder());
            int row = (int)(e.getY() / rectangularGrid.getCellSizeWithBorder());

            if (col >= 0 && col < rectangularGrid.getCols() && row >= 0 && row < rectangularGrid.getRows()) {
                if (lastToggledScreenRow != row || lastToggledScreenCol != col) {
                    toggleCell(row, col, gc);
                }
            }
        } else if (e.isSecondaryButtonDown()) {
            double deltaX = e.getSceneX() - lastMouseX[0];
            double deltaY = e.getSceneY() - lastMouseY[0];

            // Shift the canvas
            canvasContainer.setTranslateX(canvasContainer.getTranslateX() + deltaX);
            canvasContainer.setTranslateY(canvasContainer.getTranslateY() + deltaY);

            // Update the last mouse position
            lastMouseX[0] = e.getSceneX();
            lastMouseY[0] = e.getSceneY();
        }

    }

    private void toggleCell(int row, int col, GraphicsContext gc) {

        lastToggledScreenRow = row;
        lastToggledScreenCol = col;

        // We set "next enum" when mouse is dragged on screen
        GolCellState newState = logic.toggle(row, col);

        drawCell(gc, row, col, colors.get(newState));

        livePercentLabel.set((double) logic.getLiveCells().size() / rectangularGrid.getNumberOfCells());

    }

    private void drawEmptyGrid(GraphicsContext gc) {

        // No need to clear canvas (not sure)

        for (int row = 0; row < rectangularGrid.getRows(); row++) {
            for (int col = 0; col < rectangularGrid.getCols(); col++) {
                drawEmptyCell(gc, row, col);
            }
        }

    }

    private void drawEmptyCell(GraphicsContext gc, int row, int col) {

        double x = col * rectangularGrid.getCellSizeWithBorder();
        double y = row * rectangularGrid.getCellSizeWithBorder();

        gc.setFill(colors.get(GolCellState.DEAD));
        gc.fillRect(x, y, rectangularGrid.getCellSize(), rectangularGrid.getCellSize());

    }

    private void drawGrid(GraphicsContext gc) {

        long milli = currentTimeMillis();

        // No need to clear canvas (not sure)

        int counter = 0;

        for (IntCoordinates cell : logic.getLiveCells()) {
            counter++;
            drawCell(gc, cell, colors.get(GolCellState.ALIVE));
        }
        for (IntCoordinates cell : logic.getDeadCells()) {
            counter++;
            drawCell(gc, cell, colors.get(GolCellState.DEAD));
        }

        log.debug("I drew {} of {} cells in {}ms", counter, rectangularGrid.getNumberOfCells(), (currentTimeMillis() - milli));

    }

    private void drawGridLiveCells(GraphicsContext gc) {
        for (IntCoordinates cell : logic.getLiveCells()) {
            drawCell(gc, cell, colors.get(GolCellState.ALIVE));
        }
    }

    private void drawGridDeadAndEmptyCells(GraphicsContext gc) {
        for (int row = 0; row < rectangularGrid.getRows(); row++) {
            for (int col = 0; col < rectangularGrid.getCols(); col++) {
                if (!logic.getLiveCells().contains(IntCoordinates.of(col, row))) {
                    drawEmptyCell(gc, row, col);
                }
            }
        }
        for (IntCoordinates cell : logic.getDeadCells()) {
            drawCell(gc, cell, colors.get(GolCellState.DEAD));
        }
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

}
