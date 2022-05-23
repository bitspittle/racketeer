package dev.bitspittle.racketeer.model.card

import kotlinx.serialization.Serializable

@Serializable
class Rarity(val name: String, val frequency: Int, val shopCount: Int)