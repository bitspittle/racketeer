package dev.bitspittle.racketeer.model.game

import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.card.*
import dev.bitspittle.racketeer.model.score.Ranking
import dev.bitspittle.racketeer.model.score.Unlock
import dev.bitspittle.racketeer.model.text.escapeQuotes
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.yamlkt.Yaml

private val String.indentLength: Int
    get() = this.takeWhile { c -> c == ' ' }.length

private fun String.stripOutComment() = if (!this.contains('#')) this else buildString {
    var inString = false
    this@stripOutComment.forEachIndexed { i, c ->
        if (c == '#' && !inString) return@buildString
        if (c == '"' && this.getOrNull(i - 1) != '\\') {
            inString = !inString
        }
        append(c)
    }
}.trimEnd()


/**
 * @param globalActions Option extra actions which are run once before the game started, into a global scope that
 *   lives across the whole game.
 */
@Serializable
data class GameData(
    val title: String,
    val icons: GameIcons,
    val features: List<Feature>,
    val unlocks: List<Unlock>,
    val numTurns: Int,
    val initialHandSize: Int,
    val initialInfluence: Int,
    val initialLuck: Int,
    val cardTypes: List<String>,
    val traitNames: TraitNames,
    val upgradeNames: UpgradeNames,
    val rarities: List<Rarity>,
    val tierFrequencies: List<Int>,
    val shopSizes: List<Int>,
    val shopPrices: List<Int>,
    val rankings: List<Ranking>,
    val cards: List<CardTemplate>,
    val blueprints: List<Blueprint>,
    val globalActions: List<String> = listOf(),
    val initActions: List<String> = listOf(),
) {
    companion object {
        fun decodeFromString(text: String): GameData {
            // HACK ALERT!
            //
            // The multiplatform Yaml library we are using does not currently support multiline strings, so we pre-process
            // the text to convert it to a form it can handle.
            //
            // In other words, we turn this:
            //
            // ```
            // ex: |
            //   This is
            //
            //   a multiline
            //   string.
            // ```
            //
            // into:
            //
            // ```
            // ex: "This is\n\na multiline\nstring"
            // ```
            val unprocessed = text
            val processed = buildString {
                var inMultilineString = false
                var parentIndentLength = 0
                var multilineIndentLength = 0
                var prependNewline = false

                fun openMultilineString(line: String) {
                    append('"')
                    parentIndentLength = line.indentLength
                    inMultilineString = true
                    multilineIndentLength = 0
                    prependNewline = false
                }
                fun closeMultilineString() {
                    appendLine('"')
                    inMultilineString = false
                }

                unprocessed.lines().forEach { line ->
                    @Suppress("NAME_SHADOWING")
                    val line = line.stripOutComment()

                    if (inMultilineString && parentIndentLength > 0 && line.isNotBlank() && line.indentLength <= parentIndentLength) {
                        closeMultilineString()
                    }

                    if (!inMultilineString) {
                        if (line.trimEnd().endsWith(" |")) {
                            append(line.substringBeforeLast("|"))
                            openMultilineString(line)
                        } else {
                            appendLine(line)
                        }
                    } else {
                        check(inMultilineString)
                        if (prependNewline) {
                            append("\\n")
                            prependNewline = false
                        }
                        if (line.isBlank()) {
                            append("\\n")
                        } else {
                            if (multilineIndentLength == 0) {
                                multilineIndentLength = line.indentLength
                            }
                            // Be careful of nesting quotes within quotes!
                            val escapedLine = line.escapeQuotes()
                            append(escapedLine.drop(multilineIndentLength))
                            prependNewline = true
                        }
                    }
                }

                if (inMultilineString) {
                    closeMultilineString()
                }
            }

            return Yaml.decodeFromString(serializer(), processed)
        }
    }

    init {
        require(unlocks.sortedBy { it.vp } == unlocks) { "Unlocks should be specified in the order of VP required to them."}

        require(shopPrices.size == tierFrequencies.size - 1) { "There should be exactly one less entry for shop prices than tiers" }
        require(shopSizes.size == tierFrequencies.size) { "There should be exactly the same number of shop sizes as tiers" }
        shopSizes.forEachIndexed { i, size ->
            if (i > 0) {
                require(size >= shopSizes[i]) { "Subsequence shop sizes should never shrink"}
            }

        }

        require(rankings.isNotEmpty()) { "You must specify at least one rating."}
        require(rankings.first().score == 0) { "The first rating (the lowest) MUST have a score of 0"}
        for (i in rankings.indices) {
            if (i > 0) {
                require(rankings[i].score > rankings[i - 1].score) { "Each rating should have a higher score than the previous rating" }
            }
        }

        cards.forEach { card ->
            require(card.types.isNotEmpty()) { "Card named \"${card.name}\" needs to have a type." }
            val lowercaseCardTypes = cardTypes.map { it.lowercase() }
            card.types.forEach { type ->
                if (lowercaseCardTypes.none { it == type }) {
                    error("The card named \"${card.name}\" has an invalid type \"$type\" defined on it. Should be one of: ${lowercaseCardTypes.sorted()}")
                }
            }
            require(card.rarity in rarities.indices) { "Rarity value must be between 0 and ${rarities.lastIndex}"}

            card.traits.forEach { trait ->
                if (TraitType.values().map { it.name }.none { it.compareTo(trait, ignoreCase = true) == 0 }) {
                    error("The card named \"${card.name}\" has an invalid upgrade \"$trait\" defined on it. Should be one of: ${TraitType.values().map { it.name.lowercase() }.sorted()}")
                }
            }
        }
        cards.groupBy { it.name }.let { namedCards ->
            namedCards.forEach { (name, cards) ->
                if (cards.size > 1) {
                    error("The card name \"$name\" has duplicate entries")
                }
            }
        }
    }

    @Transient
    val maxTier = tierFrequencies.lastIndex
}