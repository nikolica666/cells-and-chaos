package hr.nipeta.cac.collatz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CollatzLogic {

    @Getter
    public List<Long> sequence;

    public static void main(String[] args){

        CollatzLogic collatzLogic = new CollatzLogic();

        long milli = System.currentTimeMillis();

        MaxSequenceSizeResult maxSequenceSizeResult = collatzLogic.maxSequenceSizeBetween(1_000_000, 5_000_000);
        log.info("Between {} and {} longest sequence is {} ({} elements)",
                maxSequenceSizeResult.getStart(),
                maxSequenceSizeResult.getEnd(),
                maxSequenceSizeResult.getSequenceIndex(),
                maxSequenceSizeResult.getSequenceSize());

        log.info("{}", collatzLogic.createSequence(maxSequenceSizeResult.getSequenceIndex()));

        log.info("Completed in {}ms", (System.currentTimeMillis() - milli));

    }
    public List<Long> createSequence(long start) {

        List<Long> sequence = new ArrayList<>();

        long current = start;

        while (current != 1) {
            sequence.add(current);
            if (current % 2 == 0) {
                current = current / 2;
            } else {
                current = current * 3 + 1;
            }
        }

        sequence.add(1L);

        return sequence;

    }

    public int sequenceSize(long start) {
        return createSequence(start).size();
    }

    public MaxSequenceSizeResult maxSequenceSizeBetween(long start, long end) {

        if (end <= start) {
            throw new RuntimeException(String.format("Start (%s) must be less then end (%s)", start, end));
        }

        MaxSequenceSizeResult result = new MaxSequenceSizeResult(start, end, -1,-1);
        for (long i = start; i < end + 1; i++) {

            if (i % 1_000_000 == 0) {
                log.debug("calculating index {}", i);
            }

            int sequenceSize = sequenceSize(i);
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