import javax0.jamal.api.Macro;
import javax0.jamal.groovy.GroovyEval;
import javax0.jamal.groovy.GroovyImport;
import javax0.jamal.groovy.GroovyProperty;
import javax0.jamal.groovy.GroovyShell;

module jamal.groovy {
    exports javax0.jamal.groovy;
    requires jamal.api;
    requires org.codehaus.groovy;
    requires jamal.tools;
    provides Macro with GroovyEval, GroovyShell, GroovyProperty, GroovyImport
        ;
}