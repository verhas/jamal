package javax0.jamal.kotlin

import javax0.jamal.api.Input
import javax0.jamal.api.Processor
import javax0.jamal.tools.InputHandler

val Input.fetchId
    get(): String {
        return InputHandler.fetchId(this)
    }

val Input.skipWhiteSpaces
    get():Unit {
        InputHandler.skipWhiteSpaces(this)
    }

val Input.skipWhiteSpacesNoNL
    get():Unit {
        InputHandler.skipWhiteSpacesNoNL(this)
    }

val Input.skipWhiteSpaces2EOL
    get():Unit {
        InputHandler.skipWhiteSpaces2EOL(this)
    }

fun Input.move(s: String, output: Input) {
    InputHandler.move(this, s, output)
}

infix fun Input.moveWhiteSpacesTo(sb: StringBuilder) {
    InputHandler.moveWhiteSpaces(this, sb)
}

infix fun Input.moveWhiteSpacesTo(sb: Input) {
    InputHandler.moveWhiteSpaces(this, sb)
}

val Input.trim
    get():Unit {
        InputHandler.trim(this)
    }

val Input.rtrim
    get():Unit {
        InputHandler.rtrim(this)
    }

val Input.eatEscapedNL
    get():Unit {
        InputHandler.eatEscapedNL(this)
    }

val Input.skip2EOL: Unit
    get() {
        InputHandler.skip2EOL(this)
    }

val Input.fetch2EOL
    get():String {
        return InputHandler.fetch2EOL(this)
    }

infix fun Input.getParameters(id: String): Array<String> {
    return InputHandler.getParameters(this, id)
}

val Input.getParts
    get():Array<String> = InputHandler.getParts(this)

fun Input.getParts(processor:Processor) = InputHandler.getParts(this,processor)

infix fun Input.getParts(i: Int): Array<String> = InputHandler.getParts(this, i)

fun Input.getParts(processor: Processor, i: Int): Array<String> =  InputHandler.getParts(this, processor, i)

val String.isJamalGlobalMacro
    get():Boolean {
        return InputHandler.isGlobalMacro(this)
    }

val String.convertJamalGlobal
    get():String {
        return InputHandler.convertGlobal(this)
    }

val String.isJamalIdentifier
    get():Boolean {
        return InputHandler.isIdentifier(this)
    }
