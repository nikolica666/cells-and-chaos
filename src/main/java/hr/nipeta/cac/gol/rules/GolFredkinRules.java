package hr.nipeta.cac.gol.rules;

import hr.nipeta.cac.gol.model.GolCell;

/**
 * Fredkin (B1357/S02468)
 */
public class GolFredkinRules implements GolRules {

    @Override
    public boolean becomeAlive(GolCell[] neighbours) {
        return countLiveNeighbours(neighbours) % 2 == 1;
    }

    @Override
    public boolean stayAlive(GolCell[] neighbours) {
        return countLiveNeighbours(neighbours) % 2 == 0;
    }

    @Override
    public String getPatternNotation() {
        return "B1357/S02468";
    }

}
