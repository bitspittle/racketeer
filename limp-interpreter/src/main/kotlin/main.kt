import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.utils.installDefaults
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val env = Environment()
    env.installDefaults()

    var shouldQuit = false
    env.addMethod(object : Method("quit", 0) {
        override suspend fun invoke(
            env: Environment,
            eval: Evaluator,
            params: List<Any>,
            options: Map<String, Any>,
            rest: List<Any>
        ): Any {
            shouldQuit = true
            return Unit
        }
    })

    println("Welcome to the Limp Interpreter")
    println()
    println("* Enter an expression. If it returns a value, it will be assigned to a variable called \"\$last\".")
    println("* End a line with \"\\\" if you want to split an expression out over multiple lines")
    println("* Type \"quit\" (or press CTRL-C) to end this program")
    println()

    val evaluator = Evaluator()
    val codeBuilder = StringBuilder()
    while (!shouldQuit) {
        print(if (codeBuilder.isEmpty()) "> " else "  ")
        codeBuilder.append(readln().trim())
        if (codeBuilder.endsWith('\\')) {
            codeBuilder.deleteAt(codeBuilder.lastIndex)
            codeBuilder.append('\n')
        }
        else {
            try {
                val code = codeBuilder.toString().also { codeBuilder.clear() }
                val result = evaluator.evaluate(env, code)
                if (result != Unit) {
                    env.storeValue("\$last", result, allowOverwrite = true)
                    println(result)
                }
            } catch (ex: Exception) {
                println(ex.message)
            }
        }
    }
}