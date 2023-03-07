package dev.bitspittle.racketeer.model.game

import kotlinx.serialization.Serializable

@Serializable
data class GameIcons(
    val buildings: String,
    val cash: String,
    val deck: String,
    val discard: String,
    val expendable: String,
    val flavor: String,
    val hand: String,
    val influence: String,
    val jail: String,
    val luck: String,
    val move: String,
    val new: String,
    val shop: String,
    val street: String,
    val suspicious: String,
    val swift: String,
    val tweak: String,
    val veteran: String,
    val vp: String,
)