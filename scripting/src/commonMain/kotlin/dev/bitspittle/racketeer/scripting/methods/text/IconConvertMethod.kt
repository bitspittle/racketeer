package dev.bitspittle.racketeer.scripting.methods.text

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.racketeer.model.text.Describer

class IconConvertMethod(private val describer: Describer) : Method("icon-convert", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val text = env.expectConvert<String>(params[0])
        val expand = options["expand"]?.let { env.expectConvert(it) } ?: true

        return if (expand) describer.convertIcons(text) else describer.convertIcons(text, maxExpand = 0)
    }
}