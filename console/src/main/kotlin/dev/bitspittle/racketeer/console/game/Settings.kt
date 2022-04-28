package dev.bitspittle.racketeer.console.game

import kotlinx.serialization.Serializable

@Serializable
class Settings(
    var showDebugInfo: Boolean = false,
    var enableAdminFeatures: Boolean = false,
)