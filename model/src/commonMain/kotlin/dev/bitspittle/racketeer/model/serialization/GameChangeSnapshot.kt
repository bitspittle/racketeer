@file:UseSerializers(UuidSerializer::class)

package dev.bitspittle.racketeer.model.serialization

import com.benasher44.uuid.Uuid
import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.building.BuildingProperty
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardProperty
import dev.bitspittle.racketeer.model.card.TraitType
import dev.bitspittle.racketeer.model.card.UpgradeType
import dev.bitspittle.racketeer.model.effect.Effect
import dev.bitspittle.racketeer.model.effect.Lifetime
import dev.bitspittle.racketeer.model.effect.Tweak
import dev.bitspittle.racketeer.model.game.*
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.model.shop.Shop
import dev.bitspittle.racketeer.model.text.Describer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
class BlueprintPtr(val name: String) {
    companion object {
        fun from(blueprint: Blueprint) = BlueprintPtr(blueprint.name)
    }

    fun findIn(state: GameState) =
        state.blueprints.firstOrNull { it.name == name }
            ?: state.buildings.asSequence().map { it.blueprint }.first { it.name == name }
}

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
        fun from(describer: Describer, state: GameState, pile: Pile) =
            PilePtr(pile.id, describer.describePileTitle(state, pile))
    }

    fun findIn(state: GameState) = state.allPiles.first { it.id == id }
}

// We don't technically need to save the name, but it's useful for humans browsing the file.
@Serializable
class TweakPtr(val tweakIndex: Int) {
    companion object {
        fun from(state: GameState, tweak: Tweak) = TweakPtr(state.tweaks.items.indexOf(tweak))
        fun from(shop: Shop, tweak: Tweak) = TweakPtr(shop.tweaks.items.indexOf(tweak))
    }

    fun findIn(state: GameState) = state.tweaks.items[tweakIndex]
    fun findIn(shop: Shop) = shop.tweaks.items[tweakIndex]
}

// We leave in parameters for clarity
@Suppress("UNUSED_PARAMETER")
@Serializable
sealed class GameChangeSnapshot {
    companion object {
        fun from(data: GameData, describer: Describer, state: GameState, change: GameStateChange): GameChangeSnapshot =
            when (change) {
                is GameStateChange.ShuffleDiscardIntoDeck -> ShuffleDiscardIntoDeck.from(change)
                is GameStateChange.Draw -> Draw.from(change)
                is GameStateChange.Play -> Play.from(change)
                is GameStateChange.MoveCard -> MoveCard.from(describer, state, change)
                is GameStateChange.MoveCards -> MoveCards.from(describer, state, change)
                is GameStateChange.Shuffle -> Shuffle.from(describer, state, change)
                is GameStateChange.AddCardAmount -> AddCardAmount.from(change)
                is GameStateChange.UpgradeCard -> UpgradeCard.from(change)
                is GameStateChange.AddTrait -> AddTrait.from(change)
                is GameStateChange.RemoveTrait -> RemoveTrait.from(change)
                is GameStateChange.AddBuildingAmount -> AddBuildingAmount.from(change)
                is GameStateChange.AddGameAmount -> AddGameAmount.from(change)
                is GameStateChange.SetGameData -> SetGameData.from(change)
                is GameStateChange.AddEffect -> AddEffect.from(change)
                is GameStateChange.AddGameTweak -> AddGameTweak.from(state, change)
                is GameStateChange.AddShopTweak -> AddShopTweak.from(state.shop, change)
                is GameStateChange.RestockShop -> RestockShop.from(change)
                is GameStateChange.UpgradeShop -> UpgradeShop.from(change)
                is GameStateChange.AddBlueprint -> AddBlueprint.from(change)
                is GameStateChange.Build -> Build.from(change)
                is GameStateChange.Activate -> Activate.from(change)
                is GameStateChange.EndTurn -> EndTurn.from(change)
                is GameStateChange.GameOver -> GameOver.from(change)
            }
    }

    abstract fun create(data: GameData, state: GameState): GameStateChange

