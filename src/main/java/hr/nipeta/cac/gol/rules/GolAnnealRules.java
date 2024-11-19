package hr.nipeta.cac.gol.rules;

import hr.nipeta.cac.gol.model.GolCell;

/**
 * Anneal (B4678/S35678)
 */
public class GolAnnealRules implements GolRules {

    public boolean becomeAlive(GolCell[] neighbours) {
        int liveNeighbours = countLiveNeighbours(neighbours, 6);
        return liveNeighbours == 4 || liveNeighbours >= 6;
    }

    public boolean stayAlive(GolCell[] neighbours) {
        int liveNeighbours = countLiveNeighbours(neighbours, 5);
        return liveNeighbours == 3 || liveNeighbours >= 5;
    }

    @Override
    public String getPatternNotation() {
        return "B4678/S35678";
    }

}
