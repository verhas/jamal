package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.*;

import java.io.File;
import java.util.Map;

public class CacheMacro implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var cacheDirectory = Params.<String>holder("cacheDirectory", "dir", "directory").orElse(".cache");
        final var ttl = Params.<String>holder("cacheTtl", "ttl").orElse("9000y");
        final var key = Params.<String>holder(null, "key").orElseNull();
        final var file = Params.<String>holder(null, "file", "url").orElseNull();
        final var fileExt = Params.<String>holder(null, "ext", "extension").orElse(".cache");
        final var exists = Params.<String>holder(null, "exists", "query").asBoolean();
        Scan.using(processor).from(this).tillEnd().keys(cacheDirectory, ttl, key, file, fileExt, exists).parse(in);

        final var ext = fileExt.get().charAt(0) == '.' ? fileExt.get().substring(1) : fileExt.get();
        final String keyString = calculateKey(key, file);
        final var cacheDir = FileTools.absolute(in.getReference(), cacheDirectory.get());
        final var cacheFile = new File(cacheDir, String.format("%s.%s", keyString, ext));
        final var propertiesFile = new File(cacheDir, String.format("%s.properties", keyString));
        final var entry = new Cache.Entry(cacheFile, propertiesFile, true);

        if (exists.is()) return Boolean.toString(!entry.isMiss());

        if (entry.isMiss()) {
            BadSyntax.when(!file.isPresent(), "The file name must be specified when the cache is empty.");
            final var content = FileTools.getFileBinaryContent(file.get(), true, processor);
            entry.save(content, Map.of("ttl", ttl.get()));
        }
        return String.format("%s/%s.%s", cacheDir, keyString, ext);
    }

    /**
     * Calculate the key to be used for the cache. If the key is specified, then that is used. If the key is not
     * specified, then the file is used, and the SHA256 hash of the file name is calculated and that is used as the key.
     *
     * @param key  the optionally specified key
     * @param file the file name, which is mandatory
     * @return the key to be used for the cache
     * @throws BadSyntax if there is some error parsing the parameters
     */
    private String calculateKey(Params.Param<String> key, Params.Param<String> file) throws BadSyntax {
        if (key.isPresent()) {
            return key.get();
        }
        return HexDumper.encode(SHA256.digest(file.get()));
    }

    @Override
    public String getId() {
        return "cache";
    }
}
