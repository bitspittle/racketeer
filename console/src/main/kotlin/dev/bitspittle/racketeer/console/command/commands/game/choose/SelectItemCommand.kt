package dev.bitspittle.racketeer.console.command.commands.game.choose

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.cyan
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.model.card.Card

class SelectItemCommand(
    ctx: GameContext,
    val item: Any,
    var selected: Boolean = false,
    description: String? = null,
    override val type: Type = Type.Normal
) : Command(ctx) {
    override val title get() = "[${if (selected) 'x' else ' '}] ${describeForTitle(item)}"
    override val extra get() = describeForExtra(item)
    override val description = description ?: describeForDescription(item)

    override suspend fun invoke(): Boolean {
        selected = !selected
        return true
    }

    override suspend fun handleKey(key: Key): Boolean {
        return when(key) {
            Keys.SPACE -> invoke()
            else -> false
        }
    }

    override fun renderFooterUpperInto(scope: RenderScope) {
        scope.apply {
            text("Press "); cyan { text("SPACE") }; textLine(" to toggle the selected item.")
        }
    }

    override fun renderContentLowerInto(scope: RenderScope) {
        renderContentLowerInto(scope, item)
    }
}
