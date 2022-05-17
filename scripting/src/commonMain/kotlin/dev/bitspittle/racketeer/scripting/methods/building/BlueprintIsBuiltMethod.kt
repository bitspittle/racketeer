package dev.bitspittle.racketeer.scripting.methods.building

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.building.BlueprintProperty
import dev.bitspittle.racketeer.model.building.isBuilt
import dev.bitspittle.racketeer.model.building.isOwned
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateChange

/**
 * blueprint-is-built? (Blueprint) -> Bool
 *
 * Return true if the user ever built a building from this blueprint.
 *
 * See also [BlueprintIsOwnedMethod]
 */
class BlueprintIsBuiltMethod(private val getGameState: () -> GameState) : Method("blueprint-is-built?", 1) {
    override suspend fun invoke(
        env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>
    ): Any {
        return env.expectConvert<Blueprint>(params[0]).isBuilt(getGameState())
    }
}