package dev.bitspittle.racketeer.scripting.methods.game

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.ValueToExprConverter
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.scripting.types.GameProperty

class GameSetMethod(private val getGameState: () -> GameState) : Method("game-set!", 2) {
    override suspend fun invoke(env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val identifier = env.expectConvert<Expr.Identifier>(params[0])
        val property = identifier.toEnum(GameProperty.values())

        val setExpr = env.scoped {
            env.addConverter(ValueToExprConverter(Int::class))
            env.expectConvert<Expr>(params[1])
        }

        val gameState = getGameState()
        val currValue = when (property) {
            GameProperty.CASH -> gameState.cash
            GameProperty.VP -> gameState.vp
            GameProperty.INFLUENCE -> gameState.influence
        }

        val evaluator = eval.extend(mapOf("\$it" to currValue))
        val newValue = env.expectConvert<Int>(evaluator.evaluate(env, setExpr))
        when (property) {
            GameProperty.CASH -> gameState.cash = newValue
            GameProperty.VP -> gameState.vp = newValue
            GameProperty.INFLUENCE -> gameState.influence = newValue
        }

        return Unit
    }
}