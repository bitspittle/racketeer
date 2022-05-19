package dev.bitspittle.racketeer.model.game

import kotlinx.serialization.Serializable

/**
 * @param id A consistent text value that will be used by the code to (potentially) perform some
 *   custom behavior
 * @param name A display name to show to the user for this feature
 * @param description A description that describes what this unlock does.
 */
@Serializable
class Unlock(
    val id: String,
    val name: String,
    val description: String,
    val codename: String,
    val vp: Int
)

