package dev.bitspittle.racketeer.scripting.methods

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.methods.collection.ListMethod
import dev.bitspittle.limp.methods.range.IntRangeMethod
import dev.bitspittle.limp.methods.system.SetMethod
import dev.bitspittle.limp.types.ConsoleLogger
import dev.bitspittle.limp.types.Placeholder
import dev.bitspittle.racketeer.scripting.methods.collection.ChooseHandler
import dev.bitspittle.racketeer.scripting.methods.collection.ChooseMethod
import dev.bitspittle.racketeer.scripting.types.CancelPlayException
import kotlinx.coroutines.test.runTest
import kotlin.math.log
import kotlin.test.Test

@Suppress("UNCHECKED_CAST")
class CollectionMethodsTest {
    @Suppress("JoinDeclarationAndAssignment") // Intentional style for readability
    @Test
    fun testChooseMethod() = runTest {
        val env = Environment()

        lateinit var chooseResponse: (List<Any>) -> List<Any>?
        val chooseHandler = object : ChooseHandler {
            override suspend fun query(
                prompt: String?,
                list: List<Any>,
                range: IntRange,
                requiredChoice: Boolean
            ): List<Any>? {
                return chooseResponse(list)
            }
        }
        val logger = ConsoleLogger()
        env.addMethod(ChooseMethod(logger, chooseHandler))
        env.addMethod(SetMethod(logger))
        env.addMethod(ListMethod())
        env.addMethod(IntRangeMethod())
        env.storeValue("_", Placeholder)

        val evaluator = Evaluator()

        evaluator.evaluate(env, "set '\$ints list 1 2 3 4 5")
        evaluator.evaluate(env, "set '\$empty list")

        chooseResponse = { list -> list.drop(1).take(2) }
        assertThat(evaluator.evaluate(env, "choose \$ints _") as List<Int>).containsExactly(2, 3).inOrder()

        // The chooser isn't asked about the specific case when the list is empty
        chooseResponse = { error("should not get called") }
        assertThat(evaluator.evaluate(env, "choose \$empty .. 0 _") as List<Int>).isEmpty()

        // The requested size is clamped when asking outside the range of the list size

        // Can't request taking more items than what exists in the list
        chooseResponse = { error("should not get called") }
        assertThat(evaluator.evaluate(env, "choose \$empty .. 1 5") as List<Int>).isEmpty()

        chooseResponse = { list -> list }
        assertThat(evaluator.evaluate(env, "choose \$ints .. 500 505") as List<Int>).containsExactly(1, 2, 3, 4, 5).inOrder()

        // Yikes! It's a developer bug if the choosehandler returns a list whose size isn't in the requested range
        chooseResponse = { list -> list.take(1) }
        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "choose \$ints .. 3 _")
        }
        // There, we fixed it
        chooseResponse = { list -> list.drop(1).dropLast(1) }
        assertThat(evaluator.evaluate(env, "choose \$ints .. 3 _") as List<Int>).containsExactly(2, 3, 4).inOrder()

        // Finally, test cancelling a choice (indicated by returning null
        chooseResponse = { null }
        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "choose \$ints _")
        }.also { ex ->
            // It's OK to cancel the play by default, but.... (see next block)
            assertThat(ex.cause).isInstanceOf<CancelPlayException>()
        }

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "choose --required _ \$ints _")
        }.also { ex ->
            // It's NOT OK to cancel required choices
            assertThat(ex.cause).isInstanceOf<IllegalStateException>()
        }
    }
}