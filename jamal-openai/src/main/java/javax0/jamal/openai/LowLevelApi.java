package javax0.jamal.openai;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.HexDumper;
import javax0.jamal.tools.SHA256;
import javax0.jamal.tools.Scanner;

import java.io.IOException;

public class LowLevelApi {

    public static class Get implements Macro, InnerScopeDependent, Scanner.FirstLine {
        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var opt = new Options(processor, in, this);

            try {
                final String retval = new Query(opt).get();
                check_HashCode(opt.hash, retval);
                return retval;
            } catch (final Exception e) {
                throw new BadSyntax("GET url '" + opt.url + "' failed", e);
            }
        }

        @Override
        public String getId() {
            return "openai:get";
        }
    }

    public static class Post implements Macro, InnerScopeDependent, Scanner.FirstLine {
        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var opt = new Options(processor, in, this);
            try {
                final var retval = new Query(opt).post(in.toString());
                check_HashCode(opt.hash, retval);
                return retval;
            } catch (final IOException e) {
                throw new BadSyntax("POST url '" + opt.url + "' failed", e);
            }
        }

        @Override
        public String getId() {
            return "openai:post";
        }
    }

    private static void check_HashCode(final String hash, final String retval) throws BadSyntax {
        if( hash == null ) {
            return;
        }
        if (hash.length() < 6) {
            throw new BadSyntax(String.format("The hash code '%s' is too short, you need at least 6 characters.", hash));
        }
        final var hashCode = HexDumper.encode(SHA256.digest(retval)).replaceAll("([0-9a-fA-F]{8})(?!$)", "$1-");
        if (!hashCode.contains(hash)) {
            throw new BadSyntax(String.format("The hash of the result is '%s' does not contain '%s'.", hashCode, hash));
        }
    }

}


