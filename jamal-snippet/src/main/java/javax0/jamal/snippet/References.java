package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Closer;
import javax0.jamal.api.Evaluable;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.ObjectHolder;
import javax0.jamal.api.Processor;
import javax0.jamal.api.Serializing;
import javax0.jamal.api.UserDefinedMacro;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static javax0.jamal.tools.InputHandler.isGlobalMacro;

public class References implements Macro {

    // snipline XREFS filter="(.*)"
    public static final String XREFS = "xrefs";

    // snipline REF_JRF filter="(.*)"
    public static final String REF_JRF = "ref.jrf";

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var file = Params.<String>holder("file").orElse(REF_JRF);
        final var holder = Params.<String>holder("holder").orElse(XREFS);
        Scan.using(processor).from(this).tillEnd().keys(file, holder).parse(in);
        final var id = holder.get();
        final var refHolder = new ReferenceHolder(id);
        if (isGlobalMacro(id)) {
            processor.defineGlobal(refHolder);
        } else {
            processor.define(refHolder);
        }
        final File xrefFile = new File(FileTools.absolute(in.getReference(), file.get()));
        final var closer = new ReferenceDumper(processor, xrefFile, refHolder);
        processor.deferredClose(closer);
        try {
            final var macrosSerialized = new String(Files.readAllBytes(Paths.get(xrefFile.toURI())), StandardCharsets.UTF_8).split("\n");
            final var deserializer = processor.newUserDefinedMacro("anything, not important", "", new String[0]);
            for (var macroSerialized : macrosSerialized) {
                if (macroSerialized.length() > 0) {
                    final var macro = deserializer.deserialize(macroSerialized);
                    processor.defineGlobal(macro);
                }
            }
        } catch (IOException e) {
            // when the file is not there, we just return a warning string  that gets into the place of the macro
            // telling whoever reads the document that the reference file was not there
            return String.format("WARNING The reference file %s was not found.", xrefFile.getAbsolutePath());
        }
        return "";
    }

    @Override
    public String getId() {
        return "references";
    }

    static class ReferenceHolder implements Evaluable, Identified, ObjectHolder<Set<String>> {
        private final Set<String> set = new HashSet<>();
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
        public String evaluate(final String... parameters) throws BadSyntax {
            return set.stream().collect(Collectors.joining(","));
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

        public ReferenceDumper(final Processor processor, final File file, final ReferenceHolder holder) {
            this.file = file;
            this.processor = processor;
            this.holder = holder;
        }


        @Override
        public void close() throws Exception {
            final var sb = new StringBuilder();
            final var missing = new ArrayList<String>();
            for (final var ref : holder.getObject()) {
                final var serialized = processor.getRegister()
                        .getUserDefined(ref)
                        .filter(macro -> macro instanceof UserDefinedMacro)
                        .map(macro -> (UserDefinedMacro) macro)
                        .map(Serializing::serialize);
                if (serialized.isPresent()) {
                    sb.append(serialized.get()).append('\n');
                } else {
                    missing.add(ref);
                }
            }
            BadSyntax.when(missing.size() > 0, "The following references are missing: " + String.join(", ", missing));
            Files.write(Paths.get(file.toURI()), sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        }
    }

}