package dev.bitspittle.racketeer.console.view.views.admin

import com.varabyte.kotter.foundation.input.*
import com.varabyte.kotter.foundation.text.*
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.card.Pile
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateDiff
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.scripting.utils.addVariablesInto

private class ScriptingCommand(
    ctx: GameContext,
    private val inEditingMode: () -> Boolean,
    override val title: String,
    description: String,
    private val isDisabled: () -> Boolean = { false },
    private val action: () -> Unit
) : Command(ctx) {
    override val type: Type get() = if (inEditingMode() || isDisabled()) Type.Disabled else Type.Warning
    private val _description = description
    override val description get() = _description.takeUnless { inEditingMode() }
    override suspend fun invoke(): Boolean {
        action()
        return true
    }
}

class ScriptingView(ctx: GameContext) : View(ctx) {
    init {
        onEnteringView()
    }

    override val heading = """ Edit and scripts against the current game state. """.trimIndent()

    private val stringifier = Stringifier(ctx.describer, ctx.state)

    private var inEditingMode = true
    private var inputSoFar = ""
    private val previousActions = mutableListOf<String>()
    private var lastResultLog: String? = null

    private var stateSnapshot = ctx.state.copy()
    private var latestDiff = GameStateDiff(ctx.state, stateSnapshot)

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
            ctx.state = stateSnapshot.copy()
            latestDiff = GameStateDiff(ctx.state, stateSnapshot)
        },
    )

    override fun MainRenderScope.renderContentLower() {
        scopedState {
            if (!inEditingMode) black(isBright = true)

            previousActions.forEach { action ->
                textLine("- $action")
            }
            lastResultLog?.let { lastResultLog ->
                if (inEditingMode) green { textLine(lastResultLog) }
            }
            text("- ")
            input(isActive = inEditingMode); textLine()
        }
        textLine()
    }

    override suspend fun doHandleInputChanged(onInputChangedScope: OnInputChangedScope) {
        onInputChangedScope.apply {
            inputSoFar = input
        }
    }

    @Suppress("NAME_SHADOWING")
    override suspend fun doHandleInputEntered(onInputEnteredScope: OnInputEnteredScope) {
        onInputEnteredScope.apply {
            ctx.state.addVariablesInto(ctx.env)
            val evaluator = Evaluator()
            val result = evaluator.evaluate(ctx.env, input)
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
            clearInput()
        }
    }

    override suspend fun handleAdditionalKeys(key: Key): Boolean {
        return if (key == Keys.TAB) {
            inEditingMode = !inEditingMode
            true
        } else {
            inEditingMode // Swallow all the keys if we're in editing mode; otherwise, let the system act as normal
        }
    }

    override fun RenderScope.renderFooter() {
        text("Press "); cyan { text("TAB") }; text(" to set focus to ")
        text(if (inEditingMode) { "the menu" } else { "the script editor" })
        textLine('.')
    }

    private fun defineSpecialMethods() {
        ctx.env.addMethod(object : Method("choose", 2) {
            override suspend fun invoke(
                env: Environment,
                eval: Evaluator,
                params: List<Any>,
                options: Map<String, Any>,
                rest: List<Any>
            ): Any {
                error("Due to technical limitations, you cannot use the \"choose\" method inside the scripting console.")
            }
        }, allowOverwrite = true)
    }

    private fun onEnteringView() {
        ctx.env.pushScope() // Push scope with special scripting methods added to it
        defineSpecialMethods()
        ctx.env.pushScope() // Push new scope for user
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
            is Pile -> describer.describe(gameState, value) + if (value.cards.isNotEmpty()) {
                ": " + toString(value.cards)
            } else ""
            is CardTemplate -> value.name
            is Card -> value.template.name
            else -> value.toString()
        }
    }
}