import javax0.jamal.api.Macro;

module jamal.py {
    requires jamal.tools;
    requires jamal.api;
    requires jamal.engine;
    exports javax0.jamal.py;
    provides Macro with javax0.jamal.py.PythonFunction;
}