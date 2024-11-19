package hr.nipeta.cac.gol.rules;

import hr.nipeta.cac.gol.model.GolCell;

/**
 * B3/S12345
 */
public class GolMazeRules implements GolRules {

    @Override
    public boolean becomeAlive(GolCell[] neighbours) {
        return countLiveNeighbours(neighbours, 3) == 3;
    }

    @Override
    public boolean stayAlive(GolCell[] neighbours) {
        return countLiveNeighbours(neighbours, 6) != 6;
    }

    @Override
    public String getPatternNotation() {
        return "B3/S12345";
    }

}
