package hr.nipeta.cac.ant;

import hr.nipeta.cac.model.Coordinates;
import hr.nipeta.cac.model.IntCoordinates;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LangtonAntState {

    private LangtonAntDirection direction;
    private IntCoordinates coordinates;

    public static LangtonAntState of(LangtonAntDirection direction, IntCoordinates coordinates) {
        return new LangtonAntState(direction, coordinates);
    }

}
