import javax0.jamal.api.Macro;
import javax0.jamal.assertions.AssertContains;
import javax0.jamal.assertions.AssertEndsWith;
import javax0.jamal.assertions.AssertStartsWith;
import javax0.jamal.assertions.NumericAsserts;
import javax0.jamal.assertions.NumericAsserts.AssertGreater;
import javax0.jamal.assertions.NumericAsserts.AssertGreaterOrEqual;
import javax0.jamal.assertions.NumericAsserts.AssertInt;
import javax0.jamal.assertions.NumericAsserts.AssertIntEquals;
import javax0.jamal.assertions.NumericAsserts.AssertLess;
import javax0.jamal.assertions.NumericAsserts.AssertLessOrEqual;
import javax0.jamal.assertions.NumericAsserts.AssertNumeric;
import javax0.jamal.assertions.AssertEmpty;
import javax0.jamal.assertions.AssertEquals;
import javax0.jamal.assertions.AssertFail;

module jamal.assertions {
    exports javax0.jamal.assertions;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    provides Macro with AssertEquals, AssertEmpty, AssertFail,
        AssertIntEquals, AssertLess, AssertGreater, AssertLessOrEqual, AssertGreaterOrEqual,
        AssertNumeric, AssertInt, AssertContains, AssertEndsWith, AssertStartsWith;
}