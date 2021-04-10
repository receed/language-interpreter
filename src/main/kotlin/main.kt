fun main() {
    // reads lines until EOF
    val program = generateSequence(::readLine).toList()
    try {
        val expression = Parser(program).expression
        println(expression.compute())
    } catch (e: Exception) {
        println(e.message)
    }
}