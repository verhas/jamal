package javax0.jamal.snippet;

import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

@Macro.Name("snip:clear")
public
class Clear implements Macro {

    @Override
    public String evaluate(Input in, Processor processor) {
        SnippetStore.getInstance(processor).clear();
        return "";
    }
}
/*template jm_clear
{template |clear|snip:clear|clear the snippets|}
 */