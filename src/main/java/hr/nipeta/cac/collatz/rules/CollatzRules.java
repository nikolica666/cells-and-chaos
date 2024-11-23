package hr.nipeta.cac.collatz.rules;

import lombok.AllArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor
public abstract class CollatzRules {

    protected Function<Long,Long> onEvenFunction;
    protected Function<Long,Long> onOddFunction;

    public long onEven(long number) {
        return onEvenFunction.apply(number);
    }

    public long onOdd(long number) {
        return onOddFunction.apply(number);
    }

}
