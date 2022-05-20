package dev.bitspittle.racketeer.console.command.commands.system.community

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.game.Version
import dev.bitspittle.racketeer.console.game.version
import dev.bitspittle.racketeer.console.view.views.game.buildings.BuildingListView
import java.awt.Desktop
import java.net.URI

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
                Desktop.getDesktop().browse(URI.create(urlStr.trim()))
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
