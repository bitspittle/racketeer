package dev.bitspittle.racketeer.model.game

import kotlinx.serialization.Serializable

@Serializable
data class GameIcons(
    val cash: String,
    val influence: String,
    val luck: String,
    val undercover: String,
    val counter: String,
    val vp: String,
)