package dev.bitspittle.racketeer.scripting.methods

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.scripting.types.GameResources
import dev.bitspittle.racketeer.scripting.types.GameService

/**
 * game-add (resource: 'Ident) (value: Int)
 */
class GameAddMethod(private val service: GameService) : Method("game-add", 2) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val nameIdent = env.expectConvert<Expr.Identifier>(params[0])
        val resource = nameIdent.toEnum(GameResources.values())
        val amount = env.expectConvert<Int>(params[1])

        val gameState = service.gameState
        when (resource) {
            GameResources.CASH -> gameState.cash += amount
            GameResources.VP -> gameState.vp += amount
            GameResources.INFLUENCE -> gameState.influence += amount
        }

        return Unit
    }
}