@file:UseSerializers(UuidSerializer::class)

package dev.bitspittle.racketeer.model.serialization

import com.benasher44.uuid.Uuid
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.utils.toIdentifierName
import dev.bitspittle.racketeer.model.action.Enqueuers
import dev.bitspittle.racketeer.model.game.*
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.building.MutableBuilding
import dev.bitspittle.racketeer.model.card.*
import dev.bitspittle.racketeer.model.effect.*
import dev.bitspittle.racketeer.model.pile.MutablePile
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.model.random.CopyableRandom
import dev.bitspittle.racketeer.model.shop.MutableShop
import dev.bitspittle.racketeer.model.shop.Shop
import dev.bitspittle.racketeer.model.text.Describer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
class CardSnapshot(
    val id: Uuid,
    val name: String,
    val vp: Int,
    val counter: Int,
    val traits: Set<TraitType>,
    val upgrades: Set<UpgradeType>,
) {
    companion object {
        fun from(card: Card) = CardSnapshot(
            card.id,
            card.template.name,
            card.vpBase,
            card.counter,
            card.traits,
            card.upgrades,
        )
    }

    fun create(data: GameData) = MutableCard(
        data.cards.single { it.name == name },
        vp,
        vpPassive = 0, // Will be calculated later
        counter,
        traits.toMutableSet(),
        upgrades.toMutableSet(),
        id
    )
}

@Serializable
class BuildingSnapshot(
    val id: Uuid,
    val name: String,
    val vpBase: Int,
    val counter: Int,
    val isActivated: Boolean,
) {
    companion object {
        fun from(building: Building) = BuildingSnapshot(
            building.id,
            building.blueprint.name,
            building.vpBase,
            building.counter,
            building.isActivated
        )
    }

    fun create(data: GameData) = MutableBuilding(
        BlueprintSnapshot(name).create(data),
        id,
        vpBase = vpBase,
        vpPassive = 0, // Will be calculated later
        counter,
        isActivated
    )
}

@Serializable
class BlueprintSnapshot(
    val name: String,
) {
    companion object {
        fun from(blueprint: Blueprint) = BlueprintSnapshot(
            blueprint.name
        )
    }

    fun create(data: GameData) = data.blueprints.single { it.name == name }
}

@Serializable
class ShopSnapshot(
    val tier: Int,
    val stock: List<CardSnapshot?>,
    val prices: Map<Uuid, Int>,
    val tweaks: List<Tweak.Shop>,
    val bought: Map<String, Int>,
) {
    companion object {
        fun from(shop: Shop) = ShopSnapshot(
            shop.tier,
            shop.stock.map { card -> if (card != null) CardSnapshot.from(card) else null },
            shop.prices,
            shop.tweaks.items,
            shop.bought,
        )
    }

    fun create(data: GameData, features: Set<Feature.Type>, random: CopyableRandom) = MutableShop(
        random,
        data.cards,
        features,
        data.shopSizes,
        data.tierFrequencies,
        data.rarities,
        tier,
        stock.map { it?.create(data) }.toMutableList(),
        prices.toMutableMap(),
        MutableTweaks(tweaks.toMutableList()),
        bought.toMutableMap(),
    )
}

@Serializable
class PileSnapshot(
    val id: Uuid,
    val cards: List<CardSnapshot>
) {
    companion object {
        fun from(pile: Pile) = PileSnapshot(
            pile.id,
            pile.cards.map { CardSnapshot.from(it) }
        )
    }

    fun create(data: GameData) = MutablePile(
        id,
        cards.map { it.create(data) }.toMutableList()
    )
}

@Serializable
class EffectSnapshot(
    val expr: String,
    val desc: String?,
    val lifetime: Lifetime,
    val event: GameEvent,
    val data: DataValue?,
    val testExpr: String?
) {
    companion object {
        fun from(effect: Effect<*>): EffectSnapshot = run {
            EffectSnapshot(effect.expr, effect.desc, effect.lifetime, effect.event, effect.data, effect.testExpr)
        }
    }

    suspend fun evaluate(env: Environment, evaluator: Evaluator) {
        val code = buildString {
            append("fx-add! ")
            if (desc != null) {
                append("--desc \"$desc\" ")
            }
            if (lifetime != Lifetime.TURN) {
                append("--lifetime '${lifetime.toIdentifierName()} ")
            }
            if (event != GameEvent.PLAY) {
                append("--event '${event.toIdentifierName()} ")
            }
            if (data != null) {
                append("--data ${data.asText}")
            }
            if (testExpr != null) {
                append("--if '${testExpr} ")
            }
            append("'$expr")
        }

        evaluator.evaluate(env, code)
    }
}

