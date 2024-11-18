package hr.nik.gol;

import java.util.Collection;

public class GolClassicRules implements GolRules {

    @Override
    public GolCellState nextState(GolCellState currentState, GolCell[] neighbours) {

        switch (currentState) {
            case DEAD -> {
                return nextStateIfCurrentlyDead(neighbours);
            }
            case ALIVE -> {
                return nextStateIfCurrentlyAlive(neighbours);
            }
            default -> throw new RuntimeException("Unexpected current state " + currentState);
        }

    }

    private GolCellState nextStateIfCurrentlyDead(GolCell[] neighbours) {
        int liveNeighbours = countUpToFourLiveNeighbours(neighbours);
        return liveNeighbours == 3 ? GolCellState.ALIVE : GolCellState.DEAD;
    }

    private GolCellState nextStateIfCurrentlyAlive(GolCell[] neighbours) {
        int liveNeighbours = countUpToFourLiveNeighbours(neighbours);
        return liveNeighbours == 2 || liveNeighbours == 3 ? GolCellState.ALIVE : GolCellState.DEAD;
    }

    private int countUpToFourLiveNeighbours(GolCell[] neighbours) {

        int numberOfLiveNeighbours = 0;

        for (GolCell neighbour : neighbours) {
            if (neighbour.getCurrentState() == GolCellState.ALIVE) {
                numberOfLiveNeighbours++;
            }
            // Mini optimisation, we need exactly 2 or 3 so if it's more we know cell changes to dead
            if (numberOfLiveNeighbours > 3) {
                break;
            }
        }

        return numberOfLiveNeighbours;

    }

}
