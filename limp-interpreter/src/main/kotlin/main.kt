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
            params: List<Any>,
            options: Map<String, Any>,
            rest: List<Any>
        ): Any {
            shouldQuit = true
            return Unit
        }
    })

    val evaluator = Evaluator()
    while (!shouldQuit) {
        print("> ")
        val input = readln()
        try {
            val result = evaluator.evaluate(env, input)
            if (result != Unit) {
                env.storeValue("\$last", result, allowOverwrite = true)
                println(result)
            }
        } catch (ex: Exception) {
            println(ex.message)
        }
    }
}