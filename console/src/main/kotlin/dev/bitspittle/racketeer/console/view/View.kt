package dev.bitspittle.racketeer.console.view

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.*
import com.varabyte.kotter.runtime.render.RenderScope
import com.varabyte.kotterx.decorations.BorderCharacters
import com.varabyte.kotterx.decorations.bordered
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.admin.AdminMenuView
import dev.bitspittle.racketeer.console.view.views.system.OptionsMenuView

private const val DESC_WRAP_WIDTH = 60

abstract class View(protected val ctx: GameContext) {
    protected abstract fun createCommands(): List<Command>
    private var _commandsSection: CommandsSection? = null
    private val commandsSection: CommandsSection
        get() {
            if (_commandsSection == null) {
                _commandsSection = CommandsSection(createCommands())
            }
            return _commandsSection!!
        }

    protected open val title: String? = null
    protected open val subtitle: String? = null
    protected open val heading: String? = null
    protected val currCommand get() = commandsSection.currCommand
    protected var currIndex
        get() = commandsSection.currIndex
        set(value) { commandsSection.currIndex = value }

    fun refreshCommands() {
        val newIndex = refreshCursorPosition(commandsSection.currIndex, commandsSection.currCommand)
        _commandsSection = CommandsSection(createCommands(), newIndex)
    }

    /**
     * Give child views a chance to influence the new cursor position after [refreshCommands] is called.
     *
     * By default, the cursor stays in its old position.
     */
    protected open fun refreshCursorPosition(oldIndex: Int, oldCommand: Command): Int = oldIndex

    protected fun goBack() {
        ctx.viewStack.popView()
        // Refresh commands in case the screen we were in caused a change
        ctx.viewStack.currentView.refreshCommands()
    }

    suspend fun handleKey(key: Key): Boolean {
        return when (key) {
            Keys.ESC -> {
                if (ctx.viewStack.canGoBack) {
                    onEscRequested()
                    goBack()
                    true
                } else false
            }
            Keys.ENTER -> {
                if (currCommand.type != Command.Type.Disabled) {
                    try {
                        currCommand.invoke()
                    }
                    catch (ex: Exception) {
                        ctx.app.log(ex.message ?: "Code threw exception without a message: ${ex::class.simpleName}")
                    }
                    true
                } else {
                    false
                }
            }
            Keys.TAB -> {
                if (!isInOptionsMenu() && !isInAdminMenu()) {
                    ctx.viewStack.pushView(OptionsMenuView(ctx))
                    true
                } else false
            }
            Keys.TICK -> {
                if (!isInOptionsMenu() && !isInAdminMenu()) {
                    ctx.viewStack.pushView(AdminMenuView(ctx))
                    true
                } else false
            }

            else -> handleAdditionalKeys(key) || commandsSection.handleKey(key)
        }
    }

    private fun isInOptionsMenu(): Boolean {
        return (ctx.viewStack.contains { view -> view is OptionsMenuView })
    }

    private fun isInAdminMenu(): Boolean {
        return (ctx.viewStack.contains { view -> view is AdminMenuView })
    }

    protected open suspend fun handleAdditionalKeys(key: Key): Boolean = false

    fun renderInto(scope: RenderScope) {
        scope.apply {
            renderHeader()
            renderContent()
            commandsSection.renderInto(this)

            commandsSection.currCommand.description?.let { description ->
                bordered(borderCharacters = BorderCharacters.CURVED, paddingLeftRight = 1) {
                    val descParts = description.split(" ")
                    textLine(buildString {
                        var widthSoFar = 0
                        for (i in descParts.indices) {
                            append(descParts[i])
                            if (descParts[i].contains('\n')) {
                                widthSoFar = descParts[i].substringAfterLast('\n').length
                            }
                            else {
                                widthSoFar += descParts[i].length
                            }
                            if (i < descParts.lastIndex) {
                                if (widthSoFar == 0 || widthSoFar + descParts[i + 1].length < DESC_WRAP_WIDTH) {
                                    append(' ')
                                    widthSoFar++
                                }
                                else {
                                    append('\n')
                                    widthSoFar = 0
                                }
                            }
                        }
                    })
                }
                textLine()
            }

            renderFooter()

            if (ctx.viewStack.canGoBack) {
                text("Press "); cyan { text("ESC") }; textLine(" to go back.")
            }

            if (!isInOptionsMenu()) {
                text("Press "); cyan { text("TAB") }; textLine(" to open options.")
            }
        }
    }

    private fun RenderScope.renderHeader() {
        val state = ctx.state
        textLine("${ctx.describer.describeCash(state.cash)} ${ctx.describer.describeInfluence(state.influence)} ${ctx.describer.describeLuck(state.luck)} ${ctx.describer.describeVictoryPoints(state.vp)} ")
        textLine()
        scopedState {
            val numRemainingTurns = state.numTurns - state.turn
            if (numRemainingTurns == 1) red() else if (numRemainingTurns <= 4) yellow()
            bold { textLine("Turn ${state.turn + 1} out of ${state.numTurns}") }
        }
        textLine()
        title?.let { title ->
            bold { textLine(title.uppercase()) }
            textLine()
        }
        subtitle?.let { subtitle ->
            underline { textLine(subtitle) }
            textLine()
        }
        heading?.let { heading ->
            textLine(heading)
            textLine()
        }
    }

    protected open fun RenderScope.renderContent() = Unit
    protected open fun RenderScope.renderFooter() = Unit

    protected open fun onEscRequested() = Unit
}