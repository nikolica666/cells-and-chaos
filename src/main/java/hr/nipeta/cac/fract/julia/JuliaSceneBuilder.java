package hr.nipeta.cac.fract.julia;

import hr.nipeta.cac.Main;
import hr.nipeta.cac.SceneBuilder;
import hr.nipeta.cac.fract.model.FractalResult;
import hr.nipeta.cac.model.ComplexNumber;
import hr.nipeta.cac.welcome.WelcomeSceneBuilder;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.GestureEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static hr.nipeta.cac.model.gui.SceneUtils.createButton;

@Slf4j
public class JuliaSceneBuilder extends SceneBuilder {

    private int canvasPixelsX = 1200;
    private int canvasPixelsY = 1200;
    private ComplexNumber currentCenter = ComplexNumber.ZERO;
    private double step = 0.003;

    private ComplexNumber pivot;

    public JuliaSceneBuilder(Main main) {
        super(main);
    }

    private static final List<ComplexNumber> interestingJuliaPoints = Arrays.asList(
            ComplexNumber.parse("0.285 + 0.01i"),
            ComplexNumber.parse("−0.70176 - 0.3842i"),
            ComplexNumber.parse("−0.8+0.156i"),
            ComplexNumber.parse("0.285+0.01i"),
            ComplexNumber.parse("−0.4+0.6i"),
            ComplexNumber.parse("−0.74543+0.11301i"),
            ComplexNumber.parse("0.3−0.63i"),
            ComplexNumber.parse("−1.476+0i"),
            ComplexNumber.parse("0.355+0.355i")
    );

    @Override
    public Scene createContent() {

        pivot = interestingJuliaPoints.get(new Random().nextInt(interestingJuliaPoints.size()));

        Canvas fractalCanvas = new Canvas(canvasPixelsX, canvasPixelsY);
        GraphicsContext fractalGc = fractalCanvas.getGraphicsContext2D();
        fractalGc.setFill(Color.TRANSPARENT);

        Canvas tooltipCanvas = new Canvas(canvasPixelsX, canvasPixelsY);
        GraphicsContext tooltipGc = tooltipCanvas.getGraphicsContext2D();
        tooltipGc.setFont(Main.regularFont);

        tooltipCanvas.addEventHandler(MouseEvent.MOUSE_MOVED, e -> handleMouseMoved(e, tooltipGc));
        // Tooltip layer is on top, on its event, will pass in Fractal's canvas graphic context
        tooltipCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> handleMousePressed(e, fractalGc));

        calculateAndDraw(fractalGc, currentCenter,step,canvasPixelsX, canvasPixelsY);

        StackPane stackedCanvas = new StackPane(fractalCanvas, tooltipCanvas);

        Region parent = new VBox(mainMenu(), stackedCanvas);
        parent.setPadding(new Insets(10));
        return new Scene(parent);

    }

    private Node mainMenu() {
        return horizontalMenu(
                welcomeScreenButton()
        );
    }

    private Button welcomeScreenButton() {
        return createButton(
                "Main menu",
                e -> createScene(() -> new WelcomeSceneBuilder(main)));
    }

    private void recalculateCenter(GestureEvent e) {
        recalculateCenter(e.getX(), e.getY());
    }

    private void recalculateCenter(MouseEvent e) {
        recalculateCenter(e.getX(), e.getY());
    }

    // TODO Needs refactoring and generalisation
    private void recalculateCenter(double eventX, double eventY) {

        double pixelsToCenterX = (double)canvasPixelsX / 2 - eventX;
        double pixelsToCenterY = (double)canvasPixelsY / 2 - eventY;

        double realPart = currentCenter.getX() - pixelsToCenterX * step;
        double imaginaryPart = currentCenter.getY() + pixelsToCenterY * step;

        currentCenter = ComplexNumber.xy(realPart, imaginaryPart);

    }

    private void handleMousePressed(MouseEvent e, GraphicsContext gc) {

        recalculateCenter(e);

        switch (e.getButton()) {
            case PRIMARY -> {
                step = step / 2;
            }
            case SECONDARY -> {
                step = step * 2;
            }
        }

        calculateAndDraw(gc, currentCenter, step, canvasPixelsX, canvasPixelsY);

    }

    private void handleMouseMoved(MouseEvent e, GraphicsContext gc) {

        double pixelsToCenterX = (double)canvasPixelsX / 2 - e.getX();
        double pixelsToCenterY = (double)canvasPixelsY / 2 - e.getY();

        double realPart = currentCenter.getX() - pixelsToCenterX * step;
        double imaginaryPart = currentCenter.getY() + pixelsToCenterY * step;

        gc.setFill(Color.WHEAT);
        gc.clearRect(0, 0, canvasPixelsX, canvasPixelsY);
        gc.fillText(
                String.format("pixel (%s,%s)\r\nto center (%s,%s)\r\n%f+%fi",
                        e.getX(),
                        e.getY(),
                        pixelsToCenterX,
                        pixelsToCenterY,
                        realPart,
                        imaginaryPart),
                e.getX() + 16,
                e.getY());

    }

    private void calculateAndDraw(GraphicsContext gc, ComplexNumber center, double step, int pixelsX, int pixelsY) {

        FractalResult[][] fractalResults = new JuliaLogic(pivot)
                .calculateGrid(
                        center.getX() - (double)pixelsX / 2 * step,
                        center.getY() + (double)pixelsY / 2 * step,
                        step,
                        pixelsX,
                        pixelsY);

        var pixelWriter = gc.getPixelWriter();

        gc.clearRect(0,0, canvasPixelsX, canvasPixelsY);
        gc.setFill(Color.BLACK);
        gc.fillRect(0,0, canvasPixelsX, canvasPixelsY);

        // Loop through each pixel (x, y)
        for (int x = 0; x < pixelsX; x++) {
            for (int y = 0; y < pixelsY; y++) {
                FractalResult point = fractalResults[x][y];
                if (point.isDiverged()) {
                    Color color = COLORS_RED.get(point.getIterations());
                    pixelWriter.setColor(x, y, color);
                }
            }
        }

    }

    public static final List<Color> COLORS_RED = IntStream.range(0, 256)
            .mapToObj(i -> Color.rgb(i, 0, 0)) // Example gradient
            .collect(Collectors.toList());

}

