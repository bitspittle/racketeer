package dev.bitspittle.racketeer.scripting.utils

import dev.bitspittle.limp.Environment
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.scripting.converters.PileToCardsConverter
import dev.bitspittle.racketeer.scripting.converters.MutablePileToCardsConverter
import dev.bitspittle.racketeer.scripting.methods.card.*
import dev.bitspittle.racketeer.scripting.methods.collection.ChooseMethod
import dev.bitspittle.racketeer.scripting.methods.effect.FxAddMethod
import dev.bitspittle.racketeer.scripting.methods.game.*
import dev.bitspittle.racketeer.scripting.methods.pile.PileCopyToMethod
import dev.bitspittle.racketeer.scripting.methods.pile.PileMoveToMethod
import dev.bitspittle.racketeer.scripting.methods.system.CancelMethod
import dev.bitspittle.racketeer.scripting.methods.system.StopMethod
import dev.bitspittle.racketeer.scripting.types.GameService

/**
 * Add a bunch of game-specific methods and other values here.
 */
fun Environment.installGameLogic(service: GameService) {
    // System
    addMethod(StopMethod(service::expectCardQueue))
    addMethod(CancelMethod())

    // Collection
    addMethod(ChooseMethod(service.chooseHandler))

    // Game
    addMethod(GameGetMethod(service::gameState))
    addMethod(GameSetMethod(service::gameState))
    addMethod(GameDrawMethod(service::gameState))
    addMethod(GameRemoveMethod(service::gameState))

    // Card
    addMethod(CardGetMethod())
    addMethod(CardSetMethod())
    addMethod(CardUpgradeMethod())
    addMethod(CardHasUpgradeMethod())
    addMethod(CardHasTypeMethod(service.gameData.cardTypes))
    addMethod(CardTriggerMethod(service::expectCardQueue))

    // Pile
    addConverter(MutablePileToCardsConverter())
    addConverter(PileToCardsConverter())
    addMethod(PileCopyToMethod(service::gameState))
    addMethod(PileMoveToMethod(service::gameState))

    // Effects
    addMethod(FxAddMethod(service::gameState))

    // Shop
    (0..4).forEach { i -> storeValue("\$tier${i + 1}", i) }
}

/**
 * Add all variables related to the current game state into the environment.
 *
 * You probably want to do this within an [Environment.scoped] block, to avoid ever accidentally referring to stale game
 * state from previous turns.
 */
fun GameState.addVariablesInto(env: Environment) {
    env.storeValue("\$all-cards", allCards)
    env.storeValue("\$shop-tier", shopTier)

    env.storeValue("\$deck", deck)
    env.storeValue("\$hand", hand)
    env.storeValue("\$street", street)
    env.storeValue("\$discard", discard)
    env.storeValue("\$jail", jail)

    env.storeValue("\$owned", listOf(deck, hand, street, discard, jail).flatMap { it.cards })
}

/**
 * Store the current card in the environment.
 *
 * You probably want to do this within an [Environment.scoped] block, tied to the lifetime of the current card being
 * played.
 */
fun Card.addVariableTo(env: Environment) {
    env.storeValue("\$this", this)
}
