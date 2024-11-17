package hr.nik.gol;

import hr.nik.Main;
import hr.nik.model.Coordinates;
import hr.nik.SceneBuilder;
import hr.nik.welcome.WelcomeSceneBuilder;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GolOldSceneBuilder extends SceneBuilder {

    private static final int GRID_SIZE_X = 32;
    private static final int GRID_SIZE_Y = 16;

    private static GolLogic golLogic;

    private static final double MAIN_MENU_HEIGHT = 100;
    private static final double RECT_SIZE = (double) 32 ;
    private static final double RECT_BORDER_WIDTH = 2;
    private static final double RECT_TOTAL_SIZE = RECT_SIZE + RECT_BORDER_WIDTH;

    private static final Color RECT_BORDER_COLOR = Color.DARKGREY;

    private final Rectangle[][] cells = new Rectangle[GRID_SIZE_Y][GRID_SIZE_X];
    private Rectangle lastToggledRect;

    private double timeLineDuration;
    private TextField timelineDurationInput;
    private Timeline timeline;
    private boolean timelinePlaying;

    public GolOldSceneBuilder(Main main) {
        super(main);
    }

    public Scene build() {

        golLogic = new GolLogic(GRID_SIZE_X, GRID_SIZE_Y);

        timeLineDuration = 175;

        timeline = new Timeline(new KeyFrame(Duration.millis(timeLineDuration), e -> onTick(cells)));
        timeline.setCycleCount(Timeline.INDEFINITE); // Repeat indefinitely

        timelineDurationInput = new TextField();
        timelineDurationInput.setPrefWidth(150);
        timelineDurationInput.setPromptText("" + timeLineDuration);

        Parent parent = new VBox(mainMenu(), golGridWrapped());

        Scene scene = new Scene(parent, GRID_SIZE_X * RECT_SIZE + 150, GRID_SIZE_Y * RECT_SIZE + MAIN_MENU_HEIGHT + 100);
        scene.setOnMouseDragged(e -> {

            int col = (int)(e.getX() / RECT_TOTAL_SIZE);
            int row = (int)((e.getY() - MAIN_MENU_HEIGHT) / RECT_TOTAL_SIZE);

            if (col >= 0 && col < GRID_SIZE_X && row >= 0 && row < GRID_SIZE_Y) {
                Rectangle rect = cells[row][col];
                if (lastToggledRect != rect) {
                    onElementMouseClickOrDrag(rect);
                    lastToggledRect = rect;
                }
            }
        });

        return scene;

    }

    private Node mainMenu() {
        HBox box = new HBox(
                startButton(),
                stopButton(),
                clearButton(),
                timelineDurationInput,
                timelineDurationButton(),
                welcomeScreenButton());
        box.setMinHeight(MAIN_MENU_HEIGHT);
        box.setMaxHeight(MAIN_MENU_HEIGHT);
        box.setStyle("-fx-font-size: 20px;");
        return box;
    }

    private Button welcomeScreenButton() {
        return createButton(
                "Main menu",
                e -> createScene(() -> new WelcomeSceneBuilder(main)));
    }

    private Button startButton() {
        return createButton("Start", event -> {
            if (!timelinePlaying) {
                onTick(cells);
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

    private Node clearButton() {
        return createButton("Clear", event -> {
            if (timelinePlaying) {
                timeline.stop();
                timelinePlaying = false;
            }

            for (int row = 0; row < GRID_SIZE_Y; row++) {
                for (int col = 0; col < GRID_SIZE_X; col++) {
                    clearState(cells[row][col]);
                }
            }

        });
    }

    private void clearState(Shape shape) {

        Coordinates<Integer> coordinates = castUserData(shape);

        golLogic.clearGolCell(coordinates);

        shape.setFill(initColor());

    }

    private Button timelineDurationButton() {
        return createButton("Set ms", event -> {
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
                timeline.getKeyFrames().setAll(new KeyFrame(Duration.millis(msDuration), e -> onTick(cells)));
                if (timelinePlaying) {
                    timeline.play();
                }
            }
        });
    }

    private Node golGridWrapped() {
        Node gridWrapper = golGrid();
        return gridWrapper;
    }

    private Node golGrid() {

        GridPane grid = new GridPane();

        for (int rowNumber = 0; rowNumber < GRID_SIZE_Y; rowNumber++) {

            Map<Integer, GolCell> columns = new HashMap<>();

            for (int columnNumber = 0; columnNumber < GRID_SIZE_X; columnNumber++) {

                Coordinates<Integer> coordinates = new Coordinates<>(columnNumber, rowNumber);

                Rectangle rect = rectangleAt(coordinates);

                cells[rowNumber][columnNumber] = rect;

                grid.add(rect, columnNumber, rowNumber);

                columns.put(columnNumber, GolCell.createDead(coordinates));

            }

            golLogic.setRowCells(rowNumber, columns);

        }

        return grid;

    }

    private Rectangle rectangleAt(Coordinates<Integer> coordinates) {

        Rectangle rect = new Rectangle(RECT_SIZE, RECT_SIZE, initColor());
        rect.setStroke(RECT_BORDER_COLOR); // Set the border color
        rect.setStrokeWidth(RECT_BORDER_WIDTH);
        rect.setUserData(coordinates);

        rect.setOnMousePressed(e -> {
            onElementMouseClickOrDrag(rect);
            lastToggledRect = rect;
        });

        return rect;

    }

    private void onTick(Shape[][] cells) {
        long milli = System.currentTimeMillis();
        for (int row = 0; row < GRID_SIZE_Y; row++) {
            for (int col = 0; col < GRID_SIZE_X; col++) {
                golLogic.calculateNextState(row, col);
            }
        }
        for (int row = 0; row < GRID_SIZE_Y; row++) {
            for (int col = 0; col < GRID_SIZE_X; col++) {
                boolean aliveNextState = golLogic.setAndReturnNextState(row, col);
                if (aliveNextState) {
                    cells[row][col].setFill(Color.DARKGREEN);
                } else {
                    cells[row][col].setFill(initColor());
                }
            }
        }
        log.debug("Calculated {} cells in {}ms", GRID_SIZE_Y * GRID_SIZE_X, System.currentTimeMillis() - milli);
    }

    private void onElementMouseClickOrDrag(Shape shape) {

        Coordinates<Integer> coordinates = castUserData(shape);

        boolean newStateAlive = golLogic.toggleCell(coordinates);

        if (newStateAlive) {
            shape.setFill(Color.DARKGREEN);
        } else {
            shape.setFill(initColor());
        }

    }

    private <T extends Number> Coordinates<T> castUserData(Node node) {
        return (Coordinates<T>) node.getUserData();
    }

    private Paint initColor() {
        return Color.WHEAT;
    }

}
