import java.lang.ArithmeticException
import java.lang.Exception

class InterpreterException(message: String) : Exception(message)

enum class Operation(val symbol: Char, val operator: (Int, Int) -> Int) {
    PLUS('+', Int::plus),
    MINUS('-', Int::minus),
    TIMES('*', Int::times),
    DIV('/', Int::div),
    REM('%', Int::rem),
    GREATER('>', {x, y -> if (x > y) 1 else 0}),
    LESS('<', {x, y -> if (x < y) 1 else 0}),
    EQUALS('=', {x, y -> if (x == y) 1 else 0})
}

sealed class Expression {
    abstract val value: Int

    class Number(override val value: Int): Expression()

    class BinaryExpression(val left: Expression, val operation: Operation, val right: Expression) : Expression() {
        override val value: Int
            get() {
                try {
                    return operation.operator(left.value, right.value)
                } catch (e: ArithmeticException) {
                    throw InterpreterException("RUNTIME ERROR")
                }
            }
    }
}
