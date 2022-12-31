package dev.bitspittle.racketeer.site.components.util

import dev.bitspittle.racketeer.model.serialization.GameSnapshot
import dev.bitspittle.racketeer.site.model.GameContext
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.mamoe.yamlkt.Yaml
import kotlin.js.Date

fun GameContext.downloadSnapshotToDisk() {
    val snapshot = GameSnapshot.from(describer, state)
    Data.save(Data.Keys.Quicksave, snapshot)

    document.downloadFileToDisk("do-crimes_${Date().toFilenameString()}.dcr", "text/yaml", snapshot.encodeToYaml())
}

fun GameContext.loadSnapshotFromDisk(
    scope: CoroutineScope,
    onLoaded: () -> Unit = {},
) {
    document.loadFileFromDisk(".dcr") { content ->
        scope.launch {
            val snapshot = Yaml.decodeFromString(GameSnapshot.serializer(), content)
            snapshot.create(data, env, enqueuers) { newState ->
                state = newState
            }
            onLoaded()
        }
    }
}