package dev.bitspittle.racketeer.scripting.methods.card

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.ItemToSingletonListConverter
import dev.bitspittle.limp.listTypeOf
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.game.GameState

class RemoveMethod(private val getGameState: () -> GameState) : Method("remove!", 1) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val cards = env.scoped {
            env.addConverter(ItemToSingletonListConverter(Card::class))
            env.expectConvert<List<Card>>(params[0], listTypeOf())
        }

        getGameState().remove(cards)

        return Unit
    }
}