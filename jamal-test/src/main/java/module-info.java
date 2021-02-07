import javax0.jamal.test.examples.Array;
import javax0.jamal.test.examples.Hello;
import javax0.jamal.test.examples.HelloWorld;
import javax0.jamal.test.examples.Spacer;

// snippet module_declaration
module jamal.test {
    requires jamal.api;
    requires jamal.tools;
    provides javax0.jamal.api.Macro with
        HelloWorld,
        Hello,
        Spacer,
        Array
        ;
}
//end snippet