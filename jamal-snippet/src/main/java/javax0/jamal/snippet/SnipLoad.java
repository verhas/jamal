package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Scan;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static javax0.jamal.tools.Params.holder;

public class SnipLoad implements Macro, InnerScopeDependent {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var ref = in.getReference();
        final var idRegex = holder("name", "id").orElse("").asString();
        final var fnRegex = holder("file", "fileName").orElse("").asString();
        final var textRegex = holder("text", "contains").orElse("").asString();
        final var input = holder("input").orElse("").asString();
        final var format = holder("format").orElse("XML").asString();
        Scan.using(processor).from(this).tillEnd().keys(idRegex, fnRegex, textRegex, input, format).parse(in);
        BadSyntax.when(!"XML".equals(format.get()), "The only supported format is XML");
        final var store = SnippetStore.getInstance(processor);
        final var is = new ByteArrayInputStream(FileTools.getFileContent(FileTools.absolute(ref, input.get()), processor).getBytes(StandardCharsets.UTF_8));
        SnippetXmlReader.getSnippetsFromXml(is,
                (id, content, position) -> storeSnippet(store,
                        convertRegex(idRegex.get()),
                        convertRegex(fnRegex.get()),
                        convertRegex(textRegex.get()),
                        id, content, position)
        );
        return "";
    }

    /**
     * Store the snippet in the store.
     *
     * @param store    the store
     * @param idTest   the test for the id. Store the snippet only if the id matches.
     * @param fnTest   the test for the file name. Store the snippet only if the file name matches.
     * @param textTest the test for the text. Store the snippet only if the text matches.
     * @param id       the id of the snippet
     * @param content  the textual content of the snippet
     * @param position the position of the snippet
     * @throws BadSyntax if the snippet cannot be stored, for example it is already in the store. You cannot overwrite
     *                   snippets.
     */
    private static void storeSnippet(SnippetStore store,
                                     Predicate<String> idTest,
                                     Predicate<String> fnTest,
                                     Predicate<String> textTest,
                                     String id,
                                     String content,
                                     Position position) throws BadSyntax {
        if (idTest.test(id) && fnTest.test(content) && textTest.test(content)) {
            store.snippet(id, content, position);
        }
    }

    /**
     * Convert a regex to a predicate. If the regex is null or empty then the predicate is always true.
     *
     * @param regex the regex
     * @return the predicate
     */
    private static Predicate<String> convertRegex(String regex) {
        return regex == null || regex.length() == 0 ? x -> true : Pattern.compile(regex).asPredicate();
    }

    @Override
    public String getId() {
        return "snip:load";
    }
}
