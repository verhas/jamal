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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;

public class Download implements Macro {


    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var artifact = Params.<String>holder(null, "artifact").orElse("jar");
        final var repos = Params.<String>holder("respositories", "repo", "repos").orElse("central");
        final var noDep = Params.<Boolean>holder(null, "noDependencies");
        final var local = Params.<String>holder(null, "local").orElse(null);
        Scan.using(processor).from(this).between("()").keys(artifact, repos, local, noDep).parse(in);

        final var type = ArtifactType.type(artifact.get());

        final var localPath = getLocalPath(local);

        final var reposArr = getRepos(repos);

        final var coordsString = in.toString().trim().split(":", 3);
        if (coordsString.length < 3) {
            throw new BadSyntax("artifact coordinates should be 'groupId:artifactId:version");
        }
        final var coords = new MavenCoordinates(coordsString[0], coordsString[1], coordsString[2]);
        try {
            new Downloader(localPath, reposArr).fetch(coords, Set.of(type), Set.of(Pom.DependencyScope.COMPILE));
        } catch (Exception e) {
            throw new BadSyntax("Cannot download " + in, e);
        }
        return "";
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
        return "maven:download";
    }
}
