package javax0.jamal.kotlin

import javax0.jamal.api.Identified
import javax0.jamal.api.Input
import javax0.jamal.api.Processor
import javax0.jamal.tools.Params
import javax0.jamal.tools.Params.ExtraParams
import javax0.jamal.tools.Scanner
import java.util.function.Function
import java.util.regex.Pattern


interface Scanner {

    @DslMarker
    annotation class ScannerDsl

    fun nso(
        processor: Processor,
        `in`: Input,
        it: javax0.jamal.kotlin.Scanner
    ): Scanner.ScannerObject {
        require(it is Identified) { "The Scanner interface can only be used by Macros." }
        return Scanner.ScannerObject(processor, `in`, it as Identified)
    }

    fun scanner(input: Input, processor: Processor): ScannerObject {
        return ScannerObject(nso(processor, input, this))
    }

    /**
     * The scanner interface that uses the first line of the input.
     */
    interface FirstLine : javax0.jamal.kotlin.Scanner {
        override fun scanner(input: Input, processor: Processor): ScannerObject {
            return ScannerObject(nso(processor, input, this).delimiterSetter(Function.identity()))
        }
    }


    /**
     * The scanner interface that uses the whole input.
     */
    interface WholeInput : javax0.jamal.kotlin.Scanner {
        override fun scanner(input: Input, processor: Processor): ScannerObject {
            return ScannerObject(nso(processor, input, this).delimiterSetter { obj: Params -> obj.tillEnd() })
        }
    }


    /**
     * A scanner wrapper in kotlin that delegates the tasks to the underlying Java scanner.
     */
    class ScannerObject(val delegate: Scanner.ScannerObject) {

        open class ParamDslObject {
            var name: String? = null
            var aliases: Array<out String> = emptyArray()
            var alias: String = ""
                set(value) {
                    aliases = if (value == "") emptyArray() else arrayOf(value)
                }

            fun aliases(vararg aliases: String) {
                this.aliases = aliases
            }
        }

        class Holder<T>(var value: T?)

        class ParamDslObjectWithDefault<T> : ParamDslObject() {
            var default: T? = null
            fun default(default: T) {
                this.default = default
            }

            fun default(content: () -> T) {
                this.default = content.invoke()
            }
        }

        fun <T> param(name: String?, vararg keys: String?): Params.Param<T>? {
            return delegate.param(name, *keys)
        }

        fun list(name: String?, vararg keys: String?): ListParameter {
            return ListParameter(delegate.list(name, *keys))
        }

        @ScannerDsl
        fun list(content: ParamDslObject.() -> Unit): ListParameter {
            val sp = ParamDslObject()
            sp.apply(content)
            return ListParameter(delegate.list(sp.name, *sp.aliases))
        }

        inline fun <reified K> enumeration(): EnumerationParameter {
            return EnumerationParameter(delegate.enumeration(K::class.java))
        }

        inline fun <reified K> enumeration(content: () -> Unit): EnumerationParameter {
            content.invoke()
            return EnumerationParameter(delegate.enumeration(K::class.java))
        }

        fun bool(name: String?, vararg keys: String): BooleanParameter {
            return BooleanParameter(delegate.bool(name, *keys))
        }

        @ScannerDsl
        fun bool(content: ParamDslObject.() -> Unit): BooleanParameter {
            val sp = ParamDslObject()
            sp.apply(content)

            return BooleanParameter(delegate.bool(sp.name, *sp.aliases))
        }

        fun str(name: String?, vararg keys: String): StringParameter {
            return StringParameter(delegate.str(name, *keys))
        }

        @ScannerDsl
        fun str(content: ParamDslObjectWithDefault<String>.() -> Unit): StringParameter {
            val sp = ParamDslObjectWithDefault<String>()
            sp.apply(content)
            val rv = StringParameter(delegate.str(sp.name, *sp.aliases))
            if (sp.default != null) rv default sp.default!!
            return rv
        }


        fun file(name: String?, vararg keys: String): FileParameter {
            return FileParameter(delegate.file(name, *keys))
        }

