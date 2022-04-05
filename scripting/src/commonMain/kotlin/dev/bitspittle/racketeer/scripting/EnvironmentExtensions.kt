package dev.bitspittle.racketeer.scripting

import dev.bitspittle.limp.Environment
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.scripting.converters.PileToCardsConverter

/**
 * Add a bunch of game-specific methods and other values here.
 */
fun Environment.installGameLogic() {
    addConverter(PileToCardsConverter())
}

fun Environment.withCardVariables(gameState: GameState, block: () -> Unit) {
    scoped {
        storeValue("\$deck", gameState.deck)
        storeValue("\$hand", gameState.hand)
        storeValue("\$street", gameState.street)
        storeValue("\$discard", gameState.discard)
        storeValue("\$jail", gameState.jail)

        block()
    }
}