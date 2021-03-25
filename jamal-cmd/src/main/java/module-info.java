module jamal.cmd {
    requires jamal.api;
    requires jamal.engine;
    requires jamal.tools;
    requires info.picocli;
    opens javax0.jamal.cmd to info.picocli;
    uses javax0.jamal.api.Macro;
}