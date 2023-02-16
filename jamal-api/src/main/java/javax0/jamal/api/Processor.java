package javax0.jamal.api;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.Properties;

/**
 * The processor object that can be used to process an input to generate the Jamal output.
 * <p>
 * Processor instances should not be used by multiple threads. They are <b>not</b> thread safe by design.
 * <p>
 * A processor is AutoClosable, and it has to be closed.
 * <p>
 * Creating a processor instance may be expensive consuming significant amount of CPU cycles. Create one in your code
 * when you are going to need it.
 */
public interface Processor extends AutoCloseable {
    /**
     * Get the debugger that is currently configured for the processor.
     *
     * @return the current debugger
     */
    Optional<Debugger> getDebugger();

    Optional<Debugger.Stub> getDebuggerStub();

    /**
     * Process the input and result the string after processing all built-in and user defined macros.
     *
     * @param in the input the processor has to work on.
     * @return the string after the processing
     * @throws BadSyntax when the input contains something that cannot be processed.
     */
    String process(final Input in) throws BadSyntax;

    /**
     * A convenience method that executes the Jamal process for a String. It may be handy when the processor is
     * used to process some input that is not a file.
     *
     * @param in the input string
     * @return the result of the processing
     * @throws BadSyntax in case the input contains something that cannot be processed.
     */
    default String process(final String in) throws BadSyntax {
        throw new IllegalArgumentException("The method process(String) is not supported by this processor");
    }

    /**
     * Get the macro register of this processor. See {@link MacroRegister}
     *
     * @return the register
     */
    MacroRegister getRegister();

    /**
     * Get the JShell engine that the processor has.
     * <p>
     * Note that the JShell engine may not be initialized. It initializes automatically the first time when the engine's
     * {@link JShellEngine#evaluate(String)} is invoked.
     *
     * @return the JShell engine
     */
    JShellEngine getJShellEngine();

    /**
     * Create a new user defined macro. The implementation of this method calls the constructor of the matching
     * implementation of the user defined macro. The existence of this method loosens the coupling of the user of the
     * API and the actual implementation. The code using the implementation may need only to initiate the engine that
     * implements this interface, but does not need to access directly the UserDefinedMacro or other interface
     * implementations.
     * <p>
     * NOTE: The invocation of this method creates a new object, but it DOES NOT register the created user defined macro
     * in the macro registry. The sole purpose of this method is to decouple the API usage and the implementation.
     *
     * @param id     the identifier (name) of the macro
     * @param input  the content of the macro
     * @param params the parameter names of the macro
     * @return the new user defined macro
     * @throws BadSyntax in case the parameter names contain each other
     */
    UserDefinedMacro newUserDefinedMacro(String id, String input, String... params) throws BadSyntax;

    /**
     * The same as {@link #newUserDefinedMacro(String, String, String[])} but it can also define when the macro is
     * verbatim. The default implementation ignores the verbatim flag. See the note of {@link
     * #newUserDefinedMacro(String, String, String[]) newUserDefinedMacro()}
     *
     * @param id       see {@link #newUserDefinedMacro(String, String, String[])}
     * @param input    see {@link #newUserDefinedMacro(String, String, String[])}
     * @param verbatim {@code true} if the result of the macro should not be evaluated
     * @param params   see {@link #newUserDefinedMacro(String, String, String[])}
     * @return see {@link #newUserDefinedMacro(String, String, String[])}
     * @throws BadSyntax see {@link #newUserDefinedMacro(String, String, String[])}
     */
    default UserDefinedMacro newUserDefinedMacro(String id, String input, boolean verbatim, String... params) throws BadSyntax {
        return newUserDefinedMacro(id, input, params);
    }

    /**
     * Create a new user defined script. Read the important comments for {@link #newUserDefinedMacro(String, String,
     * String[])}
     *
     * @param id         see {@link #newUserDefinedMacro(String, String, String[])}
     * @param scriptType see {@link #newUserDefinedMacro(String, String, String[])}
     * @param input      see {@link #newUserDefinedMacro(String, String, String[])}
     * @param params     see {@link #newUserDefinedMacro(String, String, String[])}
     * @return see {@link #newUserDefinedMacro(String, String, String[])}
     */
    ScriptMacro newScriptMacro(String id, String scriptType, String input, String[] params) throws BadSyntax;

