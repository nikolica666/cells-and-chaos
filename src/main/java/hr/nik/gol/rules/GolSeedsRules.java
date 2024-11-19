package hr.nik.gol.rules;

import hr.nik.gol.model.GolCell;

/**
 * Rule B2/S - 'Seeds'
 */
public class GolSeedsRules implements GolRules {

    @Override
    public boolean becomeAlive(GolCell[] neighbours) {
        return countLiveNeighbours(neighbours, 2) == 2;
    }

    @Override
    public boolean stayAlive(GolCell[] neighbours) {
        return false;
    }

    @Override
    public String getPatternNotation() {
        return "B2/S";
    }

}
