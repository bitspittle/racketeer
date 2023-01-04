package dev.bitspittle.racketeer.site.components.util

import dev.bitspittle.racketeer.model.serialization.GameSnapshot
import dev.bitspittle.racketeer.site.model.GameContext
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.mamoe.yamlkt.Yaml
import org.w3c.dom.Document
import kotlin.js.Date

fun GameContext.downloadSnapshotToDisk() {
    val snapshot = GameSnapshot.from(describer, state)
    Data.save(Data.Keys.Quicksave, snapshot)

    val prefix = this.data.title.lowercase().replace(' ', '-')
    document.downloadFileToDisk("${prefix}_${Date().toFilenameString()}.sav", "text/yaml", snapshot.encodeToYaml())
}

fun Document.loadSnapshotFromDisk(
    scope: CoroutineScope,
    provideGameContext: suspend () -> GameContext,
    onLoaded: suspend () -> Unit = {},
) {
    // .dcr was a legacy format, before I realized the title might change
    loadFileFromDisk(".dcr,.sav") { content ->
        scope.launch {
            val snapshot = Yaml.decodeFromString(GameSnapshot.serializer(), content)
            with(provideGameContext()) {
                snapshot.create(data, env, enqueuers) { newState -> state = newState }
            }
            onLoaded()
        }
    }
}

fun GameContext.loadSnapshotFromDisk(
    scope: CoroutineScope,
    onLoaded: suspend () -> Unit = {},
) {
    document.loadSnapshotFromDisk(scope, provideGameContext = { this }, onLoaded)
}