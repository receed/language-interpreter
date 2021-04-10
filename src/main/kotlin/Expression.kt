/**
 * Represents expression.
 */
sealed class Expression {
    /**
     * Computes the value of the expression.
     * @param parameters values of parameters of the function inside which the expression is located,
     * in the same order they are declared.
     */
    abstract fun compute(parameters: List<Int>): Int

    /**
     * Computes the value of the expression if it does not belong to a function.
     */
    fun compute(): Int = compute(listOf())

    /**
     * Constant expression.
     */
    class Constant(private val value: Int) : Expression() {
        override fun compute(parameters: List<Int>): Int = value
    }

    /**
     * Binary expression.
     * @property left left-hand-side operand
     * @property operation binary operation
     * @property right right-hand-side operand
     * @property string function that computes string representation of the expression
     * @property line number of line where the expression is located
     */
    class Binary(
        private val left: Expression, private val operation: Operation, private val right: Expression,
        private val string: () -> String, private val line: Int
    ) :
        Expression() {
        override fun compute(parameters: List<Int>): Int {
            try {
                return operation.operator(left.compute(parameters), right.compute(parameters))
            } catch (e: ArithmeticException) {
                throw InterpreterException("RUNTIME ERROR", string(), line)
            }
        }
    }

    /**
     * If expression.
     * Computes [nonZeroCase] If value of condition is not equal to zero and [zeroCase] otherwise.
     */
    class If(private val condition: Expression, private val nonZeroCase: Expression, private val zeroCase: Expression) :
        Expression() {
        override fun compute(parameters: List<Int>): Int {
            return if (condition.compute(parameters) != 0)
                nonZeroCase.compute(parameters)
            else
                zeroCase.compute(parameters)
        }
    }

    /**
     * Expression equal to a parameter of the enclosing function.
     * @property index position of the parameter in the function's parameter list
     */
    class Parameter(private val index: Int) : Expression() {
        override fun compute(parameters: List<Int>): Int = parameters[index]
    }

    /**
     * Call expression.
     */
    class Call(val functionName: String, val parameterExpressions: List<Expression>) : Expression() {
        /**
         * Reference to function body expression; must be initialized before the expression is evaluated.
         */
        lateinit var function: Expression
        override fun compute(parameters: List<Int>): Int {
            val parameterValues = parameterExpressions.map { it.compute(parameters) }
            return function.compute(parameterValues)
        }
    }
}