    /**
     * Register an AutoCloseable closer that has to be closed when the execution is finished.
     * <p>
     * Some user defined (Java implemented) or built-in macro may create resources that perform some actions
     * asynchronous. The typical example is when a macro that creates some external resource starts a separate thread to
     * execute the task. This task has to be joined at the end of the processing. The general model is that there is a
     * resource that has to be closed. The {@code closer} may be the resource itself or some object that will close the
     * resource.
     * <p>
     * Closing as an operation may be treated fairly liberal. Almost anything can be "closing". The macro
     * {@code xmlFormat}, for example, "closes" the operation replacing the final output of Jamal with the XML formatted
     * version.
     * <p>
     * The {@code closer} may also implement the interfaces {@link javax0.jamal.api.Closer.ProcessorAware} or {@link
     * javax0.jamal.api.Closer.OutputAware} or both.
     * In that case the
     * {@link javax0.jamal.api.Closer.ProcessorAware#set(Processor) set(Processor)}
     * and/or
     * {@link javax0.jamal.api.Closer.OutputAware#set(Input)}  set(Input)}
     * methods will be called before calling {@link AutoCloseable#close() close()} passing the {@link Processor}
     * or the {@link Input} instance holding the final processed output as argument.
     * <p>
     * Since the call to {@link AutoCloseable#close() close()} comes before {@link Processor#process(Input)} returns the
     * output may be altered by the implemented {@link AutoCloseable#close() close()} method. That way a built-in macro
     * may implement post-processing logic that works on the whole output.
     * <p>
     * The sample test {@code javax0.jamal.engine.TestProcessor#testPostProcessor()} in the file {@code
     * src/test/java/javax0/jamal/engine/TestProcessor.java} shows an example that converts the whole result to
     * uppercase.
     * <p>
     * The closer objects {@link AutoCloseable#close() close()} method may invoke the injected processors
     * {@link Processor#process(Input) process(Input)} method. In this case, however, the processor is already in a
     * state closing resources and processing the whole input again will not recursively invoke the closers. After
     * the input is processed the invocation of the closers registered in the first round continues. Any closer
     * registered during the call to {@link Processor#process(Input) process(Input)} from a closer will be ignored.
     * <p>
     * Calling this method the macro can register an {@link AutoCloseable} object. The method {@link
     * AutoCloseable#close() close()} will be invoked when the method {@link Processor#process(Input)} finishes its top
     * level execution. When the method is called in recursive calls from a macro or from any other place the deferred
     * resources will not be closed upon return, only when the top level call is to be returned.
     * <p>
     * The processor implementation guarantees that the processor will invoke the closers in the order registered.
     * The processor will never register an already registered closer. In other words, every closer is invoked only
     * once. In the order of executions the first registering is relevant. A closer {@code c2} is treated as already
     * registered if there is a registered closer {@code c1} so that {@code c1.equals(c2)}.
     * <p>
     * Note that this method, or any other method of the processor MUST NOT be invoked from other than the main thread
     * of the Jamal processing. Even if a macro spawns a new thread the new thread must not do anything with the
     * processor.
     *
     * @param closer the autocloseable object to be closed at the end of the processing.
     * @return the registered closer. It may not be the same closer as the argument {@code closer}. If the closer
     * was already registered, then the first registered closer will be returned. More formally, if there was a {@code
     * closer2} already registered such that {@code closer2.equals(closer)} then {@code closer2} will be returned.
     */
    AutoCloseable deferredClose(AutoCloseable closer);

    /**
     * Get the context object that the embedding application was setting. The context object is a general object and the
     * processor does not do anything with it, except that it provides this method for all macros to get access to the
     * object.
     * <p>
     * The implementation of the processor must return the same object during its lifetime.
     *
     * @return the context object the embedding application set or {@code null} in case the context object was not set.
     */
    Context getContext();

    /**
     * A very simple functional interface that the embedding applications can implement, provide to accommodate log
     * messages from the Jama processing.
     */
    @FunctionalInterface
    interface Logger {
        /**
         * A logger interface that the embedding application may provide for the processor.
         *
         * @param level  the message level, standard JKD level
         * @param pos    position, may be null, and the implementation MUST NOT fail if it is null
         * @param format the message or the message format to be use in String.format()
         * @param params the parameters for the format
         */
        void log(final System.Logger.Level level, final Position pos, final String format, final String... params);
    }

