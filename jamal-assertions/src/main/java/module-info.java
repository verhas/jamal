import javax0.jamal.api.Macro;
import javax0.jamal.assertions.NumericAsserts.*;

module jamal.assertions {
    exports javax0.jamal.assertions;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    provides Macro with
        // snippet AssertionMacroClasses
        AssertEquals,
        AssertEmpty,
        AssertFail,
        AssertIntEquals,
        AssertLess,
        AssertGreater,
        AssertLessOrEqual,
        AssertGreaterOrEqual,
        AssertNumeric,
        AssertInt,
        AssertContains,
        AssertEndsWith,
        AssertStartsWith
        // end snippet
        ;
}