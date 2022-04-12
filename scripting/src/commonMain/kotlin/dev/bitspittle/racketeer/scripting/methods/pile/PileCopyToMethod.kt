package dev.bitspittle.racketeer.scripting.methods.pile

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.ListTypeChecker
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.ItemToSingletonListConverter
import dev.bitspittle.limp.converters.TransformListConverter
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.card.Pile
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.scripting.converters.CardToCardTemplateConverter
import dev.bitspittle.racketeer.scripting.converters.PileToCardTemplatesConverter

class PileCopyToMethod(private val getGameState: () -> GameState) : Method("pile-copy-to!", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val toPile = env.expectConvert<Pile>(params[0])
        val cards = env.scoped {
            env.addConverter(CardToCardTemplateConverter() + ItemToSingletonListConverter(CardTemplate::class))
            env.addConverter(TransformListConverter(CardToCardTemplateConverter()))
            env.addConverter(ItemToSingletonListConverter(CardTemplate::class))
            env.addConverter(PileToCardTemplatesConverter())

            env.expectConvert(params[1], ListTypeChecker(CardTemplate::class))
        }.map { it.instantiate() }

        val strategy = options["pos"]?.let {
            env.expectConvert<Expr.Identifier>(it).toEnum(ListStrategy.values())
        } ?: ListStrategy.BACK

        val gameState = getGameState()
        return gameState.move(cards, toPile, strategy)
    }
}