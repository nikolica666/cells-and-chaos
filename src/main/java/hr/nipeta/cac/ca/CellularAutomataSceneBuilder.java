package hr.nipeta.cac.ca;

import hr.nipeta.cac.Main;
import hr.nipeta.cac.SceneBuilder;
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

import static hr.nipeta.cac.model.gui.SceneUtils.*;

@Slf4j
public class CellularAutomataSceneBuilder extends SceneBuilder {

    private static final int GRID_SIZE_X = 1024;
    private static final int GRID_SIZE_Y = 512;

    private static CellularAutomataLogic caLogic;

    private static final int RECT_SIZE = 2;
    private static final int RECT_BORDER_WIDTH = 0;
    private static final int RECT_TOTAL_SIZE = RECT_SIZE + RECT_BORDER_WIDTH;

    private PeriodicAnimationTimerGuiControl timerControl;

    private int rule;
    private TextField ruleInput;

    private boolean initOnlyFirstRow;

    private Canvas canvas;

    public CellularAutomataSceneBuilder(Main main) {
        super(main);
    }

    @Override
    public Scene createContent() {

        caLogic = new CellularAutomataLogic(GRID_SIZE_X, GRID_SIZE_Y);

        timerControl = PeriodicAnimationTimerGuiControl.of(PeriodicAnimationTimer.every(20).execute(this::evolveAndDraw));
        ruleInput = createInput("" + rule, 75, createTooltip("Enter rule number"), this::onRuleInputSubmit);

        canvas = new Canvas(GRID_SIZE_X * RECT_TOTAL_SIZE, GRID_SIZE_Y * RECT_TOTAL_SIZE);

        setNewRandomRuleThenDrawGrid();

        Region parent = new VBox(10, mainMenu(), caGrid(canvas));
        parent.setPadding(new Insets(10));
        return new Scene(parent);

    }

    private Node mainMenu() {
        return horizontalMenu(
                timerControl.getStartButton(),
                timerControl.getStopButton(),
                stepButton(),
                timerControl.getDurationInput(),
                ruleInput,
                randomRuleButton(),
                createSceneChangePopupButton()
        );
    }

    private Button stepButton() {
        return createButton("Step", event -> evolveAndDraw());
    }

    private void evolveAndDraw() {
        long milli = System.currentTimeMillis();
        caLogic.evolveRow();
        log.debug("Evolved in {}ms", System.currentTimeMillis()- milli);
        drawGrid(canvas.getGraphicsContext2D());
        log.debug("Evolved and drew in {}ms", System.currentTimeMillis()- milli);
    }

    private Button randomRuleButton() {
        return createButton("Random rule", e -> setNewRandomRuleThenDrawGrid());
    }

    private void onRuleInputSubmit() {
        Integer rule = parseRuleInput(ruleInput.getText());
        if (rule != null) {
            setNewRuleThenDrawGrid(rule);
        }
    }

    private Integer parseRuleInput(String input) {
        try {
            int intInput = Integer.parseInt(input);
            if (intInput < 0 || intInput > 255) {
                showAlertError("Rule number must be between 0 and 255.");
                return null;
            } else {
                return intInput;
            }
        } catch (NumberFormatException ex) {
            showAlertError("Invalid number. Please enter a valid number.");
            return null;
        }
    }

    private void setNewRandomRuleThenDrawGrid() {
//        setNewRuleThenDrawGrid(new Random().nextInt(256));
        setNewRuleThenDrawGrid(30);
    }

    private void setNewRuleThenDrawGrid(int rule) {
        this.rule = rule;
        caLogic.init(rule, initOnlyFirstRow);
        drawGrid(canvas.getGraphicsContext2D());
        ruleInput.setPromptText("" + rule);
    }

    private Node caGrid(Canvas canvas) {
        return new Pane(canvas);
    }

    private void drawGrid(GraphicsContext gc) {

        // Clear the canvas before redrawing
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        gc.setFill(Color.WHEAT);
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        for (int row = 0; row < GRID_SIZE_Y; row++) {
            drawRow(gc, row);
        }

    }

    private void drawRow(GraphicsContext gc, int row) {
        for (int col = 0; col < GRID_SIZE_X; col++) {
            if (caLogic.isAlive(row, col)) {
                drawCell(gc, col, row);
            }
        }

    }

    private void drawCell(GraphicsContext gc, int col, int row) {

        double x = col * RECT_TOTAL_SIZE;
        double y = row * RECT_TOTAL_SIZE;

        // Draw cell background
        gc.setFill(Color.DARKGREEN);
        gc.fillRect(x, y, RECT_SIZE, RECT_SIZE);

    }

}
