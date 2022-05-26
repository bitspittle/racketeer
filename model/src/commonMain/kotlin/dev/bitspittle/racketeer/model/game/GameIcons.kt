package dev.bitspittle.racketeer.model.game

import kotlinx.serialization.Serializable

@Serializable
data class GameIcons(
    val cash: String,
    val influence: String,
    val luck: String,
    val veteran: String,
    val vp: String,
    val flavor: String,
)