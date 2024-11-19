package hr.nipeta.cac.ant;

import hr.nipeta.cac.model.Coordinates;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LangtonAntState {

    private LangtonAntDirection direction;
    private Coordinates<Integer> coordinates;

    public static LangtonAntState of(LangtonAntDirection direction, Coordinates<Integer> coordinates) {
        return new LangtonAntState(direction, coordinates);
    }

}
