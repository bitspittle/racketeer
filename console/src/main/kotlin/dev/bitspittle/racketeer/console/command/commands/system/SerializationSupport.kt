package dev.bitspittle.racketeer.console.command.commands.system

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.getLastModifiedTime

object SerializationSupport {
    private const val SAVE_PATH_BASE = "userdata/"

    fun pathForSlot(slot: Int) = Path(SAVE_PATH_BASE, "savegame.$slot.yaml")
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
