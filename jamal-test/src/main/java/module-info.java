import javax0.jamal.test.examples.Hello;
import javax0.jamal.test.examples.HelloWorld;

// snippet module_declaration
module jamal.test {
    requires jamal.api;
    provides javax0.jamal.api.Macro with HelloWorld, Hello;
}
//end snippet