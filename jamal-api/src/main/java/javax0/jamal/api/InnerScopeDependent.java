package javax0.jamal.api;

/**
 * This is a marker interface that signals that a class implementing the {@link Macro} interface is a special macro that
 * needs to be evaluated in the scope that was starting inside the macro. It means that the code of the macro will see
 * all user defined macros that are defined inside the macro use.
 * <p>
 * This helps the implementation and the simplifies the use of macros like {@code Snippet} in the extension package.
 * That macro reads a snippet from a file. The argument of the macro is the name of the snippet. There are other
 * parameters that can also be defined. One of them is the file name. It is read from the user defined macro {@code
 * fileName}. If there are many snippets read from the same file, then it is a good practice to define this macro
 * outside of the {@code snippet} macro. If the scope of the {@code fileName} parameter is only for this one snippet
 * then it can be defined inside the {@code snippet} macro, for example:
 *
 * <pre>{@code
 *     {#snippet {@define fileName=SourceFileForASingleSnippet.java} single_snippet}
 * }</pre>
 * This can be done, because then the macro {@code snippet} is evaluated calling {@code
 * javax0.jamal.extensions.Snippet.evaluate()} the scope is still the one opened when processing the content of the
 * macro. This is because the class {@code javax0.jamal.extensions.Snippet.evaluate()} implements this interface.
 * <p>
 * Macros that want to register non-global user defined macros must not implement this interface. If they do so the
 * defined macro will be registered into the scope that exists inside the macro itself. That scope is dropped right
 * after the evaluation of the macro has finished.
 * <p>
 * The non {@code InnerScopeDependent} may freely define global macros as well as defined macros and export them to the
 * parent scope.
 */
public interface InnerScopeDependent {
}
