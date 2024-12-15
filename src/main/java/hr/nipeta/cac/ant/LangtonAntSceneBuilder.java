package hr.nipeta.cac.ant;

import hr.nipeta.cac.Main;
import hr.nipeta.cac.SceneBuilder;
import hr.nipeta.cac.model.Coordinates;
import hr.nipeta.cac.model.IntCoordinates;
import hr.nipeta.cac.model.gui.CounterLabelGuiControl;
import hr.nipeta.cac.model.gui.PeriodicAnimationTimer;
import hr.nipeta.cac.model.gui.PeriodicAnimationTimerGuiControl;
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

import java.util.Arrays;
import java.util.List;

import static hr.nipeta.cac.model.gui.SceneUtils.*;

@Slf4j
public class LangtonAntSceneBuilder extends SceneBuilder {

    private static final int GRID_SIZE_X = 256;
    private static final int GRID_SIZE_Y = 128;

    private static final double RECT_SIZE = 9;
    private static final double RECT_BORDER_WIDTH = 0;
    private static final double RECT_TOTAL_SIZE = RECT_SIZE + RECT_BORDER_WIDTH;

    private static LangtonAntLogic antLogic;

    private String antRules;
    private TextField antRulesInput;

    private PeriodicAnimationTimerGuiControl timerControl;

    private CounterLabelGuiControl countControl;

    private GraphicsContext gc;

    public LangtonAntSceneBuilder(Main main) {
        super(main);
    }

    @Override
    public Scene createContent() {

        // Classical rules are default (2 types of tiles, turn left / turn right)
        antRules = "RL";
        antRulesInput = createInput(antRules, 250, createTooltip("R=Right, L=Left, C=Continue, U=Turn around"), this::onAntRulesInputSubmit);

        antLogic = new LangtonAntLogic(GRID_SIZE_X, GRID_SIZE_Y);
        antLogic.init(antRules);

        timerControl = PeriodicAnimationTimerGuiControl.of(PeriodicAnimationTimer.every(20).execute(this::evolveAndDraw));
        countControl = CounterLabelGuiControl.of("Steps: ");

        Region parent = new VBox(10, mainMenu(), antGridWrapped(), countControl);
        parent.setPadding(new Insets(10));
        return new Scene(parent);

    }

    private Node mainMenu() {
        return horizontalMenu(
                timerControl.getStartButton(),
                timerControl.getStopButton(),
                stepButton(),
                timerControl.getDurationInput(),
                antRulesInput,
                createSceneChangePopupButton()
        );
    }

    private Button stepButton() {
        return createButton("Step", event -> evolveAndDraw());
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
        countControl.reset();

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
        countControl.increment();
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
