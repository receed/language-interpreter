class Program(private val lines: List<String>) {
    var lineNumber = 0
        private set
    val line
        get() = lines[lineNumber]
    var position: Int = 0
        private set
    val size = lines.size

    fun peek(): Char? = lines[lineNumber].getOrNull(position)
    fun nextOrNull(predicate: (Char) -> Boolean) =
        lines[lineNumber].getOrNull(position)?.takeIf(predicate)?.also { position++ }

    fun next(symbol: Char) = nextOrNull { it == symbol } ?: throw SyntaxError()
    fun next(symbols: String) = symbols.forEach { next(it) }
    fun next(): Char = lines[lineNumber].getOrNull(position++) ?: throw SyntaxError()

    fun nextLine() {
        lineNumber++
        position = 0
    }
}