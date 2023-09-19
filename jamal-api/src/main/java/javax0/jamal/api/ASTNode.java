package javax0.jamal.api;

import java.util.List;

/**
 * The AST node is the node in the Abstract Syntax Tree that is created by the parser.
 * <p>
 * The parser is not used during the Jamal execution.
 * It is an auxiliary tool that can be used to create the AST to provide editor support, like LSP implementation.
 * The parsing was developed before the LSP implementation but with editor support in mind when preparing the
 * release 2.5.0.
 */
public interface ASTNode {
    Type getType();

    String getText();

    int getStart();

    int getEnd();

    ASTNode getNext();

    boolean hasNext();

    List<ASTNode> getChildren();

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
