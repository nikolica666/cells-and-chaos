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

@Slf4j
public class MandlebrotSceneBuilder extends SceneBuilder {

    private int canvasPixelsX = 1501;
    private int canvasPixelsY = 1001;
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

        tooltipCanvas.addEventHandler(ScrollEvent.SCROLL, event -> {

            double deltaY = event.getDeltaY();

            log.debug("scroll event, deltaY={}", deltaY);

            // Zoom in or out based on the scroll direction
            if (deltaY > 0) {
                step = step / 2;  // Zoom in
            } else {
                step = step * 2;  // Zoom out
            }

            calculateAndDraw(fractalGc, currentCenter, step,(canvasPixelsX - 1) / 2, (canvasPixelsY - 1) / 2);

            //event.consume();  // Consume the event to prevent further handling

        });

        tooltipCanvas.addEventHandler(MouseEvent.MOUSE_MOVED, e -> handleMouseMoved(e, tooltipGc));
        // Tooltip layer is on top, on its event, will pass in Fractal's canvas graphic context
        tooltipCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> handleMousePressed(e, fractalGc));

        calculateAndDraw(fractalGc, currentCenter,step,(canvasPixelsX - 1) / 2, (canvasPixelsY - 1) / 2);

        StackPane root = new StackPane(fractalCanvas, tooltipCanvas);
        return new Scene(root, canvasPixelsX, canvasPixelsY);

    }

    private void handleMousePressed(MouseEvent e, GraphicsContext gc) {

        log.debug("{}", e.getEventType());

        log.debug("{} on x={}, y={}", e.getEventType(), e.getX(),e.getY());

        double pixelsToCenterX = (canvasPixelsX - 1) / 2 - e.getX();
        double pixelsToCenterY = (canvasPixelsY - 1) / 2 - e.getY();

        log.debug("pixelsToCenterX={}, pixelsToCenterY={}", pixelsToCenterX,pixelsToCenterY);

        double realPart = currentCenter.getX() - pixelsToCenterX * step;
        double imagPart = currentCenter.getY() + pixelsToCenterY * step;

        log.debug("coordX={}, coordY={}", realPart, imagPart);

        ComplexNumber newCurrentCenter = ComplexNumber.xy(realPart, imagPart);

        log.debug("oldCenter={}", currentCenter);
        log.debug("newCenter={}", newCurrentCenter);

        currentCenter = newCurrentCenter;

        switch (e.getButton()) {
            case PRIMARY -> {
                step = step / 2;
            }
            case SECONDARY -> {
                step = step * 2;
            }
        }

        calculateAndDraw(gc, currentCenter, step,(canvasPixelsX - 1) / 2, (canvasPixelsY - 1) / 2);

    }

    private void handleMouseMoved(MouseEvent e, GraphicsContext gc) {

        double pixelsToCenterX = (canvasPixelsX - 1) / 2 - e.getX();
        double pixelsToCenterY = (canvasPixelsY - 1) / 2 - e.getY();

        double realPart = currentCenter.getX() - pixelsToCenterX * step;
        double imagPart = currentCenter.getY() + pixelsToCenterY * step;

        gc.setFill(Color.WHEAT);
        gc.clearRect(0, 0, canvasPixelsX, canvasPixelsY);
        gc.fillText(
                String.format("pixel (%s,%s)\r\nto center (%s,%s)\r\n%f+%fi",
                        e.getX(),
                        e.getY(),
                        pixelsToCenterX,
                        pixelsToCenterY,
                        realPart,
                        imagPart),
                e.getX() + 16,
                e.getY());

    }

    private void calculateAndDraw(GraphicsContext gc, ComplexNumber center, double step, int stepsX, int stepsY) {

        FractalResult[][] fractalResults = new MandlebrotLogic().calculateGrid(center, step, stepsX, stepsY);

        var pixelWriter = gc.getPixelWriter();

        gc.clearRect(0,0, canvasPixelsX, canvasPixelsY);
        gc.setFill(Color.BLACK);
        gc.fillRect(0,0, canvasPixelsX, canvasPixelsY);

        // Loop through each pixel (x, y)
        for (int x = 0; x < 2 * stepsX + 1; x++) {
            for (int y = 0; y < 2 * stepsY + 1; y++) {
                FractalResult point = fractalResults[x][y];
                if (point.isDiverged()) {
                    Color color = Color.rgb((int)(point.getIterations() / MandlebrotLogic.MAX_ITERATIONS * 255), 0, 0);
                    pixelWriter.setColor(x, y, color);
                }
            }
        }

    }

}

