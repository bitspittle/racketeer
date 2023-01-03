package dev.bitspittle.racketeer.scripting.utils

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.building.isOwned
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.allCards
import dev.bitspittle.racketeer.model.game.getOwnedCards
import dev.bitspittle.racketeer.scripting.converters.MutablePileToCardsConverter
import dev.bitspittle.racketeer.scripting.converters.PileToCardsConverter
import dev.bitspittle.racketeer.scripting.methods.building.*
import dev.bitspittle.racketeer.scripting.methods.card.*
import dev.bitspittle.racketeer.scripting.methods.collection.ChooseMethod
import dev.bitspittle.racketeer.scripting.methods.effect.FxAddMethod
import dev.bitspittle.racketeer.scripting.methods.game.*
import dev.bitspittle.racketeer.scripting.methods.pile.PileCopyToMethod
import dev.bitspittle.racketeer.scripting.methods.pile.PileGetMethod
import dev.bitspittle.racketeer.scripting.methods.pile.PileMoveToMethod
import dev.bitspittle.racketeer.scripting.methods.shop.ShopPriceForMethod
import dev.bitspittle.racketeer.scripting.methods.shop.ShopRerollMethod
import dev.bitspittle.racketeer.scripting.methods.shop.ShopTweakMethod
import dev.bitspittle.racketeer.scripting.methods.system.CancelMethod
import dev.bitspittle.racketeer.scripting.methods.system.RunLaterMethod
import dev.bitspittle.racketeer.scripting.methods.system.StopMethod
import dev.bitspittle.racketeer.scripting.methods.text.IconConvertMethod
import dev.bitspittle.racketeer.scripting.types.GameService

/**
 * Add a bunch of game-specific methods and other values here.
 */
fun Environment.installGameLogic(service: GameService) {
    // System
    addMethod(StopMethod(service.enqueuers.actionQueue))
    addMethod(CancelMethod())
    addMethod(RunLaterMethod(service.enqueuers.actionQueue))

    // Collection
    addMethod(ChooseMethod(service.logger, service.chooseHandler))

    // Game
    addMethod(GameHasFeatureMethod(service::gameState))
    addMethod(GameGetMethod(service::gameState))
    addMethod(GameSetMethod(service::gameState, service::addGameChange))
    addMethod(GameDrawMethod(service::gameState, service::addGameChange))
    addMethod(GameDataGetMethod(service::gameState))
    addMethod(GameDataSetMethod(service::addGameChange))
    addMethod(GameDataIsSetMethod(service::gameState))
    addMethod(GameTweakMethod(service::addGameChange))
    // We're supplanting the underlying shuffle method with our own specialized version (which delegates to the original
    // method when it can)
    addMethod(GameShuffleMethod(service::gameState, service::addGameChange), allowOverwrite = true)

    // Card
    addMethod(CardGetMethod())
    addMethod(CardSetMethod(service::addGameChange))
    addMethod(CardAddTraitMethod(service::addGameChange))
    addMethod(CardRemoveTraitMethod(service::addGameChange))
    addMethod(CardHasTraitMethod())
    addMethod(CardUpgradeMethod(service::addGameChange))
    addMethod(CardUpgradesMethod())
    addMethod(CardHasUpgradeMethod())
    addMethod(CardHasTypeMethod(service.gameData.cardTypes))
    addMethod(CardRemoveMethod(service::gameState, service::addGameChange))
    addMethod(CardTriggerMethod(service.enqueuers.card, service::gameState))
    addMethod(CardPlayMethod(service::addGameChange))
    addMethod(CardPileMethod(service::gameState))
    addMethod(CardInstantiateMethod(service.enqueuers.card, service::gameState))
    addMethod(RandomCardsMethod(service.gameData.cards, service.gameData.rarities) { service.gameState.random() })
    storeValue("\$card-list", service.gameData.cards)
    addMethod(object : Method("\$owned-cards", 0) {
        override suspend fun invoke(
            env: Environment,
            eval: Evaluator,
            params: List<Any>,
            options: Map<String, Any>,
            rest: List<Any>
        ): Any {
            return service.gameState.getOwnedCards().toList()
        }
    })

    addMethod(object : Method("\$all-cards", 0) {
        override suspend fun invoke(
            env: Environment,
            eval: Evaluator,
            params: List<Any>,
            options: Map<String, Any>,
            rest: List<Any>
        ): Any {
            return service.gameState.allCards.toList()
        }
    })


    // Pile
    addConverter(MutablePileToCardsConverter())
    addConverter(PileToCardsConverter())
    addMethod(PileCopyToMethod(service::gameState, service::addGameChange))
    addMethod(PileMoveToMethod(service::gameState, service::addGameChange))
    addMethod(PileGetMethod(service.describer, service::gameState))

    // Effects
    addMethod(FxAddMethod(service::gameState, service::addGameChange))

    // Shop
    addMethod(ShopRerollMethod(service::addGameChange))
    addMethod(ShopPriceForMethod { service.gameState.shop })
    addMethod(ShopTweakMethod(service::addGameChange))
    (0..4).forEach { i -> storeValue("\$tier${i + 1}", i) }

    // Buildings
    addMethod(BlueprintIsBuiltMethod(service::gameState))
    addMethod(BlueprintIsOwnedMethod(service::gameState))
    addMethod(BlueprintGetMethod())
    addMethod(BlueprintOwnMethod(service::addGameChange))
    addMethod(BlueprintBuildMethod(service::addGameChange))
    addMethod(BuildingGetMethod())
    addMethod(BuildingSetMethod(service::addGameChange))
    addMethod(RandomBlueprintsMethod(service.gameData.blueprints, service.gameData.rarities) { service.gameState.random() })
    addMethod(object : Method("\$unowned-blueprints", 0) {
        override suspend fun invoke(
            env: Environment,
            eval: Evaluator,
            params: List<Any>,
            options: Map<String, Any>,
            rest: List<Any>
        ): Any {
            val state = service.gameState
            return service.gameData.blueprints.filterNot { it.isOwned(state) }.sortedBy { it.name }
        }
    })

    // Text
    addMethod(IconConvertMethod(service.describer))
}

/**
 * Add all variables related to the current game state into the environment.
 *
 * You probably want to do this within an [Environment.scoped] block, to avoid ever accidentally referring to stale game
 * state from previous turns.
 */
fun Environment.setValuesFrom(state: GameState) {
    storeValue("\$shop-tier", state.shop.tier, allowOverwrite = true)

    storeValue("\$deck", state.deck, allowOverwrite = true)
    storeValue("\$hand", state.hand, allowOverwrite = true)
    storeValue("\$street", state.street, allowOverwrite = true)
    storeValue("\$discard", state.discard, allowOverwrite = true)
    storeValue("\$jail", state.jail, allowOverwrite = true)

    storeValue("\$shop", state.shop.stock.filterNotNull(), allowOverwrite = true)

    storeValue("\$buildings", state.buildings, allowOverwrite = true)
    storeValue("\$owned-blueprints", state.blueprints, allowOverwrite = true)
}

/**
 * Store the current card in the environment.
 *
 * You probably want to do this within an [Environment.scoped] block, tied to the lifetime of the current card being
 * played.
 */
fun Environment.setValuesFrom(card: Card) {
    storeValue("\$this", card)
}

/**
 * Store the current location in the environment.
 *
 * You probably want to do this within an [Environment.scoped] block, tied to the lifetime of the current building
 * being activated.
 */
fun Environment.setValuesFrom(building: Building) {
    storeValue("\$this", building)
}
