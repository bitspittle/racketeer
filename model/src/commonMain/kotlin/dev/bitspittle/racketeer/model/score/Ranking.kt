package dev.bitspittle.racketeer.model.score

import kotlinx.serialization.Serializable

@Serializable
class Ranking(val name: String, val score: Int)

fun Iterable<Ranking>.from(vp: Int): Ranking {
    return this.last { vp >= it.score }
}


