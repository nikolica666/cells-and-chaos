package hr.nipeta.cac.gol.rules;

import hr.nipeta.cac.gol.model.GolCell;

/**
 *  Rule B3/S23 - 'Conway's game of life' (the classical one)
 **/
public class GolConwayRules implements GolRules {

    public boolean becomeAlive(GolCell[] neighbours) {
        return becomeAlive(countLiveNeighbours(neighbours, 3));
    }

    @Override
    public boolean becomeAlive(int liveNeighbours) {
        return liveNeighbours == 3;
    }

    public boolean stayAlive(GolCell[] neighbours) {
        return stayAlive(countLiveNeighbours(neighbours, 3));
    }

    @Override
    public boolean stayAlive(int liveNeighbours) {
        return liveNeighbours == 2 || liveNeighbours == 3;
    }

    @Override
    public String getPatternNotation() {
        return "B3/S23";
    }

}
