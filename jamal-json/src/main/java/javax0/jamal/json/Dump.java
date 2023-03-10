package javax0.jamal.json;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.InputHandler;

import java.io.FileWriter;
import java.io.IOException;


public class Dump implements Macro, InnerScopeDependent {

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {

        InputHandler.skipWhiteSpaces(input);
        final var id = InputHandler.fetchId(input);
        InputHandler.skipWhiteSpaces(input);
        final var from = InputHandler.fetchId(input);
        BadSyntax.when(!"to".equals(from), "json:dump needs a 'to' after the identifier");
        InputHandler.skipWhiteSpaces(input);
        var reference = input.getReference();
        var fileName = FileTools.absolute(reference, input.toString().trim());

        final var jsonMacroObject = getJson(processor, id);
        try (final var writer = new FileWriter(fileName)) {
            writer.write(jsonMacroObject.getObject().toString());
        } catch (IOException ioe) {
            throw new BadSyntax("Not possible to dump json '" + id + "' into the file '" + fileName + "'", ioe);
        }
        return "";
    }

    private static JsonMacroObject getJson(Processor processor, String id) throws BadSyntax {
        final var identified = processor.getRegister().getUserDefined(id).orElseThrow(
                () -> new BadSyntax("Cannot resolve json '" + id + "', does not exists"));
        BadSyntax.when(!(identified instanceof JsonMacroObject), "The user defined macro '%s' is not a JSON structure", id);
        return (JsonMacroObject) identified;
    }

    @Override
    public String getId() {
        return "json:dump";
    }
}
