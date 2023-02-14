package javax0.jamal.maven.load;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.EnvironmentVariables;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.engine.NullMacro;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Option;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;
import javax0.maventools.download.ArtifactType;
import javax0.maventools.download.Downloader;
import javax0.maventools.download.MavenCoordinates;
import javax0.maventools.download.Pom;
import javax0.maventools.download.Repo;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class LoadMavenJar implements Macro {

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var repos = Params.<String>holder("repositories", "repository", "repo", "repos").orElse("central");
        final var noDep = Params.<Boolean>holder("noDependencies", "noDeps").asBoolean();
        final var reload = Params.<Boolean>holder("reload", "overwrite", "update").asBoolean();
        final var local = Params.<String>holder("local").orElse(null);
        final var exclude = Params.<String>holder("exclude").orElse(null);
        Scan.using(processor).from(this).between("()").keys(repos, noDep, reload, local, exclude).parse(in);

        final Properties properties = configuration.get();
        final var localPath = getLocalPath(local, in.getPosition(), properties);

        final var reposArr = getRepos(repos, properties);

        final MavenCoordinates coords = getMavenCoordinates(in);
        checkPermissions(coords, properties, in.getPosition());
        final var downloader = new Downloader(localPath, reposArr);
        final var fn = Optional.ofNullable(properties.get("mvn.load.exclude")).map(Object::toString).orElse(null);
        if (fn != null) {
            final var excludes = getStrings(fn);
            downloader.exclude(excludes.toArray(String[]::new));
        }
        if (exclude.isPresent()) {
            final var excludes = exclude.get().split(",");
            downloader.exclude(excludes);
        }
        try {
            final File[] files;
            if (noDep.is()) {
                files = new File[]{downloader.download(coords, ArtifactType.JAR)};
            } else {
                files = downloader.fetch(coords, Set.of(ArtifactType.JAR), Set.of(Pom.DependencyScope.COMPILE));
            }
            final var urls = new URL[files.length];
            int i = 0;
            for (final var f : files) {
                urls[i++] = f.toURI().toURL();
            }
            if (reload.is()) {
                loadAllMacros(urls, processor);
            } else {
                loadNewMacros(urls, processor);
            }
            return "";
        } catch (Exception e) {
            throw new BadSyntax("Cannot download " + in, e);
        }
    }

    private static MavenCoordinates getMavenCoordinates(final Input in) throws BadSyntax {
        final var c = in.toString().trim().split(":", 3);
        if (c.length < 3) {
            throw new BadSyntax("artifact coordinates should be 'groupId:artifactId:version");
        }

        return new MavenCoordinates(c[0], c[1], c[2]);
    }

    static Supplier<Properties> configuration = LoadMavenJar::getConfiguration;

    private static Properties getConfiguration() {
        final Properties properties;
        try {
            properties = EnvironmentVariables.getNewProperties();
        } catch (IOException e) {
            // this is NOT BadSyntax because it is not a Jamal source issue, and it must not be caught using the 'try' macro
            // this is a hard-core configuration error, which may signal security breach
            throw new RuntimeException(e);
        }
        return properties;
    }

    /**
     * This class loader delegates in reverse. It looks first up the class and calls the parent class loader only if the
     * class is not found. This way the dynamically loaded macros can override
     * the already loaded macros when their implementation is different from the one already loaded.
     * <p>
     * Since the service loader uses the Macro class loaded by the old class loader, which is the parent class loader of
     * this loader, whenever we find a class from the package {@code javax0.jamal.api} we load it with the parent class.
     * <p>
     * The same is true for the classes of the Jamal engine and Tools.
     * An implemented macro gets {@code Input} and {@code Processor} as parameters. These are from the API package,
     * but there is no reason to reload them. It just may cause problem, if the macro package was compiled with a different
     * version of Jamal. Filtering all these packages out is the safest way to avoid problems. There still may be some issues
     * when the macro dynamically loaded was compiled with a different version of Jamal than the one that is running, but
     * only if the two versions are not binary compatible.
     */
    private static class ChildFirstUrlClassLoader extends URLClassLoader {
        public ChildFirstUrlClassLoader(URL[] urls) {
            super(urls, Thread.currentThread().getContextClassLoader());
        }

        private static final Class<?>[] CLASS_EXCEPTIONS = {Macro.class, NullMacro.class, Option.class};

        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            for (final var klass : CLASS_EXCEPTIONS) {
                if (name.startsWith(klass.getPackageName())) {
                    return super.loadClass(name, resolve);
                }
            }
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                try {
                    c = findClass(name);
                } catch (ClassNotFoundException ignore) {
                    // we cannot call it via super.getParent() because it is protected
                    // --> when the class is not found it also calls findClass() again and the fails
                    c = super.loadClass(name, resolve);
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }


    private void loadNewMacros(final URL[] files, final Processor processor) {
        final var cl = new ChildFirstUrlClassLoader(files);
        final var macros = Macro.getInstances(cl);
        final var register = processor.getRegister();
        for (final var macro : macros) {
            if (register.getMacro(macro.getIds()[0]).isEmpty()) {
                processor.getRegister().define(macro);
            }
        }
    }

    private void loadAllMacros(final URL[] files, final Processor processor) {
        final var cl = new ChildFirstUrlClassLoader(files);
        final var macros = Macro.getInstances(cl);
        final var register = processor.getRegister();
        for (final var macro : macros) {
            final var oldMacro = register.getMacro(macro.getIds()[0]);
            if (oldMacro.isEmpty() || macro.getClass().getClassLoader() == cl) {
                processor.getRegister().define(macro);
            }
        }
    }

    private Repo[] getRepos(final Params.Param<String> repos, final Properties properties) throws BadSyntax {
        final var lines = repos.get().split("\n");
        final Repo[] reps = new Repo[lines.length];
        int i = 0;
        for (final var line : lines) {
            final var name = line.trim();
            reps[i++] = Arrays.stream(Repo.REPOS)
                    .filter(r -> r.name.equals(name))
                    .findFirst()
                    .orElseGet(() -> new Repo("", checkPermissions(name, properties)));
        }
        return reps;
    }

    private String checkPermissions(final String url, final Properties properties) {
        final var paths = Optional.ofNullable(properties.get("maven.load.repo")).map(Object::toString).orElse(null);
        if (paths == null) {
            throw new IllegalStateException("There is no 'maven.load.repos' property in the configuration.\n" +
                    "This property must be set to a comma separated list of urls of the permitted repositories.\n");
        }
        if (getStrings(paths).noneMatch(p -> p.equals(url))) {
            throw new IllegalStateException(String.format("The repo '%s' is not permitted.", url));
        }
        return url;
    }


    private Path getLocalPath(final Params.Param<String> local, final Position pos, final Properties properties) throws BadSyntax {
        final Path localPath;
        if (local.isPresent() && local.get() != null) {
            final var fn = FileTools.absolute(pos.file, local.get());
            localPath = Paths.get(fn);
            checkPermissions(localPath, properties);
        } else {
            localPath = Paths.get(FileTools.adjustedFileName("~/.m2/repository/"));
        }
        return localPath;
    }

    private void checkPermissions(final Path local, final Properties properties) {
        final var paths = Optional.ofNullable(properties.get("maven.load.local")).map(Object::toString).orElse(null);
        if (paths == null) {
            throw new IllegalStateException("There is no 'maven.load.local' property in the configuration.\n" +
                    "This property must be set to a comma separated list of absolute path pointing to the allowed local repositories.\n");
        }
        if (getStrings(paths).noneMatch(p -> p.equals(local.toFile().getAbsolutePath()))) {
            throw new IllegalStateException(String.format("The local repo '%s' is not allowed.", local));
        }
    }

    private static class MavenCoordinatesPattern extends MavenCoordinates {
        final String path;

        public static MavenCoordinatesPattern fromString(final String coordinates) {
            final var s = coordinates.split(":", 4);
            if (s.length != 3 && s.length != 4) {
                throw new IllegalArgumentException(coordinates + " is not a valid maven coordinate, must have two : in it.");
            }
            if (s.length == 3) {
                return new MavenCoordinatesPattern(s[0], s[1], s[2], "");
            } else {
                return new MavenCoordinatesPattern(s[0], s[1], s[2], s[3]);
            }
        }

        public MavenCoordinatesPattern(final String groupId, final String artifactId, final String version, final String path) {
            super(groupId, artifactId, version);
            this.path = path;
            if (groupId.equals("*")) {
                throw new IllegalArgumentException("The the group id must not be '*'");
            }
            if (artifactId.equals("*") && !version.equals("*")) {
                throw new IllegalArgumentException("The the artifact id must not be '*' if the version is not '*'");
            }
        }

        public boolean matches(final MavenCoordinates coords, final Position position) {
            for( var pos = position ; pos != null ; pos = pos.parent) {
                if (matches(coords, pos.file)) {
                    return true;
                }
            }
            return false;
        }
        public boolean matches(final MavenCoordinates coords, final String path) {
            Objects.requireNonNull(coords);
            if (!(Objects.equals(groupId, coords.groupId) &&
                    (artifactId.equals("*") || artifactId.equals(coords.artifactId)) &&
                    (version.equals("*") || version.equals(coords.version)))) {
                return false;
            }
            if (this.path == null || this.path.isEmpty()) {
                return true;
            }
            if (this.path.endsWith("/")) {
                return path.startsWith(this.path);
            }
            return Objects.equals(path, this.path);
        }
    }

    /**
     * Check that the permissions allow the download of the given coordinates.
     *
     * @param coords     the download artifact coordinates
     * @param properties the configuration loaded
     * @param position   the position of the top level file that has uses the macro or, which includes/imports a file that
     *                   uses the macro, or includes a file that includes a file that...
     */
    private void checkPermissions(final MavenCoordinates coords, final Properties properties, final Position position) {
        var pos = position;
        while (pos.parent != null) {
            pos = pos.parent;
        }
        final var top = pos;
        final var includes = Optional.ofNullable(properties.get("maven.load.include")).map(Object::toString).orElse(null);
        if (includes == null) {
            throw new IllegalStateException("There is no 'maven.load.include' property in the configuration.\n" +
                    "This property must be set to a comma separated list of maven coordinates that are allowed to be downloaded or a file name.\n");
        }
        if (getStrings(includes).map(MavenCoordinatesPattern::fromString).noneMatch(p -> p.matches(coords, top.file))) {
            throw new IllegalStateException(String.format("The maven artifact '%s' is not included.", coords));
        }
        final var excludes = Optional.ofNullable(properties.get("maven.load.exclude")).map(Object::toString).orElse(null);
        if (excludes != null && getStrings(excludes).map(MavenCoordinatesPattern::fromString).anyMatch(p -> p.matches(coords, position))) {
            throw new IllegalStateException(String.format("The maven artifact '%s' is excluded.", coords));
        }
    }

    /**
     * Get the strings from the configuration. The strings can be either a comma separated list of strings or a file
     * name that contains the strings. The file name must be relative and in the ~/.jamal/ directory. Also, the file
     * has to be safe, as checked by {@link EnvironmentVariables#assertFileSafe(Path)}.
     *
     * @param fn the configuration property
     * @return the stream of patterns
     * @throws IllegalStateException if the configuration is in a separate file and the file is not safe
     */
    private static Stream<String> getStrings(final String fn) {
        try {
            final Stream<String> lines;
            final var path = Paths.get(FileTools.adjustedFileName("~/.jamal/" + fn));
            if (Files.exists(path)) {
                if (fn.contains("/") || fn.contains("\\")) {
                    throw new IllegalStateException("The file name for the include patterns must be in the ~/.jamal directory.");
                }
                EnvironmentVariables.assertFileSafe(path);
                lines = Stream.of(Files.readAllLines(path).toArray(new String[0]));
            } else {
                lines = Stream.of(fn.split(","));
            }
            return lines;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getId() {
        return "maven:load";
    }
}
