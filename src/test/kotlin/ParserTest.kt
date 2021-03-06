import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

internal class ParserTest {
    @Test
    fun basicOperations() {
        assertEquals(7, Operation.PLUS.operator(3, 4))
        assertEquals(-1, Operation.MINUS.operator(3, 4))
        assertEquals(12, Operation.TIMES.operator(3, 4))
        assertEquals(4, Operation.DIV.operator(9, 2))
        assertEquals(-1, Operation.DIV.operator(-3, 2))
        assertEquals(2, Operation.REM.operator(5, 3))
        assertEquals(1, Operation.GREATER.operator(5, 3))
        assertEquals(0, Operation.GREATER.operator(3, 3))
        assertEquals(0, Operation.GREATER.operator(3, 5))
        assertEquals(0, Operation.LESS.operator(5, 3))
        assertEquals(0, Operation.LESS.operator(3, 3))
        assertEquals(1, Operation.LESS.operator(3, 5))
        assertEquals(0, Operation.EQUALS.operator(5, 3))
        assertEquals(1, Operation.EQUALS.operator(3, 3))
        assertEquals(0, Operation.EQUALS.operator(3, 5))
    }

    @Test
    fun singleNumber() {
        assertEquals(0, Parser("000").expression.compute())
        assertEquals(79, Parser("79").expression.compute())
        assertEquals(-2147483648, Parser("-2147483648").expression.compute())
    }

    @TestFactory
    fun invalidExpression() =
        listOf("--1", "1+1", "1<<2", "(1+1)-", "", "((2*3)/3*2))", "-1+1)", "(1+1-").map { program ->
            DynamicTest.dynamicTest("Program \"$program\" contains syntax errors") {
                assertThrows(SyntaxError::class.java) { Parser(program).expression }
            }
        }

    @Test
    fun calculatorExpression() {
        assertEquals(4, Parser("(2+2)").expression.compute())
        assertEquals(-4, Parser("(-2+-2)").expression.compute())
        assertEquals(4, Parser("(2+((3*4)/5))").expression.compute())
        assertThrows(InterpreterException::class.java) { Parser("((2+8)%(3-3))").expression.compute() }
    }

    @Test
    fun invalidIfExpression() {
        assertThrows(SyntaxError::class.java) { Parser("[1>1]?{7}").expression }
        assertThrows(SyntaxError::class.java) { Parser("[1>1]?(7):{1}").expression }
        assertThrows(SyntaxError::class.java) { Parser("[1>1]??{7}:{1}").expression }
    }

    @Test
    fun ifExpression() {
        assertEquals(5, Parser("([(32>23)]?{7}:{1}+[0]?{2}:{-2})").expression.compute())
        assertEquals(0, Parser("[((10+20)>(20+10))]?{1}:{0}").expression.compute())
    }

    @Test
    fun functions() {
        assertEquals(
            60,
            Parser(
                listOf(
                    "g(x)={(f(x)+f((x/2)))}", "f(x)={[(x>1)]?{(f((x-1))+f((x-2)))}:{x}}", "g(10)"
                )
            ).expression.compute()
        )
    }

    @Test
    fun deepRecursion() {
        assertEquals(2000, Parser(listOf("f(x)={[(x>0)]?{(f((x-1))+2)}:{0}}", "f(1000)")).expression.compute())
    }

    @Test
    fun parameterNotFound() {
        assertThrows(
            InterpreterException::class.java,
            { Parser(listOf("f(x)={y}", "f(10)")).expression },
            "PARAMETER NOT FOUND y:1"
        )
    }

    @Test
    fun functionNotFound() {
        assertThrows(
            InterpreterException::class.java,
            { Parser(listOf("g(x)={f(x)}", "g(10)")).expression },
            "FUNCTION NOT FOUND f:1"
        )
    }

    @Test
    fun argumentNumberMismatch() {
        assertThrows(
            InterpreterException::class.java,
            { Parser(listOf("g(x)={(x+1)}", "g(10,20)")).expression },
            "ARGUMENT NUMBER MISMATCH g:2"
        )
    }

    @Test
    fun runtimeError() {
        assertThrows(
            InterpreterException::class.java,
            { Parser(listOf("g(a,b)={(a/b)}", "g(10,0)")).expression.compute() },
            "RUNTIME ERROR (a/b):1"
        )
    }

    @Test
    fun duplicateIdentifiers() {
        assertThrows(SyntaxError::class.java) { Parser(listOf("f(a,b)={1}", "f(a)={2}", "1")).expression }
        assertThrows(SyntaxError::class.java) { Parser(listOf("f(a,a)={(a+a)}", "-1")).expression }
    }
}
