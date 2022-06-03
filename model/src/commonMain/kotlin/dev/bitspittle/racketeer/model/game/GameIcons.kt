package dev.bitspittle.racketeer.model.game

import kotlinx.serialization.Serializable

@Serializable
data class GameIcons(
    val cash: String,
    val expendable: String,
    val flavor: String,
    val influence: String,
    val luck: String,
    val suspicious: String,
    val swift: String,
    val veteran: String,
    val vp: String,
)