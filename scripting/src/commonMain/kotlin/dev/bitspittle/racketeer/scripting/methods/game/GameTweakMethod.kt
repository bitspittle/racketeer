package dev.bitspittle.racketeer.scripting.methods.game

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.effect.Tweak
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.effect.Lifetime

enum class GameTweak {
    KEEP_UNSPENT,
}

/**
 * game-tweak! --lifetime ('Lifetime) ('Tweak) (value: Any)
 *
 * Tweak the behavior of the game, optionally constrained to some lifetime. If no lifetime is specified, this tweak will
 * last for the rest of the game.
 *
 * The type that the "value" parameter should be will depend on what tweak is being set.
 */
class GameTweakMethod(private val getGameState: () -> GameState) : Method("game-tweak!", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {

        val tweakIdentifier = env.expectConvert<Expr.Identifier>(params[0])
        val tweakType = tweakIdentifier.toEnum(GameTweak.values())

        val lifetime = options["lifetime"]?.let {
            val lifetimeIdentifier = env.expectConvert<Expr.Identifier>(it)
            lifetimeIdentifier.toEnum(Lifetime.values())
        } ?: Lifetime.GAME

        val tweak: Tweak.Game = when (tweakType) {
            GameTweak.KEEP_UNSPENT -> Tweak.Game.KeepUnspent(lifetime)
        }

        getGameState().apply(GameStateChange.AddGameTweak(tweak))

        return Unit
    }
}