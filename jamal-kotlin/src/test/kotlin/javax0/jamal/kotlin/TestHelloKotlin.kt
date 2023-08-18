package javax0.jamal.kotlin

import javax0.jamal.kotlin.testsupport.Position
import javax0.jamal.kotlin.testsupport.testTheInput
import org.junit.jupiter.api.Test

class TestHelloKotlin {

    @Test
    fun testHelloWorld() {
        (testTheInput("""
            {@hellokotlin boolean list=1 list="haha" stringy="habakukk"}
            """
        ) atPosition Position("rebarbara.txt")).usingTheSeparators("{", "}") results """
            Hello stringy=habakukk Kotlin World YES 2 1 haha 
            """
    }
}