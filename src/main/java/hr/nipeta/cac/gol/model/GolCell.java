package hr.nipeta.cac.gol.model;

import hr.nipeta.cac.model.IntCoordinates;
import lombok.*;

@Getter
@EqualsAndHashCode(of = {"coordinates"})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class GolCell {

    private final IntCoordinates coordinates;
    @Setter private GolCellState currentState;

    public static GolCell create(IntCoordinates coordinates, GolCellState currentState) {
        return new GolCell(coordinates, currentState);
    }

}
