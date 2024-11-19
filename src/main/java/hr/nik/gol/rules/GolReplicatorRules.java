package hr.nik.gol.rules;

import hr.nik.gol.model.GolCell;

/**
 * B1357/S1357
 */
public class GolReplicatorRules implements GolRules {
    @Override
    public boolean becomeAlive(GolCell[] neighbours) {
        return countLiveNeighbours(neighbours) % 2 == 1;
    }

    @Override
    public boolean stayAlive(GolCell[] neighbours) {
        return countLiveNeighbours(neighbours) % 2 == 1;
    }

    @Override
    public String getPatternNotation() {
        return "B1357/S1357";
    }

}
