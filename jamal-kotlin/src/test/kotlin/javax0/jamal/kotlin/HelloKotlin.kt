package javax0.jamal.kotlin

import javax0.jamal.api.Input
import javax0.jamal.api.Macro
import javax0.jamal.api.Processor


class HelloKotlin : Macro, Scanner.FirstLine {
    override fun evaluate(input: Input, processor: Processor): String {
        val s = scanner(input, processor)
        val stringParam = s.str {
            aliases("string", "stringy")
            default { "Peter" }
        }

        val integerParam = s.number {
            name = "integaire"
            alias = "integer"
            default(1)
        }
        val booleanParam = s.bool {
            alias = "boolean"
        }
        val enumParam = s.enumeration<EnumParam> {
        }
        val listParam = s.list {
            alias = "list"
        }

        s.scan{
            allowExtraParameters = false
        }

        input.skipWhiteSpaces
        val id = input.fetchId
        val k = integerParam()

        val stringName = if (stringParam.present) stringParam.name else "string"
        val z: String = if (!booleanParam) "NO" else "YES"
        val list = if (listParam().size > 1) {
            "${listParam[0]} ${listParam()[1]}"
        } else {
            "no list"
        }
        return "Hello $stringName=${stringParam()} Kotlin ${enumParam.name} World $z ${listParam().size} $list $id $k"
    }
}

enum class EnumParam {
    ONE, TWO, THREE
}