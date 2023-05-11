package com.javax0.jamal.maven;

import org.apache.maven.plugin.MojoExecutionException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class NameTransformer {

    final String from;
    final String to;


    public NameTransformer(final String transform) throws MojoExecutionException {
        if (transform.length() > 0) {
            final var sep = transform.charAt(0);
            final var lastChar = transform.charAt(transform.length() - 1);
            if (lastChar != sep) {
                throw new MojoExecutionException("The transform parameter is not valid. It should be like '/from/to/'");
            }
            from = transform.substring(1, transform.indexOf(sep, 1));
            to = transform.substring(transform.indexOf(sep, 1) + 1, transform.lastIndexOf(sep));
        } else {
            from = "";
            to = "";
        }
    }

    public Path transform(final Path name) {
        return Paths.get(name.toString().replaceAll(from, to));
    }
}
