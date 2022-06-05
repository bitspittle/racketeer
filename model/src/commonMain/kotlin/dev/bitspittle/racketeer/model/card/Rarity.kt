package dev.bitspittle.racketeer.model.card

import kotlinx.serialization.Serializable

@Serializable
class Rarity(val name: String, val blueprintFrequency: Int, val cardFrequency: Int, val shopCount: Int)