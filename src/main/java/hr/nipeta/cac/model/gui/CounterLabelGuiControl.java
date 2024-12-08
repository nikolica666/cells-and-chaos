package hr.nipeta.cac.model.gui;

import javafx.scene.control.Label;
import lombok.Getter;

public class CounterLabelGuiControl extends Label {

    @Getter private int count;
    private final String prefix;

    public CounterLabelGuiControl(String prefix) {
        super(prefix + 0);
        this.prefix = prefix;
        this.count = 0;
    }

    public static CounterLabelGuiControl of(String prefix) {
        return new CounterLabelGuiControl(prefix);
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
