class SyntaxError : Exception("SYNTAX ERROR")
class RuntimeError : Exception("RUNTIME ERROR")
class InterpreterException(message: String, name: String, line: Int) : Exception(
    "$message $name:$line"
)

class Parser(private val program: List<String>) {
    data class FunctionDefinition(val parameterCount: Int, val expression: Expression)

    private var line = 0
    private var position: Int = 0
    private var parameterIndices = mapOf<String, Int>()
    private val functions = mutableMapOf<String, FunctionDefinition>()
    private val callExpressions = mutableListOf<Expression.Call>()

    private fun peek(): Char? = program[line].getOrNull(position)
    private fun nextOrNull(predicate: (Char) -> Boolean) =
        program[line].getOrNull(position)?.takeIf(predicate)?.also { position++ }

    private fun next(predicate: (Char) -> Boolean) = nextOrNull(predicate) ?: throw SyntaxError()
    private fun next(symbol: Char) = nextOrNull { it == symbol } ?: throw SyntaxError()
    private fun next(): Char = program[line].getOrNull(position++) ?: throw SyntaxError()

    private fun parseConstantExpression(): Expression.Number {
        val sign = if (peek() == '-') {
            next(); "-"
        } else ""
        val number = sign + generateSequence { nextOrNull { it.isDigit() } }.joinToString("")
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

    private fun parseIfExpression(): Expression.If {
        next('[')
        val condition = parseExpression()
        "]?{".forEach { next(it) }
        val nonZeroCase = parseExpression()
        "}:{".forEach { next(it) }
        val zeroCase = parseExpression()
        next('}')
        return Expression.If(condition, nonZeroCase, zeroCase)
    }

    private fun Char.isCharacter(): Boolean =
        this in 'a'..'z' || this in 'A'..'Z' || this == '_'

    private fun parseIdentifier(): String =
        generateSequence { nextOrNull { it.isCharacter() } }.joinToString("").takeIf { it.isNotEmpty() }
            ?: throw SyntaxError()

    private fun parseFunctionDefinition() {
        val functionName = parseIdentifier()
        next('(')
        val parameters = generateSequence {
            if (peek() == ')')
                null
            else parseIdentifier().also {
                nextOrNull { it == ',' } ?: if (peek() != ')') throw SyntaxError()
            }
        }.toList()
        parameterIndices = parameters.mapIndexed { index, name -> name to index }.toMap()
        if (parameterIndices.size < parameters.size)
            throw SyntaxError()
        ")={".forEach { next(it) }
        if (functions.contains(functionName))
            throw RuntimeError()
        functions[functionName] = FunctionDefinition(parameters.size, parseExpression())
        next('}')
    }

    private fun parseParameterList(): List<Expression> {
        next('(')
        return generateSequence {
            if (nextOrNull { it == ')' } != null)
                null
            else
                parseExpression().also {
                    nextOrNull { it == ',' } ?: if (peek() != ')') throw SyntaxError()
                }
        }.toList()
    }

    private fun parseExpression(): Expression {
        return when (peek()) {
            '(' -> parseBinaryExpression()
            '[' -> parseIfExpression()
            '-', in '0'..'9' -> parseConstantExpression()
            else -> {
                val identifier = parseIdentifier()
                if (peek() == '(') {
                    val parameters = parseParameterList()
                    return Expression.Call(identifier, parameters).also { callExpressions.add(it) }
                } else {
                    Expression.Parameter(parameterIndices[identifier] ?: throw SyntaxError())
                }
            }
        }
    }

    private fun parseProgram(): Expression {
        while (line + 1 < program.size) {
            position = 0
            parseFunctionDefinition()
            if (position != program[line].length)
                throw SyntaxError()
            line++
        }
        position = 0
        val expression = parseExpression()
        if (position != program[line].length)
            throw SyntaxError()
        callExpressions.forEach { call ->
            val functionDefinition =
                functions[call.functionName] ?: throw InterpreterException("FUNCTION NOT FOUND", call.functionName, 0)
            if (functionDefinition.parameterCount != call.parameterExpressions.size)
                throw InterpreterException("ARGUMENT NUMBER MISMATCH", call.functionName, 0)
            call.function = functionDefinition.expression
        }
        return expression
    }

    val expression by lazy {
        parseProgram()
    }
}
