package hr.nipeta.cac.model.gui;

import javafx.scene.control.Label;
import lombok.Getter;

import java.text.DecimalFormat;

public class PercentLabelGuiControl extends Label {

    @Getter private double percent;
    private final String prefix;

    private DecimalFormat decimalFormat;

    private PercentLabelGuiControl(String prefix) {
        this(prefix, null);
    }

    private PercentLabelGuiControl(String prefix, String decimalFormatPattern) {
        super();
        this.prefix = prefix == null ? "" : prefix;
        this.percent = 0;
        if (decimalFormatPattern == null) {
            decimalFormatPattern = "#,##0.00%";
        }
        this.decimalFormat = new DecimalFormat(decimalFormatPattern);
        this.setText(prefix + format(0));
    }

    public static PercentLabelGuiControl of(String prefix) {
        return of(prefix, null);
    }

    public static PercentLabelGuiControl of(String prefix, String decimalFormatPattern) {
        return new PercentLabelGuiControl(prefix);
    }

    public void reset() {
        this.percent = 0;
        setText(prefix + format(percent));
    }

    /**
     * @param number before diving by 100 (it is being divided by 100 inside of this method)
     */
    public void set(double number) {
        this.percent = number;
        setText(prefix + format(percent));
    }

    private String format(double d) {
        return decimalFormat.format(d);
    }

}
