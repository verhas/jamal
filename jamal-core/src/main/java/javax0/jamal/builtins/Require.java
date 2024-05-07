package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Macro.Name;
import javax0.jamal.api.Processor;

import java.util.Arrays;
import java.util.Properties;

import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;
import static javax0.jamal.tools.InputHandler.startsWith;
@Name({"require", "version"})
public class Require implements Macro {

    private enum Prefix {
        // <= precedes < AND >= precedes > or else only the one character version is found by startWith
        LESS_OR_EQUAL("<="), LESS("<"), EQUAL("="), GREATER_OR_EQUAL(">="), GREATER(">");

        final String lex;

        Prefix(String lex) {
            this.lex = lex;
        }

        int len() {
            return lex.length();
        }

        static final String[] lexes = Arrays.stream(values()).map(p -> p.lex).toArray(String[]::new);
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        skipWhiteSpaces(in);
        if (in.length() == 0) {
            return Processor.jamalVersionString();
        }
        final int exact;
        final Prefix comparing;
        if ((exact = startsWith(in, Prefix.lexes)) != -1) {
            comparing = Prefix.values()[exact];
            skip(in, comparing.len());
        } else {
            comparing = Prefix.GREATER_OR_EQUAL;
        }
        final Runtime.Version requiredVersion;
        try {
            requiredVersion = Processor.jamalVersion(in.toString().trim());
        } catch (Exception e) {
            throw new BadSyntaxAt("The string '" + in.toString().trim() + "' cannot be used as a version.", in.getPosition(), e);
        }
        BadSyntaxAt.when(requiredVersion.compareTo(Processor.jamalVersion("1.6.3")) <= 0, "Required version is older than 1.6.3, which is invalid.", in.getPosition());
        final var currentVersion = Processor.jamalVersion();

        switch (comparing) {
            case LESS:
                if (currentVersion.compareTo(requiredVersion) < 0) {
                    break;
                }
                BadSyntaxAt.when(currentVersion.compareTo(requiredVersion) == 0, "The current version " + currentVersion + " is the same as the required version. It has to be older.", in.getPosition());
                BadSyntaxAt.format( "The current version " + currentVersion + " is newer than the required version. It has to be older.", in.getPosition());

            case LESS_OR_EQUAL:
                if (currentVersion.compareTo(requiredVersion) <= 0) {
                    break;
                }
                throw new BadSyntaxAt("The current version " + currentVersion + " is newer than the required version. It has to be older or the same version.", in.getPosition());
            case EQUAL:
                if (currentVersion.compareTo(requiredVersion) == 0) {
                    break;
                }
                throw new BadSyntaxAt("The current version of Jamal is " + currentVersion + ", which is not the same as the required version " + requiredVersion, in.getPosition());
            case GREATER_OR_EQUAL:
                if (currentVersion.compareTo(requiredVersion) >= 0) {
                    break;
                }
                throw new BadSyntaxAt("The current version " + currentVersion + " is older than the required version. It has to be newer.", in.getPosition());
            case GREATER:
                if (currentVersion.compareTo(requiredVersion) > 0) {
                    break;
                }
                BadSyntaxAt.when(currentVersion.compareTo(requiredVersion) == 0, () -> String.format("The current version %s is the same as the required version. It has to be newer.", currentVersion), in.getPosition());
                BadSyntaxAt.when(true, () -> String.format("The current version %s is older than the required version. It has to be newer.", currentVersion), in.getPosition());
            default:
                throw new IllegalArgumentException("The comparison in require is illegal.");
        }
        return "";
    }
}
