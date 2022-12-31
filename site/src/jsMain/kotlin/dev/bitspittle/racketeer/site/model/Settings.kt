package dev.bitspittle.racketeer.site.model

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    var admin: Admin = Admin(),
    var unlocks: Unlocks = Unlocks(),
    var features: Features = Features(),
) {
    @Serializable
    data class Admin(
        var showCode: Boolean = true,
        var enabled: Boolean = false,
    )

    @Serializable
    data class Unlocks(
        var feedback: Boolean = false,
        var buildings: Boolean = false,
        var discord: Boolean = false,
    )

    @Serializable
    data class Features(
        var buildings: Boolean = false,
    )

    fun setFrom(other: Settings) {
        admin = other.admin.copy()
        unlocks = other.unlocks.copy()
        features = other.features.copy()
    }
}

private val DEFAULT_SETTINGS = Settings()

fun Settings.clear() = setFrom(DEFAULT_SETTINGS)
val Settings.isDefault get() = this == DEFAULT_SETTINGS