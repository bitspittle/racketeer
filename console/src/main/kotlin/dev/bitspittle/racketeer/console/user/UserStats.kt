package dev.bitspittle.racketeer.console.user

import dev.bitspittle.racketeer.console.command.commands.system.UserDataDir

class UserStats(cardStats: Iterable<CardStats>, buildingStats: Iterable<BuildingStats>, gameStats: Iterable<GameStats>) {
    companion object {
        fun loadFrom(userDataDir: UserDataDir) = UserStats(
            CardStats.loadFrom(userDataDir) ?: emptyList(),
            BuildingStats.loadFrom(userDataDir) ?: emptyList(),
            GameStats.loadFrom(userDataDir) ?: emptyList()
        )
    }

    val cards = cardStats.associateBy { it.name }.toMutableMap()
    val buildings = buildingStats.associateBy { it.name }.toMutableMap()
    val games = gameStats.toMutableList()
}

fun UserStats.clear() {
    cards.clear()
    buildings.clear()
}

fun UserStats.saveInto(userDataDir: UserDataDir) {
    cards.values.saveInto(userDataDir)
    buildings.values.saveInto(userDataDir)
    games.saveInto(userDataDir)
}
