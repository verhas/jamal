package javax0.jamal.all;

import javax0.jamal.api.Macro;
import javax0.jamal.engine.Processor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.lang.reflect.Method;

public class TestMacroAnnotations {

    /**
     * Tests the macro annotations for consistency in the processor.
     * It checks each macro for the presence of a 'getId' or 'getIds' method and the {@link javax0.jamal.api.Macro.Name} annotation.
     * If a macro contains both a 'getId' or 'getIds' method and the {@link javax0.jamal.api.Macro.Name} annotation,
     * it's considered inconsistent and the macro's class name is collected.
     * If any inconsistencies are found, the method throws an {@link org.opentest4j.AssertionFailedError}
     * with the names of the problematic macro classes.
     *
     * @throws Exception Throws an {@link org.opentest4j.AssertionFailedError} if the processor's debugger stub is empty or
     *                   if any macro(s) have both a 'getId/getIds' method(s) and a {@link javax0.jamal.api.Macro.Name} annotation.
     */
    @Test
    @DisplayName("Ensure no macro defines its name with getId(), getIds() and @Name annotation")
    void testMacroAnnotations() {
        final StringBuilder error;
        final java.util.Optional<javax0.jamal.api.Debugger.Stub> stub;
        try (var processor = new Processor()) {
            error = new StringBuilder();
            stub = processor.getDebuggerStub();
            if (stub.isEmpty()) {
                throw new AssertionFailedError("The processor does not give a debugger stub.");
            }
            for (final var e : stub.get().getScopeList().get(0).getMacros().entrySet()) {
                final var macro = e.getValue();
                final var klass = macro.getClass();
                final var getidMethod = getMethodOrNull(klass, "getId");
                final var getidsMethod = getMethodOrNull(klass, "getIds");
                final var ann = klass.getAnnotation(javax0.jamal.api.Macro.Name.class);
                if ((getidMethod != null || getidsMethod != null) && ann != null) {
                    error.append("Macro ").append(klass.getCanonicalName()).append(" implements getId(s) and is annotated with @Name.").append("\n");
                }
                if (getidMethod != null && getidsMethod != null) {
                    error.append("Macro ").append(klass.getCanonicalName()).append(" implements both getId and getIds").append("\n");
                }
                if (macro.getIds() == null || macro.getIds().length == 0) {
                    error.append("Macro ").append(macro.getClass().getCanonicalName()).append(" has no id(s)\n");
                }
            }
            if (error.length() > 0) {
                throw new AssertionFailedError(error.toString());
            }
        }
    }

    /**
     * Get the declared method for the given name and no parameters; or return null if there is no such method declared.
     *
     * @param macro the class that we check for the method
     * @param name  the name of the method
     * @return the Method object or null if there is no such method declared
     */
    private static Method getMethodOrNull(Class<? extends Macro> macro, String name) {
        try {
            return macro.getDeclaredMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
