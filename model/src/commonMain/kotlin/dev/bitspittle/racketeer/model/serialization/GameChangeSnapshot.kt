@file:UseSerializers(UuidSerializer::class)

package dev.bitspittle.racketeer.model.serialization

import com.benasher44.uuid.Uuid
import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.building.BuildingProperty
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardProperty
import dev.bitspittle.racketeer.model.card.UpgradeType
import dev.bitspittle.racketeer.model.game.*
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.model.shop.Exclusion
import dev.bitspittle.racketeer.model.text.Describer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

// We don't technically need to save the name, but it's useful for humans browsing the file.
@Serializable
class CardPtr(val id: Uuid, val name: String) {
    companion object {
        fun from(card: Card) = CardPtr(card.id, card.template.name)
    }

    fun findIn(state: GameState) = state.allCards.first { it.id == id }
}

// We don't technically need to save the name, but it's useful for humans browsing the file.
@Serializable
class BuildingPtr(val id: Uuid, val name: String) {
    companion object {
        fun from(building: Building) = BuildingPtr(building.id, building.blueprint.name)
    }

    fun findIn(state: GameState) = state.buildings.first { it.id == id }
}

// We don't technically need to save the name, but it's useful for humans browsing the file.
@Serializable
class PilePtr(val id: Uuid, val name: String) {
    companion object {
        fun from(describer: Describer, state: GameState, pile: Pile) = PilePtr(pile.id, describer.describePileTitle(state, pile))
    }

    fun findIn(state: GameState) = state.allPiles.first { it.id == id }
}

// We leave in parameters for clarity
@Suppress("UNUSED_PARAMETER")
@Serializable
sealed class GameChangeSnapshot {
    companion object {
        fun from(describer: Describer, state: GameState, change: GameStateChange): GameChangeSnapshot = when(change) {
            is GameStateChange.GameStarted -> GameStarted.from(change)
            is GameStateChange.ShuffleDiscardIntoDeck -> ShuffleDiscardIntoDeck.from(change)
            is GameStateChange.Draw -> Draw.from(change)
            is GameStateChange.Play -> Play.from(change)
            is GameStateChange.MoveCard -> MoveCard.from(describer, state, change)
            is GameStateChange.MoveCards -> MoveCards.from(describer, state, change)
            is GameStateChange.Shuffle -> Shuffle.from(describer, state, change)
            is GameStateChange.AddCardAmount -> AddCardAmount.from(change)
            is GameStateChange.UpgradeCard -> UpgradeCard.from(change)
            is GameStateChange.AddBuildingAmount -> AddBuildingAmount.from(change)
            is GameStateChange.AddGameAmount -> AddGameAmount.from(change)
            is GameStateChange.AddEffect -> AddEffect.from(change)
            is GameStateChange.AddShopExclusion -> AddShopExclusion.from(change)
            is GameStateChange.RestockShop -> RestockShop.from(change)
            is GameStateChange.UpgradeShop -> UpgradeShop.from(change)
            is GameStateChange.Build -> Build.from(change)
            is GameStateChange.Activate -> Activate.from(change)
            is GameStateChange.EndTurn -> EndTurn.from(change)
            is GameStateChange.GameOver -> GameOver.from(change)
        }
    }

    abstract fun create(state: GameState): GameStateChange

