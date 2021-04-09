open class InterpreterException(message: String) : Exception(message)
class SyntaxError : InterpreterException("SYNTAX ERROR")
class RuntimeError : InterpreterException("RUNTIME ERROR")


class Parser(private val program: String) {
    private var position: Int = 0

    private fun peek(): Char? = program.getOrNull(position)
    private fun nextIf(predicate: (Char) -> Boolean) =
        program.getOrNull(position)?.takeIf(predicate)?.also { position++ }

    private fun next(predicate: (Char) -> Boolean) = nextIf(predicate) ?: throw SyntaxError()
    private fun next(symbol: Char): Boolean = nextIf { it == symbol } != null
    private fun next(): Char = program.getOrNull(position++) ?: throw SyntaxError()

    private fun parseConstantExpression(): Expression.Number {
        val sign = if (next('-')) "-" else ""
        val number = sign + generateSequence { nextIf { it.isDigit() } }.joinToString("")
        return Expression.Number(number.toIntOrNull() ?: throw SyntaxError())
    }

    private fun parseBinaryExpression(): Expression.Binary {
        next('(')
        val left = parseExpression()
        val operation = Operation.bySymbol[next()] ?: throw throw SyntaxError()
        val right = parseExpression()
        next(')')
        return Expression.Binary(left, operation, right)
    }

    private fun parseExpression(): Expression {
        if (peek() == '(')
            return parseBinaryExpression()
        return parseConstantExpression()
    }

    val expression by lazy {
        parseExpression().also { if (position != program.length) throw SyntaxError() }
    }
}
