package dev.bitspittle.racketeer.console.view.views.admin

import com.varabyte.kotter.foundation.input.CharKey
import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.black
import com.varabyte.kotter.foundation.text.cyan
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.admin.CreateCardCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View

class AdminCreateCardView(ctx: GameContext) : View(ctx) {
    private val sortedCards = ctx.data.cards.sortedBy { it.name }

    override fun createCommands(): List<Command> = sortedCards.map { CreateCardCommand(ctx, it) }

    override fun RenderScope.renderFooter() {
        text("Press "); cyan { text("A-Z") }; textLine(" to jump to cards by name.")
    }

    private var searchPrefix = ""

    override suspend fun handleAdditionalKeys(key: Key): Boolean {
        return if (key is CharKey) {
            key.code.takeIf { it in 'a'..'z' || it in 'A'..'Z' }?.let { searchLetter ->
                searchPrefix += searchLetter.lowercase()
                // Return
                // - the first card that matches the search letter (e.g. 'P' -> "P1" in ["P1", "P2", "P3"]) OR
                // - the last card smaller than it (e.g. 'P' -> "O3" in ["O1", "O2", "O3"])
                val foundCard =
                    sortedCards
                        .asSequence()
                        .filter { card -> card.name.startsWith(searchPrefix, ignoreCase = true) }
                        .firstOrNull()
                            ?: sortedCards.reversed()
                                .asSequence()
                                .filter { card -> searchPrefix > card.name.lowercase() }
                                .firstOrNull()

                currIndex = foundCard?.let { sortedCards.indexOf(it) } ?: 0
            }
            true
        } else {
            when (key) {
                Keys.BACKSPACE -> { searchPrefix = searchPrefix.dropLast(1); true }
                else -> false
            }
        }
    }

    override fun MainRenderScope.renderContentUpper() {
        if (searchPrefix != "") {
            black(isBright = true) { textLine("Search: " + searchPrefix.lowercase()) }
            textLine()
        }
    }
}