package javax0.jamal.kotlin

import javax0.jamal.api.Input
import javax0.jamal.api.Macro
import javax0.jamal.api.Processor
import javax0.jamal.kotlin.KotlinParam.BooleanType.Boolean
import javax0.jamal.kotlin.KotlinParam.IntType.Int
import javax0.jamal.kotlin.KotlinParam.ListType.List
import javax0.jamal.kotlin.KotlinParam.StringType.String
import javax0.jamal.kotlin.Line.line


class HelloKotlin : Macro {
    override fun evaluate(input: Input, processor: Processor): kotlin.String {
        val stringParam = param(null, "string", "stringy") type String default "Peter"
        val integerParam = param(null, "integer") type Int default 1
        val booleanParam = param(null, "boolean") type Boolean
        val listParam = param(null, "list") type List
        scan(stringParam, integerParam, booleanParam, listParam) using processor first line parsing input
        input.skipWhiteSpaces
        val id = input.fetchId
        val stringName = if (stringParam.isPresent) stringParam.name else "string"
        val z: kotlin.String = if (!booleanParam) {
            "NO"
        } else {
            "YES"
        }
        val list = if (listParam().size > 1) {
            "${listParam(0)} ${listParam()[1]}"
        } else {
            "no list"
        }
        return "Hello $stringName=${stringParam()} Kotlin World $z ${listParam().size} $list $id"
    }
}