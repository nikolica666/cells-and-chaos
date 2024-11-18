package hr.nik.gol;

import java.util.Collection;
import java.util.Map;

public interface GolRules {
    GolCellState nextState(GolCellState currentState, Collection<GolCellState> neighbours);
}
