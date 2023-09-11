package javax0.jamal.api;

import java.util.List;
import java.util.function.Supplier;

/**
 * An abstract syntax tree or a node of it.
 * <p>
 * An element can be
 *
 * <li> macro opening,
 * <li> macro closing,
 * <li> text,
 * <li> list,
 * <li> id (identifier),
 * <li> or a macro.
 * <p>
 * The type can be queried calling the method {@code type()}.
 * <p>
 * In addition to that, the node has a start and end position in the input.
 * The start position points at the first character of the text in the input.
 * The end position points at the first character after the text in the input.
 * <p>
 * It is the same as the {@link String#substring(int, int)} method.
 * <p>
 * These can be queried calling the methods {@code start()} and {@code end()}.
 * <p>
 * When an AST node has children then these can be queried calling the method {@code children()}.
 * <p>
 * The factory methods {@code newNode()} create new nodes. The factory methods are overloaded and used during the
 * creation of the tree.
 * <p>
 * There is no separate AST tree node.
 * The tree is represented by the root node.
 */
public interface ASTNode {
    enum Type {
        open, close, macro, text, list, id
    }

    Type type();

    int start();

    int end();

    List<ASTNode> children();

    ASTNode newNode(Input input, Type type, String file, List<javax0.jamal.api.ASTNode> children, Supplier<String> text, javax0.jamal.api.ASTNode nodeOpen);

    ASTNode newNode(Input input, Type type, Supplier<String> text, javax0.jamal.api.ASTNode nodeOpen);

    ASTNode newNode(Input input, Type type, Supplier<String> text);

}
