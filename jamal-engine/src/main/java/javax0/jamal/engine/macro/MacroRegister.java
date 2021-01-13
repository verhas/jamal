package javax0.jamal.engine.macro;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Delimiters;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Marker;
import javax0.jamal.api.Stackable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

public class MacroRegister implements javax0.jamal.api.MacroRegister {

    /**
     * Stores the data that describes the scopes that are stacked.
     */
    private static class Scope {
        /**
         * Stores the user defined macros that were defined on the level.
         */
        final Map<String, Identified> udMacros = new HashMap<>();
        /**
         * Stores the built-in macros that were defined on the level. Note that built-in macros usually are loaded on
         * the global level, especially when the service loader loads the macro instances. The built-in macro {@code
         * use} however defined the built-in macro scoped level unless the {@code global} keyword is used in it.
         */
        final Map<String, Macro> macros = new HashMap<>();
        /**
         * The delimiters that were saved on this level.
         */
        final List<Delimiters> savedDelimiterPairs = new ArrayList<>();
        /**
         * The last delimiter pair that was defined on this scope level. Null if there was none defined in this scope.
         */
        Delimiters delimiterPair = null;
        /**
         * The marker object that was used to open the scope. When the scope gets locked or closed (pop) then a marker
         * object that equals this has to be provided. This is to ensure that the last scope is closed.
         */
        final Marker checkObject;
        /**
         * When a scope is locked then the definitions will go up higher to the next scope. Even if that scope is
         * locked.
         */
        boolean locked = false;

        private Scope(Marker checkObject) {
            this.checkObject = checkObject;
        }
    }

    private final List<Scope> scopeStack = new ArrayList<>();

    public MacroRegister() {
        push(null);
    }

    private Scope currentScope() {
        return scopeStack.get(scopeStack.size() - 1);
    }

    /**
     * When defining a new macro then this offset is used from the end. It is 1 or 2 because we subtract this value from
     * the length of the stack, and the indexing starts with zero, therefore the last element is length-1, but when that
     * locked then the last writable element is length-2
     *
     * @return 1 or 2
     */
    private int writableOffset() {
        return currentScope().locked ? 2 : 1;
    }

    private <T> Optional<T> stackGet(Function<Scope, Map<String, T>> field, String id) {
        final int end = scopeStack.size() - 1;
        return IntStream.range(0, scopeStack.size())
            .sequential()
            .mapToObj(i -> scopeStack.get(end - i))
            .map(field)
            .filter(map -> map.containsKey(id))
            .map(map -> map.get(id))
            .findFirst();
    }

    @Override
    public <T extends Identified> Optional<T> getUserDefined(String id) {
        return (Optional<T>) stackGet(scope -> scope.udMacros, id);
    }

    @Override
    public Optional<Macro> getMacro(String id) {
        return stackGet(scope -> scope.macros, id);
    }

    @Override
    public void global(Identified macro) {
        scopeStack.get(0).udMacros.put(macro.getId(), macro);
    }

    @Override
    public void global(Macro macro) {
        scopeStack.get(0).macros.put(macro.getId(), macro);
    }

    @Override
    public void global(Macro macro, String alias) {
        scopeStack.get(0).macros.put(alias, macro);
    }

    @Override
    public void define(Identified macro) {
        scopeStack.get(scopeStack.size() - writableOffset()).udMacros.put(macro.getId(), macro);
    }

    @Override
    public void define(Macro macro) {
        define(macro, macro.getId());
    }

    @Override
    public void define(Macro macro, String alias) {
        scopeStack.get(scopeStack.size() - writableOffset()).macros.put(alias, macro);
    }

    @Override
    public void export(String id) throws BadSyntax {
        if (scopeStack.size() > writableOffset()) {
            var macro = scopeStack.get(scopeStack.size() - writableOffset()).udMacros.get(id);
            if (macro == null) {
                throw new BadSyntax("Macro '" + id + "' cannot be exported");
            }
            scopeStack.get(scopeStack.size() - writableOffset() - 1).udMacros.put(id, macro);
            scopeStack.get(scopeStack.size() - writableOffset()).udMacros.remove(id);
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
    public void push(Marker check) {
        final var scope = new Scope(check);
        scopeStack.add(scope);
        scopeStack.forEach(scp -> scp.macros.values().forEach(macro -> stack(macro, Stackable::push)));
        scope.delimiterPair = new javax0.jamal.engine.Delimiters();
    }

    @Override
    public void pop(Marker check) throws BadSyntax {
        if (scopeStack.size() > 1) {
            if (!Objects.equals(check, currentScope().checkObject)) {
                throw new BadSyntax("Pop was performed by " +
                    check +
                    " for a level pushed by " +
                    currentScope().checkObject);
            }
            scopeStack.remove(scopeStack.size() - 1);
            scopeStack.forEach(scope -> scope.macros.values().forEach(macro -> stack(macro, Stackable::pop)));
        } else {
            throw new BadSyntax("Cannot close the top level scope.");
        }
    }

    @Override
    public void lock(Marker check) throws BadSyntax {
        if (scopeStack.size() > 1) {
            if (!Objects.equals(check, currentScope().checkObject)) {
                throw new BadSyntax("Lock was performed by " +
                    check +
                    " for a level pushed by " +
                    currentScope().checkObject);
            }
            currentScope().locked = true;
        } else {
            throw new BadSyntax("Cannot lock the top level scope.");
        }
    }

    @Override
    public String open() {
        for (int level = scopeStack.size() - 1; level > -1; level--) {
            var delimiter = scopeStack.get(level).delimiterPair.open();
            if (delimiter != null) {
                return delimiter;
            }
        }
        return null;
    }

    @Override
    public String close() {
        for (int level = scopeStack.size() - 1; level > -1; level--) {
            var delimiter = scopeStack.get(level).delimiterPair.close();
            if (delimiter != null) {
                return delimiter;
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
        var delimiterPair = currentScope().delimiterPair;
        var savedList = currentScope().savedDelimiterPairs;
        if (openDelimiter == null || closeDelimiter == null) {
            if (savedList.size() == 0) {
                throw new BadSyntax("There was no saved macro start and end string to restore.");
            }
            var savedDelim = savedList.remove(savedList.size() - 1);
            delimiterPair.separators(savedDelim.open(), savedDelim.close());
        } else {
            var savedDelimiterPair = new javax0.jamal.engine.Delimiters();
            savedDelimiterPair.separators(delimiterPair.open(), delimiterPair.close());
            savedList.add(savedDelimiterPair);
            delimiterPair.separators(openDelimiter, closeDelimiter);
        }
    }
}
