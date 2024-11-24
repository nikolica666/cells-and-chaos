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

            if (deltaY == 0) {
                log.debug("Ignoring {} because deltaY == 0", event.getEventType());
            }

            log.debug("scroll event {}", event);

            // Zoom in or out based on the scroll direction
            if (deltaY > 0) {
                step = step / 2;  // Zoom in
            } else if (deltaY < 0){
                step = step * 2;  // Zoom out
            } else {
                return;
            }

            calculateAndDraw(fractalGc, currentCenter, step, canvasPixelsX, canvasPixelsY);

            event.consume();  // Consume the event to prevent further handling

        });

        tooltipCanvas.addEventHandler(MouseEvent.MOUSE_MOVED, e -> handleMouseMoved(e, tooltipGc));
        // Tooltip layer is on top, on its event, will pass in Fractal's canvas graphic context
        tooltipCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> handleMousePressed(e, fractalGc));

        calculateAndDraw(fractalGc, currentCenter, step, canvasPixelsX, canvasPixelsY);

        StackPane root = new StackPane(fractalCanvas, tooltipCanvas);
        return new Scene(root, canvasPixelsX, canvasPixelsY);

    }

    private void handleMousePressed(MouseEvent e, GraphicsContext gc) {

        double pixelsToCenterX = (canvasPixelsX - 1) / 2 - e.getX();
        double pixelsToCenterY = (canvasPixelsY - 1) / 2 - e.getY();

        double realPart = currentCenter.getX() - pixelsToCenterX * step;
        double imagPart = currentCenter.getY() + pixelsToCenterY * step;

        ComplexNumber newCurrentCenter = ComplexNumber.xy(realPart, imagPart);

        currentCenter = newCurrentCenter;

        switch (e.getButton()) {
            case PRIMARY -> {
                step = step / 2;
            }
            case SECONDARY -> {
                step = step * 2;
            }
        }

        calculateAndDraw(gc, currentCenter, step,canvasPixelsX, canvasPixelsY);

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

    private void calculateAndDraw(GraphicsContext gc, ComplexNumber center, double step, int pixelsX, int pixelsY) {

        FractalResult[][] fractalResults = new MandlebrotLogic()
                .calculateGrid(
                        center.getX() - (pixelsX - 1) / 2 * step,
                        center.getY() + (pixelsY - 1) / 2 * step,
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
                if (x==0&&y==0)log.debug("{}", point);
                if (x==pixelsX&&y==pixelsY)log.debug("{}", point);
                if (point.isDiverged()) {
                    Color color = Color.rgb((int)(point.getIterations() / MandlebrotLogic.MAX_ITERATIONS * 255), 0, 0);
                    pixelWriter.setColor(x, y, color);
                }
            }
        }

    }

}

