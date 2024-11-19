package hr.nipeta.cac.gol.rules;

import hr.nipeta.cac.gol.model.GolCell;

/**
 *  Rule B3678/S34678 - 'Day & Night'
 **/
public class GolDayAndNightRules implements GolRules {

    public boolean becomeAlive(GolCell[] neighbours) {
        int liveNeighbours = countLiveNeighbours(neighbours);
        return liveNeighbours == 3 || liveNeighbours >= 6;
    }

    public boolean stayAlive(GolCell[] neighbours) {
        int liveNeighbours = countLiveNeighbours(neighbours);
        return liveNeighbours == 3 || liveNeighbours == 4 || liveNeighbours >= 6;
    }

    @Override
    public String getPatternNotation() {
        return "B3678/S34678";
    }

}
