package javax0.jamal.engine.macro;

import javax0.jamal.api.*;
import javax0.jamal.tools.InputHandler;
import javax0.levenshtein.Levenshtein;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

public class MacroRegister implements javax0.jamal.api.MacroRegister, Debuggable.MacroRegister {

    private static final int TOP_LEVEL = 0;

    @Override
    public Optional<Debuggable.MacroRegister> debuggable() {
        return Optional.of(this);
    }

    /**
     * Stores the data that describes the scopes that are stacked.
     */
    public static class Scope implements Debuggable.Scope {
        /**
         * Stores the user defined macros that were defined on the level.
         */
        final Map<String, Identified> udMacros = new HashMap<>();

        public Map<String, Identified> getUdMacros() {
            return udMacros;
        }

        /**
         * Stores the built-in macros that were defined on the level. Note that built-in macros usually are loaded on
         * the global level, especially when the service loader loads the macro instances. The built-in macro {@code
         * use} however defined the built-in macro scoped level unless the {@code global} keyword is used in it.
         */
        final Map<String, Macro> macros = new HashMap<>();

        public Map<String, Macro> getMacros() {
            return macros;
        }

        /**
         * The delimiters that were saved on this level.
         */
        final List<Delimiters> savedDelimiterPairs = new ArrayList<>();
        /**
         * The last delimiter pair that was defined on this scope level. Null if there was none defined in this scope.
         */
        Delimiters delimiterPair = null;

        public Delimiters getDelimiterPair() {
            return delimiterPair;
        }

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

    @Override
    public List<Marker> getPoppedMarkers() {
        return poppedMarkers;
    }

    private final List<Marker> poppedMarkers = new ArrayList<>();

    @Override
    public List<Debuggable.Scope> getScopes() {
        //noinspection unchecked,rawtypes
        return (List<Debuggable.Scope>) (List) scopeStack;
    }

    /**
     * This variable is set in the constructor based on the
     * environment variable {@link EnvironmentVariables#JAMAL_CHECKSTATE_ENV} or the corresponding system property.
     * <p>
     * When this variable is {@code true} then registering a macro checks that the macro has no state holding fields,
     * (fields that are neither {@code final}, nor {@code static}) and refuses to load the macro if it has state.
     * <p>
     * It is generally recommended that the macros are stateless to support multi-thread evaluation when a single JVM
     * runs multiple Jamal processors in one or more threads.
     * <p>
     * If a macro has to have a state then it has to be annotated using the annotation {@link Macro.Stateful}
     * <p>
     * To programmatically switch of this checking set the system property like
     * <pre>{@code
     *          System.setProperty(Macro.JAMAL_CHECKSTATE_SYS, "false");
     * }</pre>
     * This may be needed when you want to load a macro from a library that does not conform to the stateless
     * requirement and does not use the {@link Macro.Stateful} annotation. All macros prior to Jamal 1.8.0 are like
     * that.
     */
    private final boolean checkState;

    private final Processor processor;

    /**
     * At the creation of the register we start with a new macro evaluation level, which is the top level.
     * <p>
     * The constructor also reads the environment variable {@link EnvironmentVariables#JAMAL_CHECKSTATE_ENV} when no
     * system property is given, and based on that sets the global {@link #checkState} field.
     */
    public MacroRegister(Processor processor) {
        this.processor = processor;
        try {
            push(null);
        } catch (BadSyntax badSyntax) {
            throw new RuntimeException("SNAFU: should not happen");
        }
        final var s = EnvironmentVariables.getenv(EnvironmentVariables.JAMAL_CHECKSTATE_ENV).orElse("");
        checkState = !s.equals("false");
    }

    private Scope currentScope() {
        return scopeStack.get(scopeStack.size() - 1);
    }

    /**
     * When defining a new macro then this offset is used from the end. This offset starts with 1 because we subtract
     * this value from the length of the stack, and the indexing starts with zero, therefore the last element is
     * size()-1, but when that is locked then the last writable element is size()-2, except when that is also locked
     * and so on. The top level scope cannot be locked, because if any code tries to lock the top level scope then an
     * exception is thrown.
     *
     * @return the offset you have to subtract from the scopeStack size.
     */
    private int writableOffset() {
        int i = 1;
        while (scopeStack.get(scopeStack.size() - i).locked) {
            i++;
            if (i > scopeStack.size()) {
                throw new RuntimeException("Internal Error: scopeStack is fully locked.");
            }
        }
        return i;
    }

