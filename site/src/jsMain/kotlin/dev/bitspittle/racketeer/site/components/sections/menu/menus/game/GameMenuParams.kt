package dev.bitspittle.racketeer.site.components.sections.menu.menus.game

import dev.bitspittle.racketeer.site.model.GameContext
import dev.bitspittle.racketeer.site.model.GameUpdater
import kotlinx.coroutines.CoroutineScope

class GameMenuParams(
    val scope: CoroutineScope,
    val ctx: GameContext,
    val updater: GameUpdater,
    val restart: () -> Unit,
    val quit: () -> Unit,
)
