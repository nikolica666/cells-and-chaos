package hr.nipeta.cac.lsystem;

import hr.nipeta.cac.Main;
import hr.nipeta.cac.SceneBuilder;
import hr.nipeta.cac.welcome.WelcomeSceneBuilder;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static hr.nipeta.cac.model.gui.SceneUtils.*;

public class LSystemSceneBuilder extends SceneBuilder {

    private static List<LSystemRules> interestingRules = Arrays.asList(
            new LSystemRules(Map.of('0', "1[0]0",'1', "11"),"0", 8),
            new LSystemRules(Map.of('F', "F+F−F−F+F"),"F", 8),
            new LSystemRules(Map.of('F', "F−G+F+G−F", 'G', "GG"),"F−G−G", 8),
            new LSystemRules(Map.of('F', "F+G", 'G', "F-G"),"F", 8),
            new LSystemRules(Map.of('X', "F+[[X]-X]-F[-FX]+X", 'F', "FF"),"X", 8));

    private static final int GRID_SIZE_X = 128;
    private static final int GRID_SIZE_Y = 64;

    private static final double RECT_SIZE = 12;
    private static final double RECT_BORDER_WIDTH = 0;
    private static final double RECT_TOTAL_SIZE = RECT_SIZE + RECT_BORDER_WIDTH;

    private Map<Character, String> rule;
    private TextField ruleInput;

    private String axiom;
    private TextField axiomInput;

    private GraphicsContext gc;

    public LSystemSceneBuilder(Main main) {
        super(main);
    }

    @Override
    protected Scene createContent() {

        initRuleInput();
        initAxiomInput();

        Region parent = new VBox(10, mainMenu(), caGridWrapped());
        parent.setPadding(new Insets(10));
        return new Scene(parent);
    }

    private void initRuleInput() {
        ruleInput = new TextField();
        ruleInput.setPrefWidth(200);
        ruleInput.setTooltip(createTooltip("Rules should be in format variable1:rule1, variable2:rule2 "));
    }

    private void initAxiomInput() {
        axiomInput = new TextField();
        axiomInput.setPrefWidth(200);
        axiomInput.setPromptText(axiom);
        axiomInput.setTooltip(createTooltip("Axiom is starting point for evolving"));
    }

    private Node mainMenu() {
        return horizontalMenu(
                ruleInput(),
                ruleButton(),
                axiomInput(),
                axiomButton(),
                welcomeScreenButton()
        );
    }

    private Node ruleInput() {
        return onTextInputEnter(ruleInput, this::onRuleInputSubmit);
    }

    private Node ruleButton() {
        return createButton("Set rule", event -> onRuleInputSubmit());
    }

    private void onRuleInputSubmit() {

        String input = ruleInput.getText().replace("\\s+", "");

        // We expect A:ABBA,B:AA[B] format, so first split ","
        String[] inputSplit = input.split(",");

        if (inputSplit.length > 10) {
            showAlertError("Max number of variables is 10, you entered " + inputSplit.length);
            return;
        }

        Map<Character,String> varWithRuleMap = new HashMap<>();
        // We expect A:ABBA,B:AA[B] format, so check ":"
        for (String varWithRule : inputSplit) {

            String[] varWithRuleSplit = varWithRule.split(":");

            if (varWithRuleSplit.length != 2) {
                showAlertError("Expected format is 'A:ABBA,B:A[]BB' and similar, you entered " + varWithRule);
                return;
            }
            if (varWithRuleSplit[0].length() != 1) {
                showAlertError("Expected format for variable is single Character, you entered " + varWithRuleSplit[0]);
                return;
            }

            Character varName = varWithRuleSplit[0].charAt(0);

            if (varWithRuleMap.containsKey(varName)) {
                showAlertError("Variable must be declared only once, you entered multiple " + varName);
                return;
            }

            varWithRuleMap.put(varName, varWithRuleSplit[1]);

        }

        this.rule = varWithRuleMap;

        if (axiom != null) {
            new LSystemRules(varWithRuleMap, axiom, 8);
            drawGrid(gc);
        }

    }

    private Node axiomInput() {
        return onTextInputEnter(axiomInput, this::onAxiomInputSubmit);
    }

    private Node axiomButton() {
        return createButton("Set axiom", event -> onAxiomInputSubmit());
    }

    private void onAxiomInputSubmit() {
        String input = axiomInput.getText();
        this.axiom = axiom;
        drawGrid(gc);
    }

    private Button welcomeScreenButton() {
        return createButton(
                "Main menu",
                e -> createScene(() -> new WelcomeSceneBuilder(main)));
    }

    private Node caGridWrapped() {
        return caGrid();
    }

    private Node caGrid() {

        Canvas canvas = new Canvas(GRID_SIZE_X * RECT_TOTAL_SIZE, GRID_SIZE_Y * RECT_TOTAL_SIZE);
        gc = canvas.getGraphicsContext2D();

        drawGrid(gc);

        return new Pane(canvas);

    }

    private void drawGrid(GraphicsContext gc) {

        // Clear the canvas before redrawing
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());


    }

}
