package javax0.jamal.api;

import java.util.List;
import java.util.function.Supplier;

public interface ASTNode {
    enum Type {
        open, macro, text, list
    }

    Type type();

    int start();

    int end();

    String text();

    String file();

    Input input();

    List<ASTNode> children();

    ASTNode newNode(Input input, Type type, String file, List<javax0.jamal.api.ASTNode> children, Supplier<String> text, javax0.jamal.api.ASTNode nodeOpen);

    ASTNode newNode(Input input, Type type, Supplier<String> text);

}
