package javax0.jamal.debugger;

/**
 * The different run states for the debuggers.
 */
enum RunState {
    NODEBUG, // run and do not store debug information, used during debugger provided string evaluation
    RUN, // run and do not stop after the execution of the next step
    STEP_IN, // stepping into the next evaluation
    STEP // step one evaluation and stop only if the scope level comes back to the current level
}
