package dev.bitspittle.racketeer.model.card

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4

/**
 * @param template The read-only template this card is based on
 * @param id A globally unique ID which can act as this specific card's fingerprint
 */
class Card private constructor(val template: CardTemplate, val id: Uuid, vp: Int, val upgrades: MutableSet<UpgradeType>): Comparable<Card> {
    internal constructor(template: CardTemplate) : this(template, uuid4(), template.vp, mutableSetOf())

    /**
     * Cards can earn victory points over the course of the game.
     */
    var vp = vp
        set(value) {
            field = value.coerceAtLeast(0)
        }

    fun copy(id: Uuid = this.id, vp: Int = this.vp, upgrades: Set<UpgradeType> = this.upgrades) = Card(template, id, vp, upgrades.toMutableSet())

    override fun compareTo(other: Card): Int {
        return template.compareTo(other.template).takeUnless { it == 0 }
            ?: vp.compareTo(other.vp).takeUnless { it == 0 }
            ?: upgrades.size.compareTo(other.upgrades.size).takeUnless { it == 0 }
            ?: id.compareTo(other.id) // This last check is meaningless but consistent
    }
}

fun Card.isPatient() = this.upgrades.contains(UpgradeType.UNDERCOVER)
