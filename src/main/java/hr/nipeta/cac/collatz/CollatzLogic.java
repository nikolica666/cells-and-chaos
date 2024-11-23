package hr.nipeta.cac.collatz;

import hr.nipeta.cac.collatz.rules.CollatzCell;
import hr.nipeta.cac.collatz.rules.CollatzClassicalRules;
import hr.nipeta.cac.collatz.rules.CollatzRules;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Slf4j
public class CollatzLogic {

    private CollatzRules collatzRules;

    public CollatzLogic() {
        this(new CollatzClassicalRules());
    }

    public CollatzLogic(CollatzRules rules) {
        this.collatzRules = rules;
    }

    @Getter
    public List<Long> sequence;

    public static void main(String[] args){

        CollatzLogic collatzLogic = new CollatzLogic();

        long milli = System.currentTimeMillis();

        MaxSequenceSizeResult maxSequenceSizeResult = collatzLogic.maxSequenceSizeBetween(1, 15_000_000);
        log.info("Between {} and {} longest sequence is {} ({} elements)",
                maxSequenceSizeResult.getStart(),
                maxSequenceSizeResult.getEnd(),
                maxSequenceSizeResult.getSequenceIndex(),
                maxSequenceSizeResult.getSequenceSize());

        List<CollatzCell<Void>> sequence1 = collatzLogic.createSequence(maxSequenceSizeResult.getSequenceIndex(), (number, isEven) -> new CollatzCell<>(number, null));
        List<Long> sequence2 = collatzLogic.createSequence(maxSequenceSizeResult.getSequenceIndex());

        log.info("{}", sequence1);
        log.info("{}", sequence2);

        log.info("Completed in {}ms", (System.currentTimeMillis() - milli));

        collatzLogic.createSequence(5, CollatzCell::new);

    }

    public int countSequence(long start) {

        int count = 0;

        long current = start;

        while (current != 1) {
            count++;
            if (current % 2 == 0) {
                current = collatzRules.onEven(current);
            } else {
                current = collatzRules.onOdd(current);
            }
        }

        count++;

        return count;

    }

    public List<Long> createSequence(long start) {

        List<Long> sequence = new ArrayList<>();

        long current = start;

        while (current != 1) {
            sequence.add(current);
            if (current % 2 == 0) {
                current = collatzRules.onEven(current);
            } else {
                current = collatzRules.onOdd(current);
            }
        }

        sequence.add(1L);

        return sequence;

    }

    public <T> List<CollatzCell<T>> createSequence(long start, BiFunction<Long,Boolean,CollatzCell<T>> f) {

        List<CollatzCell<T>> sequence = new ArrayList<>();

        long current = start;

        // TODO This is 20% slower then simple n/2; n*3+1, but enables generalization
        while (current != 1) {
            final boolean currentIsEven = current % 2 == 0;
            final long next;
            if (currentIsEven) {
                next = collatzRules.onEven(current);
            } else {
                next = collatzRules.onOdd(current);
            }
            sequence.add(f.apply(current, currentIsEven));
            current = next;
        }

        sequence.add(f.apply(1L, false));

        return sequence;

    }

    public Map<Number, Number> countSequencesSizesBetween(long start, long end) {

        if (start < 1) {
            throw new RuntimeException(String.format("Start (%s) must be greater then 0", start));
        }

        if (end <= start) {
            throw new RuntimeException(String.format("Start (%s) must be less then end (%s)", start, end));
        }

        HashMap<Number, Number> sizes = new HashMap<>();

        for (long i = start; i < end + 1; i++) {
            sizes.put(i, countSequence(i));
        }

        return sizes;

    }

    public MaxSequenceSizeResult maxSequenceSizeBetween(long start, long end) {

        if (start < 1) {
            throw new RuntimeException(String.format("Start (%s) must be greater then 0", start));
        }

        if (end <= start) {
            throw new RuntimeException(String.format("Start (%s) must be less then end (%s)", start, end));
        }

        MaxSequenceSizeResult result = new MaxSequenceSizeResult(start, end, -1,-1);
        for (long i = start; i < end + 1; i++) {

            if (i % 1_000_000 == 0) {
                log.debug("calculating index {}", i);
            }

            int sequenceSize = countSequence(i);
            if (sequenceSize > result.getSequenceSize()) {
                result.setSequenceIndex(i);
                result.setSequenceSize(sequenceSize);
            }
        }
        return result;
    }

    @Data
    @AllArgsConstructor
    public static class MaxSequenceSizeResult {
        private long start;
        private long end;
        private long sequenceIndex;
        private int sequenceSize;
    }

}