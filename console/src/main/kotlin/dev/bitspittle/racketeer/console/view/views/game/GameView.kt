package dev.bitspittle.racketeer.console.view.views.game

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.*
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.console.view.views.admin.AdminMenuView
import dev.bitspittle.racketeer.console.view.views.game.cards.BrowsePilesView
import dev.bitspittle.racketeer.console.view.views.game.play.GameSummaryView
import dev.bitspittle.racketeer.console.view.views.game.play.PlayCardsView
import dev.bitspittle.racketeer.console.view.views.game.play.PreDrawView
import dev.bitspittle.racketeer.console.view.views.system.OptionsMenuView
import dev.bitspittle.racketeer.console.view.views.system.TitleMenuView
import dev.bitspittle.racketeer.model.game.allPiles

abstract class GameView(val ctx: GameContext) : View(ctx.settings, ctx.viewStack, ctx.app) {
    protected open val allowEsc: Boolean = true
    protected open val allowBrowseCards: Boolean = true

    private fun hasGameStarted() = ctx.state.allPiles.any { it.cards.isNotEmpty() }

    private fun allowAdminAccess(): Boolean {
        if (!hasGameStarted()) return false
        return ctx.settings.admin.enabled && !(ctx.viewStack.contains { view -> view is AdminMenuView })
    }

    private fun allowBrowsingCards(): Boolean {
        if (!hasGameStarted()) return false
        return allowBrowseCards && ctx.viewStack.contains { view -> (view is PreDrawView || view is PlayCardsView || view is GameSummaryView) } && !ctx.viewStack.contains { view -> view is BrowsePilesView }
    }

    final override suspend fun doHandleKeys(key: Key): Boolean {
        return when (key) {
            Keys.ESC -> {
                if (allowEsc) {
                    if (ctx.viewStack.canGoBack) {
                        onEscRequested()
                        goBack()
                    } else {
                        ctx.viewStack.pushView(OptionsMenuView(ctx))
                    }
                    true
                } else {
                    false
                }
            }
            Keys.TICK, Keys.TILDE -> {
                if (allowAdminAccess()) {
                    ctx.viewStack.pushView(AdminMenuView(ctx))
                    true
                } else false
            }
            Keys.BACKSLASH -> {
                if (allowBrowsingCards()) {
                    ctx.viewStack.pushView(BrowsePilesView(ctx))
                    true
                } else false
            }

            else -> handleAdditionalKeys(key)
        }
    }

    protected open suspend fun handleAdditionalKeys(key: Key): Boolean = false

    override fun RenderScope.renderHeader() {
        textLine() // Give the top line some breathing space from the prompt

        val state = ctx.state

        if (!ctx.viewStack.contains { view -> view is TitleMenuView }) {
            textLine(
                "${ctx.describer.describeCash(state.cash)} ${ctx.describer.describeInfluence(state.influence)} ${
                    ctx.describer.describeLuck(
                        state.luck
                    )
                } ${ctx.describer.describeVictoryPoints(state.vp)} "
            )
            textLine()
            scopedState {
                val numRemainingTurns = state.numTurns - state.turn
                if (numRemainingTurns == 1) red() else if (numRemainingTurns <= 4) yellow()
                bold { textLine("Turn ${state.turn + 1} out of ${state.numTurns}") }
            }
            textLine()
        }

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

    final override fun RenderScope.renderFooter() {
        renderUpperFooter()

        if (allowBrowsingCards()) {
            text("Press "); cyan { text("\\") }; textLine(" to browse all card piles.")
        }
        if (allowAdminAccess()) {
            text("Press "); cyan { text("~") }; textLine(" to access the admin menu.")
        }
        text("Press "); cyan { text("UP/DOWN") }; text(", "); cyan { text("HOME/END") }; text(", and "); cyan { text("PGUP/PGDN") }; textLine(" to navigate choices.")
        if (allowEsc) {
            text("Press "); cyan { text("ESC") }
            if (ctx.viewStack.canGoBack) textLine(" to go back.") else textLine(" to open options.")
        }

        renderLowerFooter()
    }

    protected open fun RenderScope.renderUpperFooter() = Unit
    protected open fun RenderScope.renderLowerFooter() = Unit
    protected open fun onEscRequested() = Unit
}