package dev.bitspittle.racketeer.console.view.views.system

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.foundation.text.yellow
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.commands.system.SerializationSupport
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.game.GameView
import kotlin.io.path.exists

abstract class SaveDataListView(ctx: GameContext) : GameView(ctx) {
    override suspend fun handleAdditionalKeys(key: Key): Boolean {
        when (key) {
            Keys.DELETE -> ctx.viewStack.pushView(ConfirmDeleteSaveView(ctx, currIndex))
            else -> return false
        }
        return true
    }

    override fun RenderScope.renderUpperFooter() {
        if (SerializationSupport.pathForSlot(currIndex).exists()) {
            text("Press "); yellow { text("DELETE") }; textLine(" to delete save slot #${currIndex + 1}.")
        }
    }
}