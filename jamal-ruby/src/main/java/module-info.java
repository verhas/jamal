import javax0.jamal.api.Macro;
import javax0.jamal.ruby.RubyCloser;
import javax0.jamal.ruby.RubyEval;
import javax0.jamal.ruby.RubyImport;
import javax0.jamal.ruby.RubyProperty;
import javax0.jamal.ruby.RubyShell;

module jamal.ruby {
    exports javax0.jamal.ruby;
    requires jamal.api;
    requires jamal.tools;
    requires org.jruby.complete;
    provides Macro with RubyEval, RubyShell, RubyProperty, RubyImport, RubyCloser
        ;
}