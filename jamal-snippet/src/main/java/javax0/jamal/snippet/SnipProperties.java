package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.InputHandler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SnipProperties implements Macro {
    @Override
    public String getId() {
        return "snip:properties";
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        InputHandler.skipWhiteSpaces(in);
        final var reference = in.getReference();
        final var file = FileTools.absolute(reference, in.toString().trim());
        try (final InputStream is = new FileInputStream(file)) {
            final var properties = new Properties();
            if (file.endsWith(".xml")) {
                properties.loadFromXML(is);
            } else {
                properties.load(is);
            }
            final var pos = new Position(file);
            final var store = SnippetStore.getInstance(processor);
            for (final var e : properties.entrySet()) {
                store.snippet((String) e.getKey(), (String) e.getValue(), pos);
            }
            return "";
        } catch (IOException e) {
            throw new BadSyntax("Cannot read the properties file '" + file + "'", e);
        }
    }

}
