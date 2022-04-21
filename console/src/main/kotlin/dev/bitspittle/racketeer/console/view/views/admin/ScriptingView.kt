package dev.bitspittle.racketeer.console.view.views.admin

import com.varabyte.kotter.foundation.input.*
import com.varabyte.kotter.foundation.text.*
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.utils.toIdentifierName
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.trie.MutableTextTree
import dev.bitspittle.racketeer.console.trie.TextTree
import dev.bitspittle.racketeer.console.trie.TextTreeCursor
import dev.bitspittle.racketeer.console.trie.intoWordTree
import dev.bitspittle.racketeer.console.view.views.game.GameView
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateDiff
import dev.bitspittle.racketeer.model.game.reportTo
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.scripting.types.CardProperty
import dev.bitspittle.racketeer.scripting.types.GameProperty
import dev.bitspittle.racketeer.scripting.types.PileProperty
import dev.bitspittle.racketeer.scripting.utils.addVariablesInto

private class ScriptingCommand(
    ctx: GameContext,
    private val inEditingMode: () -> Boolean,
    override val title: String,
    description: String,
    private val isDisabled: () -> Boolean = { false },
    private val action: suspend () -> Unit
) : Command(ctx) {
    override val type: Type get() = if (inEditingMode() || isDisabled()) Type.Disabled else Type.Warning
    private val _description = description
    override val description get() = _description.takeUnless { inEditingMode() }
    override suspend fun invoke(): Boolean {
        action()
        return true
    }
}

class ScriptingView(ctx: GameContext) : GameView(ctx) {
    init {
        onEnteringView()
    }

    override val heading = """ Edit and scripts against the current game state. """.trimIndent()
    override val allowBrowseCards get() = !inEditingMode

    private val stringifier = Stringifier(ctx.describer, ctx.state)

    private var inEditingMode = true
    private var inputSuffix = ""
    private val previousActions = mutableListOf<String>()
    private var lastResultLog: String? = null

    private var stateSnapshot = ctx.state.copy()
    private var latestDiff = GameStateDiff(ctx.state, stateSnapshot)

    private val symbolTextTree = MutableTextTree()
    init { refreshSymbolTextTree() }
    private val stringTextTree: TextTree = MutableTextTree().apply {
        ctx.data.cards.forEach { card -> this.add(card.name) }
    }
    private val propertiesTextTree: TextTree = listOf(GameProperty.values(), CardProperty.values(), PileProperty.values())
        .asSequence()
        .flatMap { it.asSequence() }
        .map { it.name.toIdentifierName() }
        .toList()
        .intoWordTree()

    private var textCursor: TextTreeCursor? = null
    private val inputCompleter = object : InputCompleter {
        override fun complete(input: String): String? {
            return textCursor?.let { it.curr?.drop(it.prefix.length) }
        }
    }

    override fun createCommands(): List<Command> = listOf(
        ScriptingCommand(
            ctx,
            { inEditingMode },
            "Clear editor",
            "Remove all actions entered and variables / methods defined. This will NOT affect the underlying game state.",
        ) {
            previousActions.clear()
            lastResultLog = null
            ctx.env.popScope() // Drop user stuff defined so far
            ctx.env.pushScope() // And make a new playground for them
        },
        ScriptingCommand(
            ctx,
            { inEditingMode },
            "Take game snapshot",
            "Make a backup snapshot of the current game that you can restore to if you screw something up.",
            isDisabled = { latestDiff.hasNoChanges() },
        ) {
            ctx.state.updateVictoryPoints()
            stateSnapshot = ctx.state.copy()
            latestDiff = GameStateDiff(ctx.state, stateSnapshot)
        },
        ScriptingCommand(
            ctx,
            { inEditingMode },
            "Restore game snapshot",
            "Return to the last snapshot that you took.",
            isDisabled = { latestDiff.hasNoChanges() },
        ) {
            ctx.state.updateVictoryPoints()
            ctx.state = stateSnapshot.copy()
            latestDiff = GameStateDiff(ctx.state, stateSnapshot)
        },
    )

    override fun MainRenderScope.renderContentLower() {
        scopedState {
            if (!inEditingMode) black(isBright = true)

            if (previousActions.isNotEmpty()) {
                previousActions.forEach { action ->
                    textLine("- $action")
                }
                lastResultLog?.let { lastResultLog ->
                    if (inEditingMode) green { textLine(lastResultLog) }
                }
                textLine()
            }
            text("- ")
            yellow {
                input(completer = inputCompleter, isActive = inEditingMode)
                text(inputSuffix)
                textLine()
            }
        }
        textLine()
    }

