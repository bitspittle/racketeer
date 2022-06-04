package dev.bitspittle.racketeer.scripting.methods.game

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.game.Feature
import dev.bitspittle.racketeer.model.game.GameState

/**
 * game-has-feature? ('Feature) -> Bool
 */
class GameHasFeatureMethod(private val getGameState: () -> GameState) : Method("game-has-feature?", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val identifier = env.expectConvert<Expr.Identifier>(params[0])
        val featureType = identifier.toEnum(Feature.Type.values())

        return getGameState().features.contains(featureType)
    }
}