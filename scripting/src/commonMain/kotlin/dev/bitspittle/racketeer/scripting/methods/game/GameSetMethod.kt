package dev.bitspittle.racketeer.scripting.methods.game

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.ValueToExprConverter
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.game.GameProperty
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateChange

class GameSetMethod(private val getGameState: () -> GameState, private val addGameChange: suspend (GameStateChange) -> Unit) : Method("game-set!", 2) {
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
            GameProperty.INFLUENCE -> gameState.influence
            GameProperty.LUCK -> gameState.luck
            GameProperty.HAND_SIZE -> gameState.handSize
            GameProperty.VP -> throw EvaluationException(
                identifier.ctx,
                "Cannot set this game's property as it is read-only."
            )
        }

        val newValue = env.scoped { // Don't let values defined during the lambda escape
            val evaluator = eval.extend(mapOf("\$it" to currValue))
            env.expectConvert<Int>(evaluator.evaluate(env, setExpr))
        }
        addGameChange(GameStateChange.AddGameAmount(property, newValue - currValue))

        return Unit
    }
}