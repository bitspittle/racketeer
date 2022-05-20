package dev.bitspittle.racketeer.console.command.commands.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext

open class BrowserCommand(
    ctx: GameContext,
    final override val title: String,
    final override val description: String,
    private val linkFilename: String
) : Command(ctx) {
    private var downloading = false
    final override val type get() = if (ctx.settings.unlocks.discord) Type.Normal else Type.Hidden
    final override suspend fun invoke(): Boolean {
        if (downloading) return false

        downloading = true
        ctx.app.cloudFileService.download(linkFilename,
            onDownloaded = { urlStr ->
                ctx.app.logger.info("Please point your browser at: ${urlStr.trim()}")
                ctx.app.logger.info("\nNote: Most terminals allow ctrl / cmd clicking the link.")
                downloading = false
            },
            onFailed = {
                ctx.app.logger.error("Could not fetch the URL right now. Try again later?")
                downloading = false
            }
        )

        return true
    }
}
