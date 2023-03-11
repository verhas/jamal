package javax0.jamal.openai;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;
import org.json.JSONObject;

import java.io.IOException;

public class Post implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var opt = new Options(processor,in,this);
        try {
            return new Query(opt.cacheSeed).post(opt.url, in.toString());
        } catch (final IOException e) {
            throw new BadSyntax("POST url '" + opt.url + "' failed", e);
        }
    }

    @Override
    public String getId() {
        return "openai:post";
    }
}
