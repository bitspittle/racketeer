package dev.bitspittle.racketeer.scripting.methods.card

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.ItemToSingletonListConverter
import dev.bitspittle.limp.listTypeOf
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateChange

class CardRemoveMethod(private val getGameState: () -> GameState, private val addGameChange: suspend (GameStateChange) -> Unit) : Method("card-remove!", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val cards = env.scoped {
            env.addConverter(ItemToSingletonListConverter(Card::class))
            env.expectConvert<List<Card>>(params[0], listTypeOf())
        }

        val gameState = getGameState()
        addGameChange(GameStateChange.MoveCards(gameState, cards, gameState.graveyard))

        return Unit
    }
}