    /**
     * Return the last scope or the scope above that in case the last scope is locked.
     * <p>
     * When the active scope is locked, then the methods, like 'define', 'export' and so on, work with the scope that is above
     * the last one. This is the case even if that scope is also locked. It may happen that some macro evaluation opens
     * a new scope even after the current scope was locked.
     * <p>
     * If that happens, the writing (user defined macros or something else) in the already locked one level higher scope
     * just works until the scope gets closed. Locking a scope essentially means that we want to write new macros into
     * one level higher and not into this scope while the locked scope is the actual scope.
     * <p>
     * This is used when user defined macros are evaluated. The content of the macro is evaluated in a new scope. It
     * means that any macro defined inside the USE of a user defined macro is local in the use of the user defined
     * macro.
     * <p>
     * These macros are available when the macro itself is evaluated, but if a macro is defined inside the user defined
     * macro (not in the use) then this macro should already be defined one level higher. That way the lowers scope is
     * locked. The macros are still there and usable, but any new macro is already defined after that. Finally when the
     * result of the user defined macro is evaluated this is already fully in the outer scope, macros defined in the USE
     * of the user defined macro are not reachable any more. A good example from the test file {@code
     * TestUDMacroEvaluationOrder}:
     *
     * <pre>{@code
     * {@define firstName=Julia}
     * {@define k(h)=h, {firstName} h{@define son=Junior Bond}}
     * {k /Bond{@define firstName=James}}
     * {k /Bond}
     * {son}
     * }</pre>
     * <p>
     * will result
     *
     * <pre>{@code
     * Bond, James Bond
     * Bond, Julia Bond
     * Junior Bond
     * }</pre>
     * <p>
     * Here {@code {@define firstName=James}} is evaluated in the scope, which was opened when the evaluation of the
     * content of the first use of macro {@code k} started. The value of {@code firstName} is local inside the macro USE
     * evaluation, but this is not demonstrated in this case as it is kind of trivial.
     * <p>
     * When the macro evaluated and the content {@code h, {firstName} h{@define son=Junior Bond}} then the in-macro-use
     * redefinition if {@code firstName} is in effect. The scope where the {@code firstName} is taken from is locked,
     * but this means it still overrides the definition of the same macro in the scope above. The evaluation of {@code
     * {@define son=Junior Bond}} defines the macro {@code son} in the higher layer and not the locked one.
     * <p>
     * The second use of the macro {@code h} does not contain any redefinition of the macro {@code firstName}. In this
     * case, the value defined at the start of the example is used. When the macro is evaluated the evaluation of {@code
     * {@define son=Junior Bond}} overwrites the same macro with the same value.
     * <p>
     * Finally, the macro {@code son} is used when the top level is the current one, and it is defined (actually twice),
     * but that is not a problem, you can redefine any macro any times.
     *
     * @return the writable scope
     */
    private Scope writableScope() {
        return scopeStack.get(scopeStack.size() - writableOffset());
    }

    /**
     * Get the macro or user defined macro from the stack.
     *
     * @param field is either 'macros' or 'udMacros'
     * @param id    the identifier of the macro
     * @param <T>   the type of the macro class
     * @return the found macro class or empty
     */
    private <T> Optional<T> stackGet(Function<Scope, Map<String, T>> field, String id) {
        final int end = scopeStack.size() - 1;
        return IntStream.range(TOP_LEVEL, scopeStack.size()).sequential().mapToObj(i -> scopeStack.get(end - i)).map(field).filter(map -> map.containsKey(id)).map(map -> map.get(id)).findFirst();
    }

    /**
     * Count the object if it is present and a counted object (a.k.a. implements the {@link Counted} interface.
     *
     * @param r the object to count, optional
     */
    private <K> Optional<K> count(Optional<K> r) {
        if (r.isPresent() && r.get() instanceof Counted) {
            final Counted counted = (Counted) r.get();
            counted.count();
        }
        return r;
    }

    /**
     * Get the user defined macro. In case the macro is a global macro (contains a `:` in the name) then look for it in
     * the top level scope. Without this the macros cannot be used in the {@code :a} form, which is rarely a problem,
     * but with the introduction of the macro {@code undefine} when a macro is undefined on one level then it is not
     * possible anymore to refer to the global macro from a scope that is below.
     *
     * @param id  the identifier (name) of the macro
     * @param <T> the subtype of the identified macro stored
     * @return the optional found macro
     */
    @Override
    public <T extends Identified> Optional<T> getUserDefined(String id) {
        Objects.requireNonNull(id);
        final Optional<T> result;
        if (InputHandler.isGlobalMacro(id)) {
            //noinspection unchecked
            result = Optional.ofNullable((T) scopeStack.get(TOP_LEVEL).udMacros.get(InputHandler.convertGlobal(id)));
        } else {
            //noinspection unchecked
            result = (Optional<T>) stackGet(javax0.jamal.engine.macro.MacroRegister.Scope::getUdMacros, id);
        }
        return count(result);
    }

