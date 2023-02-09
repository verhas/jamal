package javax0.jamal.java;

import com.javax0.sourcebuddy.Compiler;
import com.javax0.sourcebuddy.Fluent;
import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Macros that implement the Java source integration.
 */
public class JavaSourceMacro {
    /**
     * The namespace of the macros in this class. JBIM stands for Java Built-In Macros.
     */
    // snipline MACRO_NS filter="(.*?)"
    private static final String MACRO_NS = "jbim:";
    // snipline DEFAULT_ID filter="(.*?)"
    private static final String DEFAULT_ID = MACRO_NS + "source";

    /**
     * An instance of this class will be stored as macro. It is used when a source class,
     * when a macro source, or module info file is added to the source set.
     */
    private static class JavaMacroSet implements Identified {

        private String moduleInfo;

        void setModuleInfo(String moduleInfo) {
            if (this.moduleInfo != null)
                throw new IllegalStateException(String.format("Module info is already set to %s", this.moduleInfo));
            this.moduleInfo = Objects.requireNonNull(moduleInfo);
        }

        final private Set<String> sources = new HashSet<>();

        void addSource(String javaSource) {
            sources.add(javaSource);
        }

        final private Set<String> macroSources = new HashSet<>();

        void addMacroSource(String macroSource) {
            macroSources.add(macroSource);
        }

        private final String id;

        private JavaMacroSet(final String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }
    }

    private static JavaMacroSet getJavaMacroSet(final Processor processor, final String name) {
        return processor.getRegister().getUserDefined(name).filter(JavaMacroSet.class::isInstance).map(JavaMacroSet.class::cast)
                .orElseGet(() -> {
                    final var holder = new JavaMacroSet(name);
                    processor.getRegister().define(holder);
                    return holder;
                });
    }

    private static String getId(final Input in, final Processor processor, Macro macro) throws BadSyntax {
        final var id = Params.<String>holder(null, "id").orElse(DEFAULT_ID);
        Scan.using(processor).from(macro).firstLine().keys(id).parse(in);
        return id.get();
    }

    /**
     * Add a Java source file to the source set. The source file will be part of the code, loaded by the classloader,
     * but will not be registered as macro, even if it implements the {@link Macro} interface.
     */
    public static class JavaClass implements Macro {

        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final String name = JavaSourceMacro.getId(in, processor, this);
            getJavaMacroSet(processor, name).addSource(in.toString());
            return "";
        }

        @Override
        public String getId() {
            return MACRO_NS + "class";
        }
    }

    /**
     * Add a Java source file to the source set. The source file will be part of the code, loaded by the classloader,
     * and will be registered as macro. It has to implement the {@link Macro} interface.
     */
    public static class JavaMacroClass implements Macro {

        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final String name = JavaSourceMacro.getId(in, processor, this);
            getJavaMacroSet(processor, name).addMacroSource(in.toString());
            return "";
        }

        @Override
        public String getId() {
            return MACRO_NS + "macro";
        }
    }

    /**
     * Add the module info file source to the compilation set. If no module info is added then a default module info
     * will be created. It will contain 'requires' for the Jamal module {@code jamal.api} and exports the packages
     * that contain a macro.
     * <p>
     * If the specified module info is zero length then no module info file will be created.
     */
    public static class JavaModuleInfo implements Macro {

        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final String name = JavaSourceMacro.getId(in, processor, this);
            getJavaMacroSet(processor, name).setModuleInfo(in.toString());
            return "";
        }


        @Override
        public String[] getIds() {
            return new String[]{MACRO_NS + "moduleInfo", MACRO_NS + "moduleinfo"};
        }
    }

    /**
     * Compile the Java source files that were added to the source set. The compilation is done in memory, the
     * compiled classes are not written to the file system. After the compilation the classes are loaded by the
     * class loader and the macro classes are registered.
     */
    public static class JavaCompile implements Macro {

        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final String name = JavaSourceMacro.getId(in, processor, this);

            final var set = getJavaMacroSet(processor, name);
            try {
                final var compiler = Compiler.java();
                final var nameSet = new HashSet<String>();
                final var pckgSet = new HashSet<String>();
                for (final var src : set.macroSources) {
                    compiler.from(src);
                    final var className = Compiler.getBinaryNameFromSource(src);
                    nameSet.add(className);
                    int i = className.lastIndexOf('.');
                    if (i > 0) {
                        pckgSet.add(className.substring(0, i));
                    }
                }
                if (set.moduleInfo != null) {
                    if (set.moduleInfo.trim().length() > 0) {
                        compiler.from("module-info", set.moduleInfo);
                    } else {
                        addDefaultModuleInfo(compiler, pckgSet);
                    }
                } else {
                    doNotAddModuleInfo();
                }
                for (final var src : set.sources) {
                    compiler.from(src);
                }
                final var loader = compiler.annotatedClasses().compile()
                        .load(Compiler.LoaderOption.REVERSE);//even if there is a class with the same name in the classpath
                final var register = processor.getRegister();
                for (final var nm : nameSet) {
                    final var macro = loader.newInstance(nm, Macro.class);
                    register.define(macro);
                }
                // delete the user defined macro replacing it with an empty set
                register.define(new JavaMacroSet(name));
                return "";
            } catch (Exception e) {
                throw new BadSyntax("There was an exception compiling the Java sources", e);
            }
        }

        @Override
        public String getId() {
            return MACRO_NS + "load";
        }
    }

    /**
     * Do not add the specified or default module info to the compiler. This method is invoked when there was no
     * module info specified.
     */
    private static void doNotAddModuleInfo() {
    }

    private static void addDefaultModuleInfo(final Fluent.AddSource compiler, final HashSet<String> pckgSet) {
        // snippet addDefaultModuleInfo
        compiler.from("module-info", "module A" + System.currentTimeMillis() + " {\n" +
                "    requires jamal.api;" +
                pckgSet.stream().map(s -> "    exports " + s + ";\n").collect(Collectors.joining("\n")) +
                "}");
        // end snippet
    }
}
