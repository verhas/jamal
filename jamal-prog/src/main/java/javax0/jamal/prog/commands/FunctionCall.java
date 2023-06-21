package javax0.jamal.prog.commands;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Evaluable;
import javax0.jamal.tools.Input;

public class FunctionCall extends Expression {
    private static final Expression[] EMPTY_ARGS = new Expression[0];
    final String function;
    final Expression[] arguments;

    public FunctionCall(String function, Expression[] arguments) {
        this.function = function;
        this.arguments = arguments == null ? EMPTY_ARGS : arguments;
    }

    @Override
    public String execute(Context context) throws BadSyntax {
        final var arguments = new String[this.arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = this.arguments[i].execute(context);
        }

        final var macro = context.getProcessor().getRegister().getMacro(function);
        if (macro.isPresent()) {
            final var m = macro.get();
            final String open, close;
            open = m.optionsStart();
            close = m.optionsEnd();

            final var argBuilder = new StringBuilder();
            if (arguments.length > 1) {
                argBuilder.append(open);
                for (int i = 0; i < arguments.length - 1; i++) {
                    argBuilder.append(arguments[i]).append(" ");
                }
                argBuilder.append(close);
            }
            if (arguments.length > 0) {
                argBuilder.append(arguments[arguments.length - 1]);
            }
            return macro.get().evaluate(Input.makeInput(argBuilder.toString()), context.getProcessor());
        } else {
            final var ud = context.getProcessor().getRegister().getUserDefined(function);
            if (ud.isPresent() && ud.get() instanceof Evaluable) {
                return ((Evaluable) ud.get()).evaluate(arguments);
            } else {
                throw new BadSyntax(String.format("Macro for the function '%s()' is not defined.", function));
            }
        }
    }
}
