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
import dev.bitspittle.racketeer.console.command.commands.system.playtestId
import dev.bitspittle.racketeer.console.game.App
import dev.bitspittle.racketeer.console.game.version
import dev.bitspittle.racketeer.console.utils.UploadThrottleCategory
import dev.bitspittle.racketeer.console.utils.encodeToYaml
import dev.bitspittle.racketeer.console.view.views.game.GameView
import java.nio.file.Files
import kotlin.io.path.deleteExisting
import kotlin.io.path.writeText

private const val DESC_WRAP_WIDTH = 60

abstract class View(
    private val viewStack: ViewStack,
    private val app: App
) {
    protected abstract fun createCommands(): List<Command>
    private var shouldRefreshCommands = true
    private var _commandsSection: CommandsSection? = null
    private val commandsSection: CommandsSection
        get() {
            if (shouldRefreshCommands) {
                shouldRefreshCommands = false

                _commandsSection?.let { cs ->
                    val newIndex = refreshCursorPosition(cs.currIndex, cs.currCommand)
                    _commandsSection = CommandsSection(createCommands(), newIndex)
                } ?: run {
                    _commandsSection = CommandsSection(createCommands())
                }
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
        shouldRefreshCommands = true
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

            // At this point, let's try to send an automatic crash report. We wrap it in an aggressive try/catch block
            // though because there's nothing more fun than throwing an exception while trying to handle an exception
            (viewStack.currentView as? GameView)?.ctx?.let { ctx ->
                try {
                    val viewName = this::class.simpleName!!.lowercase().removeSuffix("view")
                    val command = currCommand.title
                        .map { if (it.isLetterOrDigit()) it else '_' }
                        .joinToString("")
                        // Collapse all underscores and make sure any of them don't show up in weird places
                        .replace(Regex("__+"), "_")
                        .trim('_')
                        .lowercase()
                    val filename =
                        "versions:${app.version}:users:${app.userData.playtestId}:crashes:$viewName-$command.yaml"
                    val tmp = Files.createTempFile("docrimes-crash", ".yaml").apply {
                        writeText(ctx.encodeToYaml())
                    }
                    app.uploadService.upload(filename, tmp, UploadThrottleCategory.CRASH_REPORT) { tmp.deleteExisting() }
                } catch (ignored: Throwable) { }
            }
        }
    }

    suspend fun handleKey(key: Key): Boolean {
        return when (key) {
            Keys.ENTER -> {
                if (currCommand.type !in listOf(Command.Type.Disabled, Command.Type.Blocked)) {
                    runUnsafeCode { currCommand.invoke() }
                    true
                } else {
                    // Blocked should still consider the key handled; disabled does not
                    return currCommand.type == Command.Type.Blocked
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

