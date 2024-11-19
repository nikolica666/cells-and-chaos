package hr.nik.gol.model;

import hr.nik.model.Coordinates;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(of = {"coordinates"})
@ToString
public class GolCell {

    private GolCellState currentState;
    @Setter private GolCellState nextState;

    private boolean stateChanged;

    private final Coordinates<Integer> coordinates;

    private GolCell(Coordinates<Integer> coordinates, GolCellState state) {
        this.coordinates = coordinates;
        this.setCurrentState(state); // Reusing setter, there is logic in it
    }

    public static GolCell create(Coordinates<Integer> coordinates, GolCellState currentState) {
        return new GolCell(coordinates, currentState);
    }

    public void setCurrentStateToNextStateValue() {

        if (nextState != currentState) {
            currentState = nextState;
            stateChanged = true;
        } else {
            stateChanged = false;
        }

    }

    public void setCurrentState(GolCellState currentState) {
        if (this.currentState != currentState) {
            this.currentState = currentState;
            stateChanged = true;
        } else {
            stateChanged = false;
        }
    }

}
