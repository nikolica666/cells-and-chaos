package hr.nipeta.cac.fract.logistic;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@AllArgsConstructor
public class LogisticLogic {

    private double r;
    private double tolerance;

    public static void main(String[] args) {

        long mili = System.currentTimeMillis();
        LogisticLogic logic = new LogisticLogic(3.27, 1e-12);
        List<Double> logisticResult = logic.calculate(0.5, 33336);

        log.debug("{}", logisticResult);

        log.debug("calculated values in {}ms", (System.currentTimeMillis()-mili));

        List<Double> points = logic.calculateAndDetectPeriodicity(0.5, 50000);
        log.debug("{}",points);

        log.debug("calculated values and period in {}ms", (System.currentTimeMillis()-mili));

    }

    public List<Double> calculateAndDetectPeriodicity(double x0, int iterations) {

        List<Double> calculated = calculate(x0, iterations);
        int period = detectPeriodicity(calculated.subList(iterations / 3, iterations));

        if (period == -1) {
            return Collections.emptyList();
        }

        return calculated.subList(iterations - period, iterations);

    }

    public List<Double> calculate(double x0, int iterations) {

        List<Double> values = new ArrayList<>();
        values.add(x0);

        double xCurrent = x0;

        for (int i = 1; i < iterations; i++) {
            double xNext = calculateNext(xCurrent);
            values.add(xNext);
            xCurrent = xNext;
        }
        return values;
    }

    public double calculateNext(double current) {
        return r * current * (1 - current);
    }

    public int detectPeriodicity(List<Double> values) {

        ArrayList<Double> seenValues = new ArrayList<>();

        // Iterate through the list to find the first repeating value
        for (int currentIndex = 0; currentIndex < values.size(); currentIndex++) {

            // Round to avoid floating-point errors
            double roundedValue = Math.round(values.get(currentIndex) / tolerance) * tolerance;

            // If current value already exists in the list, period = thisIndex - existingIndex
            int existingIndex = seenValues.indexOf(roundedValue);
            if (existingIndex >= 0) {
                final int period = currentIndex - existingIndex;
                log.debug("Found value {} on position {}. Current position is {} so period is {}",
                        roundedValue, existingIndex, currentIndex, period);
                return period;
            }
            seenValues.add(roundedValue);
        }

        // If it's chaotic

        log.debug("Could be chaotic, no same value found for size {} and tolerance {}", values.size(), tolerance);

        return -1;

    }

}
