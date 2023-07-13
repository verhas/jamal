package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.tools.ScriptingTools;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import static jdk.jshell.Snippet.Status.RECOVERABLE_NOT_DEFINED;
import static jdk.jshell.Snippet.Status.REJECTED;

/**
 * A JShell engine that helps the invocation of the JShell from the JDK.
 * <p>
 * The engine has two methods. One is {@link #define(String)}. The other one is {@link #evaluate(String)}.
 * <p>
 * The JShell interpreter is created only when the first call to {@link #evaluate(String)} happens. This is to avoid
 * unnecessary creation of the JShell engine, which is a costly operation. A Jamal file may include a set of definitions
 * containing a lot of {@code JShell} and {@code script} macros. When the macro {@code JShell} is executed it calls
 * {@link #define(String)}. It still may happen that the processed file does not use these macros. In that case creating
 * a JShell engine (it runs in a separate process) would be a waste of resource and would significantly slow Jamal
 * down.
 * <p>
 * For this reason the execution of the defines are postponed until the first invocation of {@link #evaluate(String)}.
 */
public class JShellEngine implements javax0.jamal.api.JShellEngine {


    private JShell js;
    private ByteArrayOutputStream output;
    private final List<String> deferredDefines;
    private final AtomicBoolean isOpen;

    public JShellEngine(){
        js = null;
        output = null;
        deferredDefines = new ArrayList<>();
        isOpen = new AtomicBoolean(false);
    }

    /**
     * Initialize the JShell this engine used. It creates the JShell object, the byte array output stream that captures
     * the output of the executions and calls define for all deferred defines.
     * <p>
     * Note that when {@link #define(String)} is called from this method the JShell interpreter is already initialized
     * and thus the call will execute the definitions and does not defer it again.
     *
     * @throws BadSyntax if the deferred definitions are erroneous
     */
    private void init() throws BadSyntax {
        output = new ByteArrayOutputStream();
        js = JShell.builder().out(new PrintStream(output)).build();
        isOpen.set(true);
        js.onShutdown(jShell -> isOpen.set(false));
        for (String deferredDefine : deferredDefines) {
            define(deferredDefine);
        }
    }

    /**
     * Evaluate the input string using the JShell interpreter.
     * <p>
     * The method first checks if the JShell interpreter is already initialized. If it is not then it calls {@link
     * #init()}. This will create the JShell interpreter and execute all deferred defines.
     * <p>
     * After that it resets the output byte buffer, evaluates the input and returns the bytes (as a String converted
     * using UTF-8) emitted by the evaluation to the standard output.
     * <p>
     * The output is usually the string that the snippet or snippets print to the {@code System.out} using {@code
     * System.out.print()} or {@code System.out.println()} or some other way. If this string has zero length then the
     * evaluation will return the value of the last evaluated snippet.
     * <p>
     * If the JShell throws an exception or the evaluation status is some error then the method throws {@code
     * BadSyntax}.
     *
     * @param input text to evaluate as JShell snippet
     * @return the standard output of the snippet
     * @throws BadSyntax if there was any error during the evaulation of the input or during the evaluation of the
     *                   deferred definitions.
     */
    public String evaluate(String input) throws BadSyntax {
        if (js == null) {
            init();
        }
        evaluate(input,
            Predicate.<Snippet.Status>isEqual(RECOVERABLE_NOT_DEFINED).or(Predicate.isEqual(REJECTED)));
        return output.toString(StandardCharsets.UTF_8);
    }

    /**
     * Evaluate the input assuming that this code defines something for the JShell interpreter, like a method, class
     * variable etc.
     * <p>
     * If the JShell interpreter was not initialized yet then this definition will be stored in a deferred list and it
     * will be evaluated only later when the first actual use of the JShell interpreter happens.
     * <p>
     * The evaluation is accepted if there is no error or if there is some error that is recoverable. For example you
     * can have a snippet that references some global variable that is going to be defined only later.
     *
     * @param input the input that defines one single something, like a class, method, variable. It should not include
     *              multiple definitions.
     * @throws BadSyntax when the evaluation results an exception or rejects the snippet
     */
    public void define(String input) throws BadSyntax {
        if (js == null) {
            deferredDefines.add(input);
        } else {
            evaluate(input, Predicate.isEqual(REJECTED));
        }
    }

    /**
     * Evaluate the input and throw a BadSyntax exception in case the evaluation results an exception or the
     * SnippetEvent is not good enough.
     * <p>
     * During the evaluation the input is split up into snippet calling the JShell code analysis and the individual
     * snippets are evaluated.
     * <p>
     * If the snippet does not print out anything or prints a zero length string then the value of the last snipped will
     * be appended to the output buffer. This makes it simple to create snippets that just do something simple thing,
     * like evaluating an expression.
     *
     * @param input   the input that may contain many snippets
     * @param isError is a predicate that checks that the event status is either {@code REJECTED} (in case of define,
     *                hard error); or {@code REJECTED} or {@code RECOVERABLE_NOT_DEFINED} (in case of evaluate, when
     *                undefined variables are also problematic).
     * @throws BadSyntax if there is an exception while evaluating the input or the interpreter rejects the code
     */
    private void evaluate(String input, Predicate<Snippet.Status> isError) throws BadSyntax {
        output.reset();
        String lastValue = "";
        final var analyzer = js.sourceCodeAnalysis();
        var script = input;
        while (script.length() > 0) {
            final var info = analyzer.analyzeCompletion(script);
            script = info.remaining();
            final var result = info.completeness();
            final String source = info.source() + (result == SourceCodeAnalysis.Completeness.COMPLETE_WITH_SEMI ? ";" : "");
            final List<SnippetEvent> events = evaluateAndGetEvents(source);
            for (SnippetEvent e : events) {
                lastValue = e.value() != null ? e.value() : lastValue;
                if (isError.test(e.status()) || e.exception() != null) {
                    throw new BadSyntax("Error in the JShell snippet :\n"
                        + e.snippet()
                        + "\n",
                        e.exception());
                }
            }
        }
        if (output.toString(StandardCharsets.UTF_8).length() == 0) {
            if (lastValue.length() > 0 && lastValue.charAt(0) == '"') {
                lastValue = ScriptingTools.unescape(lastValue);
            }
            output.writeBytes(lastValue.getBytes(StandardCharsets.UTF_8));
        }
    }

    private List<SnippetEvent> evaluateAndGetEvents(String source) throws BadSyntax {
        BadSyntax.when(!isOpen.get(), "The JShell interpreter was closed. Will not be recreated.");
        final List<SnippetEvent> events;
        final var original = System.err;
        try {
            // jdk.jshell.execution.StreamingExecutionControl#readAndReportExecutionResult prints stack trace to System.err
            System.setErr(new PrintStream(OutputStream.nullOutputStream()));
            events = js.eval(source);
        } catch (Exception e) {
            throw new BadSyntax("The JShell snippet '" + source + "' produced error.", e);
        } finally {
            System.setErr(original);
        }
        BadSyntax.when(!isOpen.get(),  "The JShell snippet '%s' closed the JShell interpreter. Will not be recreated.", source);
        return events;
    }

    @Override
    public void close() {
        if (js != null) {
            js.close();
        }
    }
}
