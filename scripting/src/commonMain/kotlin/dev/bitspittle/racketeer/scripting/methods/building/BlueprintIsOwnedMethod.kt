package dev.bitspittle.racketeer.scripting.methods.building

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.building.BlueprintProperty
import dev.bitspittle.racketeer.model.building.isOwned
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateChange

/**
 * blueprint-is-owned? (Blueprint) -> Bool
 *
 * Return true if this blueprint is currently in the user's blueprint pile OR if they owned it previously and built a
 * building with it.
 *
 * See also [BlueprintIsBuiltMethod]
 */
class BlueprintIsOwnedMethod(private val getGameState: () -> GameState) : Method("blueprint-is-owned?", 1) {
    override suspend fun invoke(
        env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>
    ): Any {
        return env.expectConvert<Blueprint>(params[0]).isOwned(getGameState())
    }
}