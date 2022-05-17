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
import dev.bitspittle.racketeer.console.command.commands.admin.CreateBuildingCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.BlueprintSearcher
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.model.building.isBuilt

class AdminBuildBuildingView(ctx: GameContext) : View(ctx) {
    private val blueprintSearcher = BlueprintSearcher(ctx.data.blueprints.filterNot { it.isBuilt(ctx.state) })
    private var searchPrefix = ""

    override fun createCommands(): List<Command> = blueprintSearcher.items.map { CreateBuildingCommand(ctx, it) }

    override suspend fun handleAdditionalKeys(key: Key): Boolean {
        return if (key is CharKey && (key.code.isLetter() || key.code == ' ')) {
            searchPrefix += key.code.lowercase()
            currIndex = blueprintSearcher.search(searchPrefix)?.let { found -> blueprintSearcher.items.indexOf(found) } ?: 0
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

    override fun RenderScope.renderFooterUpper() {
        text("Press "); cyan { text("A-Z") }; textLine(" to jump to cards by name.")
    }

}