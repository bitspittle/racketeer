package dev.bitspittle.racketeer.console.command.commands.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.game.buildings.BuildingListView

class BuildingListCommand(ctx: GameContext) : Command(ctx) {
    override val type = if (ctx.settings.unlocks.buildings) Type.Normal else Type.Hidden
    override val title = "Building list"
    override val description: String = "Look through all the buildings in the game and see what you've built (${ctx.data.blueprints.count { ctx.userStats.buildings.contains(it.name) } * 100 / ctx.data.blueprints.size}%)."
    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(BuildingListView(ctx))
        return true
    }
}
