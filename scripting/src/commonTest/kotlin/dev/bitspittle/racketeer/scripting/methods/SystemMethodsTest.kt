package dev.bitspittle.racketeer.scripting.methods

import com.varabyte.truthish.assertThat
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.racketeer.scripting.TestGameService
import dev.bitspittle.racketeer.scripting.methods.system.DbgMethod
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class SystemMethodsTest {
    @Test
    fun testDbgMethod() = runTest {
        val env = Environment()
        val service = TestGameService()
        env.addMethod(DbgMethod(service))

        assertThat(service.logs.isEmpty())

        val evaluator = Evaluator()

        evaluator.evaluate(env, "dbg 123")
        assertThat(service.logs).containsExactly("[DBG] 123")

        evaluator.evaluate(env, "dbg --msg \"Debug value\" 456")
        assertThat(service.logs).containsExactly("[DBG] 123", "[DBG] Debug value: 456")
    }
}