package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Debuggable;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Marker;
import javax0.jamal.api.OptionsControlled;
import javax0.jamal.api.Processor;
import javax0.jamal.api.Stackable;
import javax0.jamal.tools.HexDumper;
import javax0.jamal.tools.SHA256;
import javax0.jamal.tools.Scanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax0.jamal.api.SpecialCharacters.IMPORT_CLOSE;
import static javax0.jamal.api.SpecialCharacters.IMPORT_OPEN;
import static javax0.jamal.api.SpecialCharacters.IMPORT_SHEBANG1;
import static javax0.jamal.api.SpecialCharacters.IMPORT_SHEBANG2;
import static javax0.jamal.tools.FileTools.getInput;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

/**
 * Imports the macros from the file.
 * <p>
 * The file name is following the macro keyword {@code import}. If the file was already imported on the actual level or
 * some higher level then the file will not be imported again. This is to ensure that the same macro file will not be
 * imported uselessly more than once. Also if the macro file was imported on a level only that is deeper than the
 * current level then the macros are not available at the current position (except those that were exported) and
 * therefore will be imported again.
 * <p>
 * The output generated from the file is ignored and is thrown away.
 * <p>
 * The class implements {@link Stackable}. This will signal the processor to invoke {@link #push()} and {@link #pop()}
 * <p>
 * when the macro context opens and closes one level. That way the macro can keep track of the level of already imported
 * files in it's own stack.
 * <p>
 * If the imported file starts with the characters { and @ then the import will set the macro opening and closing
 * strings to { and } for the time of the import. This way a macro file or an application can freely use any macro
 * opening and closing string and still can import macros defined in a web resource or in a JAR resource. The author of
 * a JAR file or a macro package published on the net cannot know what macro strings the user will use. It is
 * recommended to use { and @, and then the import is seamless.
 * <p>
 * Prior to version 1.5.0 importing such a file needed, assuming that {@code ((} and {@code ))} are the opening and
 * closing macro strings:
 *
 * <pre>{@code
 * ((@sep { }))
 * {@import re:my_resource.jim}
 * {@sep}
 * }</pre>
 * <p>
 * Using 1.5.0 or later the code is
 * <pre>{@code
 * ((@import re:my_resource.jim))
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
        final var inDirs = scanner.str(null, "in");
        final var globalImport = scanner.bool(null, "global");
        scanner.done();
        position = Include.repositionToTop(position, top);
        final var prefixes = Include.getPrefixes(inDirs);
        skipWhiteSpaces(input);
        var fileName = input.toString().trim();
        final var in = getInput(prefixes, fileName, position, noCache.is(), processor);
        final var hash = new Hash(SHA256.digest(in.toString()));
        if (!wasImported(hash)) {
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
