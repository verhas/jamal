package javax0.jamal.io;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Copy implements Macro {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var from = Params.holder(null, "from").asString();
        final var to = Params.holder(null, "to").asString();
        final var append = Params.holder("io:append", "append").asBoolean();
        final var mkdir = Params.holder("io:mkdir", "mkdir").asBoolean();
        final var useCache = Params.holder("cache").asBoolean();
        final var overwrite = Params.holder("overwrite").asBoolean();

        Scan.using(processor).from(this).tillEnd().keys(from, to, append, mkdir, useCache,overwrite).parse(in);

        final var toName = Utils.getFile(to, in);
        final var fromName = Utils.getFile(from, in);
        final var f = new File(toName);
        if( f.exists() && !overwrite.is() ){
            return "";
        }
        if (mkdir.is()) {
            //noinspection ResultOfMethodCallIgnored
            f.getParentFile().mkdirs();
        }

        try (final var fos = new FileOutputStream(f, append.is())) {
            final var bytes = FileTools.getFileBinaryContent(fromName, !useCache.is(), processor);
            fos.write(bytes);
        } catch (IOException ioException) {
            throw new BadSyntax(String.format("There was an IOException copying the file '%s' to '%s'", fromName, toName), ioException);
        }
        return "";
    }

    @Override
    public String getId() {
        return "io:copy";
    }
}
