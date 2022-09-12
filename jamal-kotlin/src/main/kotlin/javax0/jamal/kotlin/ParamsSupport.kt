package javax0.jamal.kotlin

import javax0.jamal.api.Input
import javax0.jamal.api.Macro
import javax0.jamal.api.Processor
import javax0.jamal.tools.Params
import javax0.jamal.tools.Params.Param
import java.util.function.Function
import java.util.regex.Pattern

fun param(vararg aliases: String?): KotlinParam<Any> {
    return KotlinParam<Any>(Params.holder(*aliases))
}

class ScanDelimiters<T : Any>(
    private val params: Array<out KotlinParam<out T>>,
    private val processor: Processor,
    private val macro: Macro,
    private val start: Char?,
    private val terminal: Char?
) {
    infix fun parsing(input: Input) {
        var call = Params.using(processor).from(macro)
        if (start != null) call = call.startWith(start)
        if (terminal != null) call = call.endWith(terminal)
        call.keys(*params).parse(input)
    }
}

class ScanStart<T : Any>(
    private val params: Array<out KotlinParam<out T>>,
    private val processor: Processor,
    private val macro: Macro,
    private val start: Char
) {
    infix fun endWith(terminal: Char): ScanDelimiters<out T> {
        return ScanDelimiters(params, processor, macro, start, terminal)
    }
}

enum class Line { line }
enum class End { end }

class ScanMacro<T : Any>(
    private val macro: Macro,
    private val params: Array<out KotlinParam<out T>>,
    private val processor: Processor
) {
    infix fun delimited(delimiters: String): ScanDelimiters<out T> {
        if (delimiters.length != 2) {
            throw IllegalArgumentException("The argument to method 'between()' has to be a 2-character string. It was '$delimiters'");
        }
        val start = delimiters[0];
        val terminal = delimiters[1];
        return ScanDelimiters(params, processor, macro, start, terminal)
    }

    infix fun startWith(start: Char): ScanStart<out T> {
        return ScanStart(params, processor, macro, start)
    }

    infix fun endWith(terminal: Char): ScanDelimiters<out T> {
        return ScanDelimiters(params, processor, macro, null, terminal)
    }

    infix fun first(what: Line): ScanDelimiters<out T> {
        return ScanDelimiters(params, processor, macro, null, '\n')
    }

    infix fun till(what: End): ScanDelimiters<out T> {
        return ScanDelimiters(params, processor, macro, null, null)
    }

}

class ScanKeys<T : Any>(private val macro: Macro, private val params: Array<out KotlinParam<out T>>) {
    infix fun using(processor: Processor): ScanMacro<T> {
        return ScanMacro(macro, params, processor)
    }
}

fun <T : Any> Macro.scan(vararg params: KotlinParam<out T>): ScanKeys<T> {
    return ScanKeys(this, params)
}

class KotlinParam<T : Any>(var delegate: Param<T>) : Param<T> {

    override fun keys(): Array<String> {
        return delegate.keys()
    }

    override infix fun copy(p: Param<*>?) {
        delegate.copy(p)
    }

    override fun inject(processor: Processor?, macroName: String?) {
        delegate.inject(processor, macroName)
    }

    override infix fun set(value: String?) {
        delegate.set(value)
    }

    override infix fun orElse(s: String?): KotlinParam<T> {
        return KotlinParam<T>(delegate.orElse(s))
    }

    infix fun default(s: String?): KotlinParam<T> {
        return KotlinParam<T>(delegate.orElse(s))
    }

    @Deprecated(
        message = "Use orElse null",
        level = DeprecationLevel.WARNING,
        replaceWith = ReplaceWith("KotlinParam<T> orElse null", "com.javax0.jamal.kotlin.KotlinParam")
    )
    override fun orElseNull(): KotlinParam<T> {
        return KotlinParam<T>(delegate.orElseNull())

    }

    infix fun default(i: Int): KotlinParam<Int> {
        return KotlinParam<Int>(delegate.orElseInt(i))
    }

    @Deprecated(
        message = "Use orElse int value",
        level = DeprecationLevel.WARNING,
        replaceWith = ReplaceWith("KotlinParam<Int> orElse i", "com.javax0.jamal.kotlin.KotlinParam")
    )
    override infix fun orElseInt(i: Int): KotlinParam<Int> {
        return this default i
    }

    override fun <K : Any> `as`(klass: Class<K>?, converter: Function<String, K>?): KotlinParam<K> {
        return KotlinParam(delegate.`as`(klass, converter))
    }

    fun <K : Any> converting(klass: Class<K>?, converter: Function<String, K>?): KotlinParam<K> {
        return `as`(klass, converter)
    }

    override fun asPattern(): KotlinParam<Pattern> {
        return KotlinParam(delegate.asPattern())
    }

    enum class PatternType { PatternParam }

    infix fun type(type: PatternType): KotlinParam<Pattern> {
        return asPattern()
    }

    override fun asInt(): KotlinParam<Int> {
        return KotlinParam(delegate.asInt())
    }

    enum class IntType { Int }

    infix fun type(type: IntType): KotlinParam<Int> {
        return asInt()
    }

    override fun asBoolean(): KotlinParam<Boolean> {
        return KotlinParam(delegate.asBoolean())
    }

    enum class BooleanType { Boolean }

    infix fun type(type: BooleanType): KotlinParam<Boolean> {
        return asBoolean()
    }

    override fun asString(): KotlinParam<String> {
        return KotlinParam(delegate.asString())
    }

    enum class StringType { String }

    infix fun type(type: StringType): KotlinParam<String> {
        return asString()
    }

    override fun asList(): KotlinParam<MutableList<*>> {
        return KotlinParam(delegate.asList())
    }

    enum class ListType { List }

    infix fun type(type: ListType): KotlinParam<MutableList<*>> {
        return asList()
    }

    override infix fun <K : Any> asType(type: Class<K>?): KotlinParam<K> {
        return KotlinParam(delegate.asType(type))
    }

    override infix fun <K : Any?> asList(k: Class<K>?): KotlinParam<MutableList<K>> {
        return KotlinParam(delegate.asList(k))

    }

    override fun get(): T {
        return delegate.get()
    }

    override fun `is`(): Boolean {
        return delegate.`is`()
    }

    override fun isPresent(): Boolean {
        return delegate.isPresent()
    }

    val name: String?
        get() {
            return delegate.name()
        }

    override fun name(): String {
        return delegate.name()
    }

    override infix fun setName(id: String?) {
        delegate.setName(id)
    }

    override fun `as`(converter: Function<String, T>?): KotlinParam<T> {
        return KotlinParam(delegate.`as`(converter))
    }

    operator fun invoke(): T {
        return delegate.get()
    }

    operator fun not(): Boolean {
        return !delegate.`is`()
    }

    operator fun invoke(i: Int): String {
        return (delegate.get() as List<String>) .get(i)
    }
}
