# Jamal Maven Plugin

Jamal maven plugin is a Maven plugin that executes Jamal for several files in the project directory and
creates output files processing the Jamal macros.

You can use the plugin even without any Maven project just executing the command line

```
mvn com.javax0.jamal:jamal-maven-plugin:1.0.2:jamal
```

(Note that `1.0.2` should be replaced with the actual version.)

The below command line can be used to create the `pom.xml` file(s) from the `pom.xml.jam` files in the root directory
and recursively under the modules directories.
```
mvn  -DfilePattern=.*pom\.xml\.jam$ com.javax0.jamal:jamal-maven-plugin:1.0.2:jamal
```

You can download from the `null.pom` file from https://raw.githubusercontent.com/verhas/jamal/master/null.pom
to add it to the actual project and use the `-f null.pom` so that you can run the command even when the `pom.xml`
does not exist or even was ruined because you make some mistake editing the `pom.xml.jam`.

```
mvn  -f null.pom -DfilePattern=.*pom\.xml\.jam$ com.javax0.jamal:jamal-maven-plugin:1.0.2:jamal
```

Another possibility is to download the `genpom.xml` file from
https://raw.githubusercontent.com/verhas/jamal/master/genpom.xml and use this in the command line:

```
mvn  -f genpom.xml clean
```

This pom file configures the Maven Jamal plugin to process the `pom.xml.jam` files and this is attached to the
`clean` phase. The `clean` phase was selected because it is much shorter than `generate-sources` or `generate-resources`
and it seems to be a good idea to recompile everything afterwards when the pom file has changed.

This can be used starting with version `1.0.2`.

If there is no `pom.xml` for Maven to process then the plugin will search for all files that are in the
current directory or in any subdirectory and have the extension `.jam`. When processing these files
the macro opening string will be `{` and the macro closing string will be `}`. (Note that it is only a convention
in Jamal and Jamal itself does not have any default value. These default values, in this case, are set by
the plugin.) The processed files will be created in the same directory where the source file is with the
`.jam` extension chopped off.

If you have an actual Maven project then you can configure the plugin in the `pom.xml`.

```xml
<plugin>
    <groupId>com.javax0.jamal</groupId>
    <artifactId>jamal-maven-plugin</artifactId>
    <version>1.0.1</version>
    <configuration>
        <transformFrom>\.jam$</transformFrom>
        <transformTo>.jamo</transformTo>
        <filePattern>.*\.jam$</filePattern>
        <exclude>target|\.iml$|\.java$|\.xml$</exclude>
        <sourceDirectory>${project.basedir}/</sourceDirectory>
        <targetDirectory>${project.build.outputDirectory}</targetDirectory>
        <macroOpen>{</macroOpen>
        <macroClose>}</macroClose>
    </configuration>
</plugin>
```
(Note that `1.0.0` should be replaced with the actual version.)

The configuration values are the following:

* `sourceDirectory` defines the source directory. The default is the project source directory.
* `targetDirectory` defines the target directory where the generated files after Jamal processing are written.
                    the default value is the `target` directory.
* `filePattern`     can define a regular expression that has to match the file names to be processed. The default
                    value is `\.jam$`, which means that the files that have the extension `.jam` are processed
                    only. If this configuration value is empty then all files are processed, which are not excluded
                    by the regular expression defined in the `exclude` configuration parameter. 
* `exclude`         can define a regular expression. Any file with a name that matches the regular expression will
                    be excluded from the processing. The default value is an empty string which also means that no file
                    is excluded from the processing
* `transformFrom`   can define a regular expression, which is used to create the output file name from the
                    input file name together with the configuration parameter `transformTo`. When a source
                    file is processed with the name `sourceDirectory/ ... some path /filename` then the output
                    file will be `(targetDirectory/ ... some path /filename).replaceAll(transformFrom, transformTo)`
                    The path and the file name will be all the same as in the input, but instead of the source
                    directory the output file will be generated in the `targetDirectory` and under the target
                    directory in a subdirectory that matches the subdirectory of the source under the
                    `sourceDirectory`. The file name and the whole path as a string is transformed using the
                    Java String method `replaceAll()`. The default value for `transformFrom` is `\.jam$` and 
                    the default value for `tranfromTo` is empty string that means that the `.jam` extension
                    from the end of the file name will be removed.  
* `transformTo`     specifies what to replace the string in the file name that was matched by `transformFrom`.
                    for further information see the documentation above about `transformFrom`.
* `macroOpen`       can define the macro opening string. The default value is the conventional `{` one character
                    string.
* `macroClose`      can define the macro closing string. The default value is the conventional `}` one character
                    string.
                    
Note that when you define a regular expression in the `pom.xml` file you should not duplicate the `\`
escape character.