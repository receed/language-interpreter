enum class Operation(val symbol: Char, val operator: (Int, Int) -> Int) {
    PLUS('+', Int::plus),
    MINUS('-', Int::minus),
    TIMES('*', Int::times),
    DIV('/', Int::div),
    REM('%', Int::rem),
    GREATER('>', { x, y -> if (x > y) 1 else 0 }),
    LESS('<', { x, y -> if (x < y) 1 else 0 }),
    EQUALS('=', { x, y -> if (x == y) 1 else 0 });

    companion object {
        val bySymbol = Operation.values().map { it.symbol to it }.toMap()
    }
}
