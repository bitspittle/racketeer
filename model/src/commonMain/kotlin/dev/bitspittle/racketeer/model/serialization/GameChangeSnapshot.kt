@file:UseSerializers(UuidSerializer::class)

package dev.bitspittle.racketeer.model.serialization

import com.benasher44.uuid.Uuid
import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardProperty
import dev.bitspittle.racketeer.model.card.UpgradeType
import dev.bitspittle.racketeer.model.game.*
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.model.shop.Exclusion
import dev.bitspittle.racketeer.model.text.Describer
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
            is GameStateChange.AddGameAmount -> AddGameAmount.from(change)
            is GameStateChange.AddStreetEffect -> AddStreetEffect.from(change)
            is GameStateChange.AddShopExclusion -> AddShopExclusion.from(change)
            is GameStateChange.RestockShop -> RestockShop.from(change)
            is GameStateChange.UpgradeShop -> UpgradeShop.from(change)
            is GameStateChange.EndTurn -> EndTurn.from(change)
            is GameStateChange.GameOver -> GameOver.from(change)
        }
    }

    abstract fun create(state: GameState): GameStateChange

    @Serializable
    class GameStarted : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.GameStarted) = GameStarted()
        }
        override fun create(state: GameState) = GameStateChange.GameStarted()
    }

    @Serializable
    class ShuffleDiscardIntoDeck : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.ShuffleDiscardIntoDeck) = ShuffleDiscardIntoDeck()
        }
        override fun create(state: GameState) = GameStateChange.ShuffleDiscardIntoDeck()
    }

    @Serializable
    class Draw(val count: Int) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.Draw) = Draw(change.count)
        }
        override fun create(state: GameState) = GameStateChange.Draw(count)
    }

    @Serializable
    class Play(val handIndex: Int) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.Play) = Play(change.handIndex)
        }
        override fun create(state: GameState) = GameStateChange.Play(handIndex)
    }

    @Serializable
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
    class Shuffle(val pilePtr: PilePtr) : GameChangeSnapshot() {
        companion object {
            fun from(describer: Describer, state: GameState, change: GameStateChange.Shuffle) = Shuffle(PilePtr.from(describer, state, change.pile))
        }
        override fun create(state: GameState) = GameStateChange.Shuffle(pilePtr.findIn(state))
    }

    @Serializable
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
    class UpgradeCard(val cardPtr: CardPtr, val upgradeType: UpgradeType) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.UpgradeCard) = UpgradeCard(CardPtr.from(change.card), change.upgradeType)
        }
        override fun create(state: GameState) = GameStateChange.UpgradeCard(cardPtr.findIn(state), upgradeType)
    }

    @Serializable
    class AddGameAmount(val property: GameProperty, val amount: Int) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.AddGameAmount) = AddGameAmount(change.property, change.amount)
        }
        override fun create(state: GameState) = GameStateChange.AddGameAmount(property, amount)
    }

    @Serializable
    class AddStreetEffect(val expr: String, val desc: String) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.AddStreetEffect) = AddStreetEffect(change.effect.expr, change.effect.desc)
        }
        override fun create(state: GameState) = GameStateChange.AddStreetEffect(Effect(expr, desc) { error("Not expected to get called") })
    }

    @Serializable
    class AddShopExclusion(val expr: String) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.AddShopExclusion) = AddShopExclusion(change.exclusion.expr)
        }
        // Just create a dummy exclusion here. We're not really expecting to recreate anything here. We're just keeping
        // the history so a human can look things over in the save file
        override fun create(state: GameState) = GameStateChange.AddShopExclusion(Exclusion(expr) { error("Not expected to get called") })
    }

    @Serializable
    class RestockShop : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.RestockShop) = RestockShop()
        }
        override fun create(state: GameState) = GameStateChange.RestockShop()
    }

    @Serializable
    class UpgradeShop : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.UpgradeShop) = UpgradeShop()
        }
        override fun create(state: GameState) = GameStateChange.UpgradeShop()
    }

    @Serializable
    class EndTurn : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.EndTurn) = EndTurn()
        }
        override fun create(state: GameState) = GameStateChange.EndTurn()
    }

    @Serializable
    class GameOver : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.GameOver) = GameOver()
        }
        override fun create(state: GameState) = GameStateChange.GameOver()
    }


}