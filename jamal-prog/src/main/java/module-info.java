import javax0.jamal.api.Macro;

module jamal.prog {
    exports javax0.jamal.prog;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    provides Macro with Program, Expression, Decimal;
}