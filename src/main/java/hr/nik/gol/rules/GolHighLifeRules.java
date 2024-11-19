package hr.nik.gol.rules;

import hr.nik.gol.model.GolCell;

/**
 *  Rule B36/S23 - 'High life'
 **/
public class GolHighLifeRules implements GolRules {

    public boolean becomeAlive(GolCell[] neighbours) {
        int liveNeighbours = countLiveNeighbours(neighbours, 6);
        return liveNeighbours == 3 || liveNeighbours == 6;
    }

    public boolean stayAlive(GolCell[] neighbours) {
        int liveNeighbours = countLiveNeighbours(neighbours, 3);
        return liveNeighbours == 2 || liveNeighbours == 3;
    }

    @Override
    public String getPatternNotation() {
        return "B36/S23";
    }

}
