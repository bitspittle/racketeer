package dev.bitspittle.racketeer.scripting.methods

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.racketeer.model.action.ActionQueue
import dev.bitspittle.racketeer.model.action.ActionRunner
import dev.bitspittle.racketeer.scripting.methods.system.CancelMethod
import dev.bitspittle.racketeer.scripting.methods.system.StopMethod
import dev.bitspittle.racketeer.scripting.types.CancelPlayException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class SystemMethodsTest {
    @Test
    fun testPlayExceptionMethods() = runTest {
        val env = Environment()
        var actionQueue: ActionQueue? = null
        env.addMethod(StopMethod { actionQueue })
        env.addMethod(CancelMethod())

        val evaluator = Evaluator()

        actionQueue = ActionQueue(env).apply {
            enqueue(listOf(Expr.Stub(1), Expr.Stub(2), Expr.Stub(3)))
        }

        assertThat(actionQueue.size).isEqualTo(3)
        evaluator.evaluate(env, "stop!")
        assertThat(actionQueue.size).isEqualTo(0)

        // stop! requires an action queue to be present
        actionQueue = null
        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "stop!")
        }

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "cancel!")
        }.also { ex ->
            assertThat(ex.cause is CancelPlayException)
        }
    }
}