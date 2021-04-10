sealed class Expression {
    abstract val value: Int

    class Number(override val value: Int) : Expression()

    class Binary(val left: Expression, val operation: Operation, val right: Expression) : Expression() {
        override val value: Int
            get() {
                try {
                    return operation.operator(left.value, right.value)
                } catch (e: ArithmeticException) {
                    throw RuntimeError()
                }
            }
    }

    class If(val condition: Expression, val nonZeroCase: Expression, val zeroCase: Expression) :
        Expression() {
        override val value: Int
            get() {
                return if (condition.value != 0) nonZeroCase.value else zeroCase.value
            }
    }
}