package hr.nik.gol.rules;

import hr.nik.gol.model.GolCell;

/**
 * Morley (B368/S245)
 */
public class GolMorleyRules implements GolRules {

    @Override
    public boolean becomeAlive(GolCell[] neighbours) {
        int liveNeighbours = countLiveNeighbours(neighbours);
        return liveNeighbours == 3 || liveNeighbours == 6 || liveNeighbours == 8;
    }

    @Override
    public boolean stayAlive(GolCell[] neighbours) {
        int liveNeighbours = countLiveNeighbours(neighbours, 5);
        return liveNeighbours == 2 || liveNeighbours == 4 || liveNeighbours == 5;
    }

    @Override
    public String getPatternNotation() {
        return "B368/S245";
    }

}
