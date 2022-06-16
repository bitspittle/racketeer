package dev.bitspittle.racketeer.scripting.methods.shop

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.IntToIntRangeConverter
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.effect.Tweak
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.effect.Lifetime

enum class ShopTweak {
    FROZEN,
    PRICES,
    SIZE,
}

/**
 * shop-tweak! --lifetime ('Lifetime) ('Tweak) (value: Any)
 *
 * Tweak the behavior of the shop, optionally constrained to some lifetime. If no lifetime is specified, this tweak will
 * last for the rest of the game.
 *
 * The type that the "value" parameter should be will depend on what tweak is being set.
 */
class ShopTweakMethod(private val getGameState: () -> GameState) : Method("shop-tweak!", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {

        val tweakIdentifier = env.expectConvert<Expr.Identifier>(params[0])
        val tweakType = tweakIdentifier.toEnum(ShopTweak.values())

        val lifetime = options["lifetime"]?.let {
            val lifetimeIdentifier = env.expectConvert<Expr.Identifier>(it)
            lifetimeIdentifier.toEnum(Lifetime.values())
        } ?: Lifetime.GAME

        val tweak = when (tweakType) {
            ShopTweak.FROZEN -> Tweak.Shop.Frozen(lifetime)
            ShopTweak.PRICES -> {
                val amount = env.scoped {
                    env.addConverter(IntToIntRangeConverter())
                    env.expectConvert<IntRange>(params[1])
                }
                Tweak.Shop.Prices(lifetime, amount)
            }

            ShopTweak.SIZE -> Tweak.Shop.Size(lifetime, env.expectConvert(params[1]))
        }

        getGameState().apply(GameStateChange.AddShopTweak(tweak))

        return Unit
    }
}