package dev.bitspittle.racketeer.model.building

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4

interface Building : Comparable<Building> {
    val blueprint: Blueprint
    val id: Uuid
    val isActivated: Boolean
}

/**
 * @param blueprint The blueprint this building was constructed from.
 * @param id A globally unique ID which can act as this specific card's fingerprint
 */
class MutableBuilding internal constructor(
    override val blueprint: Blueprint,
    override val id: Uuid = uuid4(),
    override var isActivated: Boolean,
) : Building {
    internal constructor(blueprint: Blueprint) : this(blueprint, uuid4(), isActivated = false)

    fun copy(
        id: Uuid = this.id,
        isActivated: Boolean = this.isActivated
    ) = MutableBuilding(blueprint, id, isActivated)

    override fun compareTo(other: Building): Int {
        return blueprint.compareTo(other.blueprint).takeUnless { it == 0 }
            ?: id.compareTo(other.id) // This last check is meaningless but consistent
    }
}