    @Override
    public Optional<Macro> getMacro(String id) {
        final Optional<Macro> result;
        if (InputHandler.isGlobalMacro(id)) {
            result = Optional.ofNullable(scopeStack.get(TOP_LEVEL).macros.get(InputHandler.convertGlobal(id)));
        } else {
            result = stackGet(javax0.jamal.engine.macro.MacroRegister.Scope::getMacros, id);
        }
        return count(result);
    }

    @Override
    public Optional<Macro> getMacroLocal(String alias) {
        return count(Optional.ofNullable(writableScope().macros.get(alias)));
    }

    @Override
    public Optional<Identified> getUdMacroLocal(String alias) {
        return count(Optional.ofNullable(writableScope().udMacros.get(alias)));
    }

    @Override
    public void global(final Identified macro) {
        global(macro, macro.getId());
    }

    @Override
    public void global(final Identified macro, final String alias) {
        scopeStack.get(TOP_LEVEL).udMacros.put(alias, macro);
    }

    @Override
    public void global(Macro macro) {
        global(macro, macro.getId());
    }

    @Override
    public void global(Macro macro, String alias) {
        assertMacroClassIsStateless(macro.getClass());
        scopeStack.get(TOP_LEVEL).macros.put(alias, macro);
    }

    @Override
    public Set<String> suggest(String spelling) {
        final Set<String> suggestions = new HashSet<>();
        int minDistance = 3;
        for (Scope scope : scopeStack) {
            for (final var macro : scope.macros.keySet()) {
                final int distance = Levenshtein.distance(macro, spelling);
                if (distance < minDistance) {
                    minDistance = distance;
                    suggestions.clear();
                }
                if (distance <= minDistance) {
                    suggestions.add("@" + macro);
                }
            }
            for (final var macro : scope.udMacros.entrySet()) {
                if (!(macro.getValue() instanceof Identified.Undefined)) {
                    final int distance = Levenshtein.distance(macro.getKey(), spelling);
                    if (distance < minDistance) {
                        minDistance = distance;
                        suggestions.clear();
                    }
                    if (distance <= minDistance) {
                        suggestions.add(macro.getKey());
                    }
                }
            }
        }
        return suggestions;
    }

    @Override
    public void define(Identified macro) {
        define(macro, macro.getId());
    }

    @Override
    public void define(final Identified macro, final String alias) {
        writableScope().udMacros.put(alias, macro);
    }

    @Override
    public void define(Macro macro) {
        for (final var alias : macro.getIds()) {
            define(macro, alias);
        }
    }

    @Override
    public void define(Macro macro, String alias) {
        assertMacroClassIsStateless(macro.getClass());
        writableScope().macros.put(alias, macro);
    }

    /**
     * Store all the macro classes that were already checked for tastefulness, so that further checks can
     * ignore them.
     * <p>
     * The map contains a class as a key if it was checked and the value is {@code true} if the class is
     * stateful but not annotated.
     */
    private static final Map<Class<? extends Macro>, Boolean> notAnnotatedStateful = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * This method will check that a macro class is either stateless (does not have any field) or is declared to be
     * stateful, which is not recommended, but sometimes may be a reasonable approach. A class is declared to be
     * stateful if it implements the {@link javax0.jamal.api.Macro.Stateful Stateful} interface.
     * <p>
     * If the macro is not declared to be stateful, but has declared fields, which are neither final, nor static, then
     * the method throws a run-time exception. This check is performed for the class and in the chain of the
     * superclasses for each parent class up to the {@link Object} class. The {@link Object} class does have fields, and
     * they are ignored. If we checked the Object class, every macro would be stateful.
     *
     * @param klass the macro class we check
     * @throws RuntimeException if the macro class is not stateless and is not declared to be stateful
     */
    private void assertMacroClassIsStateless(Class<? extends Macro> klass) {
        if (!checkState) {
            return;
        }
        final Boolean statefulAndWrong = notAnnotatedStateful.get(klass);
        if (statefulAndWrong != null) {
            if (statefulAndWrong) {
                throwUp(klass, klass, null);
            }
            return;
        }
        for (Class<?> k = klass; k != Object.class; k = k.getSuperclass()) {
            if (k.getDeclaredAnnotation(Macro.Stateful.class) == null) {
                for (Field field : k.getDeclaredFields()) {
                    if ((field.getModifiers() & Modifier.FINAL) == 0 && (field.getModifiers() & Modifier.STATIC) == 0) {
                        notAnnotatedStateful.put(klass, true);
                        throwUp(klass, k, field);
                    }
                }
            }
        }
        notAnnotatedStateful.put(klass, false);
    }

