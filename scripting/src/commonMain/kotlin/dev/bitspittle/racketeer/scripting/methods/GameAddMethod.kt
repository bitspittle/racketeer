package dev.bitspittle.racketeer.scripting.methods

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.scripting.types.GameResources

/**
 * game-add (resource: 'Ident) (value: Int)
 */
class GameAddMethod(private val produceGameState: () -> GameState) : Method("game-add", 2) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val nameIdent = env.expectConvert<Expr.Identifier>(params[0])
        val resource = GameResources.values().single { it.name.lowercase() == nameIdent.name }
        val amount = env.expectConvert<Int>(params[1])

        val gameState = produceGameState()
        when (resource) {
            GameResources.CASH -> gameState.cash += amount
            GameResources.VP -> gameState.vp += amount
            GameResources.INFLUENCE -> gameState.influence += amount
        }

        return Unit
    }
}