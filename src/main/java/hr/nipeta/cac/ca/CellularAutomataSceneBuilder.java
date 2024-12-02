package hr.nipeta.cac.ca;

import hr.nipeta.cac.Main;
import hr.nipeta.cac.SceneBuilder;
import hr.nipeta.cac.model.gui.PeriodicAnimationTimer;
import hr.nipeta.cac.model.gui.PeriodicAnimationTimerGuiControl;
import hr.nipeta.cac.welcome.WelcomeSceneBuilder;
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

import java.util.Random;

import static hr.nipeta.cac.model.gui.SceneUtils.*;

@Slf4j
public class CellularAutomataSceneBuilder extends SceneBuilder {

    private static final int GRID_SIZE_X = 2200;
    private static final int GRID_SIZE_Y = 1100;

    private static CellularAutomataLogic caLogic;

    private static final double RECT_SIZE = 1;
    private static final double RECT_BORDER_WIDTH = 0;
    private static final double RECT_TOTAL_SIZE = RECT_SIZE + RECT_BORDER_WIDTH;

    private PeriodicAnimationTimerGuiControl timerControl;

    private int rule;
    private TextField ruleInput;

    private GraphicsContext gc;

    public CellularAutomataSceneBuilder(Main main) {
        super(main);
    }

    @Override
    public Scene createContent() {

        rule = new Random().nextInt(256);

        caLogic = new CellularAutomataLogic(GRID_SIZE_X, GRID_SIZE_Y);
        caLogic.init(rule);

        timerControl = new PeriodicAnimationTimerGuiControl(PeriodicAnimationTimer.every(20).execute(this::evolveAndDraw));

        ruleInput = createInput("" + rule, 75, createTooltip("Enter rule number"), this::onRuleInputSubmit);

        Region parent = new VBox(10, mainMenu(), caGridWrapped());
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
                welcomeScreenButton()
        );
    }

    private Button stepButton() {
        return createButton("Step", event -> evolveAndDraw());
    }

    private Button welcomeScreenButton() {
        return createButton(
                "Main menu",
                e -> createScene(() -> new WelcomeSceneBuilder(main)));
    }

    private void onRuleInputSubmit() {
        String input = ruleInput.getText();
        Integer rule = null;
        try {
            int intInput = Integer.parseInt(input);
            if (intInput < 0 || intInput > 255) {
                showAlertError("Rule number must be between 0 and 255.");
            } else {
                rule = intInput;
            }
        } catch (NumberFormatException ex) {
            showAlertError("Invalid number. Please enter a valid number.");
        }
        if (rule != null) {
            this.rule = rule;
            caLogic.init(rule);
            drawGrid(gc);
        }
    }

    private Node caGridWrapped() {
        return caGrid();
    }

    private Node caGrid() {

        Canvas canvas = new Canvas(GRID_SIZE_X * RECT_TOTAL_SIZE, GRID_SIZE_Y * RECT_TOTAL_SIZE);
        gc = canvas.getGraphicsContext2D();

        drawGrid(gc);

        return new Pane(canvas);

    }

    private void drawGrid(GraphicsContext gc) {

        // Clear the canvas before redrawing
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        gc.setFill(Color.WHEAT);
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        for (int row = 0; row < GRID_SIZE_Y; row++) {
            for (int col = 0; col < GRID_SIZE_X; col++) {
                if (caLogic.isAlive(row, col)) {
                    drawCell(gc, col, row);
                }
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

    private void evolveAndDraw() {
        long milli = System.currentTimeMillis();
        caLogic.evolveRow();
        drawGrid(gc);
        log.debug("Evolved in {}ms", System.currentTimeMillis()- milli);
    }

}
