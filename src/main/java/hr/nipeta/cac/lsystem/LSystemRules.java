package hr.nipeta.cac.lsystem;

import hr.nipeta.cac.model.ComplexNumber;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Random;

@Slf4j
public class LSystemRules {

    public static void main(String[] args) {
        LSystemRules rules1 = new LSystemRules(Map.of('0', "1[0]0",'1', "11"),"0", 8);
        LSystemRules rules2 = new LSystemRules(Map.of('F', "F+F−F−F+F"),"F", 8);
        LSystemRules rules3 = new LSystemRules(Map.of('F', "F−G+F+G−F", 'G', "GG"),"F−G−G", 8);
        LSystemRules rules4 = new LSystemRules(Map.of('F', "F+G", 'G', "F-G"),"F", 8);
        LSystemRules rules5 = new LSystemRules(Map.of('X', "F+[[X]-X]-F[-FX]+X", 'F', "FF"),"X", 8);
    }

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
