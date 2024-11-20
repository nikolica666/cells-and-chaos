package hr.nipeta.cac.gol.rules;

import hr.nipeta.cac.gol.model.GolCell;

/**
 * Rule B2/S - 'Seeds'
 */
public class GolSeedsRules implements GolRules {

    @Override
    public boolean becomeAlive(GolCell[] neighbours) {
        return becomeAlive(countLiveNeighbours(neighbours, 2));
    }

    @Override
    public boolean becomeAlive(int liveNeighbours) {
        return liveNeighbours == 2;
    }

    @Override
    public boolean stayAlive(GolCell[] neighbours) {
        return false;
    }

    @Override
    public boolean stayAlive(int liveNeighbours) {
        return false;
    }

    @Override
    public String getPatternNotation() {
        return "B2/S";
    }

}
