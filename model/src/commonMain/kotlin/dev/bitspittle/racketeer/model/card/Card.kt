package dev.bitspittle.racketeer.model.card

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4

/**
 * @param template The read-only template this card is based on
 * @param id A globally unique ID which can act as this specific card's fingerprint
 */
class Card private constructor(
    val template: CardTemplate,
    val id: Uuid,
    vpBase: Int,
    vpBonus: Int,
    counter: Int,
    val upgrades: MutableSet<UpgradeType>): Comparable<Card> {
    internal constructor(template: CardTemplate) : this(template, uuid4(), template.vp, 0, 0,
        template.upgrades.map { upgradeStr ->
            UpgradeType.values().first { it.name.compareTo(upgradeStr, ignoreCase = true) == 0 }
        }.toMutableSet()
    )

    /**
     * Cards can earn victory points over the course of the game via upgrades and rewards from other cards.
     */
    var vp = vpBase
        set(value) {
            field = value.coerceAtLeast(0)
        }

    /**
     * Some cards have bonus VP amount generated by passive actions.
     */
    var vpPassive = vpBonus
        set(value) {
            field = value.coerceAtLeast(0)
        }

    /**
     * Cards can be given counters that can then be spent later for any desired effect.
     */
    var counter = counter
        set(value) {
            field = value.coerceAtLeast(0)
        }

    fun copy(
        id: Uuid = this.id,
        vpBase: Int = this.vp,
        vpBonus: Int = this.vpPassive,
        counter: Int = this.counter,
        upgrades: Set<UpgradeType> = this.upgrades
    ) = Card(template, id, vpBase, vpBonus, counter, upgrades.toMutableSet())

    override fun compareTo(other: Card): Int {
        return template.compareTo(other.template).takeUnless { it == 0 }
            ?: vpTotal.compareTo(other.vpTotal).takeUnless { it == 0 }
            ?: upgrades.size.compareTo(other.upgrades.size).takeUnless { it == 0 }
            ?: counter.compareTo(other.counter).takeUnless { it == 0 }
            ?: id.compareTo(other.id) // This last check is meaningless but consistent
    }
}

fun Card.isDexterous() = this.upgrades.contains(UpgradeType.CASH)
fun Card.isArtful() = this.upgrades.contains(UpgradeType.INFLUENCE)
fun Card.isLucky() = this.upgrades.contains(UpgradeType.LUCK)
fun Card.isJailbird() = this.upgrades.contains(UpgradeType.JAILBIRD)
fun Card.isUndercover() = this.upgrades.contains(UpgradeType.UNDERCOVER)
val Card.vpTotal get() = vp + vpPassive


