package javax0.jamal.maven.input;

import javax0.jamal.api.EnvironmentVariables;
import javax0.jamal.api.ResourceReader;
import javax0.jamal.tools.FileTools;
import javax0.maventools.download.ArtifactType;
import javax0.maventools.download.Downloader;
import javax0.maventools.download.MavenCoordinates;
import javax0.maventools.download.Pom;
import javax0.maventools.download.Repo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * MavenInput can read a file which is inside an archive file in a Maven repository.
 * <p>
 * The format of the reference is
 *
 * <pre>
 *     maven:groupId:artifactId:version:classifier:fileName
 * </pre>
 * <p>
 * or
 *
 * <pre>
 *     maven:groupId:artifactId:version::fileName
 * </pre>
 * <p>
 * When there is no classifier then the actual resource is consulted only.
 * <p>
 * If there is a classifier then the dependency tree is followed to find the resource.
 * In this latter case all the dependencies and resources are downloaded, even if the resource can be found in the actual artifact.
 */
public class MavenInput implements ResourceReader {

    private static class ResourceCoordinates {
        final String groupId;
        final String artifactId;
        final String version;
        final String classifier;
        final String fileName;

        ResourceCoordinates(String url) {
            final var parts = url.split(":", 6);
            if (parts.length != 6) {
                throw new IllegalArgumentException("The Maven coordinate must be 'maven:groupId:artifactId:version:classifier:fileName'");
            }
            groupId = parts[1].trim();
            artifactId = parts[2].trim();
            version = parts[3].trim();
            classifier = parts[4].trim();
            fileName = parts[5].trim();
        }
    }

    private final String PREFIX = "maven:";
    private final int PREFIX_LENGTH = PREFIX.length();

    @Override
    public boolean canRead(final String fileName) {
        return fileName.startsWith(PREFIX);
    }

    private static final String JAMAL_LOCAL_REPO_ENV = "JAMAL_LOCAL_REPO";
    private static final String JAMAL_REMOTE_REPOS_ENV = "JAMAL_REMOTE_REPOS";

    private static final Path LOCAL_REPO;
    private static final Repo[] REMOTE_REPOS;


    static {
        var localRepo = EnvironmentVariables.getenv(JAMAL_LOCAL_REPO_ENV).orElse(FileTools.adjustedFileName("~/.m2/repository"));
        LOCAL_REPO = Paths.get(localRepo);
        REMOTE_REPOS = Arrays.stream(EnvironmentVariables.getenv(JAMAL_REMOTE_REPOS_ENV).orElse("central").split(",")) //
                .map(name ->
                        Arrays.stream(Repo.REPOS)
                                .filter(r -> r.name.equals(name))
                                .map(r -> r.url)
                                .findFirst()
                                .orElse(name))
                .map(name -> new Repo("", name))
                .toArray(Repo[]::new);
    }

    private static final Downloader downloader = new Downloader(LOCAL_REPO, REMOTE_REPOS);

    @Override
    public int fileStart(final String fileName) {
        int count = 0;
        for (int i = 0; i < fileName.length(); i++) {
            if (fileName.charAt(i) == ':') {
                count++;
                if (count == 5) {
                    return i + 1;
                }
            }
        }
        return -1;
    }

    @Override
    public String read(final String fileName) throws IOException {
        return read(fileName, false);
    }

    @Override
    public String read(final String fileName, final boolean noCache) throws IOException {
        try {
            final var coor = new ResourceCoordinates(fileName);
            final Set<Pom.DependencyScope> classifiers;
            if( coor.classifier.length() == 0 ){
                classifiers = Set.of();
            } else {
                classifiers = Arrays.stream(coor.classifier.split(",")).map(c -> Pom.DependencyScope.fromString(c.trim())).collect(Collectors.toSet());
            }

            final File[] files;
            if (classifiers.isEmpty()) {
                files = new File[1];
                files[0] = new Downloader(LOCAL_REPO, REMOTE_REPOS).download(new MavenCoordinates(coor.groupId, coor.artifactId, coor.version), ArtifactType.JAR);
            } else {
                files = new Downloader(LOCAL_REPO, REMOTE_REPOS).fetch(new MavenCoordinates(coor.groupId, coor.artifactId, coor.version), Set.of(ArtifactType.JAR), classifiers);
            }
            for (final var file : files) {
                try( final var  jarFile = new JarFile(file)){
                    final var entry = jarFile.getJarEntry(coor.fileName);
                    if (entry != null) {
                        return new String(jarFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8);
                    }
                }
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
        throw new IOException("The file '" + fileName + "' was not found in the Maven archives");
    }
}
