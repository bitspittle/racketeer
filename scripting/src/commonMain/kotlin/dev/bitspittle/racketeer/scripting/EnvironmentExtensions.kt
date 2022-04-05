package dev.bitspittle.racketeer.scripting

import dev.bitspittle.limp.Environment
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.scripting.converters.PileToCardsConverter
import dev.bitspittle.racketeer.scripting.methods.GameAddMethod

/**
 * Add a bunch of game-specific methods and other values here.
 *
 * @param getGameState Returns the current game state (via a callback as it will change throughout the lifetime of the
 *   game)
 */
fun Environment.installGameLogic(getGameState: () -> GameState) {
    addConverter(PileToCardsConverter())

    addMethod(GameAddMethod(getGameState))
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