    /**
     * Throws a run-time exception that the macro class is not stateless.
     *
     * @param klass the macro class
     * @param k     the parent class that has the non-final, non-static field
     * @param field the field that is non-final, non-static and violates the stateless requirement
     * @throws RuntimeException always
     */
    private void throwUp(final Class<? extends Macro> klass, final Class<?> k, final Field field) {
        throw new RuntimeException(String.format("The macro class '%1$s' is not stateless, %2$s has non-final, non-static field%3$s.",
                klass.getName(),
                (k == klass ? "it " : "parent class " + k.getName()),
                (field == null ? "" : (" '" + field.getName() + "'"))));
    }

    /**
     * Find the scope where to export. The search starts one level above the writable scope and goes until it finds a
     * non-locked scope. If all the scopes are locked, then the top level scope is returned.
     *
     * @return the scope where to export the macro
     */
    private Scope findExportScope() {
        final int offset = writableOffset();
        for (int i = 1; scopeStack.size() - offset - i > 0; i++) {
            final var scope = scopeStack.get(scopeStack.size() - offset - i);
            if (!scope.locked) {
                return scope;
            }
        }
        return scopeStack.get(TOP_LEVEL);
    }

    @Override
    public void export() throws BadSyntax {
        final var ids = new HashSet<String>();
        ids.addAll(writableScope().udMacros.keySet());
        ids.addAll(writableScope().macros.keySet());
        for (final var id : ids) {
            export(id);
        }
    }

