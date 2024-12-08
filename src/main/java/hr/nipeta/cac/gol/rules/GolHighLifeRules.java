package hr.nipeta.cac.gol.rules;

import hr.nipeta.cac.gol.model.GolCell;

/**
 *  Rule B36/S23 - 'High life'
 **/
public class GolHighLifeRules extends GolRules {

    public boolean becomeAlive(GolCell[] neighbours) {
        int liveNeighbours = countLiveNeighbours(neighbours, 6);
        return becomeAlive(liveNeighbours);
    }

    @Override
    public boolean becomeAlive(int liveNeighbours) {
        return liveNeighbours == 3 || liveNeighbours == 6;
    }

    public boolean stayAlive(GolCell[] neighbours) {
        int liveNeighbours = countLiveNeighbours(neighbours, 3);
        return stayAlive(liveNeighbours);
    }

    @Override
    public boolean stayAlive(int liveNeighbours) {
        return liveNeighbours == 2 || liveNeighbours == 3;
    }

    @Override
    public String getPatternNotation() {
        return "B36/S23";
    }

    @Override
    public String getName() {
        return "High life";
    }

}
