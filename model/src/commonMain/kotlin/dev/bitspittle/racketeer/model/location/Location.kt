package dev.bitspittle.racketeer.model.location

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import dev.bitspittle.racketeer.model.card.MutableCard
import dev.bitspittle.racketeer.model.card.UpgradeType

interface Location : Comparable<Location> {
    val blueprint: Blueprint
    val id: Uuid
    val isActivated: Boolean
}

/**
 * @param blueprint The blueprint this location was built from.
 * @param id A globally unique ID which can act as this specific card's fingerprint
 */
class MutableLocation internal constructor(
    override val blueprint: Blueprint,
    override val id: Uuid = uuid4(),
    override var isActivated: Boolean,
) : Location {
    internal constructor(blueprint: Blueprint) : this(blueprint, uuid4(), isActivated = false)

    fun copy(
        id: Uuid = this.id,
        isActivated: Boolean = this.isActivated
    ) = MutableLocation(blueprint, id, isActivated)

    override fun compareTo(other: Location): Int {
        return blueprint.compareTo(other.blueprint).takeUnless { it == 0 }
            ?: id.compareTo(other.id) // This last check is meaningless but consistent
    }
}
