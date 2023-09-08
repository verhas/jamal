package javax0.jamal.engine;

import javax0.jamal.api.Input;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class ASTNode implements javax0.jamal.api.ASTNode {

    public static class Null extends ASTNode {

        @Override
        public Null newNode(Input input, Type type, Supplier<String> text) {
            return new Null(null, Type.text, null);
        }

        public Null(Input input, Type type, String text) {
        }

        @Override
        public Null newNode(Input input, Type type, String file, List<javax0.jamal.api.ASTNode> children, Supplier<String> text, javax0.jamal.api.ASTNode nodeOpen) {
            return new Null(null, Type.text, null, null, null, null);
        }

        public Null(Input input, Type type, String file, List<javax0.jamal.api.ASTNode> children, String text, javax0.jamal.api.ASTNode nodeOpen) {
        }

        @Override
        void add(javax0.jamal.api.ASTNode node) {
        }

        @Override
        public void normalize() {
        }
    }

    int start;
    int end;
    final Type type;

    String text;

    String file;

    Input input;

    private ASTNode() {
        type = Type.text;
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    public String file() {
        return file;
    }

    @Override
    public Input input() {
        return input;
    }

    final List<javax0.jamal.api.ASTNode> children = new ArrayList<>();


    @Override
    public ASTNode newNode(Input input, Type type, Supplier<String> text) {
        return new ASTNode(input, type, text.get());
    }

    public ASTNode(Input input, Type type, String text) {
        this.input = input;
        this.file = input.getReference();
        this.type = type;
        this.text = text;
    }

    @Override
    public ASTNode newNode(Input input, Type type, String file, List<javax0.jamal.api.ASTNode> children, Supplier<String> text, javax0.jamal.api.ASTNode nodeOpen) {
        return new ASTNode(input, type, file, children, text.get(), nodeOpen);
    }

    public ASTNode(Input input, Type type, String file, List<javax0.jamal.api.ASTNode> children, String text, javax0.jamal.api.ASTNode nodeOpen) {
        this.input = input;
        this.file = input.getReference();
        this.type = type;
        if (nodeOpen != null) {
            this.children.add(nodeOpen);
        }
        for (final var child : children) {
            if (!child.input().generated() && Objects.equals(child.file(), file)) {
                this.children.add(child);
            }
        }
        this.text = text;
    }

    void add(javax0.jamal.api.ASTNode node) {
        if (type == Type.list || type == Type.macro) {
            children.add(node);
        } else {
            throw new IllegalStateException("Cannot add child to node of type " + type);
        }
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public int start() {
        return start;
    }

    @Override
    public int end() {
        return end;
    }

    @Override
    public List<javax0.jamal.api.ASTNode> children() {
        return children;
    }

    private static void place(final ASTNode node, int pos, String text) {
        node.start = text.indexOf(node.text(), pos);
        if (node.start != -1) {
            node.end = node.start + node.text().length();
        } else {
            throw new IllegalArgumentException("Cannot find node text '" + node.text() + "' in the input '" + text + "' from " + pos);
        }
        for (final var child : node.children) {
            place((ASTNode) child, pos, text);
            pos += child.text().length();
        }
    }

    public void normalize() {
        start = 0;
        end = text.length();

        int pos = 0;
        for (final var child : children) {
            place((ASTNode) child, pos, text);
            pos += child.text().length();
        }

    }

}
