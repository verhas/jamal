package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class UrlEncode implements Macro, Scanner {

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var scanner = newScanner(in,processor);
        final var cs = scanner.str(null, "charset","cs").defaultValue("UTF-8");
        scanner.done();
        try {
            return URLEncoder.encode(in.toString().trim(), cs.get());
        } catch (UnsupportedEncodingException e) {
            throw new BadSyntax(String.format("Charset '%s' for macro urlEncode is not supported.", cs.get()), e);
        }
    }

}
