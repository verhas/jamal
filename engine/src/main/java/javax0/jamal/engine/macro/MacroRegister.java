package javax0.jamal.engine.macro;

import javax0.jamal.api.Macro;
import javax0.jamal.api.UserDefinedMacro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MacroRegister implements javax0.jamal.api.MacroRegister {
    private final ArrayList<Map<String, UserDefinedMacro>> udMacroStack = new ArrayList<>();
    private final ArrayList<Map<String, Macro>> macroStack = new ArrayList<>();


    public MacroRegister(){
        push();
    }
    /**
     * Get a macro based on the id of the macro.
     * <p>
     * The
     *
     * @param id the identifier (name) of the macro
     * @return the user defined macro in an optional. Optional.empty() if the macro can not be found.
     */
    public Optional<UserDefinedMacro> getUserMacro(String id) {
        for (int level = udMacroStack.size() - 1; level > -1; level--) {
            var map = udMacroStack.get(level);
            if (map.containsKey(id)) {
                return Optional.of(map.get(id));
            }
        }
        return Optional.empty();
    }

    public Optional<Macro> geMacro(String id) {
        for (int level = macroStack.size() - 1; level > -1; level--) {
            var map = macroStack.get(level);
            if (map.containsKey(id)) {
                return Optional.of(map.get(id));
            }
        }
        return Optional.empty();
    }

    @Override
    public void put(UserDefinedMacro macro) {
        udMacroStack.get(udMacroStack.size() - 1).put(macro.getId(), macro);
    }

    @Override
    public void put(Macro macro) {
        macroStack.get(macroStack.size() - 1).put(macro.getId(), macro);
    }

    @Override
    public void push() {
        udMacroStack.add(new HashMap<>());
        macroStack.add(new HashMap<>());
    }

    @Override
    public void pop() {
        udMacroStack.remove(udMacroStack.size() - 1);
        macroStack.remove(macroStack.size() - 1);
    }


}
