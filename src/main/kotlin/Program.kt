/**
 * Represents program text. Allows to sequentially access program characters and observe the next character.
 */
class Program(private val lines: List<String>) {
    val a = lines.iterator()
    var lineNumber = 0
        private set
    val line: String
        get() = lines[lineNumber]
    var position: Int = 0
        private set
    val size = lines.size

    /**
     * Returns the next character in the current line or `null` if there is no more characters
     * in the current line.
     */
    fun peek(): Char? = lines[lineNumber].getOrNull(position)

    /**
     * If the next character in the current line matches [predicate], skips it and returns it; otherwise returns `null`.
     */
    fun nextOrNull(predicate: (Char) -> Boolean): Char? =
        lines[lineNumber].getOrNull(position)?.takeIf(predicate)?.also { position++ }

    /**
     * If the next character in the current line is [character], skips it and returns `true`; otherwise return `false`.
     */
    fun trySkip(character: Char): Boolean = (lines[lineNumber].getOrNull(position) == character).also {
        if (it)
            position++
    }

    /**
     * If the next character in the current line is [character], skips it; otherwise throws [SyntaxError].
     */
    fun skip(character: Char) {
        if (!trySkip(character))
            throw SyntaxError()
    }

    /**
     * If the next characters in the current line are [characters], skips them all; otherwise throws [SyntaxError].
     */
    fun skipAll(characters: String) = characters.forEach { skip(it) }

    /**
     * Skips the next character in the current line and returns it; if there are no more characters, throws [SyntaxError].
     */
    fun next(): Char = lines[lineNumber].getOrNull(position++) ?: throw SyntaxError()

    /**
     * Skips the remaining characters in the current line and goes to the next line.
     */
    fun nextLine() {
        lineNumber++
        position = 0
    }
}