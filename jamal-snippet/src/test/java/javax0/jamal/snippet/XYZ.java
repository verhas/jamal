package javax0.jamal.snippet;

import java.util.ArrayList;

public class XYZ {


    //<editor-fold template="settersGetters">
    // {%@define fields=int|n,ArrayList<String>|lines,boolean|reverse%}
    private int n;
    private ArrayList<String> lines;
    private boolean reverse;
    public void setN(int n) {
        this.n = n;
    }
    public void setLines(ArrayList<String> lines) {
        this.lines = lines;
    }
    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }
    public int getN() {
        return n;
    }
    public ArrayList<String> getLines() {
        return lines;
    }
    public boolean isReverse() {
        return reverse;
    }
    //</editor-fold>
}
