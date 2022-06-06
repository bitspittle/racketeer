@file:UseSerializers(IntRangeSerializer::class)

package dev.bitspittle.racketeer.model.common

import dev.bitspittle.racketeer.model.game.Lifetime
import dev.bitspittle.racketeer.model.serialization.IntRangeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

interface Tweaks<T: Tweak> {
    val items: List<T>

    /**
     * Return a list of all tweaks that match the passed in predicate.
     *
     * The list will be returned in the order that the tweaks were originally added.
     *
     * @param consume If true, any matching tweaks registered with the [Lifetime.ONCE] lifetime will be removed by this
     *   operation.
     */
    fun collect(consume: Boolean = false, predicate: (Tweak) -> Boolean): List<Tweak>
}

inline fun <reified T: Tweak> Tweaks<in T>.collectInstances(consume: Boolean = false): List<T> {
    @Suppress("UNCHECKED_CAST")
    return this.collect(consume) { it is T } as List<T>
}

class MutableTweaks<T: Tweak>(override val items: MutableList<T> = mutableListOf()) : Tweaks<T> {
    fun copy(): MutableTweaks<T> {
        return MutableTweaks(items.toMutableList())
    }

    fun notifyTurnEnded() {
        items.removeAll { it.lifetime == Lifetime.TURN }
    }

    override fun collect(consume: Boolean, predicate: (Tweak) -> Boolean): List<Tweak> {
        val matches = items.filter { predicate(it) }
        if (consume) {
            matches
                .asSequence()
                .filter { it.lifetime == Lifetime.ONCE }
                .forEach { items.remove(it) }
        }

        return matches
    }
}

@Serializable
sealed class Tweak {
    abstract val lifetime: Lifetime
    abstract val desc: String

    @Serializable
    @SerialName("TweakShop")
    sealed class Shop : Tweak() {
        @Serializable
        @SerialName("TweakShopPrices")
        class Prices(
            override val lifetime: Lifetime,
            val amount: IntRange
        ) : Shop() {
            override val desc = buildString {
                if (amount.first != amount.last) {
                    append("Prices in the shop are randomized.")
                } else {
                    if (amount.first < 0) {
                        append("Prices in the shop are reduced by $.")
                    }
                    else if (amount.first > 0) {
                        append("Prices in the shop are increased by $.")
                    }
                }
            }
        }

        @Serializable
        @SerialName("TweakShopSize")
        class Size(
            override val lifetime: Lifetime,
            val amount: Int
        ) : Shop() {
            override val desc = buildString {
                if (amount < 0) {
                    append("The size of the shop is increased by $amount.")
                }
                else if (amount > 0) {
                    append("The size of the shop is decreased by $amount.")
                }
            }
        }

        @Serializable
        @SerialName("TweakShopFrozen")
        class Frozen(
            override val lifetime: Lifetime,
        ) : Shop() {
            override val desc = "The shop is frozen."
        }
    }

    @Serializable
    @SerialName("TweakGame")
    sealed class Game : Tweak() {
        @Serializable
        @SerialName("TweakGameKeepUnspent")
        class KeepUnspent(
            override val lifetime: Lifetime,
        ) : Game() {
            override val desc = "Any cash unspent this turn will be kept."
        }
    }
}