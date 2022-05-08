package dev.bitspittle.racketeer.console.utils

import net.mamoe.yamlkt.Yaml

fun <T: Any?> T.encodeToYaml() = Yaml { this.encodeDefaultValues = false }.encodeToString(this)
