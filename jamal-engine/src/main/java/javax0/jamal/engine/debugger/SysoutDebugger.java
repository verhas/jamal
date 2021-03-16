package javax0.jamal.engine.debugger;

public class SysoutDebugger implements Debugger{

    SysoutDebugger(){

    }


    @Override
    public void setInput(int level, CharSequence input) {
        System.out.println("level = " + level + ", input = " + input);
    }

    @Override
    public void setAfter(int level, CharSequence input, CharSequence output) {
        System.out.println("level = " + level + ", input = " + input + ", output = " + output);
    }

    @Override
    public void setStart(CharSequence macro) {
        System.out.println("macro = " + macro);
    }

    @Override
    public void close() {

    }
}
