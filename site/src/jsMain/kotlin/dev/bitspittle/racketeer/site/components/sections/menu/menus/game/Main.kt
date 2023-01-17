package dev.bitspittle.racketeer.site.components.sections.menu.menus.game

import androidx.compose.runtime.*
import com.varabyte.kobweb.silk.components.forms.Button
import dev.bitspittle.racketeer.model.game.isGameInProgress
import dev.bitspittle.racketeer.model.serialization.GameSnapshot
import dev.bitspittle.racketeer.site.components.sections.menu.*
import dev.bitspittle.racketeer.site.components.sections.menu.menus.user.UserDataMenu
import dev.bitspittle.racketeer.site.components.util.Data
import dev.bitspittle.racketeer.site.components.util.Payload
import dev.bitspittle.racketeer.site.components.util.Uploads
import dev.bitspittle.racketeer.site.components.util.toPopupParams
import dev.bitspittle.racketeer.site.components.widgets.YesNo
import dev.bitspittle.racketeer.site.components.widgets.YesNoDialog
import dev.bitspittle.racketeer.site.model.user.GameCancelReason
import dev.bitspittle.racketeer.site.model.user.GameStats
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.dom.*

class MainMenu(private val params: GameMenuParams) : Menu {
    override val title = "Main"

    @Composable
    override fun renderContent(actions: MenuActions) {
        if (params.ctx.settings.admin.enabled) {
            MenuButton(actions, Admin(params))
        }

        MenuButton(actions, BrowseAllCardsMenu(params))
        MenuButton(actions, ReviewHistory(params))
        MenuButton(actions, UserDataMenu(params.ctx.toPopupParams()))

        run {
            var showConfirmQuestion by remember { mutableStateOf(false) }
            Button(onClick = { showConfirmQuestion = true }) { Text("Restart") }

            if (showConfirmQuestion) {
                YesNoDialog(
                    "Are you sure?",
                ) { yesNo ->
                    showConfirmQuestion = false
                    if (yesNo == YesNo.YES) {
                        params.scope.launch { Uploads.upload(Payload.Abort(params.ctx)) }
                        params.ctx.userStats.games.add(GameStats.from(params.ctx.state, GameCancelReason.RESTARTED))
                        params.restart()
                    }
                }
            }
        }

        run {
            var showConfirmQuestion by remember { mutableStateOf(false) }
            Button(onClick = { showConfirmQuestion = true }, enabled = params.ctx.state.isGameInProgress) { Text("Save and Quit") }

            if (showConfirmQuestion) {
                YesNoDialog(
                    "Are you sure?",
                ) { yesNo ->
                    showConfirmQuestion = false
                    if (yesNo == YesNo.YES) {
                        val ctx = params.ctx
                        Data.save(Data.Keys.Quicksave, GameSnapshot.from(ctx.describer, ctx.state))
                        params.quit()
                    }
                }
            }
        }

        run {
            var showConfirmQuestion by remember { mutableStateOf(false) }
            Button(onClick = { showConfirmQuestion = true }, enabled = params.ctx.state.isGameInProgress) { Text("Quit") }

            if (showConfirmQuestion) {
                YesNoDialog(
                    "Are you sure?",
                    "You will lose your game if you quit. Consider running Quit & Save instead."
                ) { yesNo ->
                    showConfirmQuestion = false
                    if (yesNo == YesNo.YES) {
                        params.scope.launch { Uploads.upload(Payload.Abort(params.ctx)) }
                        params.ctx.userStats.games.add(GameStats.from(params.ctx.state, GameCancelReason.ABORTED))
                        Data.delete(Data.Keys.Quicksave)
                        params.quit()
                    }
                }
            }
        }
    }
}
