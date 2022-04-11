package dev.bitspittle.racketeer.scripting.methods

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.methods.system.DbgMethod
import dev.bitspittle.racketeer.model.card.CardQueue
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.scripting.TestGameService
import dev.bitspittle.racketeer.scripting.methods.system.CancelMethod
import dev.bitspittle.racketeer.scripting.methods.system.StopMethod
import dev.bitspittle.racketeer.scripting.types.CancelPlayException
import dev.bitspittle.racketeer.scripting.types.CardQueueImpl
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class SystemMethodsTest {
    @Test
    fun testPlayExceptionMethods() = runTest {
        val env = Environment()
        var cardQueue: CardQueue? = null
        val gameService = TestGameService { cardQueue }
        env.addMethod(StopMethod(gameService::expectCardQueue))
        env.addMethod(DbgMethod(gameService.logger))
        env.addMethod(CancelMethod())

        val evaluator = Evaluator()

        cardQueue = CardQueueImpl(env).apply {
            enqueuePlayActions(CardTemplate("Log #1", "", listOf(), tier = 0, playActions = listOf("dbg 1")).instantiate())
            enqueuePlayActions(CardTemplate("Log #2", "", listOf(), tier = 0, playActions = listOf("dbg 2")).instantiate())
            enqueuePlayActions(CardTemplate("Stop", "", listOf(), tier = 0, playActions = listOf("stop!")).instantiate())
            enqueuePlayActions(CardTemplate("Log #3", "", listOf(), tier = 0, playActions = listOf("dbg 3")).instantiate())
        }

        assertThat(gameService.logs).isEmpty()
        cardQueue.start()
        assertThat(gameService.logs).containsExactly(
            "Debug: 1 # Int",
            "Debug: 2 # Int",
        ).inOrder()

        // stop! requires an action queue to be present
        cardQueue = null
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