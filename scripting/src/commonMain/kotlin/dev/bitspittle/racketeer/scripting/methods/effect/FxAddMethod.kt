package dev.bitspittle.racketeer.scripting.methods.effect

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.racketeer.model.game.GameState

class FxAddMethod(private val getGameState: () -> GameState) : Method("fx-add!", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val effectExpr = env.expectConvert<Expr>(params[0])
        val desc = options["desc"]?.let { env.expectConvert<String>(it) } ?: effectExpr.ctx.text
        getGameState().installStreetEffect(desc) { card ->
            eval.extend(mapOf("\$card" to card)).evaluate(env, effectExpr)
        }

        return Unit
    }
}