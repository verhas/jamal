package javax0.jamal.kotlin

import javax0.jamal.kotlin.testsupport.Position
import javax0.jamal.kotlin.testsupport.testTheInput
import org.junit.jupiter.api.Test

class TestHelloKotlin {

    @Test
    fun testHelloWorld() {
        (testTheInput("""
            {@hellokotlin boolean list=1 ONE list="haha"
            zopa}
            """
        ) atPosition Position("rebarbara.txt")).usingTheSeparators("{", "}") results """
            Hello string=Peter Kotlin ONE World YES 2 1 haha zopa 1
            """
    }
}