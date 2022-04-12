package dev.bitspittle.racketeer.console.view

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.*
import com.varabyte.kotter.runtime.render.RenderScope
import com.varabyte.kotterx.decorations.BorderCharacters
import com.varabyte.kotterx.decorations.bordered
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.ConfirmQuitView

abstract class View(protected val ctx: GameContext) {
    protected abstract fun createCommands(): List<Command>
    private var _commandsSection: CommandsSection? = null
        get() {
            _commandsSection = field ?: CommandsSection(createCommands())
            return field!!
        }
    private val commandsSection get() = _commandsSection!!

    protected open val subtitle: String? = null
    protected open val heading: String? = null
    protected val currCommand get() = commandsSection.currCommand

    protected open val allowQuit = true
    protected open val allowGoBack = true

    fun refreshCommands() {
        _commandsSection = null
    }

    protected fun goBack() {
        ctx.viewStack.popView()
        // Refresh commands in case the screen we were in caused a change
        ctx.viewStack.currentView.refreshCommands()
    }

    suspend fun handleKey(key: Key): Boolean {
        return when (key) {
            Keys.ESC -> {
                if (allowGoBack) {
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
            Keys.Q -> {
                if (allowQuit) ctx.viewStack.pushView(ConfirmQuitView(ctx)); true
            }

            else -> handleAdditionalKeys(key) || commandsSection.handleKey(key)
        }
    }

    protected open suspend fun handleAdditionalKeys(key: Key): Boolean = false

    fun renderInto(scope: RenderScope) {
        scope.apply {
            renderHeader()
            renderContent()
            commandsSection.renderInto(this)

            commandsSection.currCommand.description?.let { description ->
                bordered(borderCharacters = BorderCharacters.CURVED, paddingLeftRight = 1) {
                    val MAX_WIDTH = 60
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
                                if (widthSoFar == 0 || widthSoFar + descParts[i + 1].length < MAX_WIDTH) {
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

            if (ctx.viewStack.canGoBack && allowGoBack) {
                textLine("Press ESC to go back.")
            }

            if (allowQuit) {
                text("Press "); yellow { text("Q") }; textLine(" to quit.")
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

    protected open fun onEscRequested() = Unit
}