    @Override
    public void export(String id) throws BadSyntax {
        final int offset = writableOffset();
        boolean exported = false;
        if (scopeStack.size() > offset) {
            final var exportToScope = findExportScope();
            var udMacro = writableScope().udMacros.get(id);
            if (udMacro != null) {
                final var udMacros = exportToScope.udMacros;
                udMacros.put(id, udMacro);
                writableScope().udMacros.remove(id);
                exported = true;
            }
            final var macro = writableScope().macros.get(id);
            if (macro != null) {
                final var macros = exportToScope.macros;
                macros.put(id, macro);
                writableScope().macros.remove(id);
                exported = true;
            }
            BadSyntax.when(!exported, "Macro '%s' cannot be exported, not in the scope of export.", id);
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
        BadSyntax.when(markerIsInTheStack(check), "Push was performed using the marker %s which happens to be already in the stack.", check);
        final var scope = new Scope(check);
        scopeStack.add(scope);
        scopeStack.forEach(scp -> scp.macros.values().forEach(macro -> stack(macro, Stackable::push)));
        scope.delimiterPair = new javax0.jamal.engine.Delimiters();
        poppedMarkers.clear();
    }

    @Override
    public Marker test() {
        return currentScope().checkObject;
    }

    @Override
    public void test(Marker check) throws BadSyntax {
        if (scopeStack.size() > 1) {
            final var current = currentScope();
            if (!Objects.equals(check, current.checkObject)) {
                if (markerIsInTheStack(check)) {
                    tryCleanUpStack(check);
                }
                throw new BadSyntaxAt("Scope was changed from " + check + " to " + current.checkObject + " and it was not closed before the end.", current.checkObject.getPosition());
            }
        } else {
            BadSyntax.when(check != null, "Scope opened with %s was closed immature.", check);
        }
    }

    /**
     * This implementation checks the marker and in case it is not the last one it tries to clean the stack before
     * throwing the exception. If the marker is included in the stack, but not in the last level, then the method
     * will iteratively close all levels below and at the level of the marker, and only then it will throw the
     * exception.
     *
     * @param check the marker to check
     * @throws BadSyntax if the last marked when calling push was not the one passed to this method or if we are on the
     *                   global level.
     */
    @Override
    public void pop(Marker check) throws BadSyntax {
        if (scopeStack.size() > 1) {
            final var current = currentScope();
            if (!Objects.equals(check, current.checkObject)) {
                if (markerIsInTheStack(check)) {
                    tryCleanUpStack(check);
                    popStackOneLevel();
                }
                throw new BadSyntax("Pop was performed by " + check + " for a level pushed by " + current.checkObject);
            }
            popStackOneLevel();
        } else {
            throw new BadSyntax("Cannot close the top level scope.");
        }
    }

    private boolean markerIsInTheStack(Marker check) {
        for (final var scopeWalker : scopeStack) {
            if (Objects.equals(check, scopeWalker.checkObject)) {
                return true;
            }
        }
        return false;
    }

    private void tryCleanUpStack(Marker check) throws BadSyntax {
        while (!scopeStack.isEmpty() && !Objects.equals(check, currentScope().checkObject)) {
            popStackOneLevel();
        }
    }

    /**
     * Clean one level of the stack. It means that the user defined macro stack last element is removed as well as the
     * macro stack last element is removed. If any of the removed macros or user defined macros are AutoCloseable then
     * the close method is invoked.
     * <p>
     * Note that the processor or the input will NOT be injected even if the macro implements one of the
     * {@code Closer.*Aware} interfaces. That is because macros are generally stateless and as such, injecting to a
     * macro object (either built-in or user defined) is not possible.
     * <p>
     * The marker of the removed scope is added to the {@code poppedMarkers} list. This is used to create better error
     * messages later on.
     * <p>
     * Finally, all remaining (not removed) built-in macros that implement the {@link Stackable} interface are invoked
     * calling the method {@link Stackable#pop()}.
     *
     * @throws BadSyntax when there is an error
     */
    private void popStackOneLevel() throws BadSyntax {
        if (!scopeStack.isEmpty()) {
            final var removedScope = scopeStack.remove(scopeStack.size() - 1);
            for (final var macro : removedScope.getMacros().values()) {
                if (macroCanBeClosedWithoutOutput(macro)) {
                    try {
                        if (macro instanceof Closer.ProcessorAware) {
                            ((Closer.ProcessorAware) macro).set(processor);
                        }
                        ((AutoCloseable) macro).close();
                    } catch (Exception e) {
                        throw new BadSyntax("Closing AutoCloseable macro '" + macro.getId() + "' caused exception.", e);
                    }
                }
                if (macro instanceof Counted) {
                    if (((Counted) macro).counted() == 0) {
                        processor.logger().log(System.Logger.Level.WARNING, null, "Built-in macro %s was declared but not used.", macro.getId());
                    }
                }
            }
            for (final var macro : removedScope.getUdMacros().values()) {
                if (macroCanBeClosedWithoutOutput(macro)) {
                    try {
                        if (macro instanceof Closer.ProcessorAware) {
                            ((Closer.ProcessorAware) macro).set(processor);
                        }
                        ((AutoCloseable) macro).close();
                    } catch (Exception e) {
                        throw new BadSyntax("Closing AutoCloseable user defined macro '" + macro.getId() + "' caused exception.", e);
                    }
                }
                if (macro instanceof Counted) {
                    if (((Counted) macro).counted() == 0) {
                        processor.logger().log(System.Logger.Level.WARNING, null, "User defined macro %s was declared but not used.", macro.getId());
                    }
                }
            }
            poppedMarkers.add(removedScope.checkObject);
            scopeStack.forEach(scope -> scope.macros.values().forEach(macro -> stack(macro, Stackable::pop)));
        }
    }

    private static boolean macroCanBeClosedWithoutOutput(Object macro) {
        return macro instanceof AutoCloseable && !(macro instanceof Closer.OutputAware);
    }

    @Override
    public void lock(Marker check) throws BadSyntax {
        if (scopeStack.size() > 1) {
            BadSyntax.when(!Objects.equals(check, currentScope().checkObject), "Lock was performed by %s for a level pushed by %s", check, currentScope().checkObject);
            currentScope().locked = true;
        } else {
            throw new BadSyntax("Cannot lock the top level scope.");
        }
    }

    @Override
    public void lock() throws BadSyntax {
        if (scopeStack.size() > 1) {
            currentScope().locked = true;
        } else {
            throw new BadSyntax("Cannot lock the top level scope.");
        }
    }

    /**
     * Walk up the stack and return the first open or close string.
     *
     * @param openOrClose either Delimiters::open or Delimiters::close
     * @return the defined string from the stack or null
     */
    private String stackGet(Function<Delimiters, String> openOrClose) {
        final int end = scopeStack.size() - 1;
        return IntStream.range(TOP_LEVEL, scopeStack.size()).sequential().mapToObj(i -> scopeStack.get(end - i)).map(scope -> scope.delimiterPair).map(openOrClose).filter(Objects::nonNull).findFirst().orElse(null);
    }

    @Override
    public String open() {
        return stackGet(Delimiters::open);
    }

    @Override
    public String close() {
        return stackGet(Delimiters::close);
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
            BadSyntax.when(savedList.isEmpty(), "There was no saved macro start and end string to restore.");
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
