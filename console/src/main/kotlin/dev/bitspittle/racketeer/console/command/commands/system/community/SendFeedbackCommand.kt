package dev.bitspittle.racketeer.console.command.commands.system.community

import dev.bitspittle.racketeer.console.command.commands.system.BrowserCommand
import dev.bitspittle.racketeer.console.game.GameContext

class SendFeedbackCommand(ctx: GameContext) : BrowserCommand(
    ctx,
    "Send Feedback",
    "Get a link for filing feedback that the devs will see.",
    "feedback.txt",
    ctx.settings.unlocks.feedback
)