    /**
     * @return the logger implementation that was set by the embedding application. There is no method to set the logger
     * object, just as there is no metjod to set the context. Both of these objects are application specific and as the
     * embedding applications are using a specific implementation of this interface they will use the one that provides
     * the possibility to set the logger (context, {@link #getContext}).
     * <p>
     * The default implementation returns a null logger that just does not log.
     */
    default Logger logger() {
        return (level, pos, format, params) -> {
        };
    }

    /**
     * IOHookResult is the type of the object returned by an IO Hook object {@link FileReader#read(String)} or
     * {@link FileWriter#write(String, String)} method.
     */
    interface IOHookResult {
        enum Type {
            IGNORE, // the reader does not care
            REDIRECT, // reader identified the final file name, get() returns the name
            DONE // reader was reading the file, get() returns the content
        }

        /**
         * Get the type of the result.
         *
         * @return the result type.
         */
        Type type();

        /**
         * Get the result of the reader.
         *
         * @return the name of the file to read/write in case of REDIRECT or
         * the content of the file in case the result is DONE and the hook was reading.
         * In any other cases the method will throw {@link IllegalStateException}.
         */
        String get();

        /**
         * A singleton instance to be returned by FileReader implementations when the file reading is ignored by the
         * hook.
         */
        IOHookResult IGNORE = new IOHookResult() {
            @Override
            public Type type() {
                return Type.IGNORE;
            }

            @Override
            public String get() {
                throw new IllegalStateException("IO hook result was IGNORE, nothing to \"get()\".");
            }
        };
    }

    class IOHookResultImpl implements IOHookResult {
        private final Type type;
        private final String content;

        public IOHookResultImpl(final Type type, final String content) {
            this.type = type;
            this.content = content;
        }

        @Override
        public Type type() {
            return type;
        }

        @Override
        public String get() {
            return content;
        }
    }

    class IOHookResultDone extends IOHookResultImpl {
        public IOHookResultDone(final String content) {
            super(Type.DONE, content);
        }

        public IOHookResultDone() {
            super(Type.DONE, null);
        }
    }

    /**
     * Use {@code IOHookResultRedirect("fileName)} to redirect the file reading or writing to a different file.
     */
    class IOHookResultRedirect extends IOHookResultImpl {
        public IOHookResultRedirect(final String content) {
            super(Type.REDIRECT, content);
        }
    }

    /**
     * A file writer can be set to work with a processor to intercept any file writing operations the macros may make.
     * If the writer is set into the processor via the {@link #setFileWriter(FileWriter)} it will be invoked whenever
     * a macro wants to write a file. It can be used to implement a special file system or file mapping.
     */
    @FunctionalInterface
    interface FileWriter {
        /**
         * Tries to write the file, decides on redirect or do nothing.
         *
         * @param fileName the original name of the file
         * @return the structure containing the result, which is nothing, or final name
         */
        IOHookResult write(final String fileName, final String content);
    }

    void setFileWriter(FileWriter fileWriter);

    Optional<FileWriter> getFileWriter();

    @FunctionalInterface
    interface FileReader {

        /**
         * Tries to read the file, decides on redirect or do nothing.
         *
         * @param fileName the original name of the file
         * @return the structure containing the result, which is nothing, the final name of the file or the content of
         * the file
         */
        IOHookResult read(final String fileName);

        /**
         * The processor calls this method in case the result of the reading was {@link IOHookResult.Type#REDIRECT} or
         * {@link IOHookResult.Type#IGNORE} after reading the file. When the result is {@link IOHookResult.Type#DONE}
         * the method is not invoked because in that case the reader already had access to the content, it does not
         * need to get it again.
         * <p>
         * Note that the processor may invoke this method for the same file multiple times. This happens when the file
         * is redirected. For example
         *
         * <ul>
         *     <li>Jamal includes the file {@code f1}</li>
         *     <li>The read hook redirects it to {@code f2}</li>
         *     <li>The processor invokes the read hook again for the file name {@code f2}.</li>
         *     <li>The read hook returns {@link IOHookResult.Type#IGNORE}</li>
         *     <li>The processor reads the content of the file {@code f3}</li>
         *     <li>The processor calls the read hook {@link #set(String, String) set("f3", "...")} with the content</li>
         *     <li>The processor calls the read hook {@link #set(String, String) set("f2", "...")} with the content</li>
         * </ul>
         *
         * @param fileName the name of the file, which was passed to the {@link #read(String)} method (not the altered
         *                 name returned).
         * @param content  the content of the file, which was read by the processor.
         */
        default void set(final String fileName, final String content) {
        }
    }

