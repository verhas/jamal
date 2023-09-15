package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Marker;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.engine.util.MacroBodyFetcher;
import javax0.jamal.tools.Input;
import javax0.jamal.tools.InputHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax0.jamal.api.SpecialCharacters.IDENT;
import static javax0.jamal.api.SpecialCharacters.NO_PRE_EVALUATE;
import static javax0.jamal.api.SpecialCharacters.POST_VALUATE;
import static javax0.jamal.api.SpecialCharacters.PRE_EVALUATE;

/**
 * Parse the input string and create an AST (Abstract Syntax Tree) from it.
 * <p>
 * The parsing is limited because it does not evaluate all the macros.
 * It may not work properly
 * <p>
 * <li> if the separator macro is invoked with calculated content, or
 * <li> if the separators are changed by some built-in macro other than the {@code sep} macro.
 * <p>
 * The parsing will treat the input of a built-in macro as a text, and it will not parse it if it is used with the
 * {@code @} character. If the built-in macro is used with the {@code #} character then the parsing will be done,
 * as it is done by Jamal itself and not by the macro implementation, like in the case of {@code eval}.
 * <p>
 * The parsing uses the {@link MacroBodyFetcher} to get the content of the macro. It means that macros like
 * {@code escape} (currently the only one in the distribution of Jamal implementing in its own parsing) will work
 * properly.
 */
public class Parser {

    /**
     * An Abstract Syntax Tree (AST) node contains a
     *
     * <li> type
     * <li> text of the node
     * <li> start position, and
     * <li> end position.
     * <p>
     * The start and end positions are the positions of the node in the input string.
     * In other ways it is always true that {@code input.substring(start,end).equals(text)}.
     * <p>
     * The type is defined in the enum {@link Type}.
     */
    public static class ASTnode {
        public final Type type;
        public final String text;
        public final int start, end;
        public final List<ASTnode> children = new ArrayList<>();

        public ASTnode(Type type, int start, String text) {
            this.type = type;
            this.text = text;
            this.start = start;
            this.end = start + text.length();
        }

        /**
         * Format the AST node as a string hierarchically.
         * It can be used for debugging purposes, and it is heavily used in unit tests.
         *
         * @return the formatted string
         */
        @Override
        public String toString() {
            return toString(0);
        }

        private String toString(int margin) {
            return String.format("%s%s[%s,%s] '%s'\n", "  ".repeat(margin), type, start, end, text) +
                    children.stream().map(c -> c.toString(margin + 1)).collect(Collectors.joining(""));
        }

        /**
         * The type of the node.
         * <p>
         * It can be one of the following:
         *
         * <li> {@link Type#LIST} - the node is a list of other nodes
         * <li> {@link Type#OPEN} - the node is a macro opening string
         * <li> {@link Type#CLOSE} - the node is a macro closing string
         * <li> {@link Type#ID} - the node is an identifier
         * <li> {@link Type#TEXT} - the node is a text
         * <li> {@link Type#PREFIX} - the node is a prefix
         * <li> {@link Type#BIMCHAR} - the node is a built-in macro character, either {@code @} or {@code #}
         */
        public enum Type {
            LIST, OPEN, CLOSE, ID, TEXT, PREFIX, BIMCHAR
        }
    }


    /**
     * Parse the input string and create an AST (Abstract Syntax Tree) from it.
     * <p>
     *
     * @param processor the processor that is used to evaluate some macros, and it also provides the up-to-date macro
     *                  opening and closing strings. Although this processor may be used to process the input after the
     *                  parsing, it is a non-tested use case. Also, using a processor used to process input before may
     *                  not properly work.
     * @param input     the input string
     * @return the root node of the AST, it will be a {@link ASTnode.Type#LIST} node
     * @throws BadSyntax if the input string is not a valid input.
     *                   Note that this method does not evaluate the macros, therefore, it may happen that the parsing
     *                   is successful, but the calling {@link Processor#process(javax0.jamal.api.Input)} will still
     *                   fail.
     */
    public static ASTnode parse(final Processor processor, final String input) throws BadSyntax {
        return parse(processor, Input.makeInput(input), 0);
    }

