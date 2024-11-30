package hr.nipeta.cac.fract.logistic;

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

import java.util.List;

import static hr.nipeta.cac.model.gui.SceneUtils.*;

@Slf4j
public class LogisticSceneBuilder extends SceneBuilder {

    private static final int GRID_SIZE_X = 2200;
    private static final int GRID_SIZE_Y = 1100;

    private static LogisticLogic logic;

    private static final double RECT_SIZE = 1;
    private static final double RECT_BORDER_WIDTH = 0;
    private static final double RECT_TOTAL_SIZE = RECT_SIZE + RECT_BORDER_WIDTH;

    private PeriodicAnimationTimerGuiControl timerControl;

    private GraphicsContext gc;

    public LogisticSceneBuilder(Main main) {
        super(main);
    }

    @Override
    public Scene createContent() {

        timerControl = new PeriodicAnimationTimerGuiControl(
                PeriodicAnimationTimer.every(3).execute(this::evolveAndDraw));

        Region parent = new VBox(10, mainMenu(), caGridWrapped());
        parent.setPadding(new Insets(10));
        return new Scene(parent);

    }

    private Node mainMenu() {
        return horizontalMenu(
                timerControl.getStartButton(),
                timerControl.getStopButton(),
                createButton("Step", event -> evolveAndDraw()),
                timerControl.getDurationInput(),
                createButton("Main menu", e -> createScene(() -> new WelcomeSceneBuilder(main)))
        );
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
            if (timerControl.getTimer().isPlaying()) {
                timerControl.getTimer().stop();
            }
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
