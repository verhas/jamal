package javax0.jamal.api;

import java.util.LinkedList;

/**
 * A simple position stack implementation used in the default implementation of {@link Macro#fetch(Processor, Input)}.
 */
class PositionStack {

    final private LinkedList<Position> refStack;

    PositionStack(Position pos) {
        refStack = new LinkedList<>();
        refStack.add(pos);
    }

    Position pop() {
        return refStack.pop();
    }

    void push(Position pos) {
        refStack.push(pos);
    }

    boolean popAndEmpty() {
        refStack.pop();
        return refStack.size() == 0;
    }

    int size() {
        return refStack.size();
    }
}
