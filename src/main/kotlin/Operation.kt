/**
 * Represents binary operation.
 * @property character character corresponding to the operation.
 * @property operator function that computes value of the operation for two arguments.
 */
enum class Operation(val character: Char, val operator: (Int, Int) -> Int) {
    PLUS('+', Int::plus),
    MINUS('-', Int::minus),
    TIMES('*', Int::times),
    DIV('/', Int::div),
    REM('%', Int::rem),
    GREATER('>', { x, y -> if (x > y) 1 else 0 }),
    LESS('<', { x, y -> if (x < y) 1 else 0 }),
    EQUALS('=', { x, y -> if (x == y) 1 else 0 });

    companion object {
        val bySymbol = values().map { it.character to it }.toMap()
    }
}
