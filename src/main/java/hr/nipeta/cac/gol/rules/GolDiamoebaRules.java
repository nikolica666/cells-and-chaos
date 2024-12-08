package hr.nipeta.cac.gol.rules;

import hr.nipeta.cac.gol.model.GolCell;

/**
 *  Diamoeba (B35678/S5678)
 */
public class GolDiamoebaRules extends GolRules {

    public boolean becomeAlive(GolCell[] neighbours) {
        return becomeAlive(countLiveNeighbours(neighbours, 5));
    }

    @Override
    public boolean becomeAlive(int liveNeighbours) {
        return liveNeighbours == 3 || liveNeighbours >= 5;
    }

    public boolean stayAlive(GolCell[] neighbours) {
        return stayAlive(countLiveNeighbours(neighbours, 5));
    }

    @Override
    public boolean stayAlive(int liveNeighbours) {
        return liveNeighbours >= 5;
    }

    @Override
    public String getPatternNotation() {
        return "B35678/S5678";
    }

    @Override
    public String getName() {
        return "Diamoeba";
    }

}
