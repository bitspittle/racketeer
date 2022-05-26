package dev.bitspittle.racketeer.model.card

import kotlinx.serialization.Serializable

@Serializable
class Rarity(val name: String, val blueprintFrequency: Int, val shopFrequency: Int, val shopCount: Int)