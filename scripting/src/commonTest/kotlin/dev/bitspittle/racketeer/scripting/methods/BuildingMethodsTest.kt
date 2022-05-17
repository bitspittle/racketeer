package dev.bitspittle.racketeer.scripting.methods

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.racketeer.scripting.TestGameService
import dev.bitspittle.racketeer.scripting.methods.building.BlueprintGetMethod
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@Suppress("UNCHECKED_CAST")
class BuildingMethodsTest {
    @Test
    fun testBlueprintGetMethod() = runTest {
        val env = Environment()
        val service = TestGameService()
        env.addMethod(BlueprintGetMethod())

        env.storeValue("\$city-hall", service.gameData.blueprints.single { it.name == "City Hall" })
        env.storeValue("\$newsstand", service.gameData.blueprints.single { it.name == "Newsstand" })
        env.storeValue("\$stock-exc", service.gameData.blueprints.single { it.name == "Stock Exchange" })

        val evaluator = Evaluator()
        assertThat(evaluator.evaluate(env, "blueprint-get \$city-hall 'name")).isEqualTo("City Hall")
        assertThat(evaluator.evaluate(env, "blueprint-get \$newsstand 'name")).isEqualTo("Newsstand")
        assertThat(evaluator.evaluate(env, "blueprint-get \$stock-exc 'name")).isEqualTo("Stock Exchange")

        assertThat(evaluator.evaluate(env, "blueprint-get \$city-hall 'vp")).isEqualTo(0)
        assertThat(evaluator.evaluate(env, "blueprint-get \$newsstand 'vp")).isEqualTo(1)

        assertThat(evaluator.evaluate(env, "blueprint-get \$city-hall 'rarity")).isEqualTo(0)
        assertThat(evaluator.evaluate(env, "blueprint-get \$stock-exc 'rarity")).isEqualTo(2)

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "blueprint-get 'invalid-property")
        }
    }
}