package hr.nipeta.cac.ca;

public enum CellularAutomataState {
    TTT,
    TTF,
    TFT,
    TFF,
    FTT,
    FTF,
    FFT,
    FFF;

    public static CellularAutomataState of(boolean left, boolean central, boolean right) {

        final String leftVal = left ? "T" : "F";
        final String centralVal = central ? "T" : "F";
        final String rightVal = right ? "T" : "F";

        return valueOf(leftVal + centralVal + rightVal);

    }

}