package hr.nipeta.cac.fract.julia;

import hr.nipeta.cac.fract.model.FractalResult;
import hr.nipeta.cac.model.ComplexNumber;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.IntStream;

@Slf4j
@AllArgsConstructor
public class JuliaLogic {

    private final ComplexNumber pivot;

    public static final double MAX_ITERATIONS = 255;
    public static final short MAX_MAGNITUDE = 2;
    // So we don't need to calculate SQRT for magmitude
    public static final short MAX_MAGNITUDE_SQUARED = MAX_MAGNITUDE * MAX_MAGNITUDE;
    public static final double EPSILON = 1e-9;

    public FractalResult[][] calculateGrid(double fromTopLeftX, double fromTopLeftY, double step, int stepsX, int stepsY) {

        long milli = System.currentTimeMillis();
        log.debug("from {} + {}i step {} stepsX={} stepsY={}", fromTopLeftX, fromTopLeftY, step, stepsX, stepsY);
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
        return calculate(c, c.magnitudeSq(), pivot, 0);
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
