fun main() {
    val program = readLine()!!
    try {
        val expression = Parser(program).expression
        println(expression.value)
    } catch (e: InterpreterException) {
        println(e.message)
    }
}