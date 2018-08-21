package javax0.jamal.api;

import java.util.Optional;

public interface MacroRegister extends Delimiters{
    Optional<Macro> geMacro(String id);
    Optional<UserDefinedMacro> getUserMacro(String id);
    void global(UserDefinedMacro macro);
    void global(Macro macro);
    void define(UserDefinedMacro macro);
    void define(Macro macro);
    void export(String id) throws BadSyntax;
    void push();
    void pop();
}
