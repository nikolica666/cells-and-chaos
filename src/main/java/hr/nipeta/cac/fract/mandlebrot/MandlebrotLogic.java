package hr.nipeta.cac.fract.mandlebrot;

import hr.nipeta.cac.fract.model.FractalResult;
import hr.nipeta.cac.model.ComplexNumber;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.IntStream;

@Slf4j
public class MandlebrotLogic {

    private static final double PERIOD_2_BULB_RADIUS = 0.25;
    private static final double PERIOD_2_BULB_RADIUS_SQUARED = PERIOD_2_BULB_RADIUS * PERIOD_2_BULB_RADIUS;

    public static final short MAX_ITERATIONS = 255;
    public static final short MAX_MAGNITUDE = 2;
    // So we don't need to calculate SQRT for magmitude
    public static final short MAX_MAGNITUDE_SQUARED = MAX_MAGNITUDE * MAX_MAGNITUDE;
    public static final double EPSILON = 1e-9;

    public FractalResult[][] calculateGrid(double fromTopLeftX, double fromTopLeftY, double step, int stepsX, int stepsY) {

        log.debug("from {} + {}i step {} stepsX={} stepsY={}", fromTopLeftX, fromTopLeftY, step, stepsX, stepsY);

        long milli = System.currentTimeMillis();

        FractalResult[][] result = new FractalResult[stepsX][stepsY];

        IntStream.range(0, stepsX).parallel().forEach(i -> {
            for (int j = 0; j < stepsY; j++) {
                result[i][j] = calculate(ComplexNumber.xy(fromTopLeftX + i * step, fromTopLeftY - j * step));
            }
        });

        log.debug("calculated grid in {}ms", (System.currentTimeMillis() - milli));
        return result;

    }

    public FractalResult calculate(ComplexNumber c) {

        if (insideMainCardioid(c)) {
            return FractalResult.converged(c, 0, "Inside main cardioid");
        }

        if (insideLeftCircle(c)) {
            return FractalResult.converged(c, 0, "Inside left circle (period-2 bulb)");
        }

        return calculate(ComplexNumber.ZERO, 0, c, 0);
    }

    /**
     * Main cardioid  is the large heart-shaped region centered at approximately at (-0.5,0)
     * {@code (x−0.25)^2 + y^2 <= (x-0.25)^4}
     */
    private boolean insideMainCardioid(ComplexNumber c) {

        // TODO this is just for mini optimisation, can be done more clever
        double x = c.getX();
        double y = c.getY();
        return x < 0.2 && x > -0.55 && y < 0.4 && y > -0.4;

    }

    /**
     * By 'left circle' we mean Period-2 bulb (smaller circle to the left of the main cardioid).
     * Formula is {@code (x+1)^2 + y^2 <= 1/16}
     */
    private boolean insideLeftCircle(ComplexNumber c) {

        double x = c.getX();
        if (x < -1 - PERIOD_2_BULB_RADIUS || x > -1 + PERIOD_2_BULB_RADIUS) {
            return false;
        }

        double y = c.getY();
        if (y < -PERIOD_2_BULB_RADIUS || y > PERIOD_2_BULB_RADIUS) {
            return false;
        }

        return y*y + (x+1)*(x+1) <= PERIOD_2_BULB_RADIUS_SQUARED;

    }

    public FractalResult calculate(ComplexNumber zCurr, double zCurrMagnitudeSquared, ComplexNumber c, int iteration) {

        ComplexNumber zNext = zCurr.sq().add(c);
        double zNextMagnitudeSquared = zNext.magnitudeSq();

        if (zNextMagnitudeSquared > MAX_MAGNITUDE_SQUARED) {
            return FractalResult.diverged(c, iteration);
        }

        if (Math.abs(zCurrMagnitudeSquared - zNextMagnitudeSquared) < EPSILON) {
            return FractalResult.converged(c, iteration, "Magnitude difference less then " + EPSILON);
        }

        if (iteration >= MAX_ITERATIONS) {
            return FractalResult.converged(c, iteration, "Iteration > max " + MAX_ITERATIONS);
        }

        iteration++;

        return calculate(zNext, zNextMagnitudeSquared, c, iteration);

    }

}
