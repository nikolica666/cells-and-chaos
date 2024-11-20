package hr.nipeta.cac.gol.rules;

import hr.nipeta.cac.gol.model.GolCell;
import hr.nipeta.cac.gol.model.GolCellState;

/**
 * Rules for classical Born/Survive rules i.e. 2 GolCellStates
 */
public interface GolRules {

    default GolCellState nextState(GolCellState currentState, GolCell[] neighbours) {
        switch (currentState) {
            case DEAD -> {
                return becomeAlive(neighbours) ? GolCellState.ALIVE : GolCellState.DEAD;
            }
            case ALIVE -> {
                return stayAlive(neighbours) ? GolCellState.ALIVE : GolCellState.DEAD;
            }
            default -> throw new RuntimeException("Unexpected current state " + currentState);
        }
    }

    boolean becomeAlive(GolCell[] neighbours);
    boolean becomeAlive(int liveNeighbours);
    boolean stayAlive(GolCell[] neighbours);
    boolean stayAlive(int liveNeighbours);

    // TODO refactor this altough we'll probably end up deleting and use something optimal
    default int countLiveNeighbours(GolCell[] neighbours, Integer enoughLiveNeighbours) {

        int numberOfLiveNeighbours = 0;

        for (GolCell neighbour : neighbours) {
            if (numberOfLiveNeighbours >= enoughLiveNeighbours) {
                break;
            }
            if (neighbour.getCurrentState() == GolCellState.ALIVE) {
                numberOfLiveNeighbours++;
            }
        }

        return numberOfLiveNeighbours;

    }

    default int countLiveNeighbours(GolCell[] neighbours) {

        int numberOfLiveNeighbours = 0;

        for (GolCell neighbour : neighbours) {
            if (neighbour.getCurrentState() == GolCellState.ALIVE) {
                numberOfLiveNeighbours++;
            }
        }

        return numberOfLiveNeighbours;

    }

    /**
     *
     * @return pattern notation in Bx/Sy format (B = born, S = survive)
     */
    String getPatternNotation();

}