    @Serializable
    @SerialName("GameStarted")
    class GameStarted : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.GameStarted) = GameStarted()
        }
        override fun create(state: GameState) = GameStateChange.GameStarted()
    }

    @Serializable
    @SerialName("ShuffleDiscardIntoDeck")
    class ShuffleDiscardIntoDeck : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.ShuffleDiscardIntoDeck) = ShuffleDiscardIntoDeck()
        }
        override fun create(state: GameState) = GameStateChange.ShuffleDiscardIntoDeck()
    }

    @Serializable
    @SerialName("Draw")
    class Draw(val count: Int) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.Draw) = Draw(change.count)
        }
        override fun create(state: GameState) = GameStateChange.Draw(count)
    }

    @Serializable
    @SerialName("Play")
    class Play(val handIndex: Int) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.Play) = Play(change.handIndex)
        }
        override fun create(state: GameState) = GameStateChange.Play(handIndex)
    }

    @Serializable
    @SerialName("MoveCard")
    class MoveCard(val cardPtr: CardPtr, val pilePtr: PilePtr, val listStrategy: ListStrategy = ListStrategy.BACK) : GameChangeSnapshot() {
        companion object {
            fun from(describer: Describer, state: GameState, change: GameStateChange.MoveCard) = MoveCard(
                CardPtr.from(change.card),
                PilePtr.from(describer, state, change.intoPile),
                change.listStrategy
            )
        }
        override fun create(state: GameState) = GameStateChange.MoveCard(cardPtr.findIn(state), pilePtr.findIn(state), listStrategy)
    }

    @Serializable
    @SerialName("MoveCards")
    class MoveCards(val cardPtrs: List<CardPtr>, val pilePtr: PilePtr, val listStrategy: ListStrategy = ListStrategy.BACK) : GameChangeSnapshot() {
        companion object {
            fun from(describer: Describer, state: GameState, change: GameStateChange.MoveCards) = MoveCards(
                change.cards.map { card -> CardPtr.from(card) },
                PilePtr.from(describer, state, change.intoPile),
                change.listStrategy
            )
        }
        override fun create(state: GameState) = GameStateChange.MoveCards(
            cardPtrs.map { cardPtr -> cardPtr.findIn(state) },
            pilePtr.findIn(state),
            listStrategy)
    }

    @Serializable
    @SerialName("Shuffle")
    class Shuffle(val pilePtr: PilePtr) : GameChangeSnapshot() {
        companion object {
            fun from(describer: Describer, state: GameState, change: GameStateChange.Shuffle) = Shuffle(PilePtr.from(describer, state, change.pile))
        }
        override fun create(state: GameState) = GameStateChange.Shuffle(pilePtr.findIn(state))
    }

    @Serializable
    @SerialName("AddCardAmount")
    class AddCardAmount(val property: CardProperty, val cardPtr: CardPtr, val amount: Int) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.AddCardAmount) = AddCardAmount(
                change.property,
                CardPtr.from(change.card),
                change.amount
            )
        }
        override fun create(state: GameState) = GameStateChange.AddCardAmount(property, cardPtr.findIn(state), amount)
    }

    @Serializable
    @SerialName("UpgradeCard")
    class UpgradeCard(val cardPtr: CardPtr, val upgradeType: UpgradeType) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.UpgradeCard) = UpgradeCard(CardPtr.from(change.card), change.upgradeType)
        }
        override fun create(state: GameState) = GameStateChange.UpgradeCard(cardPtr.findIn(state), upgradeType)
    }

    @Serializable
    @SerialName("AddBuildingAmount")
    class AddBuildingAmount(val property: BuildingProperty, val buildingPtr: BuildingPtr, val amount: Int) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.AddBuildingAmount) =
                AddBuildingAmount(change.property, BuildingPtr.from(change.building), change.amount)
        }

        override fun create(state: GameState) =
            GameStateChange.AddBuildingAmount(property, buildingPtr.findIn(state), amount)
    }

    @Serializable
    @SerialName("AddGameAmount")
    class AddGameAmount(val property: GameProperty, val amount: Int) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.AddGameAmount) = AddGameAmount(change.property, change.amount)
        }
        override fun create(state: GameState) = GameStateChange.AddGameAmount(property, amount)
    }

    @Serializable
    @SerialName("AddEffect")
    class AddEffect(
        val expr: String,
        val desc: String?,
        val lifetime: Lifetime,
        val event: GameEvent,
        val data: String?,
        val testExpr: String?
    ) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.AddEffect): AddEffect {
                val effect = change.effect
                return AddEffect(effect.expr, effect.desc, effect.lifetime, effect.event, effect.data, effect.testExpr)
            }
        }

        override fun create(state: GameState) = run {
            // These effects won't ever get run, they're just saved so that we can review a user's history (at the
            // moment, at least!). So just create dummy effects for now to satisfy the serializer.
            val dummyEffect = Effect<Any>(desc, lifetime, event, data, testExpr, expr, test = { error("Dummy effect") }, action = { error("Dummy effect") })
            GameStateChange.AddEffect(dummyEffect)
        }
    }

    @Serializable
    @SerialName("AddShopExclusion")
    class AddShopExclusion(val expr: String) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.AddShopExclusion) = AddShopExclusion(change.exclusion.expr)
        }
        // Just create a dummy exclusion here. We're not really expecting to recreate anything here. We're just keeping
        // the history so a human can look things over in the save file
        override fun create(state: GameState) = GameStateChange.AddShopExclusion(Exclusion(expr) { error("Not expected to get called") })
    }

    @Serializable
    @SerialName("RestockShop")
    class RestockShop : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.RestockShop) = RestockShop()
        }
        override fun create(state: GameState) = GameStateChange.RestockShop()
    }

    @Serializable
    @SerialName("UpgradeShop")
    class UpgradeShop : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.UpgradeShop) = UpgradeShop()
        }
        override fun create(state: GameState) = GameStateChange.UpgradeShop()
    }

    @Serializable
    @SerialName("Build")
    class Build(val blueprintIndex: Int) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.Build) = Build(change.blueprintIndex)
        }
        override fun create(state: GameState) = GameStateChange.Build(blueprintIndex)
    }

    @Serializable
    @SerialName("Activate")
    class Activate(val buildingIndex: Int) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.Activate) = Activate(change.buildingIndex)
        }
        override fun create(state: GameState) = GameStateChange.Activate(buildingIndex)
    }

    @Serializable
    @SerialName("EndTurn")
    class EndTurn : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.EndTurn) = EndTurn()
        }
        override fun create(state: GameState) = GameStateChange.EndTurn()
    }

    @Serializable
    @SerialName("GameOver")
    class GameOver : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.GameOver) = GameOver()
        }
        override fun create(state: GameState) = GameStateChange.GameOver()
    }
}