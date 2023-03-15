package javax0.jamal.prog.commands;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;

/**
 * A command that the interpreter can execute
 */
public interface Command {
    /**
     * Execute the command.
     *
     * @param context the Jamal processing environment
     * @return the result of the execution
     */
    String execute(Context context) throws BadSyntax;
}
