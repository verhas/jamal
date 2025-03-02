package javax0.jamal.snippet;

import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.HexDumper;
import javax0.jamal.tools.SHA256;

@Macro.Name("hashCode")
public
class HashCode implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) {
        return HexDumper.encode(SHA256.digest(in.toString()));
    }

}
