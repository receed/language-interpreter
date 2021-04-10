class SyntaxError : Exception("SYNTAX ERROR")

class InterpreterException(message: String, name: String, line: Int) : Exception(
    "$message $name:${line + 1}"
)

/**
 * Builds expression tree for the given program.
 */
class Parser(private val program: Program) {
    constructor(lines: List<String>) : this(Program(lines))
    constructor(line: String) : this(line.split('\n'))

    private class FunctionDefinition(val parameterCount: Int, val expression: Expression)

    /**
     * Positions of parameters in the last processed parameter list.
     */
    private var parameterIndices = mapOf<String, Int>()

    /**
     * Parsed functions stored by their names.
     */
    private val functions = mutableMapOf<String, FunctionDefinition>()

    /**
     * All processed call expressions. Used to bind function bodies to calls.
     */
    private val callExpressions = mutableListOf<Expression.Call>()

    private fun parseConstantExpression(): Expression.Constant {
        val sign = if (program.trySkip('-')) "-" else ""
        val number = sign + generateSequence { program.nextOrNull { it.isDigit() } }.joinToString("")
        return Expression.Constant(number.toIntOrNull() ?: throw SyntaxError())
    }

    private fun parseBinaryExpression(): Expression.Binary {
        val startIndex = program.position
        program.skip('(')
        val left = parseExpression()
        val operation = Operation.bySymbol[program.next()] ?: throw throw SyntaxError()
        val right = parseExpression()
        program.skip(')')
        val expressionString = program.line
        val endIndex = program.position
        return Expression.Binary(
            left, operation, right, { expressionString.substring(startIndex, endIndex) },
            program.lineNumber
        )
    }

    private fun parseIfExpression(): Expression.If {
        program.skip('[')
        val condition = parseExpression()
        program.skipAll("]?{")
        val nonZeroCase = parseExpression()
        program.skipAll("}:{")
        val zeroCase = parseExpression()
        program.skip('}')
        return Expression.If(condition, nonZeroCase, zeroCase)
    }

    private fun Char.isCharacter(): Boolean =
        this in 'a'..'z' || this in 'A'..'Z' || this == '_'

    private fun parseIdentifier(): String =
        generateSequence { program.nextOrNull { it.isCharacter() } }.joinToString("").takeIf { it.isNotEmpty() }
            ?: throw SyntaxError()

    private fun parseFunctionDefinition() {
        val functionName = parseIdentifier()
        program.skip('(')
        val parameters = generateSequence {
            if (program.trySkip(')'))
                null
            else parseIdentifier().also {
                if (!program.trySkip(',') && program.peek() != ')') throw SyntaxError()
            }
        }.toList()
        parameterIndices = parameters.mapIndexed { index, name -> name to index }.toMap()
        if (parameterIndices.size < parameters.size)
            throw SyntaxError()
        program.skipAll("={")
        if (functions.contains(functionName))
            throw SyntaxError()
        functions[functionName] = FunctionDefinition(parameters.size, parseExpression())
        program.skip('}')
    }

    private fun parseParameterList(): List<Expression> {
        program.skip('(')
        return generateSequence {
            if (program.trySkip(')'))
                null
            else
                parseExpression().also {
                    if (!program.trySkip(',') && program.peek() != ')') throw SyntaxError()
                }
        }.toList()
    }

    private fun parseExpression(): Expression {
        return when (program.peek()) {
            '(' -> parseBinaryExpression()
            '[' -> parseIfExpression()
            '-', in '0'..'9' -> parseConstantExpression()
            else -> {
                val identifier = parseIdentifier()
                if (program.peek() == '(') {
                    val parameters = parseParameterList()
                    return Expression.Call(identifier, parameters).also { callExpressions.add(it) }
                } else {
                    Expression.Parameter(
                        parameterIndices[identifier] ?: throw InterpreterException(
                            "ARGUMENT NUMBER MISMATCH",
                            identifier,
                            program.lineNumber
                        )
                    )
                }
            }
        }
    }

    private fun parseProgram(): Expression {
        while (program.lineNumber + 1 < program.size) {
            parseFunctionDefinition()
            if (program.position != program.line.length)
                throw SyntaxError()
            program.nextLine()
        }
        val expression = parseExpression()
        if (program.position != program.line.length)
            throw SyntaxError()
        callExpressions.forEach { call ->
            val functionDefinition =
                functions[call.functionName] ?: throw InterpreterException(
                    "FUNCTION NOT FOUND",
                    call.functionName,
                    program.lineNumber
                )
            if (functionDefinition.parameterCount != call.parameterExpressions.size)
                throw InterpreterException("ARGUMENT NUMBER MISMATCH", call.functionName, program.lineNumber)
            call.function = functionDefinition.expression
        }
        return expression
    }

    val expression by lazy {
        parseProgram()
    }
}
