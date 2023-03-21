package javax0.jamal.openai;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.io.File;

/**
 * This class holds the options for the low level APIs
 */
public class Options {

    /**
     * The URL of the service to call
     */
    final String url;

    /**
     * The seed to use for the cache identifier.
     * When the seed is an arbitrary string, and it is used while calculating the hash identifier.
     * The case identifier is used as a file name.
     * The cache identifier is a hash value calculated from the parameters of the request and also the seed.
     * <p>
     * There is no automatic cache evision. When the seed is changed, the cache is invalidated.
     */
    final String cacheSeed;


    /**
     * When the local flag is set to true, the cache file is created in the directory where the input files is.
     * It can be used to deliver the response from openAI into the package and create a version that is sealed, and
     * does not need to be downloaded again.
     */
    final boolean local;

    /**
     * This flag tells the macro that the response must be treated as sealed and the openAI server must not be consuted.
     * If the cache file either local or the central is not found then an error is to be thrown.
     */
    final boolean sealed;

    /**
     * The hash value of the result. When this hash is set and is not the same as the
     */
    final String hash;

    final File top;

    /**
     * Create a new object and parse the options from the input.
     *
     * @param processor used to process the options
     * @param in the input
     * @param macro used by the underlying processor to report error messages
     * @throws BadSyntax if there is some error with the options
     */
    public Options(Processor processor, Input in, Macro macro) throws BadSyntax {
        final var url = Params.<String>holder("url").asString();
        final var cacheSeed = Params.<String>holder("seed").asString();
        final var local = Params.<Boolean>holder("openai:local", "local").asBoolean();
        final var sealed = Params.<Boolean>holder("openai:sealed", "sealed").asBoolean();
        final var hash = Params.<String>holder("hash").orElse(null);

        Scan.using(processor).from(macro).firstLine().keys(url, cacheSeed, local, sealed, hash).parse(in);
        this.url = url.get();
        this.cacheSeed = cacheSeed.get();
        this.local = local.is();
        this.sealed = sealed.is();
        this.hash = hash.isPresent() ? hash.get().trim() : null;
        final String top = in.getPosition().top().file;
        this.top = new File(top == null ? "." : top);
    }
}