@file:UseSerializers(UuidSerializer::class)

package dev.bitspittle.racketeer.model.serialization

import com.benasher44.uuid.Uuid
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.utils.toIdentifierName
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardQueue
import dev.bitspittle.racketeer.model.card.MutableCard
import dev.bitspittle.racketeer.model.card.UpgradeType
import dev.bitspittle.racketeer.model.game.*
import dev.bitspittle.racketeer.model.pile.MutablePile
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.model.random.CopyableRandom
import dev.bitspittle.racketeer.model.shop.Exclusion
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
    val upgrades: Set<UpgradeType>
) {
    companion object {
        fun from(card: Card) = CardSnapshot(
            card.id,
            card.template.name,
            card.vpBase,
            card.counter,
            card.upgrades,
        )
    }

    fun create(data: GameData) = MutableCard(
        data.cards.single { it.name == name },
        vp,
        vpPassive = 0, // Will be calculated later
        counter,
        upgrades.toMutableSet(),
        id
    )
}

@Serializable
class ExclusionSnapshot(val expr: String) {
    companion object {
        fun from(exclusion: Exclusion) = ExclusionSnapshot(exclusion.expr)
    }

    suspend fun evaluate(env: Environment, evaluator: Evaluator) {
        evaluator.evaluate(env, "shop-exclude! '$expr") as Exclusion
    }
}

@Serializable
class ShopSnapshot(
    val tier: Int,
    val stock: List<CardSnapshot?>,
    val exclusions: List<ExclusionSnapshot>,
) {
    companion object {
        fun from(shop: Shop) = ShopSnapshot(
            shop.tier,
            shop.stock.map { card -> if (card != null) CardSnapshot.from(card) else null },
            shop.exclusions.map { ExclusionSnapshot.from(it) },
        )
    }

    fun create(data: GameData, random: CopyableRandom) = MutableShop(
        random,
        data.cards,
        data.shopSizes,
        data.tierFrequencies,
        data.rarities.map { it.frequency },
        tier,
        stock.map { it?.create(data) }.toMutableList(),
        exclusions = mutableListOf() // Populated by GameSnapshot
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
    val data: String?,
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
                append("--data \"$data\" ")
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
    val random: CopyableRandom,
    val isPreDraw: Boolean,
    val numTurns: Int,
    val turn: Int,
    // Note: We technically don't need to save "vp", as we don't read it back. But it's useful for humans reading the
    // file to know how many points were earned at this point.
    val vp: Int,
    val cash: Int,
    val influence: Int,
    val luck: Int,
    val handSize: Int,
    val shop: ShopSnapshot,
    val deck: PileSnapshot,
    val hand: PileSnapshot,
    val street: PileSnapshot,
    val discard: PileSnapshot,
    val jail: PileSnapshot,
    val graveyard: PileSnapshot,
    val effects: List<EffectSnapshot>,
    val history: List<GameChangeSnapshot>,
) {
    companion object {
        fun from(describer: Describer, gameState: GameState, isPreDraw: Boolean) = GameSnapshot(
            gameState.random,
            isPreDraw,
            gameState.numTurns,
            gameState.turn,
            gameState.vp,
            gameState.cash,
            gameState.influence,
            gameState.luck,
            gameState.handSize,
            ShopSnapshot.from(gameState.shop),
            PileSnapshot.from(gameState.deck),
            PileSnapshot.from(gameState.hand),
            PileSnapshot.from(gameState.street),
            PileSnapshot.from(gameState.discard),
            PileSnapshot.from(gameState.jail),
            PileSnapshot.from(gameState.graveyard),
            gameState.effects.items.map { effect -> EffectSnapshot.from(effect) },
            gameState.history.map { change -> GameChangeSnapshot.from(describer, gameState, change) }
        )
    }

    /**
     * @param onGameStateCreated A callback that is triggered after the game state was created but before we run some
     *   initialization on it that requires it be hooked up into the scripting system. This is kind of a hack since it
     *   couples this method with awareness of the scripting system, but it's isolated at least and not terrible.
     */
    suspend fun create(data: GameData, env: Environment, cardQueue: CardQueue, onGameStateCreated: (MutableGameState) -> Unit) {
        val gs = MutableGameState(
            random,
            cardQueue,
            numTurns,
            turn,
            cash,
            influence,
            luck,
            0, // Recaluclated shortly
            handSize,
            shop.create(data, random),
            deck.create(data),
            hand.create(data),
            street.create(data),
            discard.create(data),
            jail.create(data),
            graveyard.create(data),
            effects = MutableEffects(), // Populated shortly
            history = mutableListOf(), // Populated shortly
        )
        gs.history.addAll(history.map { it.create(gs) })

        onGameStateCreated(gs)

        env.scoped {
            val evaluator = Evaluator()
            shop.exclusions.forEach { exclusion -> exclusion.evaluate(env, evaluator) }
            effects.forEach { effect -> effect.evaluate(env, evaluator) }
        }
        gs.updateVictoryPoints()
    }
}
