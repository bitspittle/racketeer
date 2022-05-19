package dev.bitspittle.racketeer.model.game

import kotlinx.serialization.Serializable

private val TEXT_RESOLVERS: Map<String, Unlock.(GameData) -> String> = mapOf(
    "\$feature" to { data -> data.features.first { it.id == id }.name }
)

private fun String.resolveVariables(unlock: Unlock, data: GameData): String {
    var resolved = this
    TEXT_RESOLVERS.forEach { (varName, resolver) ->
        if (resolved.contains(varName)) {
            resolved = resolved.replace(varName, resolver(unlock, data))
        }
    }
    return resolved
}

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
) {
    fun resolvedName(data: GameData) = name.resolveVariables(this, data)
    fun resolvedDescription(data: GameData) = description.resolveVariables(this, data)
}
