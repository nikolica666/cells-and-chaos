package hr.nik.gol.rules;

import hr.nik.gol.model.GolCell;

import java.util.Arrays;
import java.util.List;

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
