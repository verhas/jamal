package javax0.jamal.openai;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;
public class Options {

    final String url;
    final String cacheSeed;


    public Options(Processor processor, Input in, Macro macro) throws BadSyntax {
        final var url = Params.<String>holder("url").asString();
        final var cacheSeed= Params.<String>holder("seed").asString();

        Scan.using(processor).from(macro).firstLine().keys(url,cacheSeed).parse(in);
        this.url = url.get();
        this.cacheSeed = cacheSeed.get();
    }

}
