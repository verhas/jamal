package javax0.jamal.api;

public interface Processor {
    String process(final Input in) throws BadSyntax;
    MacroRegister getRegister();
    UserDefinedMacro newUserDefinedMacro(String id, String input, String[]  params) throws BadSyntax;
}
