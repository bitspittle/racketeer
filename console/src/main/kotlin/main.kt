import dev.bitspittle.racketeer.console.game.GameSession
import dev.bitspittle.racketeer.model.game.GameData
import net.mamoe.yamlkt.Yaml
import kotlin.io.path.Path
import kotlin.io.path.readText

private val String.indentLength: Int
    get() = this.takeWhile { c -> c == ' ' }.length

fun main() {
    val gameData = with(Path("gamedata.yaml").readText(Charsets.UTF_8)) {
        // HACK ALERT!
        //
        // The multiplatform Yaml library we are using does not currently support multiline strings, so we pre-process
        // the text to convert it to a form it can handle.
        //
        // In other words, we turn this:
        //
        // ```
        // ex: |
        //   This is
        //
        //   a multiline
        //   string.
        // ```
        //
        // into:
        //
        // ```
        // ex: "This is\n\na multiline\nstring"
        // ```
        val unprocessed = this
        val processed = buildString {
            var inMultilineString = false
            var parentIndentLength = 0
            var multilineIndentLength = 0
            var prependNewline = false

            fun openMultilineString(line: String) {
                append('"')
                parentIndentLength = line.indentLength
                inMultilineString = true
                multilineIndentLength = 0
                prependNewline = false
            }
            fun closeMultilineString() {
                appendLine('"')
                inMultilineString = false
            }

            unprocessed.lines().forEach { line ->
                if (inMultilineString && parentIndentLength > 0 && line.isNotBlank() && line.indentLength <= parentIndentLength) {
                    closeMultilineString()
                }

                if (!inMultilineString) {
                    if (line.endsWith(": |")) {
                        append(line.substringBeforeLast("|"))
                        openMultilineString(line)
                    } else {
                        appendLine(line)
                    }
                } else {
                    assert(inMultilineString)
                    if (prependNewline) {
                        append("\\n")
                        prependNewline = false
                    }
                    if (line.isBlank()) {
                        append("\\n")
                    } else {
                        if (multilineIndentLength == 0) {
                            multilineIndentLength = line.indentLength
                        }
                        // Be careful of nesting quotes within quotes!
                        val escapedLine = line.replace("\"", "\\\"")
                        append(escapedLine.drop(multilineIndentLength))
                        prependNewline = true
                    }
                }
            }

            if (inMultilineString) {
                closeMultilineString()
            }
        }

        Yaml.decodeFromString(GameData.serializer(), processed)
    }

    GameSession(gameData).start()
}