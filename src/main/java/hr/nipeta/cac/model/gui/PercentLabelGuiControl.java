package hr.nipeta.cac.model.gui;

import javafx.scene.control.Label;
import lombok.Getter;

public class PercentLabelGuiControl extends Label {

    @Getter private int count;
    private final String prefix;

    public PercentLabelGuiControl(String prefix) {
        super(prefix + 0);
        this.prefix = prefix;
        this.count = 0;
    }

    public static PercentLabelGuiControl of(String prefix) {
        return new PercentLabelGuiControl(prefix);
    }

    public void reset() {
        this.count = 0;
        setText(prefix + count);
    }

    public void increment() {
        count++;
        setText(prefix + count);
    }
}
