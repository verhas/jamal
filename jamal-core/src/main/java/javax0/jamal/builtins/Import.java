package javax0.jamal.builtins;

import javax0.jamal.api.Macro;
import javax0.jamal.api.*;
import javax0.jamal.tools.HexDumper;
import javax0.jamal.tools.SHA256;
import javax0.jamal.tools.Scanner;

import java.util.*;

import static javax0.jamal.api.SpecialCharacters.*;
import static javax0.jamal.tools.FileTools.getInput;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

/**
 * Imports the macros from the file.
 * <p>
 * The file name follows the macro keyword `import`. If the file has already been imported at the current level or at
 * any higher level, it will not be imported again. This prevents the same macro file from being imported multiple times
 * unnecessarily. Furthermore, if the macro file was imported only at a deeper level than the current one, the macros
 * are not accessible at the current position (except for those that have been exported). Therefore, the file will be
 * imported again in such cases.
 * <p>
 * The output produced by the file is disregarded and discarded.
 * <p>
 * The class implements the {@link Stackable} interface, signaling the processor to execute the {@link #push()} and
 * {@link #pop()} methods when the macro context enters and exits a level, respectively. This allows the macro to
 * maintain a stack of the levels at which files have been imported.
 * <p>
 * If the imported file begins with the characters `{` and `@`, the import process temporarily sets the macro opening
 * and closing strings to `{` and `}`, respectively. This flexibility enables a macro file or an application to use any
 * macro opening and closing strings while still being able to import macros from web resources or JAR files. As the
 * creator of a JAR file or a macro package published online cannot anticipate the macro strings a user might choose,
 * it is advisable to use `{` and `@` to ensure a smooth import process.
 * <p>
 * Before version 1.5.0, importing such a file, assuming that the macro opening and closing strings were `[` and `]`,
 * required:
 *
 * <pre>{@code
 * [@sep { }]
 * {@import re:my_resource.jim}
 * {@sep}
 * }</pre>
 * <p>
 * Using 1.5.0 or later the code is
 * <pre>{@code
 * [@import re:my_resource.jim]
 * }</pre>
 */
@Macro.Stateful
public class Import implements Stackable, OptionsControlled.Core, Scanner.Core {

    private static class Hash {
        private final byte[] hash;

        private Hash(byte[] hash) {
            this.hash = hash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Hash hash1 = (Hash) o;
            return Arrays.equals(hash, hash1.hash);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(hash);
        }

        @Override
        public String toString() {
            return HexDumper.encode(hash);
        }
    }

    private final List<Set<Hash>> importedAlready = new ArrayList<>();

    /**
     * The constructor of this class is not implicit because it has to initialize the stack of the sets that keeps
     * track of the already imported files (one element of the set for the corresponding level of the stack).
     * When the application starts, the stack (implemented as an ArrayList) has to have one (empty set) element already.
     */
    public Import() {
        push();
    }

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        var position = input.getPosition();
        final var scanner = newScanner(input, processor);
        final var top = scanner.bool(null, "top");
        final var noCache = scanner.bool(null, "noCache");
        final var isolate = scanner.bool(null, "isolate", "isolated");
        final var inDirs = scanner.str(null, "in").optional();
        final var globalImport = scanner.bool(null, "global");
        final var force = scanner.bool("import$force", "force", "forced");
        scanner.done();
        position = Include.repositionToTop(position, top);
        final var prefixes = Include.getPrefixes(inDirs);
        skipWhiteSpaces(input);
        var fileName = input.toString().trim();
        final var in = getInput(prefixes, fileName, position, noCache.is(), processor);
        final var hash = new Hash(SHA256.digest(in.toString()));
        if (!wasImported(hash) || force.is()) {
            registerImport(hash, globalImport.is());
            final var weArePseudoDefault = processor.getRegister().open().equals("{") && processor.getRegister().close().equals("}");
            final var useDefaultSeparators = in.length() > 1 && in.charAt(0) == IMPORT_SHEBANG1 && in.charAt(1) == IMPORT_SHEBANG2 && !weArePseudoDefault;
            final Processor myProcessor;
            final Marker marker;
            if (isolate.is()) {
                myProcessor = processor.spawn();
                marker = null;
            } else {
                marker = processor.getRegister().test();
                myProcessor = processor;
            }
            if (useDefaultSeparators) {
                myProcessor.separators(IMPORT_OPEN, IMPORT_CLOSE);
                myProcessor.process(in);
                myProcessor.separators(null, null);
            } else {
                myProcessor.process(in);
            }
            if (isolate.is()) {
                final var topLevelScope = myProcessor.getRegister().debuggable()
                        .map(Debuggable.MacroRegister::getScopes)
                        .map(t -> t.get(0)).orElseThrow(() -> new BadSyntax("Internal error: cannot get top level scope, debugger is not implemented"));
                for (final var entry : topLevelScope.getUdMacros().entrySet()) {
                    processor.getRegister().define(entry.getValue(), entry.getKey());
                }
                for (final var entry : topLevelScope.getMacros().entrySet()) {
                    processor.getRegister().define(entry.getValue(), entry.getKey());
                }
            } else {
                processor.getRegister().test(marker);
            }
        }
        return "";
    }

    /**
     * Register that the file was imported. The file is registered on the current level. If the import is global then
     * the file is registered on the top level.
     *
     * @param fileName the name of the file to be registered
     * @param isGlobal if the import is global
     */
    private void registerImport(final Hash fileName, final boolean isGlobal) {
        if (isGlobal) {
            importedAlready.get(0).add(fileName);
        } else {
            importedAlready.get(importedAlready.size() - 1).add(fileName);
        }
    }

    /**
     * Check if the file was already imported. In case a file was already imported, it should not be processed
     * again. A file was imported if the import macro was invoked on this level or any level higher. In these cases, the
     * macros that were imported have the effect at the current level, and thus there is no reason to import the file
     * again. <p>
     * <p>
     * If the file was imported any level deeper then the macros are already purged (import should be performed on this
     * level to have the effect, macros defined on this level) and so are the elements of the list that keeps track of
     * the files imported on each level.<p>
     *
     * @param fileContentHash the hash code calculated from the file content.
     * @return {@code true} if the file was imported already, {@code false} otherwise.
     */
    private boolean wasImported(Hash fileContentHash) {
        for (int level = importedAlready.size() - 1; level > -1; level--) {
            var importSet = importedAlready.get(level);
            if (importSet.contains(fileContentHash)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void push() {
        importedAlready.add(new HashSet<>());
    }

    @Override
    public void pop() {
        importedAlready.remove(importedAlready.size() - 1);
    }
}
