package dev.bitspittle.racketeer.scripting.methods.game

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.scripting.types.GameProperty
import dev.bitspittle.racketeer.scripting.types.GameService

class GameGetMethod(private val getGameState: () -> GameState) : Method("game-get", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val identifier = env.expectConvert<Expr.Identifier>(params[0])
        val property = identifier.toEnum(GameProperty.values())

        val gameState = getGameState()
        return when (property) {
            GameProperty.CASH -> gameState.cash
            GameProperty.VP -> gameState.vp
            GameProperty.INFLUENCE -> gameState.influence
            GameProperty.LUCK -> gameState.luck
            GameProperty.HAND_SIZE -> gameState.handSize
        }
    }
}