        @ScannerDsl
        fun file(content: ParamDslObject.() -> Unit): FileParameter {
            val sp = ParamDslObject()
            sp.apply(content)
            return FileParameter(delegate.file(sp.name, *sp.aliases))
        }

        fun pattern(name: String?, vararg keys: String): PatternParameter {
            return PatternParameter(delegate.pattern(name, *keys))
        }

        @ScannerDsl
        fun pattern(content: ParamDslObject.() -> Unit): PatternParameter {
            val sp = ParamDslObject()
            sp.apply(content)
            return PatternParameter(delegate.pattern(sp.name, *sp.aliases))
        }

        @ScannerDsl
        fun number(content: ParamDslObjectWithDefault<Int>.() -> Unit): IntegerParameter {
            val sp = ParamDslObjectWithDefault<Int>()
            sp.apply(content)
            val rv = IntegerParameter(delegate.number(sp.name, *sp.aliases))
            if (sp.default != null) rv default sp.default!!
            return rv
        }

        fun number(name: String?, vararg keys: String): IntegerParameter {
            return IntegerParameter(delegate.number(name, *keys))
        }

        private var allowExtraParametersWasSet = false
        var allowExtraParameters: Boolean = false
            set(value) {
                if( allowExtraParametersWasSet ) throw IllegalArgumentException("Cannot set allowExtraParameters multiple times")
                allowExtraParametersWasSet = true
                if (value) delegate.extra()
            }

        fun scan() {
            delegate.done()
        }

        fun scan(content: ScannerObject.() -> Unit) {
            apply(content)
            delegate.done()
        }
    }
}

abstract class AbstractTypedParameter<T>(val abstractDelegate: javax0.jamal.tools.param.AbstractTypedParameter<T>) {

    val param: Params.Param<T>
        get() {
            return abstractDelegate.param
        }

    val present: Boolean
        get() {
            return abstractDelegate.isPresent
        }

    val name: String
        get() {
            return abstractDelegate.name()
        }
}

class StringParameter(val delegate: javax0.jamal.tools.param.StringParameter) :
    AbstractTypedParameter<String>(delegate) {

    val value: String
        get() {
            return delegate.get()
        }

    operator fun invoke(): String {
        return delegate.get()
    }

    fun optional(): StringParameter {
        delegate.optional()
        return this
    }

    infix fun default(dV: String): StringParameter {
        delegate.defaultValue(dV)
        return this
    }
}

class IntegerParameter(val delegate: javax0.jamal.tools.param.IntegerParameter) :
    AbstractTypedParameter<Int>(delegate) {
    val value: Int
        get() {
            return delegate.get()
        }

    operator fun invoke(): Int {
        return delegate.get()
    }

    fun optional(): IntegerParameter {
        delegate.optional()
        return this
    }

    infix fun default(dV: Int): IntegerParameter {
        delegate.defaultValue(dV)
        return this
    }
}

class BooleanParameter(val delegate: javax0.jamal.tools.param.BooleanParameter) :
    AbstractTypedParameter<Boolean>(delegate) {
    operator fun invoke(): Boolean {
        return delegate.`is`()
    }

    operator fun not(): Boolean {
        return !invoke()
    }
}

class EnumerationParameter(val delegate: javax0.jamal.tools.param.EnumerationParameter) :
    AbstractTypedParameter<Boolean>(delegate) {
    operator fun <K> invoke(klass: Class<K>): K {
        return delegate.get(klass)
    }
}

class ListParameter(val delegate: javax0.jamal.tools.param.ListParameter) :
    AbstractTypedParameter<List<String>>(delegate) {
    operator fun invoke(): List<String> {
        return delegate.get()
    }

    operator fun get(i: Int): String {
        return invoke()[i]
    }
}

class PatternParameter(val delegate: javax0.jamal.tools.param.PatternParameter) :
    AbstractTypedParameter<Pattern>(delegate) {
    operator fun invoke(): Pattern {
        return delegate.get()
    }
}

class FileParameter(val delegate: javax0.jamal.tools.param.FileParameter) : AbstractTypedParameter<String>(delegate) {
    operator fun invoke(): String {
        return delegate.get()
    }
}