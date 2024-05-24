package javax0.jamal.jamal;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;

@Macro.Stateful
public class Output implements Macro, Scanner {
    /**
     * Instantiating a processor also instantiates all macro classes via the ServiceLoader. Instantiating here the
     * processor would instantiate a new instance of all the macros including this one and that would mean an infinite
     * recursion via the service loader.
     */
    private Processor localProc = null;

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var isolate = scanner.bool("isolatedOutput", "isolate");
        final var embedError = scanner.bool("embedError");
        scanner.done();
        InputHandler.skipWhiteSpaces2EOL(in);
        if (isolate.is()) {
            try (var isolatedProc = processor.spawn()) {
                isolatedProc.separators("{", "}");
                return isolatedProc.process(new javax0.jamal.tools.Input(in.toString(), in.getPosition()));
            } catch (LinkageError e) {
                if( embedError.is() ){
                    return "Error: " + e.getMessage();
                }
                throw new BadSyntax("There was an error running isolated Jamal instance", e);
            } catch (Exception e) {
                if( embedError.is() ){
                    return "Error: " + e.getMessage();
                }
                throw new BadSyntax("There was an exception running isolated Jamal instance", e);
            }
        } else {
            if (localProc == null) {
                localProc = processor.spawn();
                localProc.separators("{", "}");
            }
            try {
                return localProc.process(new javax0.jamal.tools.Input(in.toString(), in.getPosition()));
            } catch (Exception e) {
                if(embedError.is()){
                    return "Error: " + e.getMessage();
                }
                throw new BadSyntax("There was an exception running Jamal instance", e);
            }catch (LinkageError le){
                if( embedError.is() ){
                    return "Error: " + le.getMessage();
                }
                throw new BadSyntax("There was an error running Jamal instance", le);
            }
        }
    }
}