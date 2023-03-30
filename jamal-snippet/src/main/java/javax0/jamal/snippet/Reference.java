package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

public class Reference implements Macro {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        InputHandler.skipWhiteSpaces(in);
        final var id = InputHandler.fetchId(in);
        InputHandler.skipWhiteSpaces(in);
        final String ref;
        if (InputHandler.firstCharIs(in, '>')) {
            InputHandler.skip(in, 1);
            InputHandler.skipWhiteSpaces(in);
            ref = InputHandler.fetchId(in);
        } else {
            ref = References.XREFS;
        }
        final var xref = processor.getRegister().getUserDefined(ref)
                .filter(m -> m instanceof References.ReferenceHolder)
                .map(m -> (References.ReferenceHolder) m)
                .orElseThrow(() -> new BadSyntax(String.format("The reference macro '%s' is not defined", ref)));
        xref.getObject().add(id);
        if( !processor.getRegister().getUserDefined(id).isPresent())    {
            final var dummy = processor.newUserDefinedMacro(id,"UNDEFINED",false,true, new String[]{"..."});
            processor.defineGlobal(dummy);
        }
        return "";
    }

    private static final String[] names = new String[]{"ref","reference"};

    @Override
    public String[] getIds() {
        return names;
    }
}
