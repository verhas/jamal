import javax0.jamal.api.Macro;
import javax0.jamal.java.JavaSourceMacro;

module jamal.java {
    exports javax0.jamal.java;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires SourceBuddy;
    provides Macro with JavaSourceMacro.JavaClass,
            JavaSourceMacro.JavaModuleInfo,
            JavaSourceMacro.JavaMacroClass,
            JavaSourceMacro.JavaCompile;
}