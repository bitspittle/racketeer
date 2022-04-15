package dev.bitspittle.racketeer.console.view.views.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.EndTurnCommand
import dev.bitspittle.racketeer.console.view.View

class ConfirmEndTurnView(ctx: GameContext) : View(ctx) {
    init {
        require(ctx.state.cash > 0)
    }

    override val heading = "You still have ${ctx.describer.describeCash(ctx.state.cash)} left that will be gone after this turn. Continue without spending?"

    override fun createCommands(): List<Command> = listOf(
        object : Command(ctx) {
            override val type = Type.Read

            override val title = "Confirm"

            override val description = "Press ENTER to proceed to the next turn. Otherwise, go back!"

            override suspend fun invoke(): Boolean {
                ctx.viewStack.popView() // Remove this view, and then...
                return EndTurnCommand(ctx, showConfirmationIfNecessary = false).invoke()
            }
        }
    )
}