@Serializable
class GameSnapshot(
    val id: Uuid,
    val random: CopyableRandom,
    val features: Set<Feature.Type>,
    val numTurns: Int,
    val turn: Int,
    // Note: We technically don't need to save "vp", as we don't read it back. But it's useful for humans reading the
    // file to know how many points were earned at this point.
    val vp: Int,
    val cash: Int,
    val influence: Int,
    val luck: Int,
    val handSize: Int,
    val blueprints: List<BlueprintSnapshot>,
    val buildings: List<BuildingSnapshot>,
    val effects: List<EffectSnapshot>,
    val tweaks: List<Tweak.Game>,
    val shop: ShopSnapshot,
    val deck: PileSnapshot,
    val hand: PileSnapshot,
    val street: PileSnapshot,
    val discard: PileSnapshot,
    val jail: PileSnapshot,
    val graveyard: PileSnapshot,
    val data: Map<String, DataValue>,
    val history: List<GameChangesSnapshot>,
) {
    companion object {
        fun from(describer: Describer, gameState: GameState) = GameSnapshot(
            gameState.id,
            gameState.random,
            gameState.features,
            gameState.numTurns,
            gameState.turn,
            gameState.vp,
            gameState.cash,
            gameState.influence,
            gameState.luck,
            gameState.handSize,
            gameState.blueprints.map { blueprint -> BlueprintSnapshot.from(blueprint) },
            gameState.buildings.map { building -> BuildingSnapshot.from(building) },
            gameState.effects.items.map { effect -> EffectSnapshot.from(effect) },
            gameState.tweaks.items,
            ShopSnapshot.from(gameState.shop),
            PileSnapshot.from(gameState.deck),
            PileSnapshot.from(gameState.hand),
            PileSnapshot.from(gameState.street),
            PileSnapshot.from(gameState.discard),
            PileSnapshot.from(gameState.jail),
            PileSnapshot.from(gameState.graveyard),
            gameState.data,
            gameState.history
                .map { changes -> GameChangesSnapshot.from(describer, gameState, changes) }
        )
    }

    /**
     * @param onGameStateCreated A callback that is triggered after the game state was created but before we run some
     *   initialization on it that requires it be hooked up into the scripting system. This is kind of a hack since it
     *   couples this method with awareness of the scripting system, but it's isolated at least and not terrible.
     */
    suspend fun create(
        data: GameData,
        env: Environment,
        enqueuers: Enqueuers,
        onGameStateCreated: (MutableGameState) -> Unit
    ) {
        val gs = MutableGameState(
            id,
            random,
            features,
            enqueuers,
            numTurns,
            turn,
            cash,
            influence,
            luck,
            0, // Recaluclated shortly
            handSize,
            shop.create(data, features, random),
            deck.create(data),
            hand.create(data),
            street.create(data),
            discard.create(data),
            jail.create(data),
            graveyard.create(data),
            blueprints.map { it.create(data) }.toMutableList(),
            buildings.map { it.create(data) }.toMutableList(),
            effects = MutableEffects(), // Populated shortly
            tweaks = MutableTweaks(tweaks.toMutableList()),
            this.data.toMutableMap(),
            history = mutableListOf(), // Populated shortly
        )

        gs.history.addAll(history.map { it.create(data, gs) })

        onGameStateCreated(gs)

        // Normally the game won't be over but can be true for admins who save on the last screen
        // We need to temporarily remove this or else the following requests to apply VP calculations will fail.
        // See also GameState.apply's implementation.
        val gameOverChange = if (gs.isGameOver) gs.history.removeLast() else null

        // We need to create a temporary history group which needs to exist when adding effects and calculating passive
        // VP.
        gs.startRecordingChanges()
        env.scoped {
            val evaluator = Evaluator()
            effects.forEach { effect -> effect.evaluate(env, evaluator) }
        }
        gs.onBoardChanged() // Trigger passive VP calculations
        if (gs.finishRecordingChanges()) {
            gs.history.removeLast()
        }

        if (gameOverChange != null) {
            gs.history.add(gameOverChange)
        }
    }
}
