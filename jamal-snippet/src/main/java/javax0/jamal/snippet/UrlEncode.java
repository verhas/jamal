package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UrlEncode implements Macro {

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var cs = Params.holder(null, "charset","cs").asString().orElse("UTF-8");
        Scan.using(processor).from(this).between("()").keys(cs).parse(in);
        try {
            return URLEncoder.encode(in.toString().trim(), cs.get());
        } catch (UnsupportedEncodingException e) {
            throw new BadSyntax(String.format("Charset '%s' for macro urlEncode is not supported.", cs.get()), e);
        }
    }

}
