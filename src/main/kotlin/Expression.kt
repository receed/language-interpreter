sealed class Expression {
    abstract fun compute(parameters: List<Int>): Int
    fun compute(): Int = compute(listOf())

    class Number(private val value: Int) : Expression() {
        override fun compute(parameters: List<Int>): Int = value
    }

    class Binary(private val left: Expression, private val operation: Operation, private val right: Expression) :
        Expression() {
        override fun compute(parameters: List<Int>): Int {
            try {
                return operation.operator(left.compute(parameters), right.compute(parameters))
            } catch (e: ArithmeticException) {
                throw RuntimeError()
            }
        }
    }

    class If(private val condition: Expression, private val nonZeroCase: Expression, private val zeroCase: Expression) :
        Expression() {
        override fun compute(parameters: List<Int>): Int {
            return if (condition.compute(parameters) != 0)
                nonZeroCase.compute(parameters)
            else
                zeroCase.compute(parameters)
        }
    }

    class Parameter(private val index: Int) : Expression() {
        override fun compute(parameters: List<Int>): Int = parameters[index]
    }

    class Call(val functionName: String, val parameterExpressions: List<Expression>) : Expression() {
        lateinit var function: Expression
        override fun compute(parameters: List<Int>): Int {
            val parameterValues = parameterExpressions.map { it.compute(parameters) }
            return function.compute(parameterValues)
        }
    }
}