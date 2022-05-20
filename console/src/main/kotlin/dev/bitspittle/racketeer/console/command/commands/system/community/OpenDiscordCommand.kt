package dev.bitspittle.racketeer.console.command.commands.system.community

import dev.bitspittle.racketeer.console.command.commands.system.BrowserCommand
import dev.bitspittle.racketeer.console.game.GameContext

class OpenDiscordCommand(ctx: GameContext) : BrowserCommand(
    ctx,
    "Open Discord",
    "Get a link for joining the ${ctx.data.title} Discord Server.",
    "discord.txt",
    ctx.settings.unlocks.discord
)