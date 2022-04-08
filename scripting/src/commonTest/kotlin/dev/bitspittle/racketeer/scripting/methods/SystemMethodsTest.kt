package dev.bitspittle.racketeer.scripting.methods

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.racketeer.scripting.methods.system.CancelMethod
import dev.bitspittle.racketeer.scripting.methods.system.StopMethod
import dev.bitspittle.racketeer.scripting.types.CancelPlayException
import dev.bitspittle.racketeer.scripting.types.FinishPlayException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class SystemMethodsTest {
   @Test
    fun testPlayExceptionMethods() = runTest {
        val env = Environment()
        env.addMethod(StopMethod())
        env.addMethod(CancelMethod())

        val evaluator = Evaluator()

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "stop!")
        }.also { ex ->
            assertThat(ex.cause is FinishPlayException)
        }

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "cancel!")
        }.also { ex ->
            assertThat(ex.cause is CancelPlayException)
        }
    }
}