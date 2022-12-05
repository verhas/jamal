package javax0.jamal.api;

import java.util.LinkedList;

/**
 * A simple position stack implementation used in the default implementation of {@link Macro#fetch(Processor, Input)}.
 *
 * This is not a public class.
 */
class PositionStack {

    final private LinkedList<Position> refStack;

    /**
     * Create a stack that contains only the position {@code pos}
     * @param pos teh element of the stack at the creation
     */
    PositionStack(Position pos) {
        refStack = new LinkedList<>();
        refStack.add(pos);
    }

    /**
     * Pop one element from the position stack.
     * @return the top element popped from the position stack.
     */
    Position pop() {
        return refStack.pop();
    }

    /**
     * Push one element on the top of the position stack.
     * @param pos the position to place on the top of te stack.
     */
    void push(Position pos) {
        refStack.push(pos);
    }

    /**
     * Pops an element, throws the element away.
     * @return {@code true} if the stack is empty after the element was dropped.
     */
    boolean popAndEmpty() {
        refStack.pop();
        return refStack.size() == 0;
    }

    /**
     *
     * @return the size of the position stack
     */
    int size() {
        return refStack.size();
    }
}
