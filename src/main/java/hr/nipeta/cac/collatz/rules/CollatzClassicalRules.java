package hr.nipeta.cac.collatz.rules;

import java.util.function.Function;

public class CollatzClassicalRules extends CollatzRules {

    public CollatzClassicalRules() {
        super((num) -> num / 2, (num) -> 3 * num + 1);
    }

}
