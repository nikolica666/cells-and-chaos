package hr.nipeta.cac.gol.rules;

import hr.nipeta.cac.gol.model.GolCell;

/**
 * B1357/S1357
 */
public class GolReplicatorRules extends GolRules {


    @Override
    public boolean becomeAlive(GolCell[] neighbours) {
        return becomeAlive(countLiveNeighbours(neighbours));
    }

    @Override
    public boolean becomeAlive(int liveNeighbours) {
        return liveNeighbours % 2 == 1;
    }

    @Override
    public boolean stayAlive(GolCell[] neighbours) {
        return stayAlive(countLiveNeighbours(neighbours));
    }

    @Override
    public boolean stayAlive(int liveNeighbours) {
        return liveNeighbours % 2 == 1;
    }

    @Override
    public String getPatternNotation() {
        return "B1357/S1357";
    }

    @Override
    public String getName() {
        return "Replicator";
    }

}
