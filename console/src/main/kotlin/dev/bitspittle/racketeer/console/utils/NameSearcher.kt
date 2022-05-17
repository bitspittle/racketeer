package dev.bitspittle.racketeer.console.utils

import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.card.CardTemplate


class NameSearcher<T: Any>(items: List<T>, private val nameOf: (T) -> String) {
    val items = items.sortedBy { nameOf(it) }

    fun search(searchPrefix: String): T? {
        return items
            .asSequence()
            .filter { item -> nameOf(item).startsWith(searchPrefix, ignoreCase = true) }
            .firstOrNull()
            ?: items.reversed()
                .asSequence()
                .filter { item -> searchPrefix > nameOf(item).lowercase() }
                .firstOrNull()
    }
}

fun CardSearcher(cards: List<CardTemplate>) = NameSearcher(cards) { card -> card.name }
fun BlueprintSearcher(blueprints: List<Blueprint>) = NameSearcher(blueprints) { blueprint -> blueprint.name }
