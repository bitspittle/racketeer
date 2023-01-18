package dev.bitspittle.racketeer.site.model

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    var features: Features = Features(),
) {
    @Serializable
    data class Features(
        var buildings: Boolean = false,
    )

    fun setFrom(other: Settings) {
        features = other.features.copy()
    }
}

private val DEFAULT_SETTINGS = Settings()

fun Settings.clear() = setFrom(DEFAULT_SETTINGS)
val Settings.isDefault get() = this == DEFAULT_SETTINGS