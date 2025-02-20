package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.InputHandler;

/**
 * The macro "ref" declares a macro to be saved into a reference file. It does not load, save or define a macro.
 * Well, sometimes it defines a macro.
 * <p>
 * The list of macros to be saved/loaded from an external reference file is kept in a macro named {@code xrefs} as
 * defined in the class {@link References}. The macro {@code ref} simply declares that a macro should be listed in
 * {@code xrefs} and so the other macro implemented in {@link References} will load and save this macro.
 * <p>
 * When this macro is evaluated, the reference holder macro should already exist. It exists if the macro {@code
 * references} was already executed. That macro defines the name of the file to save/load the macro values to and from.
 * <p>
 * If this is the first execution of the macro file that defines a macro with {@code ref} then the macro was not saved
 * in the reference file, and thus it is not defined. In this case this code will define the macro as the text literal
 * {@code UNDEFINED}.
 * <p>
 * The macro holder name {@code xrefs} is not hard-wired, it is a default value only. If the Jamal file wants to use
 * multiple reference files, the macro names can be maintained in multiple holders separating them.
 */
@Macro.Name({"ref", "reference"})
public class Reference implements Macro {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        InputHandler.skipWhiteSpaces(in);
        final var id = InputHandler.fetchId(in);
        InputHandler.skipWhiteSpaces(in);
        final String ref = getReferenceHolderMacroName(in);
        final var xref = Identified.find(processor.getRegister(), References.ReferenceHolder.class, ref)
                .orElseThrow(() -> new BadSyntax("No reference holder found for '" + ref + "'"));
        xref.getObject().add(id);
        defineMacro(processor, id);
        return "";
    }

    private void defineMacro(Processor processor, String id) throws BadSyntax {
        if (processor.getRegister().getUserDefined(id).isEmpty()) {
            final var dummy = processor.newUserDefinedMacro(id, "UNDEFINED", false,
                    true, "...");
            if( dummy instanceof Configurable){
                ((Configurable)dummy).configure(Configurable.Keys.SOFT,true);
            }
            processor.defineGlobal(dummy);
        }
    }

    /**
     * Get the name of the macro that holds the macro-name list saved to the reference file.
     * <p>
     * If there is a {@code >} character after the name of the referenced macro then the rest is the name of the
     * macro that holds the list of references.
     * The default name is {@code xrefs} defined in the class {@link References}.
     *
     * @param in the input.
     * @return the reference macro name
     */
    private String getReferenceHolderMacroName(Input in) {
        final String ref;
        if (InputHandler.firstCharIs(in, '>')) {
            InputHandler.skip(in, 1);
            InputHandler.skipWhiteSpaces(in);
            ref = InputHandler.fetchId(in);
        } else {
            ref = References.XREFS;
        }
        return ref;
    }
}
