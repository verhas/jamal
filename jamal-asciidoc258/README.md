# Asciidoctor Version Incompatibility Hack


## Problem Statement

Asciidoctor's preprocessors are required to implement the `org.asciidoctor.extension.Preprocessor` abstract class.
From version `2.5.10` version to `3.0.0-alpha-1` the method signature of

```java
public abstract void process(Document document, PreprocessorReader reader);
```
changed to

```java
public abstract Reader process(Document document, PreprocessorReader reader);
```
A preprocessor cannot implement both incompatible methods.


## Solution Approach

The solution is to have two different versions of the preprocessor in two modules.
One module depends on the new version of the Asciidoc library, the other on the old version.
This Maven module implements the preprocessor for the old library.

The new version implements the preprocessor registering service `org.asciidoctor.jruby.extension.spi.ExtensionRegistry`.
It is invoked by Asciidoctor to register the preprocessor.
When this happens, the `ExtensionRegistry` implementation reflectively queries the `org.asciidoctor.extension.Preprocessor` abstract class.
It looks at the method `process()`pass:['] return type.

1. If it is not `void` then the Asciidoctor calling the extension is compatible with the new SPI.
2. If it is `void`, then the Asciidoctor calling the extension is compatible with the old SPI.

In the first case, the code registers the `JamalPreprocessor` class.
It implements the new SPI as well as the registering.

In the second case the code registers the `JamalPreprocessor258` class.
This is the implementation of the preprocessor for the old SPI implemented in this module.

The actual logic is implemented only once in the module `jamal-asciidoctor`.
The other module, `jamal-asciidoctor258` implements the `JamalPreprocessor258` class.
This class creates an instance of the `JamalPreprocessor` class and delegates to it.
The instantiation and the delegation are done using reflection.


## Dependency Structure

The Java class `JamalPreprocessor` plays two roles.

1. It is a preprocessor, but
1. It also acts as an ExtensionRegistry implementation that registers the preprocessors.

The module, containing this class is the actual module for the preprocessing.
It is packaged into a ZIP file that contains all the dependency JARs as well as the module itself.
We need to have the module `jamal-asciidoctor258` artifact in the package.
Therefore, it is added to the module `jamal-asciidoctor` as a dependency.

The dependency also makes it possible for the module `jamal-asciidoctor` to return the class `JamalPreprocessor258` when it is needed for registering.

## Summary and Roadmap

This module is a patch to the terrible decision of the Asciidoctor developers to change the SPI in an incompatible way.
It is a temporary solution and will be deprecated in a few years as the AsciiDoctor library 2.X.X versions will be phased out.
