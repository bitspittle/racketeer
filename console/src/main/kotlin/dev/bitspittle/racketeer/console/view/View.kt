package dev.bitspittle.racketeer.console.view

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.render.RenderScope
import com.varabyte.kotterx.decorations.BorderCharacters
import com.varabyte.kotterx.decorations.bordered
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.App

private const val DESC_WRAP_WIDTH = 60

abstract class View(
    private val viewStack: ViewStack,
    private val app: App
) {
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
        set(value) {
            commandsSection.currIndex = value
        }

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
        viewStack.popView()
        // Refresh commands in case the screen we were in caused a change
        viewStack.currentView.refreshCommands()
    }

    private inline fun runUnsafeCode(block: () -> Unit) {
        try {
            block()
        } catch (ex: Exception) {
            app.logger.error(ex.message ?: "Code threw exception without a message: ${ex::class.simpleName}")
        }
    }

    suspend fun handleKey(key: Key): Boolean {
        return when (key) {
            Keys.ENTER -> {
                if (currCommand.type != Command.Type.Disabled) {
                    runUnsafeCode { currCommand.invoke() }
                    true
                } else {
                    false
                }
            }

            else -> doHandleKeys(key) || commandsSection.handleKey(key)
        }
    }

    suspend fun handleInputChanged(input: String) = runUnsafeCode {
        doHandleInputChanged(input)
    }
    suspend fun handleInputEntered(input: String, clearInput: () -> Unit) = runUnsafeCode {
        doHandleInputEntered(input, clearInput)
    }
    protected open suspend fun doHandleInputChanged(input: String) = Unit
    protected open suspend fun doHandleInputEntered(input: String, clearInput: () -> Unit) = Unit

    protected open suspend fun doHandleKeys(key: Key): Boolean = false

    fun renderInto(scope: MainRenderScope) {
        scope.apply {
            renderHeader()

            renderContentUpper()
            commandsSection.renderInto(this)
            renderContentLower()

            commandsSection.currCommand.description?.let { description ->
                bordered(borderCharacters = BorderCharacters.CURVED, paddingLeftRight = 1) {
                    val lines = description.split("\n").toMutableList()
                    text(buildString {
                        while (lines.isNotEmpty()) {
                            val line = lines.removeFirst()
                            if (line.length >= DESC_WRAP_WIDTH) {
                                val charIndexToBreakAt = line.take(DESC_WRAP_WIDTH).lastIndexOf(' ')
                                val firstPart = line.substring(0, charIndexToBreakAt)
                                val secondPart = line.substring(charIndexToBreakAt + 1)
                                lines.add(0, secondPart) // Return second part for further processing
                                appendLine(firstPart)
                            } else {
                                appendLine(line)
                            }
                        }
                        check(this.last() == '\n')
                        this.deleteAt(this.lastIndex)
                    })
                }
                textLine()
            }

            renderFooter()
        }
    }

    /** Content (with possible `input()`) rendered just above all commands */
    protected open fun MainRenderScope.renderContentUpper() = Unit
    /** Content (with possible `input()`) rendered just below all commands */
    protected open fun MainRenderScope.renderContentLower() = Unit
    protected open fun RenderScope.renderHeader() = Unit
    protected open fun RenderScope.renderFooter() = Unit
}

