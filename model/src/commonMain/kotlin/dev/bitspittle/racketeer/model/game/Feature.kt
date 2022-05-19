package dev.bitspittle.racketeer.model.game

import dev.bitspittle.limp.utils.toIdentifierName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * @param id A consistent text value that will be used to associate this feature metadata with the code that drives it.
 * @param name A display name to show to the user for this feature
 * @param description A description that describes what this feature does.
 */
@Serializable
class Feature(
    val id: String,
    val name: String,
    val description: String,
) {
    enum class Type {
        BUILDINGS,
    }

    @Transient
    val type = Type.values().first { it.toIdentifierName() == id }
}



