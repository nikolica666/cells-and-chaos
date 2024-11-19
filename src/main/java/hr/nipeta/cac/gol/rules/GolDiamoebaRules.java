package hr.nipeta.cac.gol.rules;

import hr.nipeta.cac.gol.model.GolCell;

/**
 *  Diamoeba (B35678/S5678)
 */
public class GolDiamoebaRules implements GolRules {

    public boolean becomeAlive(GolCell[] neighbours) {
        int liveNeighbours = countLiveNeighbours(neighbours, 5);
        return liveNeighbours == 3 || liveNeighbours >= 5;
    }

    public boolean stayAlive(GolCell[] neighbours) {
        int liveNeighbours = countLiveNeighbours(neighbours, 5);
        return liveNeighbours >= 5;
    }

    @Override
    public String getPatternNotation() {
        return "B35678/S5678";
    }

}
