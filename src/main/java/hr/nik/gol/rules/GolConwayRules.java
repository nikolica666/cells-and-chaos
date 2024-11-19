package hr.nik.gol.rules;

import hr.nik.gol.model.GolCell;

/**
 *  Rule B3/S23 - 'Conway's game of life' (the classical one)
 **/
public class GolConwayRules implements GolRules {

    public boolean becomeAlive(GolCell[] neighbours) {
        int liveNeighbours = countLiveNeighbours(neighbours, 3);
        return liveNeighbours == 3;
    }

    public boolean stayAlive(GolCell[] neighbours) {
        int liveNeighbours = countLiveNeighbours(neighbours);
        return liveNeighbours == 2 || liveNeighbours == 3;
    }

    @Override
    public String getPatternNotation() {
        return "B3/S23";
    }

}
