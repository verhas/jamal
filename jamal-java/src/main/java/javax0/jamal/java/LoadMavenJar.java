package javax0.jamal.java;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;
import javax0.maventools.download.ArtifactType;
import javax0.maventools.download.Downloader;
import javax0.maventools.download.MavenCoordinates;
import javax0.maventools.download.Pom;
import javax0.maventools.download.Repo;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;

public class LoadMavenJar implements Macro {

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var repos = Params.<String>holder("respositories", "repo", "repos").orElse("central");
        final var noDep = Params.<Boolean>holder(null, "noDependencies","noDeps").asBoolean();
        final var reload = Params.<Boolean>holder(null, "reload", "overwrite", "update").asBoolean();
        final var local = Params.<String>holder(null, "local").orElse(null);
        Scan.using(processor).from(this).between("()").keys(repos, noDep, reload, local).parse(in);


        final var localPath = getLocalPath(local);

        final var reposArr = getRepos(repos);

        final var coordsString = in.toString().trim().split(":", 3);
        if (coordsString.length < 3) {
            throw new BadSyntax("artifact coordinates should be 'groupId:artifactId:version");
        }
        final var coords = new MavenCoordinates(coordsString[0], coordsString[1], coordsString[2]);
        try {
            final var files = new Downloader(localPath, reposArr).fetch(coords, Set.of(ArtifactType.JAR), Set.of(Pom.DependencyScope.COMPILE));
            final var urls = new URL[files.length];
            int i = 0;
            for (final var f : files) {
                urls[i++] = f.toURI().toURL();
            }
            if (reload.is()) {
                loadAllMacros(urls, processor);
            } else  {
                loadNewMacros(urls, processor);
            }
            return "";
        } catch (Exception e) {
            throw new BadSyntax("Cannot download " + in, e);
        }
    }

    private static class MyUrlClassLoader extends URLClassLoader {
        public MyUrlClassLoader(URL[] urls) {
            super(urls, Thread.currentThread().getContextClassLoader());
        }

        public void addURL(URL url) {
            super.addURL(url);
        }
    }

    private void loadNewMacros(final URL[] files, final Processor processor) {
        final var cl = new MyUrlClassLoader(files);
        final var macros = Macro.getInstances(cl);
        final var register = processor.getRegister();
        for (final var macro : macros) {
            if (register.getMacro(macro.getIds()[0]).isEmpty()) {
                processor.getRegister().define(macro);
            }
        }
    }

    private void loadAllMacros(final URL[] files, final Processor processor) {
        final var cl = new MyUrlClassLoader(files);
        Macro.getInstances(cl).forEach(processor.getRegister()::define);
    }

    private Repo[] getRepos(final Params.Param<String> repos) throws BadSyntax {
        final var lines = repos.get().split("\n");
        final Repo[] reps = new Repo[lines.length];
        int i = 0;
        for (final var line : lines) {
            final var name = line.trim();
            final var repo = Arrays.stream(Repo.REPOS).filter(r -> r.name.equals(name)).findFirst();
            reps[i++] = repo.orElseGet(() -> new Repo("", name));
        }
        return reps;
    }

    private static Path getLocalPath(final Params.Param<String> local) throws BadSyntax {
        final Path localPath;
        if (local.isPresent() && local.get() != null) {
            localPath = Paths.get(local.get());
        } else {
            localPath = Paths.get(System.getProperty("user.home") + "/.m2/repository/");
        }
        return localPath;
    }

    @Override
    public String getId() {
        return "maven:load";
    }
}
