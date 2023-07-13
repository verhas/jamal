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

    fun nso(processor: Processor, `in`: Input, it: javax0.jamal.kotlin.Scanner): javax0.jamal.tools.Scanner.ScannerObject {
        require(it is Identified) { "The Scanner interface can only be used by Macros." }
        return javax0.jamal.tools.Scanner.ScannerObject(processor, `in`, it as Identified)
    }

    fun newScanner(input: Input, processor: Processor): ScannerObject {
        return ScannerObject(nso(processor, input, this))
    }

    /**
     * The scanner interface that uses the first line of the input.
     */
    interface FirstLine : javax0.jamal.kotlin.Scanner {
        override fun newScanner(input: Input, processor: Processor): ScannerObject {
            return ScannerObject(nso(processor, input, this).delimiterSetter(Function.identity()))
        }
    }


    /**
     * The scanner interface that uses the whole input.
     */
    interface WholeInput : javax0.jamal.kotlin.Scanner {
        override fun newScanner(input: Input, processor: Processor): ScannerObject {
            return ScannerObject(nso(processor, input, this).delimiterSetter { obj: Params -> obj.tillEnd() })
        }
    }


    class ScannerObject(val delegate: Scanner.ScannerObject) {

        fun <T> param(name: String?, vararg keys: String?): Params.Param<T>? {
            return delegate.param(name, *keys)
        }

        fun list(name: String?, vararg keys: String?): ListParameter {
            return ListParameter(delegate.list(name, *keys))
        }


        fun <K> enumeration(klass: Class<K>): EnumerationParameter {
            return EnumerationParameter(delegate.enumeration(klass))
        }

        fun bool(name: String?, vararg keys: String): BooleanParameter {
            return BooleanParameter(delegate.bool(name, *keys))
        }

        fun str(name: String?, vararg keys: String): StringParameter {
            return StringParameter(delegate.str(name, *keys))
        }

        fun file(name: String?, vararg keys: String): FileParameter {
            return FileParameter(delegate.file(name, *keys))
        }

        fun pattern(name: String?, vararg keys: String): PatternParameter {
            return PatternParameter(delegate.pattern(name, *keys))
        }

        fun number(name: String?, vararg keys: String): IntegerParameter {
            return IntegerParameter(delegate.number(name, *keys))
        }

        fun extra(): ExtraParams? {
            return delegate.extra()
        }

        fun done() {
            delegate.done()
        }
    }
}

abstract class AbstractTypedParameter<T>(val abstractDelegate: javax0.jamal.tools.param.AbstractTypedParameter<T>) {

    val param: Params.Param<T>
        get() {
            return abstractDelegate.getParam()
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

class StringParameter(val delegate: javax0.jamal.tools.param.StringParameter) : AbstractTypedParameter<String>(delegate) {

    val value: String
        get() {
            return delegate.get()
        }

    operator fun invoke(): String {
        return delegate.get()
    }

    infix fun default(dV: String): StringParameter {
        delegate.defaultValue(dV)
        return this
    }
}

class IntegerParameter(val delegate: javax0.jamal.tools.param.IntegerParameter) : AbstractTypedParameter<Int>(delegate) {
    val value: Int
        get() {
            return delegate.get()
        }

    operator fun invoke(): Int {
        return delegate.get()
    }

    infix fun default(dV: Int): IntegerParameter {
        delegate.defaultValue(dV)
        return this
    }
}

class BooleanParameter(val delegate: javax0.jamal.tools.param.BooleanParameter) : AbstractTypedParameter<Boolean>(delegate) {
    operator fun invoke(): Boolean {
        return delegate.`is`()
    }

    operator fun not(): Boolean {
        return !invoke()
    }
}

class EnumerationParameter(val delegate: javax0.jamal.tools.param.EnumerationParameter) : AbstractTypedParameter<Boolean>(delegate) {
    operator fun <K> invoke(klass: Class<K>): K {
        return delegate.get(klass)
    }
}

class ListParameter(val delegate: javax0.jamal.tools.param.ListParameter) : AbstractTypedParameter<List<String>>(delegate) {
    operator fun invoke(): List<String> {
        return delegate.get()
    }

    operator fun get(i: Int): String {
        return invoke().get(i)
    }
}

class PatternParameter(val delegate: javax0.jamal.tools.param.PatternParameter) : AbstractTypedParameter<Pattern>(delegate) {
    operator fun invoke(): java.util.regex.Pattern {
        return delegate.get()
    }
}

class FileParameter(val delegate: javax0.jamal.tools.param.FileParameter) : AbstractTypedParameter<String>(delegate) {
    operator fun invoke(): String {
        return delegate.get()
    }
}