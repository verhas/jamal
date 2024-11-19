import javax0.jamal.api.ResourceReader;

module jamal.jar.input {
    requires jamal.api;
    requires jamal.tools;
    exports javax0.jamal.jar.input;
    uses ResourceReader;
    provides ResourceReader with JarInput;
}