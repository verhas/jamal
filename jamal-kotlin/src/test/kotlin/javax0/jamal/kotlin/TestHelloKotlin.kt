package javax0.jamal.kotlin

import javax0.jamal.testsupport.TestThat
import org.junit.jupiter.api.Test

class TestHelloKotlin {

    @Test
    fun testHelloWorld() {
        TestThat.theInput("{@hellokotlin boolean list=1 list=\"haha\" stringy=\"habakukk\"}").results("Hello Kotlin World")
    }
}