    @Serializable
    @SerialName("ShuffleDiscardIntoDeck")
    class ShuffleDiscardIntoDeck : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.ShuffleDiscardIntoDeck) = ShuffleDiscardIntoDeck()
        }

        override fun create(data: GameData, state: GameState) = GameStateChange.ShuffleDiscardIntoDeck()
    }

    @Serializable
    @SerialName("Draw")
    class Draw(val count: Int) : GameChangeSnapshot() {
        companion object {
            // Will always be non-null by this time, after the draw change was applied
            fun from(change: GameStateChange.Draw) = Draw(change.count!!)
        }

        override fun create(data: GameData, state: GameState) = GameStateChange.Draw(count)
    }

    @Serializable
    @SerialName("Play")
    class Play(val cardPtr: CardPtr) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.Play) = Play(CardPtr.from(change.card))
        }

        override fun create(data: GameData, state: GameState) = GameStateChange.Play(cardPtr.findIn(state))
    }

    @Serializable
    @SerialName("MoveCard")
    class MoveCard(val cardPtr: CardPtr, val pilePtr: PilePtr, val listStrategy: ListStrategy = ListStrategy.BACK) :
        GameChangeSnapshot() {
        companion object {
            fun from(describer: Describer, state: GameState, change: GameStateChange.MoveCard) = MoveCard(
                CardPtr.from(change.card),
                PilePtr.from(describer, state, change.intoPile),
                change.listStrategy
            )
        }

        override fun create(data: GameData, state: GameState) =
            GameStateChange.MoveCard(cardPtr.findIn(state), pilePtr.findIn(state), listStrategy)
    }

    @Serializable
    @SerialName("MoveCards")
    class MoveCards(
        val cardPtrs: List<CardPtr>,
        val pilePtr: PilePtr,
        val listStrategy: ListStrategy = ListStrategy.BACK
    ) : GameChangeSnapshot() {
        companion object {
            fun from(describer: Describer, state: GameState, change: GameStateChange.MoveCards) = MoveCards(
                change.cards.map { card -> CardPtr.from(card) },
                PilePtr.from(describer, state, change.intoPile),
                change.listStrategy
            )
        }

        override fun create(data: GameData, state: GameState) = GameStateChange.MoveCards(
            cardPtrs.map { cardPtr -> cardPtr.findIn(state) },
            pilePtr.findIn(state),
            listStrategy
        )
    }

    @Serializable
    @SerialName("Shuffle")
    class Shuffle(val pilePtr: PilePtr) : GameChangeSnapshot() {
        companion object {
            fun from(describer: Describer, state: GameState, change: GameStateChange.Shuffle) =
                Shuffle(PilePtr.from(describer, state, change.pile))
        }

        override fun create(data: GameData, state: GameState) = GameStateChange.Shuffle(pilePtr.findIn(state))
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

        override fun create(data: GameData, state: GameState) =
            GameStateChange.AddCardAmount(property, cardPtr.findIn(state), amount)
    }

    @Serializable
    @SerialName("UpgradeCard")
    class UpgradeCard(val cardPtr: CardPtr, val upgradeType: UpgradeType) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.UpgradeCard) = UpgradeCard(CardPtr.from(change.card), change.upgradeType)
        }

        override fun create(data: GameData, state: GameState) =
            GameStateChange.UpgradeCard(cardPtr.findIn(state), upgradeType)
    }

    @Serializable
    @SerialName("AddTrait")
    class AddTrait(val cardPtr: CardPtr, val traitType: TraitType) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.AddTrait) = AddTrait(CardPtr.from(change.card), change.traitType)
        }

        override fun create(data: GameData, state: GameState) =
            GameStateChange.AddTrait(cardPtr.findIn(state), traitType)
    }

    @Serializable
    @SerialName("RemoveTrait")
    class RemoveTrait(val cardPtr: CardPtr, val traitType: TraitType) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.RemoveTrait) = RemoveTrait(CardPtr.from(change.card), change.traitType)
        }

        override fun create(data: GameData, state: GameState) =
            GameStateChange.RemoveTrait(cardPtr.findIn(state), traitType)
    }

    @Serializable
    @SerialName("AddBuildingAmount")
    class AddBuildingAmount(val property: BuildingProperty, val buildingPtr: BuildingPtr, val amount: Int) :
        GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.AddBuildingAmount) =
                AddBuildingAmount(change.property, BuildingPtr.from(change.building), change.amount)
        }

        override fun create(data: GameData, state: GameState) =
            GameStateChange.AddBuildingAmount(property, buildingPtr.findIn(state), amount)
    }

    @Serializable
    @SerialName("AddGameAmount")
    class AddGameAmount(val property: GameProperty, val amount: Int) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.AddGameAmount) = AddGameAmount(change.property, change.amount)
        }

        override fun create(data: GameData, state: GameState) = GameStateChange.AddGameAmount(property, amount)
    }

    @Serializable
    @SerialName("SetGameData")
    class SetGameData(val key: String, val value: DataValue) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.SetGameData) = SetGameData(change.key, change.value)
        }

        override fun create(data: GameData, state: GameState) = GameStateChange.SetGameData(key, value)
    }

    @Serializable
    @SerialName("AddEffect")
    class AddEffect(
        val expr: String,
        val desc: String?,
        val lifetime: Lifetime,
        val event: GameEvent,
        val data: DataValue?,
        val testExpr: String?
    ) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.AddEffect): AddEffect {
                val effect = change.effect
                return AddEffect(effect.expr, effect.desc, effect.lifetime, effect.event, effect.data, effect.testExpr)
            }
        }

        override fun create(data: GameData, state: GameState) = run {
            // These effects won't ever get run, they're just saved so that we can review a user's history (at the
            // moment, at least!). So just create dummy effects for now to satisfy the serializer.
            val dummyEffect = Effect<Any>(
                desc,
                lifetime,
                event,
                this.data,
                testExpr,
                expr,
                test = { error("Dummy effect") },
                action = { error("Dummy effect") })
            GameStateChange.AddEffect(dummyEffect)
        }
    }

    @Serializable
    @SerialName("AddGameTweak")
    class AddGameTweak(val tweakPtr: TweakPtr) : GameChangeSnapshot() {
        companion object {
            fun from(state: GameState, change: GameStateChange.AddGameTweak): AddGameTweak = AddGameTweak(TweakPtr.from(state, change.tweak))
        }

        override fun create(data: GameData, state: GameState) = run {
            GameStateChange.AddGameTweak(tweakPtr.findIn(state))
        }
    }

    @Serializable
    @SerialName("AddShopTweak")
    class AddShopTweak(val tweakPtr: TweakPtr) : GameChangeSnapshot() {
        companion object {
            fun from(shop: Shop, change: GameStateChange.AddShopTweak): AddShopTweak = AddShopTweak(TweakPtr.from(shop, change.tweak))
        }

        override fun create(data: GameData, state: GameState) = run {
            GameStateChange.AddShopTweak(tweakPtr.findIn(state.shop))
        }
    }

    @Serializable
    @SerialName("RestockShop")
    class RestockShop : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.RestockShop) = RestockShop()
        }

        override fun create(data: GameData, state: GameState) = GameStateChange.RestockShop()
    }

    @Serializable
    @SerialName("UpgradeShop")
    class UpgradeShop : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.UpgradeShop) = UpgradeShop()
        }

        override fun create(data: GameData, state: GameState) = GameStateChange.UpgradeShop()
    }

    @Serializable
    @SerialName("AddBlueprint")
    class AddBlueprint(val name: String) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.AddBlueprint) = AddBlueprint(change.blueprint.name)
        }

        override fun create(data: GameData, state: GameState) =
            GameStateChange.AddBlueprint(data.blueprints.single { it.name == name })
    }

    @Serializable
    @SerialName("Build")
    class Build(val blueprintPtr: BlueprintPtr) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.Build) = Build(BlueprintPtr.from(change.blueprint))
        }

        override fun create(data: GameData, state: GameState): GameStateChange.Build {
            return GameStateChange.Build(blueprintPtr.findIn(state))
        }
    }

    @Serializable
    @SerialName("Activate")
    class Activate(val buildingPtr: BuildingPtr) : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.Activate) = Activate(BuildingPtr.from(change.building))
        }

        override fun create(data: GameData, state: GameState) = GameStateChange.Activate(buildingPtr.findIn(state))
    }

    @Serializable
    @SerialName("EndTurn")
    class EndTurn : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.EndTurn) = EndTurn()
        }

        override fun create(data: GameData, state: GameState) = GameStateChange.EndTurn()
    }

    @Serializable
    @SerialName("GameOver")
    class GameOver : GameChangeSnapshot() {
        companion object {
            fun from(change: GameStateChange.GameOver) = GameOver()
        }

        override fun create(data: GameData, state: GameState) = GameStateChange.GameOver()
    }
}