    /**
     * Set a {@link FileReader} hook to work with a processor to intercept any file reading operations the macros may make.
     *
     * @param fileReader the file reader
     */
    void setFileReader(FileReader fileReader);

    /**
     * Get the file reader hook.
     *
     * @return the file reader hook
     */
    Optional<FileReader> getFileReader();

    /**
     * @param id the identifier of the user defined macro
     * @return {@code true} if the user defined macro is defined at the current contex and {@code false} otherwise.
     */
    default boolean isDefined(String id) {
        return getRegister().getUserDefined(id).isPresent();
    }

    /**
     * Define a new user defined macro on the global level. Technically anything can be defined that implements the
     * {@link Identified} interface. Usually {@link UserDefinedMacro} is registered using this method.
     *
     * @param macro the macro to be registered
     */
    default void defineGlobal(Identified macro) {
        getRegister().global(macro);
    }

    /**
     * Define a new user defined macro on the current scope. Technically anything can be defined that implements the
     * {@link Identified} interface. Usually {@link UserDefinedMacro} is registered using this method.
     *
     * @param macro the macro to be registered
     */
    default void define(Identified macro) {
        getRegister().define(macro);
    }

    /**
     * This is a convenience method with the default implementation calling to the {@link
     * MacroRegister#separators(String, String)} method.
     *
     * @param openMacro  see {@link MacroRegister#separators(String, String)}
     * @param closeMacro see {@link MacroRegister#separators(String, String)}
     * @throws BadSyntax see {@link MacroRegister#separators(String, String)}
     */
    default void separators(String openMacro, String closeMacro) throws BadSyntax {
        getRegister().separators(openMacro, closeMacro);
    }

    Deque<BadSyntax> EMPTY_DEQUEUE = new ArrayDeque<>();

    /**
     * @return the current number of errors that were detected in the source file, but were not aborting the evaluation.
     */
    default Deque<BadSyntax> errors() {
        return EMPTY_DEQUEUE;
    }

    /**
     * Throw the last exception that was deferred.
     */
    default void throwUp() throws BadSyntax {
    }

    /**
     * Convert a Jamal version string to a {@link Runtime.Version}.
     * <p>
     * The method removes the trailing zero versions, because those are not allowed by the parsing of {@link
     * Runtime.Version}.
     *
     * @param version the version string, probably from the macro argument {@code require}
     * @return the parsed version, which can be compared to the current version
     */
    static Runtime.Version jamalVersion(final String version) {
        return Runtime.Version.parse(version.replaceAll("(?:\\.0+){0,2}\\.0+(-|$)", "$1"));
    }

    /**
     * Load the version property from the properties file and store it into the properties variable {@code version}. The
     * properties will contain one property named {@code "version"}.
     *
     * The implementation loads all the {@code version.properties} files from the classpath and selects the one that
     * contains the string {@code "jamal-api"} in the path. This is needed because there are some implementations, like
     * the IntelliJ embedding where there is a {@code version.properties} file in the classpath, but it is not the one we want,
     * and also it happens sooner in the classpath, so it is loaded first.
     *
     * @param version the properties that will hold the version property
     */
    static void jamalVersion(Properties version) {
        try {
            final var it = Processor.class.getClassLoader().getResources("version.properties").asIterator();
            while (it.hasNext()) {
                final var url = it.next();
                if (url.getPath().contains("jamal-api")) {
                    version.load(url.openStream());
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Version information of Jamal cannot be identified.");
        }
    }

    /**
     * @return the current Jamal version in the form of a {@link Runtime.Version}
     */
    static Runtime.Version jamalVersion() {
        final var version = new Properties();
        jamalVersion(version);
        return jamalVersion(version.getProperty("version"));
    }
}
