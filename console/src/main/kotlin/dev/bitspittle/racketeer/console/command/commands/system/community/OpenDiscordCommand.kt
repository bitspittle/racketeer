package dev.bitspittle.racketeer.console.command.commands.system.community

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext

class OpenDiscordCommand(ctx: GameContext) : Command(ctx) {
    private var downloading = false
    override val type get() = if (ctx.settings.unlocks.discord) Type.Normal else Type.Hidden
    override val title = "Open Discord"
    override val description: String = "Open the ${ctx.data.title} Discord Server in your browser."
    override suspend fun invoke(): Boolean {
        if (downloading) return false

        downloading = true
        ctx.app.cloudFileService.download("discord.txt",
            onDownloaded = { urlStr ->
                ctx.app.logger.info("Please point your browser at: ${urlStr.trim()}")
                ctx.app.logger.info("\nNote: Most terminals allow ctrl / cmd clicking the link.")
                downloading = false
           },
            onFailed = {
                ctx.app.logger.error("Could not fetch Discord URL right now. Try again later?")
                downloading = false
            }
        )

        return true
    }
}
