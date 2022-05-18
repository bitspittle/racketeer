package dev.bitspittle.racketeer.console.command.commands.system

import net.harawata.appdirs.AppDirsFactory
import java.nio.file.Paths
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.getLastModifiedTime

class UserDataDir(appName: String) {
    companion object {
        const val QUICKSAVE_SLOT = -1
    }

    val path = Paths.get(AppDirsFactory.getInstance().getUserDataDir(appName, null, null))

    fun firstFreeSlot(): Int {
        var slot = 0
        while (pathForSlot(slot).exists()) {
            ++slot
        }
        return slot
    }

    fun pathForSlot(slot: Int) = path.resolve(if (slot >= 0) "savegame.$slot.yaml" else "quicksave.yaml")

    fun pathForCardStats() = path.resolve("cardstats.yaml")
    fun pathForBuildingStats() = path.resolve("bldgstats.yaml")
    fun pathForSettings() = path.resolve("settings.yaml")

    fun modifiedTime(slot: Int): String {
        val path = pathForSlot(slot).takeIf { it.exists() }
        return if (path != null) {
            path.getLastModifiedTime().toInstant().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss"))
        }
        else {
            "(EMPTY)"
        }
    }
}
