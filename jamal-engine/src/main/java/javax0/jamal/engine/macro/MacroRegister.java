package javax0.jamal.engine.macro;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Delimiters;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Marker;
import javax0.jamal.api.Stackable;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class MacroRegister implements javax0.jamal.api.MacroRegister {
    private final List<Map<String, Identified>> udMacroStack = new ArrayList<>();
    private final List<Map<String, Macro>> macroStack = new ArrayList<>();
    private final List<Delimiters> delimiters = new ArrayList<>();
    private final List<List<Delimiters>> savedDelimiters = new ArrayList<>();
    private final Deque<Object> stackCheckObjects = new LinkedList<>();
    private boolean registerLocked = false;

    public MacroRegister() {
        try {
            push(null);
        } catch (BadSyntax badSyntax) {
            throw new RuntimeException("It must not happen. We just initialized registerLocked to false.");
        }
    }

    /**
     * When defining a new macro then this offset is used from the end. It is 1 or 2 because we substract this value
     * from the length of the stack, and the indexing starts with zero, therefore the last element is length-1, but when
     * that locked then the last writable element is length-2
     *
     * @return
     */
    private int writableOffset() {
        return registerLocked ? 2 : 1;
    }

    public <T extends Identified> Optional<T> getUserDefined(String id) {
        for (int level = udMacroStack.size() - 1; level > -1; level--) {
            var map = udMacroStack.get(level);
            if (map.containsKey(id)) {
                return Optional.of((T) map.get(id));
            }
        }
        return Optional.empty();
    }

    public Optional<Macro> getMacro(String id) {
        for (int level = macroStack.size() - 1; level > -1; level--) {
            var map = macroStack.get(level);
            if (map.containsKey(id)) {
                return Optional.of(map.get(id));
            }
        }
        return Optional.empty();
    }

    @Override
    public void global(Identified macro) {
        udMacroStack.get(0).put(macro.getId(), macro);
    }

    @Override
    public void global(Macro macro) {
        macroStack.get(0).put(macro.getId(), macro);
    }

    @Override
    public void global(Macro macro, String alias) {
        macroStack.get(0).put(alias, macro);
    }

    @Override
    public void define(Identified macro) {
        udMacroStack.get(udMacroStack.size() - writableOffset()).put(macro.getId(), macro);
    }

    @Override
    public void define(Macro macro) {
        define(macro, macro.getId());
    }

    @Override
    public void define(Macro macro, String alias) {
        macroStack.get(macroStack.size() - writableOffset()).put(alias, macro);
    }

    @Override
    public void export(String id) throws BadSyntax {
        if (udMacroStack.size() > writableOffset()) {
            var macro = udMacroStack.get(udMacroStack.size() - writableOffset()).get(id);
            if (macro == null) {
                throw new BadSyntax("Macro '" + id + "' cannot be exported");
            }
            udMacroStack.get(udMacroStack.size() - writableOffset() - 1).put(id, macro);
            udMacroStack.get(udMacroStack.size() - writableOffset()).remove(id);
        } else {
            throw new BadSyntax("Macro '" + id + "' cannot be exported from the top level");
        }
    }

    private void stack(Macro macro, Consumer<Stackable> c) {
        if (macro instanceof Stackable) {
            c.accept((Stackable) macro);
        }
    }

    @Override
    public void push(Marker check) throws BadSyntax {
        if (registerLocked) {
            throw new BadSyntax("The register is locked when trying to open a new layer.");
        }
        registerLocked = false;
        stackCheckObjects.addLast(check);
        macroStack.forEach(macros -> macros.values().forEach(macro -> stack(macro, Stackable::push)));
        macroStack.add(new HashMap<>());
        udMacroStack.add(new HashMap<>());
        delimiters.add(new javax0.jamal.engine.Delimiters());
        savedDelimiters.add(new ArrayList<>());
    }

    @Override
    public void pop(Marker check) throws BadSyntax {
        if (!Objects.equals(check, stackCheckObjects.getLast())) {
            throw new BadSyntax("Pop was performed by " +
                check +
                " for a level pushed by " +
                stackCheckObjects.getLast());
        }
        if (udMacroStack.size() > 1) {
            stackCheckObjects.removeLast();
            macroStack.remove(macroStack.size() - 1);
            macroStack.forEach(macros -> macros.values().forEach(macro -> stack(macro, Stackable::pop)));
            udMacroStack.remove(udMacroStack.size() - 1);
            delimiters.remove(delimiters.size() - 1);
            savedDelimiters.remove(savedDelimiters.size() - 1);
        } else {
            throw new BadSyntax("Cannot close the top level scope.");
        }
    }

    @Override
    public void lock(Marker check) throws BadSyntax {
        if (!Objects.equals(check, stackCheckObjects.getLast())) {
            throw new BadSyntax("Lock was performed by " +
                check +
                " for a level pushed by " +
                stackCheckObjects.getLast());
        }

        if (udMacroStack.size() > 1) {
            registerLocked = true;
        } else {
            throw new BadSyntax("Cannot lock the top level scope.");
        }
    }

    @Override
    public String open() {
        for (int level = delimiters.size() - 1; level > -1; level--) {
            var delim = delimiters.get(level);
            if (delim.open() != null) {
                return delim.open();
            }
        }
        return null;
    }

    @Override
    public String close() {
        for (int level = delimiters.size() - 1; level > -1; level--) {
            var delim = delimiters.get(level);
            if (delim.close() != null) {
                return delim.close();
            }
        }
        return null;
    }

    /**
     * Sets the opening and closing delimiter strings. If {@code openDelimiter} or {@code closeDelimiter} is {@code
     * null} then it resets the delimiter to the last value that was saved in the stack. If {@code openDelimiter} is
     * {@code null} then {@code closeDelimiter} is ignored, and also the other way around, but it is good practice to
     * pass {@code null} in both arguments when resetting to the last saved delimiter pair.
     *
     * @param openDelimiter  the macro opening string to be set. If this parameter is {@code null} then the method
     *                       treats this information as a restore process. This class saves the old values of the
     *                       separators in a stack and when {@code openDelimiter} is {@code null} it restores the
     *                       delimiters from the top of the stack.
     * @param closeDelimiter the macro closing string to be set. Ignored when {@code openDelimiter} is {@code null}. If
     *                       this parameter is {@code null} the functionality will be the same as in case {@code
     *                       openDelimiter} is {@code null}.
     * @throws BadSyntaxAt when the call tries to restore an older version but there is no saved older version.
     */
    @Override
    public void separators(String openDelimiter, String closeDelimiter) throws BadSyntax {
        if (openDelimiter == null || closeDelimiter == null) {
            var delim = delimiters.get(delimiters.size() - 1);
            var list = savedDelimiters.get(savedDelimiters.size() - 1);
            if (list.size() == 0) {
                throw new BadSyntax("There was no saved macro start and end string to restore.");
            }
            var savedDelim = list.remove(list.size() - 1);
            delim.separators(savedDelim.open(), savedDelim.close());
        } else {
            var delim = delimiters.get(delimiters.size() - 1);
            var list = savedDelimiters.get(savedDelimiters.size() - 1);
            var savedDelim = new javax0.jamal.engine.Delimiters();
            savedDelim.separators(delim.open(), delim.close());
            list.add(savedDelim);
            delim.separators(openDelimiter, closeDelimiter);
        }
    }
}
