import javax0.jamal.api.Macro;
import javax0.jamal.scriptbasic.Basic;

module jamal.scriptbasic {
    exports javax0.jamal.scriptbasic;
    requires jamal.api;
    requires jscriptbasic;
    provides Macro with Basic
        ;
}