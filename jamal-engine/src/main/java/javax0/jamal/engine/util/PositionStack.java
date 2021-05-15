package javax0.jamal.engine.util;

import javax0.jamal.api.Position;

import java.util.LinkedList;

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

    boolean popAndEmpty(){
        refStack.pop();
        return refStack.size() == 0;
    }

    int size() {
        return refStack.size();
    }
}
