package dev.bitspittle.racketeer.scripting.methods

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.methods.system.DbgMethod
import dev.bitspittle.racketeer.scripting.TestCard
import dev.bitspittle.racketeer.scripting.TestEnqueuers
import dev.bitspittle.racketeer.scripting.TestGameService
import dev.bitspittle.racketeer.scripting.methods.system.CancelMethod
import dev.bitspittle.racketeer.scripting.methods.system.RunLaterMethod
import dev.bitspittle.racketeer.scripting.methods.system.StopMethod
import dev.bitspittle.racketeer.scripting.types.CancelPlayException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class SystemMethodsTest {
    @Test
    fun testStopAndCancelMethods() = runTest {
        val env = Environment()
        val service = TestGameService(enqueuers = TestEnqueuers(env))
        env.addMethod(StopMethod(service.enqueuers.actionQueue))
        env.addMethod(DbgMethod(service.logger))
        env.addMethod(CancelMethod())

        val evaluator = Evaluator()

        assertThat(service.logs).isEmpty()
        service.enqueuers.apply {
            card.enqueuePlayActions(service.gameState, TestCard("Log #1", playActions = listOf("dbg 1")))
            card.enqueuePlayActions(service.gameState, TestCard("Log #2", playActions = listOf("dbg 2")))
            card.enqueuePlayActions(service.gameState, TestCard("Stop", playActions = listOf("stop!")))
            card.enqueuePlayActions(service.gameState, TestCard("Log #3", playActions = listOf("dbg 3")))
            actionQueue.runEnqueuedActions()
        }

        assertThat(service.logs).containsExactly(
            "[D] 1 # Int",
            "[D] 2 # Int",
            // Debug: 3 never happens because it got stopped
        ).inOrder()

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "cancel!")
        }.also { ex ->
            assertThat(ex.cause is CancelPlayException)
        }
    }

    @Test
    fun testRunLaterMethod() = runTest {
        val env = Environment()
        val service = TestGameService(enqueuers = TestEnqueuers(env))
        env.addMethod(RunLaterMethod(service.enqueuers.actionQueue))
        env.addMethod(DbgMethod(service.logger))

        assertThat(service.logs).isEmpty()
        service.enqueuers.apply {
            card.enqueuePlayActions(service.gameState, TestCard("Log #1", playActions = listOf("dbg 1")))
            card.enqueuePlayActions(service.gameState, TestCard("Run Later #4", playActions = listOf("run-later '(dbg 4)")))
            card.enqueuePlayActions(service.gameState, TestCard("Log #2", playActions = listOf("dbg 2")))
            card.enqueuePlayActions(service.gameState, TestCard("Run Later #5", playActions = listOf("run-later '(dbg 5)")))
            card.enqueuePlayActions(service.gameState, TestCard("Log #3", playActions = listOf("dbg 3")))
            actionQueue.runEnqueuedActions()
        }

        assertThat(service.logs).containsExactly(
            "[D] 1 # Int",
            "[D] 2 # Int",
            "[D] 3 # Int",
            "[D] 4 # Int",
            "[D] 5 # Int",
        ).inOrder()
    }
}