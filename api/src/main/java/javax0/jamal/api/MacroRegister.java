package javax0.jamal.api;

import java.util.Optional;

public interface MacroRegister {
    Optional<Macro> geMacro(String id);
    Optional<UserDefinedMacro> getUserMacro(String id);
    void put(UserDefinedMacro macro);

    void put(Macro macro);

    void push();

    void pop();
}