    private static ASTnode parse(final Processor processor, final Input in, int offset) throws BadSyntax {
        final var result = new ASTnode(ASTnode.Type.LIST, offset, in.toString());
        while (!in.isEmpty()) {
            final var openStart = in.indexOf(processor.getRegister().open());
            if (openStart == 0) {
                final var openNode = new ASTnode(ASTnode.Type.OPEN, in.getPosition().charpos + offset, processor.getRegister().open());
                result.children.add(openNode);
                in.delete(processor.getRegister().open().length());
                prefix(in, result.children, offset);
                final var inbefore = in.toString();
                space(in, result.children, offset);
                final int contentPos = in.getPosition().charpos;
                final var macro = Macro.getMacro(processor.getRegister(), in, 0);
                final var content = MacroBodyFetcher.getNextMacroBody(in, processor);
                final var contentInput = Input.makeInput(content);
                final boolean prevaluate;
                if (!contentInput.isEmpty() && (contentInput.charAt(0) == NO_PRE_EVALUATE || contentInput.charAt(0) == PRE_EVALUATE)) {
                    prevaluate = contentInput.charAt(0) == PRE_EVALUATE;
                    final var biNode = new ASTnode(ASTnode.Type.BIMCHAR, contentPos + offset, contentInput.charAt(0) + "");
                    result.children.add(biNode);
                    contentInput.delete(1);
                } else {
                    prevaluate = true;
                }
                final var idPos = contentInput.getPosition().charpos;
                final var id = InputHandler.fetchId(contentInput);
                final var idNode = new ASTnode(ASTnode.Type.ID, idPos + contentPos + offset, id);
                result.children.add(idNode);
                if (prevaluate) {
                    final var marker = newMarker();
                    processor.getRegister().push(marker);
                    final var contentNode = parse(processor, Input.makeInput(contentInput.toString()), contentInput.getPosition().charpos + contentPos + offset);
                    processor.getRegister().pop(marker);
                    if (!contentNode.children.isEmpty()) {
                        result.children.add(contentNode);
                    }
                } else {
                    final var contentNode = new ASTnode(ASTnode.Type.TEXT, contentInput.getPosition().charpos + contentPos + offset, contentInput.toString());
                    result.children.add(contentNode);
                }
                if (macro.isPresent() && macro.get().getId().equals("sep")) {
                    macro.get().evaluate(contentInput, processor);
                }
                if (inbefore.substring(content.length()).startsWith(processor.getRegister().close())) {
                    final var closeNode = new ASTnode(ASTnode.Type.CLOSE, contentPos + content.length() + offset, processor.getRegister().close());
                    result.children.add(closeNode);
                }
            } else {
                final var charpos = in.getPosition().charpos;
                final var text = openStart > 0 ? in.toString().substring(0, openStart) : in.toString();
                in.delete(openStart > 0 ? openStart : in.length());
                final var node = new ASTnode(ASTnode.Type.TEXT, charpos + offset, text);
                result.children.add(node);
            }
        }
        return result;
    }

    private static Marker newMarker() {
        return new Marker() {
            @Override
            public Position getPosition() {
                return null;
            }
        };
    }

    /**
     * Parse the prefix of the macro.
     * If there is any prefix character in front of the macro then create a node from it and add to the children list.
     *
     * @param input    that may contain one or more {@code !} and {@code `} characters.
     * @param children the list of nodes to add the prefix node to
     * @param offset   the offset where the poptional prefix characters start
     */
    private static void prefix(Input input, List<ASTnode> children, int offset) {
        int charpos = input.getPosition().charpos;
        final var sb = new StringBuilder();
        while (!input.isEmpty() && (input.charAt(0) == POST_VALUATE || input.charAt(0) == IDENT)) {
            sb.append(input.charAt(0));
            input.delete(1);
        }
        if (sb.length() > 0) {
            final var node = new ASTnode(ASTnode.Type.PREFIX, charpos + offset, sb.toString());
            children.add(node);
        }
    }

    /**
     * Parse the space characters in front of the macro.
     * If there is any space character in front of the macro, then create a node from it and add to the children list.
     *
     * @param input    that may contain one or more space characters.
     * @param children the list of nodes to add the space node to
     * @param offset   the offset where the optional space characters start
     */
    private static void space(Input input, List<ASTnode> children, int offset) {
        int charpos = input.getPosition().charpos;
        final var sb = new StringBuilder();
        while (!input.isEmpty() && Character.isWhitespace(input.charAt(0))) {
            sb.append(input.charAt(0));
            input.delete(1);
        }
        if (sb.length() > 0) {
            final var node = new ASTnode(ASTnode.Type.TEXT, charpos + offset, sb.toString());
            children.add(node);
        }
    }
}