    override suspend fun doHandleInputChanged(input: String) {
        var openedParensCount = 0
        var inString = false

        input.toCharArray().toMutableList().let { remaining ->
            while (remaining.isNotEmpty()) {
                when (remaining.removeFirst()) {
                    '(' -> if (!inString) {
                        ++openedParensCount
                    }
                    ')' -> if (!inString) {
                        --openedParensCount
                    }
                    '"' -> inString = !inString
                    '\\' -> remaining.removeFirstOrNull() // Eat next char, it's not for us to count
                }
            }
        }

        inputSuffix = buildString {
            if (inString) {
                append('"')
            }
            repeat(openedParensCount.coerceAtLeast(0)) { append(')')}
        }

        textCursor = null
        input.takeLastWhile { !it.isWhitespace() }.let { lastWord ->
            @Suppress("NAME_SHADOWING")
            var lastWord = lastWord
            while (textCursor == null && lastWord.isNotEmpty()) {
                when (lastWord.first()) {
                    '"' -> {
                        val str = lastWord.drop(1)
                        textCursor = stringTextTree.cursor(str)
                    }
                    '\'' -> {
                        lastWord = lastWord.drop(1)
                        textCursor = propertiesTextTree.cursor(lastWord).takeIf { it.curr != null }
                    }
                    '(' -> {
                        lastWord = lastWord.drop(1)
                    }
                    else -> {
                        textCursor = symbolTextTree.cursor(lastWord)
                    }
                }
            }
        }
    }

    private fun refreshSymbolTextTree() {
        symbolTextTree.clear()
        (ctx.env.getMethodNames() + ctx.env.getVariableNames()).forEach { name ->
            symbolTextTree.add(name)
        }
        symbolTextTree.add("\$this")
        symbolTextTree.add("\$it")
        symbolTextTree.add("\$card")
    }

    @Suppress("NAME_SHADOWING")
    override suspend fun doHandleInputEntered(input: String, clearInput: () -> Unit) {
        val input = input + inputSuffix
        ctx.state.addVariablesInto(ctx.env)
        val prevState = ctx.state.copy()

        val evaluator = Evaluator()
        val result = evaluator.evaluate(ctx.env, input)
        ctx.state.updateVictoryPoints()
        latestDiff = GameStateDiff(stateSnapshot, ctx.state)

        previousActions.add(input)
        while (previousActions.size > Constants.PAGE_SIZE) {
            previousActions.removeFirst()
        }

        lastResultLog = null
        result.takeIf { it != Unit }?.let { result ->
            ctx.env.storeValue("\$last", result, allowOverwrite = true)
            lastResultLog = "\$last = ${stringifier.toString(result)}"
        }
        refreshSymbolTextTree()

        GameStateDiff(prevState, ctx.state).reportTo(ctx.describer, ctx.app.logger)

        inputSuffix = ""
        clearInput()
    }

    override suspend fun handleAdditionalKeys(key: Key): Boolean {
        return if (key == Keys.TAB) {
            inEditingMode = !inEditingMode
            true
        } else {
            if (inEditingMode) {
                when (key) {
                    Keys.UP -> textCursor?.prev()
                    Keys.DOWN -> textCursor?.next()
                }
            }
            inEditingMode // Swallow all the keys if we're in editing mode; otherwise, let the system act as normal
        }
    }

    override fun RenderScope.renderUpperFooter() {
        text("Press "); cyan { text("TAB") }; text(" to set focus to ")
        text(if (inEditingMode) { "the menu" } else { "the script editor" })
        textLine('.')
    }

    private fun defineSpecialMethods() {
    }

    private fun onEnteringView() {
        ctx.env.pushScope() // Push scope with special scripting methods added to it
        defineSpecialMethods()
        ctx.env.pushScope() // Push new scope for user
        ctx.state.addVariablesInto(ctx.env)
        // See: onEscRequested for teardown
    }

    override fun onEscRequested() {
        ctx.env.popScope() // Pop user scope
        ctx.env.popScope() // Pop scripting scope
    }
}

private class Stringifier(private val describer: Describer, private val gameState: GameState) {
    fun <T: Any?> toString(value: T): String {
        return when (value) {
            is Iterable<*> -> value.joinToString(prefix = "[", postfix = "]") { toString(it) }
            is Pile -> describer.describePileTitle(gameState, value) + if (value.cards.isNotEmpty()) {
                ": " + toString(value.cards)
            } else ""
            is CardTemplate -> value.name
            is Card -> value.template.name
            else -> value.toString()
        }
    }
}