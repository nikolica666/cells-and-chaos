package hr.nipeta.cac.fract.mandlebrot;

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
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static hr.nipeta.cac.model.gui.SceneUtils.createButton;

@Slf4j
public class MandlebrotSceneBuilder extends SceneBuilder {

    // Interesting point -0.15985113725373606 + 1.0465216943636952i step 1.1368683772161603E-15 stepsX=1500 stepsY=1000
    // Interesting point -0.87 + 0.27i
    // Interesting point -0.5555 + 0.636i

    private int canvasPixelsX = 1500;
    private int canvasPixelsY = 1000;
    private ComplexNumber currentCenter = ComplexNumber.MINUS_ONE;
    private double step = 0.0025;

    private Canvas fractalCanvas;
    private Canvas tooltipCanvas;

    public MandlebrotSceneBuilder(Main main) {
        super(main);
    }

    @Override
    public Scene createContent() {

        fractalCanvas = new Canvas(canvasPixelsX, canvasPixelsY);
        GraphicsContext fractalGc = fractalCanvas.getGraphicsContext2D();
        fractalGc.setFill(Color.TRANSPARENT);

        tooltipCanvas = new Canvas(canvasPixelsX, canvasPixelsY);
        GraphicsContext tooltipGc = tooltipCanvas.getGraphicsContext2D();
        tooltipGc.setFont(Main.regularFont);

        tooltipCanvas.addEventHandler(MouseEvent.MOUSE_MOVED, e -> handleMouseMoved(e, tooltipGc));
        // Tooltip layer is on top, on its event, will pass in Fractal's canvas graphic context
        tooltipCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> handleMousePressed(e, fractalGc));
        tooltipCanvas.addEventHandler(ScrollEvent.SCROLL, event -> handleScroll(event, fractalGc));

        fractalGc.clearRect(0, 0, canvasPixelsX, canvasPixelsY);
        fractalGc.setFill(Color.BLACK);
        fractalGc.fillRect(0, 0, canvasPixelsX, canvasPixelsY);

        calculateAndDraw(fractalGc, currentCenter, step, canvasPixelsX, canvasPixelsY);

        StackPane stackedCanvas = new StackPane(fractalCanvas, tooltipCanvas);

        Region parent = new VBox(10, createSceneChangePopupButton(), stackedCanvas);
        parent.setPadding(new Insets(10));
        return new Scene(parent);

    }

    private Button welcomeScreenButton() {
        return createButton(
                "Main menu",
                e -> createScene(() -> new WelcomeSceneBuilder(main)));
    }

    private void handleScroll(ScrollEvent e, GraphicsContext gc) {

        double deltaY = e.getDeltaY();

        if (deltaY == 0) {
            log.debug("Ignoring {} because deltaY == 0", e.getEventType());
            return;
        }

        currentCenter = recalculateCenter(e);

        if (deltaY > 0) {
            step = step / 2;  // Zoom in
        } else {
            step = step * 2;  // Zoom out
        }

        calculateAndDraw(gc, currentCenter, step, canvasPixelsX, canvasPixelsY);

    }

    private ComplexNumber recalculateCenter(GestureEvent e) {
        return recalculateCenter(e.getX(), e.getY(), canvasPixelsX, canvasPixelsY);
    }

    private ComplexNumber recalculateCenter(MouseEvent e) {
        return recalculateCenter(e.getX(), e.getY(), canvasPixelsX, canvasPixelsY);
    }

    // TODO Needs refactoring and generalisation
    private ComplexNumber recalculateCenter(double pointX, double pointY, int totalPixelsX, int totalPixelsY) {

        double pixelsToCenterX = (double)totalPixelsX / 2 - pointX;
        double pixelsToCenterY = (double)totalPixelsY / 2 - pointY;

        double realPart = currentCenter.getX() - pixelsToCenterX * step;
        double imaginaryPart = currentCenter.getY() + pixelsToCenterY * step;

        return ComplexNumber.xy(realPart, imaginaryPart);

    }

    public void zoomInManually(int times, ComplexNumber newCenter) {

        currentCenter = newCenter;
        step = Math.pow(step, times);

        calculateAndDraw(fractalCanvas.getGraphicsContext2D(), currentCenter, step, canvasPixelsX, canvasPixelsY);

    }

    private void handleMousePressed(MouseEvent e, GraphicsContext gc) {

        currentCenter = recalculateCenter(e);

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

        FractalResult[][] fractalResults = new MandlebrotLogic()
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
                    // TODO ideas for better colors
                    //  Color color = Color.hsb((double)point.getIterations() / MandlebrotLogic.MAX_ITERATIONS * 180, .7, .7); // HSB color model
                    //  Color color = Color.color((double)point.getIterations() / MandlebrotLogic.MAX_ITERATIONS, 0, 0); // more then 255 red shades
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

