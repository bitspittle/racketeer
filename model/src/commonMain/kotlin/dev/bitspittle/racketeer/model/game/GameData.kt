package dev.bitspittle.racketeer.model.game

import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.card.Rarity
import dev.bitspittle.racketeer.model.card.UpgradeNames
import dev.bitspittle.racketeer.model.card.UpgradeType
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.yamlkt.Yaml

private val String.indentLength: Int
    get() = this.takeWhile { c -> c == ' ' }.length

/**
 * @param globalActions Option extra actions which are run once before the game started, into a global scope that
 *   lives across the whole game.
 */
@Serializable
data class GameData(
    val title: String,
    val icons: GameIcons,
    val numTurns: Int,
    val initialHandSize: Int,
    val initialCash: Int,
    val initialInfluence: Int,
    val initialLuck: Int,
    val initialDeck: List<String>,
    val cardTypes: List<String>,
    val upgradeNames: UpgradeNames,
    val rarities: List<Rarity>,
    val tierFrequencies: List<Int>,
    val shopSizes: List<Int>,
    val shopPrices: List<Int>,
    val ratingScores: List<Int>,
    val cards: List<CardTemplate>,
    val globalActions: List<String> = listOf()
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
                            val escapedLine = line.replace("\"", "\\\"")
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
        require(shopPrices.size == tierFrequencies.size - 1) { "There should be exactly one less entry for shop prices than tiers" }
        require(shopSizes.size == tierFrequencies.size) { "There should be exactly the same number of shop sizes as tiers" }
        shopSizes.forEachIndexed { i, size ->
            if (i > 0) {
                require(size >= shopSizes[i]) { "Subsequence shop sizes should never shrink"}
            }

        }
        require(ratingScores.size == Rating.values().size - 1) {
            "Too many scores defined for rating values: ${
                Rating.values().joinToString { it.name }
            }"
        }

        cards.forEach { card ->
            require(card.types.isNotEmpty()) { "Card named \"${card.name}\" needs to have a type." }
            card.types.forEach { type ->
                if (cardTypes.none { it.compareTo(type, ignoreCase = true) == 0 }) {
                    error("The card named \"${card.name}\" has an invalid type \"$type\" defined on it. Should be one of: ${cardTypes.map { it.lowercase() }.sorted()}")
                }
            }
            require(card.rarity in rarities.indices) { "Rarity value must be between 0 and ${rarities.lastIndex}"}

            card.upgrades.forEach { upgrade ->
                if (UpgradeType.values().map { it.name }.none { it.compareTo(upgrade, ignoreCase = true) == 0 }) {
                    error("The card named \"${card.name}\" has an invalid upgrade \"$upgrade\" defined on it. Should be one of: ${UpgradeType.values().map { it.name.lowercase() }.sorted()}")
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