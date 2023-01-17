package dev.bitspittle.racketeer.site.components.util

import net.mamoe.yamlkt.Yaml

fun <T: Any> T.encodeToYaml() = Yaml { this.encodeDefaultValues = false }.encodeToString(this)
