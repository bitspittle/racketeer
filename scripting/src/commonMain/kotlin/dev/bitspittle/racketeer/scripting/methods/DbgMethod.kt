package dev.bitspittle.racketeer.scripting.methods

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.scripting.types.GameResources
import dev.bitspittle.racketeer.scripting.types.GameService

/**
 * dbg --msg (String) (Any)
 *   returns (Any)
 *
 * Print a value out to the console, passing it through afterwards, so you can temporarily insert a dbg statement into
 * a chain while experimenting.
 */
class DbgMethod(private val service: GameService) : Method("dbg", 1) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val message = options["msg"]?.let { env.expectConvert<String>(it) }
        service.log(buildString {
            append("[DBG] ")
            if (!message.isNullOrBlank()) {
                append(message)
                append(": ")
            }
            append(params[0])
        })

        return params[0]
    }
}