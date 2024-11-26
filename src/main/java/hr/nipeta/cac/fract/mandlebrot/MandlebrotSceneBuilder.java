package hr.nipeta.cac.fract.mandlebrot;

import hr.nipeta.cac.Main;
import hr.nipeta.cac.SceneBuilder;
import hr.nipeta.cac.fract.model.FractalResult;
import hr.nipeta.cac.model.ComplexNumber;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class MandlebrotSceneBuilder extends SceneBuilder {

    private int canvasPixelsX = 1500;
    private int canvasPixelsY = 1000;
    private ComplexNumber currentCenter = ComplexNumber.MINUS_ONE;
    private double step = 0.0025;

    public MandlebrotSceneBuilder(Main main) {
        super(main);
    }

    @Override
    public Scene createContent() {

        Canvas fractalCanvas = new Canvas(canvasPixelsX, canvasPixelsY);
        GraphicsContext fractalGc = fractalCanvas.getGraphicsContext2D();
        fractalGc.setFill(Color.TRANSPARENT);

        Canvas tooltipCanvas = new Canvas(canvasPixelsX, canvasPixelsY);
        GraphicsContext tooltipGc = tooltipCanvas.getGraphicsContext2D();
        tooltipGc.setFont(new Font(16));

        tooltipCanvas.addEventHandler(MouseEvent.MOUSE_MOVED, e -> handleMouseMoved(e, tooltipGc));
        // Tooltip layer is on top, on its event, will pass in Fractal's canvas graphic context
        tooltipCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> handleMousePressed(e, fractalGc));
        tooltipCanvas.addEventHandler(ScrollEvent.SCROLL, event -> handleScroll(event, fractalGc));

        calculateAndDraw(fractalGc, currentCenter, step, canvasPixelsX, canvasPixelsY);

        StackPane root = new StackPane(fractalCanvas, tooltipCanvas);
        return new Scene(root, canvasPixelsX, canvasPixelsY);

    }

    private void handleScroll(ScrollEvent e, GraphicsContext gc) {
        double deltaY = e.getDeltaY();

        if (deltaY == 0) {
            log.debug("Ignoring {} because deltaY == 0", e.getEventType());
        }

        // Zoom in or out based on the scroll direction
        if (deltaY > 0) {
            step = step / 2;  // Zoom in
        } else if (deltaY < 0){
            step = step * 2;  // Zoom out
        } else {
            return;
        }

        calculateAndDraw(gc, currentCenter, step, canvasPixelsX, canvasPixelsY);

        e.consume();

    }

    private void handleMousePressed(MouseEvent e, GraphicsContext gc) {

        double pixelsToCenterX = (double)canvasPixelsX / 2 - e.getX();
        double pixelsToCenterY = (double)canvasPixelsY / 2 - e.getY();

        double realPart = currentCenter.getX() - pixelsToCenterX * step;
        double imaginaryPart = currentCenter.getY() + pixelsToCenterY * step;

        currentCenter = ComplexNumber.xy(realPart, imaginaryPart);

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

