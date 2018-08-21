package javax0.jamal.engine.macro;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Delimiters;
import javax0.jamal.api.Macro;
import javax0.jamal.api.UserDefinedMacro;

import java.util.*;

public class MacroRegister implements javax0.jamal.api.MacroRegister {
    private final List<Map<String, UserDefinedMacro>> udMacroStack = new ArrayList<>();
    private final List<Map<String, Macro>> macroStack = new ArrayList<>();
    private final List<Delimiters> delimiters = new ArrayList<>();

    public MacroRegister() {
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
    public void global(UserDefinedMacro macro) {
        udMacroStack.get(0).put(macro.getId(), macro);
    }

    @Override
    public void global(Macro macro) {
        macroStack.get(0).put(macro.getId(), macro);
    }

    @Override
    public void define(UserDefinedMacro macro) {
        udMacroStack.get(udMacroStack.size() - 1).put(macro.getId(), macro);
    }

    @Override
    public void define(Macro macro) {
        macroStack.get(macroStack.size() - 1).put(macro.getId(), macro);
    }

    @Override
    public void export(String id) throws BadSyntax {
        if (udMacroStack.size() > 1) {
            var macro = udMacroStack.get(udMacroStack.size() - 1).get(id);
            if (macro == null) {
                throw new BadSyntax("Macro '" + id + "' cannot be exported");
            }
            udMacroStack.get(udMacroStack.size() - 2).put(id,
                macro);
        }
    }

    @Override
    public void push() {
        udMacroStack.add(new HashMap<>());
        macroStack.add(new HashMap<>());
        delimiters.add(new javax0.jamal.engine.Delimiters());
    }

    @Override
    public void pop() {
        udMacroStack.remove(udMacroStack.size() - 1);
        macroStack.remove(macroStack.size() - 1);
        delimiters.remove(delimiters.size() - 1);
    }


    @Override
    public String open() {
        for (int level = delimiters.size() - 1; level > -1; level--) {
            var delim = delimiters.get(level);
            if (delim.open() != null ) {
                return delim.open();
            }
        }
        return null;
    }

    @Override
    public String close() {
        for (int level = delimiters.size() - 1; level > -1; level--) {
            var delim = delimiters.get(level);
            if (delim.close() != null ) {
                return delim.close();
            }
        }
        return null;
    }

    @Override
    public void separators(String openDelimiter, String closeDelimiter) {
        var delim = delimiters.get(delimiters.size() - 1);
        delim.separators(openDelimiter,closeDelimiter);
    }
}
