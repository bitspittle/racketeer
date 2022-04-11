package dev.bitspittle.racketeer.console.view

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.bold
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.foundation.text.underline
import com.varabyte.kotter.runtime.render.RenderScope
import com.varabyte.kotterx.decorations.BorderCharacters
import com.varabyte.kotterx.decorations.bordered
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.ConfirmQuitView

abstract class View(protected val ctx: GameContext) {
    private val commandsSection by lazy { CommandsSection(commands) }

    protected open val subtitle: String? = null
    protected open val heading: String? = null
    protected abstract val commands: List<Command>
    protected val currCommand get() = commandsSection.currCommand

    protected open val allowQuit = true
    protected open val allowGoBack = true

    suspend fun handleKey(key: Key): Boolean {
        return when (key) {
            Keys.ESC -> {
                if (allowGoBack) {
                    onGoingBack()
                    ctx.viewStack.popView()
                    true
                } else false
            }
            Keys.ENTER -> {
                if (currCommand.type != Command.Type.Disabled) {
                    currCommand.invoke()
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
                textLine("Press Q to quit.")
            }
        }
    }

    private fun RenderScope.renderHeader() {
        val state = ctx.state
        textLine("${ctx.describer.describeCash(state.cash)} ${ctx.describer.describeInfluence(state.influence)} ${ctx.describer.describeLuck(state.luck)} ${ctx.describer.describeVictoryPoints(state.vp)} ")
        textLine()
        bold { textLine("Turn ${state.turn + 1} out of ${state.numTurns}") }
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

    protected open fun onGoingBack() = Unit
}