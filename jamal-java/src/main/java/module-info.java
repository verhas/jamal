import javax0.jamal.api.Macro;

module jamal.java {
    exports javax0.jamal.java;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires com.javax0.sourcebuddy;
    requires java.xml;
    provides Macro with JavaSourceMacro.JavaClass,
            JavaSourceMacro.JavaModuleInfo,
            JavaSourceMacro.JavaMacroClass,
            JavaSourceMacro.JavaCompile,
            Jdsl;
}