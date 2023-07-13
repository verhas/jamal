package javax0.jamal.kotlin

import javax0.jamal.api.Input
import javax0.jamal.api.Macro
import javax0.jamal.api.Processor


class HelloKotlin : Macro, Scanner.FirstLine {
    override fun evaluate(input: Input, processor: Processor): kotlin.String {
        val scanner = newScanner(input, processor)
        val stringParam = scanner.str(null, "string", "stringy") default "Peter"
        val integerParam = scanner.number("integaire", "integer") default 1
        val booleanParam = scanner.bool(null, "boolean")
        val listParam = scanner.list(null, "list")
        scanner.done()
        input.skipWhiteSpaces
        val id = input.fetchId
        val h = integerParam()
        val stringName = if (stringParam.present) stringParam.name else "string"
        val z: String = if (!booleanParam) "NO" else "YES"
        val list = if (listParam().size > 1) {
            "${listParam[0]} ${listParam()[1]}"
        } else {
            "no list"
        }
        return "Hello $stringName=${stringParam()} Kotlin World $z ${listParam().size} $list $id"
    }
}