package hr.nipeta.cac.collatz.rules;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = {"number"})
@ToString
public class CollatzCell<T> {

    private long number;
    private T property;

}
