package javax0.jamal.engine;

import javax0.jamal.api.Input;
import javax0.jamal.api.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class ASTNode implements javax0.jamal.api.ASTNode {

    public static class Null extends ASTNode {
        private static final Null instance = new Null();

        Null() {
        }

        @Override
        public Null newNode(Input input, Type type, Supplier<String> text) {
            return instance;
        }

        @Override
        public Null newNode(Input input, Type type, Supplier<String> text, javax0.jamal.api.ASTNode nodeOpen) {
            return instance;
        }

        @Override
        public Null newNode(Input input, Type type, String file, List<javax0.jamal.api.ASTNode> children, Supplier<String> text, javax0.jamal.api.ASTNode nodeOpen) {
            return instance;
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

    final List<javax0.jamal.api.ASTNode> children = new ArrayList<>();


    @Override
    public ASTNode newNode(Input input, Type type, Supplier<String> text) {
        return new ASTNode(input, type, text.get());
    }

    public ASTNode newNode(Position pos, Type type, Supplier<String> text) {
        return new ASTNode(pos, type, text.get());
    }


    @Override
    public ASTNode newNode(Input input, Type type, Supplier<String> text, javax0.jamal.api.ASTNode nodeOpen) {
        ASTNode node = new ASTNode(input, type, text.get());
        node.add(nodeOpen);
        return node;
    }

    public ASTNode(Input input, Type type, String text) {
        this.input = input;
        this.file = input == null ? null : input.getReference();
        this.type = type;
        this.text = text;
    }
    public ASTNode(Position pos, Type type, String text) {
        this.input = null;
        this.file = pos.file;
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
            if (!((ASTNode)child).input.generated() && Objects.equals(((ASTNode)child).file, file)) {
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
        node.start = text.indexOf(node.text, pos);
        if (node.start != -1) {
            node.end = node.start + node.text.length();
            node.text = null; // can be collected
            node.input = null; // can be collected
            node.file = null; // can be collected
        } else {
            throw new IllegalArgumentException("Cannot find node text '" + node.text + "' in the input '" + text + "' from " + pos);
        }
        for (final var child : node.children) {
            final var l = ((ASTNode)child).text.length();
            place((ASTNode) child, pos, text);
            pos += l;
        }
    }

    public void normalize() {
        start = 0;
        end = text.length();

        int pos = 0;
        for (final var child : children) {
            final var l = ((ASTNode)child).text.length();
            place((ASTNode) child, pos, text);
            pos += l;
        }

    }

}
