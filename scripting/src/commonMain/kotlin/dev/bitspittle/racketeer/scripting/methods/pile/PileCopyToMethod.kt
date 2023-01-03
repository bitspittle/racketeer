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
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.MutableCard
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.scripting.converters.CardTemplateToCardConverter
import dev.bitspittle.racketeer.scripting.converters.PileToCardsConverter

class PileCopyToMethod(private val getGameState: () -> GameState, private val addGameChange: suspend (GameStateChange) -> Unit) : Method("pile-copy-to!", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val toPile = env.expectConvert<Pile>(params[0])
        val cards: List<Card> = env.scoped {
            env.addConverter(CardTemplateToCardConverter() + ItemToSingletonListConverter(Card::class))
            env.addConverter(TransformListConverter(CardTemplateToCardConverter()))
            env.addConverter(ItemToSingletonListConverter(Card::class))
            env.addConverter(PileToCardsConverter())

            env.expectConvert(params[1], ListTypeChecker(Card::class))
        }.map { origCard -> MutableCard(origCard) }

        val strategy = options["pos"]?.let {
            env.expectConvert<Expr.Identifier>(it).toEnum(ListStrategy.values())
        } ?: ListStrategy.BACK

        val gameState = getGameState()
        addGameChange(GameStateChange.MoveCards(gameState, cards, toPile, strategy))

        return Unit
    }
}