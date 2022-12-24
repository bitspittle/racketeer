package dev.bitspittle.racketeer.site.model

import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.card.TraitType
import dev.bitspittle.racketeer.model.card.UpgradeType
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.text.Describer

sealed interface TooltipData {
    val name: String
    class OfText(override val name: String, val text: String) : TooltipData
    class OfCard(val card: CardTemplate) : TooltipData {
        override val name = card.name
    }
    class OfBlueprint(val blueprint: Blueprint) : TooltipData {
        override val name = blueprint.name
    }
}

data class TooltipRange(val range: IntRange, val tooltip: TooltipData)

private class TooltipTrieTree {
    private class Node(val c: Char) {
        var data: TooltipData? = null
            internal set
        var children = mutableMapOf<Char, Node>()

        fun register(key: String, keyIndex: Int, data: TooltipData) {
            val currChar = key[keyIndex]
            val child = children.getOrPut(currChar) { Node(c) }
            when (keyIndex) {
                (key.length - 1) -> child.data = data
                else -> child.register(key, keyIndex + 1, data)
            }
        }

        fun find(key: String, keyIndex: Int): TooltipData? {
            val currChar = key[keyIndex]
            val child = children[currChar] ?: return null
            return when {
                child.data != null -> child.data
                keyIndex == key.length -> child.data
                else -> child.find(key, keyIndex + 1)
            }
        }
    }

    private val root = Node(Char.MIN_VALUE)

    fun register(key: String, data: TooltipData) {
        if (key.isEmpty()) return
        root.register(key, 0, data)
    }

    fun find(text: String, fromIndex: Int = 0): TooltipData? {
        if (fromIndex >= text.length) return null
        return root.find(text, fromIndex)
    }

    operator fun set(text: String, data: TooltipData) {
        register(text, data)
    }
}

class TooltipParser(data: GameData, private val describer: Describer) {
    private val tooltipTree = TooltipTrieTree().apply {
        TraitType.values().forEach { traitType ->
            val name = describer.describeTraitTitle(traitType)
            this[name] = TooltipData.OfText(name, describer.describeTraitBody(traitType))
        }

        UpgradeType.values().forEach { upgradeType ->
            val name = describer.describeUpgradeTitle(upgradeType, icons = false)
            this[name] = TooltipData.OfText(name, describer.describeUpgradeBody(upgradeType))
        }

        data.cards.forEach { card -> this[card.name] = TooltipData.OfCard(card) }
        data.blueprints.forEach { blueprint -> this[blueprint.name] = TooltipData.OfBlueprint(blueprint) }
    }

    fun parse(text: String): List<TooltipRange> {
        val tooltipRanges = mutableListOf<TooltipRange>()

        var startIndex = 0
        while (startIndex < text.length) {
            val data = tooltipTree.find(text, startIndex)
            if (data != null) {
                val range = startIndex until startIndex + data.name.length
                tooltipRanges.add(TooltipRange(range, data))
                startIndex = range.last
            } else {
                ++startIndex
            }
        }

        return tooltipRanges
    }
}