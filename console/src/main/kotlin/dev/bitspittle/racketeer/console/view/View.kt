package dev.bitspittle.racketeer.console.view

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.sections.PickCommandSection

abstract class View {
    private val commandsSection by lazy { PickCommandSection(commands) }

    open fun handleKey(key: Key): Boolean = false

    fun render(ctx: GameContext, scope: RenderScope) {
        scope.apply {
            renderHeader(ctx)
            renderContent(ctx)
            renderCommands()
        }
    }

    private fun RenderScope.renderHeader(ctx: GameContext) {
        val icons = ctx.config.icons
        val state = ctx.state
        textLine("${icons.cash} ${state.cash} ${icons.influence} ${state.influence} ${icons.luck} ${state.luck} ${icons.victoryPoints} ${state.victoryPoints}")
        textLine()
        textLine("Turn ${state.turn + 1}")
        textLine()
        subtitle?.let { subtitle ->
            textLine(subtitle)
            textLine()
        }
    }

    private fun RenderScope.renderCommands() {
        commandsSection.renderInto(this)
    }

    protected open fun RenderScope.renderContent(ctx: GameContext) = Unit

    protected open val subtitle: String? = null
    protected open val commands: List<Command> get() = listOf()
}