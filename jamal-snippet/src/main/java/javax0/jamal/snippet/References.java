package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Scanner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;

import static javax0.jamal.tools.InputHandler.isGlobalMacro;

@Macro.Name("references")
public
class References implements Macro, Scanner.WholeInput {

    // snipline XREFS filter="(.*)"
    public static final String XREFS = "xrefs";

    // snipline REF_JRF filter="(.*)"
    public static final String REF_JRF = "ref.jrf";

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var file = scanner.str("file").defaultValue(REF_JRF);
        final var holder = scanner.str("holder").defaultValue(XREFS);
        final var comments = scanner.bool("jrf$comments", "comments", "comment");
        scanner.done();
        final var id = holder.get();
        final var refHolder = new ReferenceHolder(id);
        if (isGlobalMacro(id)) {
            processor.defineGlobal(refHolder);
        } else {
            processor.define(refHolder);
        }
        final File xrefFile = new File(FileTools.absolute(in.getReference(), file.get()));
        final var closer = new ReferenceDumper(processor, xrefFile, refHolder, comments.is());
        processor.deferredClose(closer);
        try {
            final var macrosSerialized = Files.readAllLines(Paths.get(xrefFile.toURI()));
            closer.addSerializedLines(macrosSerialized);
            final var deserializer = getDeserializerMacroObject(processor);
            for (var macroSerialized : macrosSerialized) {
                if (notComment(macroSerialized)) {// empty and comment lines are ignored
                    final var macro = deserializer.deserialize(macroSerialized);
                    if (macro instanceof Configurable) {
                        ((Configurable) macro).configure(Configurable.Keys.SOFT, true);
                    }
                    processor.defineGlobal(macro);
                }
            }
        } catch (IOException e) {
            // when the file is not there, we just return a warning string that gets into the place of the macro
            // telling whoever reads the document that the reference file was not there
            throw new IdempotencyFailed(String.format("The reference file %s was not found.", xrefFile.getAbsolutePath()));
        }
        return "";
    }

    private static boolean notComment(String macroSerialized) {
        return !macroSerialized.isEmpty() && macroSerialized.charAt(0) != '#';
    }

    /**
     * Create a macro object that will be used to deserialize the strings into macro objects.
     * This deserializer macro object will (should) NOT be registered in the macro registry, hence the name is not
     * interesting.
     * <p>
     * The deserialization could be a static method, but it is designed to be defined in the interface and thus
     * different user defined macro implementations can implement different serialization and deserialization.
     *
     * @param processor used for the processing of the macro
     * @return the macro object that will be used to deserialize the strings into macro objects
     * @throws BadSyntax if the macro object cannot be created
     */
    private static UserDefinedMacro getDeserializerMacroObject(Processor processor) throws BadSyntax {
        return processor.newUserDefinedMacro("_", "");
    }

    static class ReferenceHolder implements Evaluable, Identified, ObjectHolder<Set<String>> {
        private final SortedSet<String> set = new TreeSet<>();
        private final String id;

        public ReferenceHolder(final String id) {
            this.id = id;
        }

        @Override
        public Set<String> getObject() {
            return set;
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public String evaluate(final String... parameters) {
            return String.join(",", set);
        }

        @Override
        public int expectedNumberOfArguments() {
            return 0;
        }
    }

    private static class ReferenceDumper implements Closer, AutoCloseable {
        private final File file;
        private final Processor processor;
        private final ReferenceHolder holder;
        private final boolean comments;

        private String[] macrosSerialized;

        public ReferenceDumper(final Processor processor, final File file, final ReferenceHolder holder, boolean comments) {
            this.file = file;
            this.processor = processor;
            this.holder = holder;
            this.comments = comments;
        }


        void addSerializedLines(final List<String> serialized) {
            macrosSerialized = serialized.toArray(String[]::new);
        }

        @Override
        public void close() throws Exception {
            final var sb = new StringBuilder();
            if (comments) {
                sb.append("# This is a Jamal reference file containing serialized base64 encoded macros\n")
                        .append("# Created: ")
                        .append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").format(Calendar.getInstance().getTime()))
                        .append('\n')
                        .append("# id|openStr|closeStr|verbatim|tailParameter|pure|content|parameters\n");
            }
            final var missing = new ArrayList<String>();
            for (final var ref : holder.getObject()) {
                final var serialized = processor.getRegister()
                        .getUserDefined(ref)
                        .filter(macro -> macro instanceof UserDefinedMacro)
                        .map(macro -> (UserDefinedMacro) macro)
                        .map(Serializing::serialize);
                if (serialized.isPresent()) {
                    if (comments) {
                        sb.append("# ").append(ref).append('\n');
                    }
                    sb.append(serialized.get()).append('\n');
                } else {
                    missing.add(ref);
                }
            }
            Files.writeString(Paths.get(file.toURI()), sb.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            BadSyntax.when(!missing.isEmpty(), "The following references are missing: " + String.join(", ", missing));
            if (macrosSerialized != null) {
                final var diff = checkIdempotency(macrosSerialized, sb.toString().split("\n"));
                IdempotencyFailed.when(!diff.isEmpty(), "The following references are not idempotent: " + diff);
            }
        }

        /**
         * Check if the two sets of serialized macros are idempotent. The first set is the old set, and the second set is
         * the new set. The method returns a string that contains the list of the macros that are new, deleted or
         * changed.
         * <p>
         * The serialization set may contain empty and comment lines. These are ignored.
         *
         * @param oldSet the old set of serialized macros as string
         * @param newSet the new set of serialized macros as string
         * @return the list of the macros that are new, deleted or changed, essentially a difference
         * @throws BadSyntax if the deserialization of the serialized macro fails
         */
        private String checkIdempotency(String[] oldSet, String[] newSet) throws BadSyntax {
            final var diffMap = new HashMap<String, String[]>();
            final var deserializer = getDeserializerMacroObject(processor);
            for (final var line : oldSet) {
                if (notComment(line)) {
                    final var id = deserializer.deserialize(line).getId();
                    diffMap.put(id, new String[]{line, null});
                }
            }
            for (final var line : newSet) {
                if (notComment(line)) {
                    final var id = deserializer.deserialize(line).getId();
                    if (diffMap.containsKey(id)) {
                        diffMap.get(id)[1] = line;
                    } else {
                        diffMap.put(id, new String[]{null, line});
                    }
                }
            }
            final var err = new StringBuilder();
            for (final var e : diffMap.entrySet()) {
                if (e.getValue()[0] == null) {
                    err.append("macro '").append(e.getKey()).append("' is new\n");
                } else if (e.getValue()[1] == null) {
                    err.append("macro '").append(e.getKey()).append("' is deleted\n");
                } else if (!e.getValue()[0].equals(e.getValue()[1])) {
                    err.append("macro '").append(e.getKey()).append("' has changed\n");
                }
            }
            return err.toString();
        }
    }

}