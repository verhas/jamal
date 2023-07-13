package javax0.jamal.prog.analyzer;

import javax0.jamal.api.BadSyntax;

import java.util.ArrayList;

public class Lex {
    public Lex(final Type type, final String text) {
        this.type = type;
        this.text = text;
    }

    @Override
    public String toString() {
        return String.format("Lex{type=%s, text='%s'}", type, text);
    }

    public enum Type {
        IDENTIFIER, RESERVED, STRING
    }

    final public Type type;
    final public String text;

    public boolean is(String text) {
        return this.text.equals(text);
    }

    public static class List {
        private final ArrayList<Lex> lexes;

        public List(final java.util.List<Lex> lexes) {
            this.lexes = new ArrayList<>(lexes);
        }

        public Lex next() throws BadSyntax {
            if (lexes.size() == 0) {
                throw new BadSyntax("more elements expected");
            }
            return lexes.remove(0);
        }

        public Lex peek() throws BadSyntax {
            if (lexes.size() == 0) {
                throw new BadSyntax("more elements expected");
            }
            return lexes.get(0);
        }

        public boolean hasNext() {
            return lexes.size() > 0;
        }

        public boolean isEmpty() {
            return !hasNext();
        }

        public void eol(final String msg) throws BadSyntax {
            if (!eol()) {
                throw new BadSyntax(msg);
            }
            if (hasNext()) {
                next();
            }
        }

        public boolean eol() {
            return isEmpty() || lexes.get(0).is("\n");
        }

        public boolean is(String text) {
            return hasNext() && lexes.get(0).is(text);
        }

        public Lex assume(Type type, String msg) throws BadSyntax {
            if (lexes.isEmpty() || lexes.get(0).type != type) {
                throw new BadSyntax(msg);
            }
            return next();
        }

        public void assumeKW(String text, String msg) throws BadSyntax {
            assume(Type.RESERVED, text, msg);
        }

        public void assumeEndKWNL(String text, String msg, String newLineMsg) throws BadSyntax {
            assumeEnd(text, msg);
            if (newLineMsg != null) {
                eol(newLineMsg);
            }
        }

        public void assumeKWNL(String text, String msg, String newLineMsg) throws BadSyntax {
            assume(Type.RESERVED, text, msg);
            if (newLineMsg != null) {
                eol(newLineMsg);
            }
        }

        public Lex assumeEnd(String text, String msg) throws BadSyntax {
            if (lexes.isEmpty() || lexes.get(0).type != Type.RESERVED) {
                throw new BadSyntax(msg);
            }
            if (lexes.get(0).text.equals("end" + text)) {
                return next();
            }
            if( lexes.size() < 2 ){
                throw new BadSyntax(msg);
            }
            if( lexes.get(1).type != Type.RESERVED || !lexes.get(0).text.equals("end") || !lexes.get(1).text.equals(text) ){
                throw new BadSyntax(msg);
            }
            next();
            return next();
        }

        public Lex assume(Type type, String text, String msg) throws BadSyntax {
            if (lexes.isEmpty() || lexes.get(0).type != type || !lexes.get(0).text.equals(text)) {
                throw new BadSyntax(msg);
            }
            return next();
        }

    }
}
