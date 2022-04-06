package dev.bitspittle.racketeer.scripting.methods.pile

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.ItemToSingletonListConverter
import dev.bitspittle.limp.listTypeOf
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.Pile
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.scripting.converters.PileToCardsConverter

class MoveToMethod(private val getGameState: () -> GameState) : Method("move-to!", 2) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val toPile = env.expectConvert<Pile>(params[0])
        val cards = env.scoped {
            env.addConverter(ItemToSingletonListConverter(Card::class))
            env.addConverter(PileToCardsConverter())

            env.expectConvert(params[1], listTypeOf<Card>())
        }

        val strategy = options["pos"]?.let {
            env.expectConvert<Expr.Identifier>(it).toEnum(ListStrategy.values())
        } ?: ListStrategy.BACK

        val gameState = getGameState()
        return gameState.move(cards, toPile, strategy)
    }
}