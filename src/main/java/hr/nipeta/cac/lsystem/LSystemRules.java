package hr.nipeta.cac.lsystem;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class LSystemRules {

    private final Map<Character,String> variablesAndRules;

    public static void main(String[] args) {
        LSystemRules rules1 = new LSystemRules(Map.of('0', "1[0]0",'1', "11"),"0", 5);
        LSystemRules rules2 = new LSystemRules(Map.of('F', "F+F−F−F+F"),"F", 5);
        LSystemRules rules3 = new LSystemRules(Map.of('F', "F−G+F+G−F", 'G', "GG"),"F−G−G", 5);
        LSystemRules rules4 = new LSystemRules(Map.of('F', "F+G", 'G', "F-G"),"F", 5);
        LSystemRules rules5 = new LSystemRules(Map.of('X', "F+[[X]-X]-F[-FX]+X", 'F', "FF"),"X", 15);
    }

    public LSystemRules(Map<Character,String> variablesAndRules, String axiom, int steps) {
        this.variablesAndRules = variablesAndRules;
        evolve(axiom, steps);
    }

    public String evolve(String currentState, int steps) {

        log.debug("Current state = {}", currentState);

        if (steps == 0) {
            return currentState;
        }

        StringBuilder newStateBuilder = new StringBuilder();

        for (Character c : currentState.toCharArray()) {
            // Get rule for this char, or default just append itself (because that means it's a constant)
            newStateBuilder.append(variablesAndRules.getOrDefault(c, c.toString()));
        }

        return evolve(newStateBuilder.toString(), steps - 1);

    }

}
