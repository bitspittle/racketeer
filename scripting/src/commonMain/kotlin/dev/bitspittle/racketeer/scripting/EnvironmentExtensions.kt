package dev.bitspittle.racketeer.scripting

import dev.bitspittle.limp.Environment
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.scripting.converters.PileToCardsConverter
import dev.bitspittle.racketeer.scripting.converters.MutablePileToCardsConverter
import dev.bitspittle.racketeer.scripting.methods.card.CardAddMethod
import dev.bitspittle.racketeer.scripting.methods.card.CardGetMethod
import dev.bitspittle.racketeer.scripting.methods.card.CardSetMethod
import dev.bitspittle.racketeer.scripting.methods.system.DbgMethod
import dev.bitspittle.racketeer.scripting.methods.game.GameAddMethod
import dev.bitspittle.racketeer.scripting.methods.game.GameGetMethod
import dev.bitspittle.racketeer.scripting.methods.game.GameSetMethod
import dev.bitspittle.racketeer.scripting.types.GameService

/**
 * Add a bunch of game-specific methods and other values here.
 */
fun Environment.installGameLogic(service: GameService) {
    addConverter(MutablePileToCardsConverter())
    addConverter(PileToCardsConverter())

    addMethod(GameGetMethod(service::gameState))
    addMethod(GameSetMethod(service::gameState))
    addMethod(GameAddMethod(service::gameState))

    addMethod(CardGetMethod())
    addMethod(CardSetMethod())
    addMethod(CardAddMethod())
}

fun Environment.withCardVariables(gameState: GameState, card: Card, block: () -> Unit) {
    scoped {
        storeValue("\$this", card)
        storeValue("\$deck", gameState.deck)
        storeValue("\$hand", gameState.hand)
        storeValue("\$street", gameState.street)
        storeValue("\$discard", gameState.discard)
        storeValue("\$jail", gameState.jail)

        block()
    }
}