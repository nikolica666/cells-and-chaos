package hr.nipeta.cac.gol.rules;

import hr.nipeta.cac.gol.model.GolCell;

/**
 * Anneal (B4678/S35678)
 */
public class GolAnnealRules implements GolRules {

    public boolean becomeAlive(GolCell[] neighbours) {
        return becomeAlive(countLiveNeighbours(neighbours, 6));
    }

    @Override
    public boolean becomeAlive(int liveNeighbours) {
        return liveNeighbours == 4 || liveNeighbours >= 6;
    }

    public boolean stayAlive(GolCell[] neighbours) {
        return stayAlive(countLiveNeighbours(neighbours, 5));
    }

    @Override
    public boolean stayAlive(int liveNeighbours) {
        return liveNeighbours == 3 || liveNeighbours >= 5;
    }

    @Override
    public String getPatternNotation() {
        return "B4678/S35678";
    }

}
