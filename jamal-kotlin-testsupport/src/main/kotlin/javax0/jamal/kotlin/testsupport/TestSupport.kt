package javax0.jamal.kotlin

import javax0.jamal.api.Processor
import javax0.jamal.builtins.Macro
import java.util.function.Predicate


fun testTheInput(input: String): TestThat {
    return TestThat(javax0.jamal.testsupport.TestThat.theInput(input.trimIndent()))
}

fun theMacro(klassMacro: Class<out Macro>): TestThat {
    return TestThat(javax0.jamal.testsupport.TestThat.theMacro(klassMacro))
}

class TestThat(val delegate: javax0.jamal.testsupport.TestThat) : AutoCloseable {


    infix fun results(result: String) {
        delegate.results(result.trimIndent());
    }

    infix fun resultsBin(result: String) {
        delegate.resultsBin(result)
    }

    infix fun results(predicate: Predicate<String>) {
        delegate.results(predicate);
    }

    infix fun matches(pattern: String) {
        delegate.matches(pattern)
    }

    fun throwsBadSyntax() {
        delegate.throwsBadSyntax()
    }

    fun throwsBadSyntax(regex: String) {
        delegate.throwsBadSyntax(regex)
    }

    fun throwsUp(exception: Class<out Throwable>) {
        delegate.throwsUp(exception)
    }

    fun throwsUp(exception: Class<out Throwable>, regex: String) {
        delegate.throwsUp(exception, regex)
    }

    infix fun atPosition(pos: Position): TestThat {
        delegate.atPosition(pos.delegate)
        return this
    }

    fun usingTheSeparators(open: String, close: String): TestThat {
        delegate.usingTheSeparators(open, close)
        return this
    }

    fun atPosition(file: String, line: Int = 1, column: Int = 1): TestThat {
        delegate.atPosition(file, line, column)
        return this
    }

    fun define(macro: Macro, alias: String = macro.getId()) {
        delegate.define(macro, alias)
    }

    fun define(macroName: String, content: String, vararg arguments: String) {
        delegate.define(macroName, content, *arguments)
    }

    fun global(macro: Macro, alias: String = macro.getId()) {
        delegate.global(macro, alias)
    }

    fun global(macroName: String, content: String, vararg arguments: String) {
        delegate.global(macroName, content, *arguments)
    }

    infix fun fromTheInput(input: String): TestThat {
        delegate.fromTheInput(input.trimIndent());
        return this
    }

    val logs: List<String> get() = delegate.getLogs()

    fun ignoreLineEnding(): TestThat {
        delegate.ignoreLineEnding()
        return this
    }

    fun ignoreSpaces(): TestThat {
        delegate.ignoreSpaces()
        return this
    }

    val processor: Processor get() = delegate.getProcessor()


    override fun close() {
        delegate.close();
    }
}

class Position(val file: String, val line: Int = 1, val column: Int = 1) {
    val delegate = javax0.jamal.api.Position(file, line, column)
}