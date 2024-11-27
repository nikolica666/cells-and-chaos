package hr.nipeta.cac.lsystem;

import hr.nipeta.cac.model.ComplexNumber;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Random;

@Slf4j
public class LSystemRules {

    public LSystemRules(Map<Character,String> variablesAndRules, String axiom, int steps) {
        evolve(variablesAndRules, axiom, steps);
    }

    public String evolve(Map<Character,String> variablesAndRules, String currentState, int steps) {

        log.debug("Current state = {}", currentState);

        if (steps == 0) {
            log.debug("Last state size = {}", currentState.length());
            return currentState;
        }

        StringBuilder newStateBuilder = new StringBuilder();

        for (Character c : currentState.toCharArray()) {
            // Get rule for this char, or default just append itself (because that means it's a constant)
            newStateBuilder.append(variablesAndRules.getOrDefault(c, c.toString()));
        }

        return evolve(variablesAndRules, newStateBuilder.toString(), steps - 1);

    }

}
