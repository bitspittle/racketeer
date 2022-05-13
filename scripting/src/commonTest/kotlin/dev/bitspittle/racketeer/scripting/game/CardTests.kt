package dev.bitspittle.racketeer.scripting.game

import com.varabyte.truthish.assertThat
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.methods.math.AddMethod
import dev.bitspittle.limp.methods.system.DbgMethod
import dev.bitspittle.limp.methods.system.SetMethod
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.scripting.TestEnqueuers
import dev.bitspittle.racketeer.scripting.TestGameService
import dev.bitspittle.racketeer.scripting.methods.card.CardGetMethod
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class CardTests {
    @Test
    fun testEnqueuePlayActions() = runTest {
        val env = Environment()
        val service = TestGameService(enqueuers = TestEnqueuers(env))

        env.addMethod(SetMethod(service.logger))
        env.addMethod(DbgMethod(service.logger))
        env.addMethod(CardGetMethod())
        env.addMethod(AddMethod())

        // We're testing three things here...
        // 1 - That cards can set a variable in one part of their card and read from it later
        // 2 - That cards don't interfere with each other's variables (e.g. $c defined in card1 is removed before
        //     card2 runs)
        // 3 - That the $this variable is correct for each card
        val dummyCard1 = CardTemplate(
            "Card1",
            "unused",
            listOf("trickster"),
            tier = 0,
            playActions = listOf(
                "set '\$a 1",
                "set '\$b 2",
                "set '\$c + \$a \$b",
                "dbg card-get \$this 'name",
                "dbg \$c",
            )
        ).instantiate()
        val dummyCard2 = CardTemplate(
            "Card2",
            "unused",
            listOf("trickster"),
            tier = 0,
            playActions = listOf(
                "set '\$c 123",
                "dbg card-get \$this 'name",
                "dbg \$c",
            )
        ).instantiate()

        service.enqueuers.card.enqueuePlayActions(service.gameState, dummyCard1)
        service.enqueuers.card.enqueuePlayActions(service.gameState, dummyCard2)
        service.enqueuers.actionQueue.runEnqueuedActions()

        assertThat(service.logs).containsExactly(
            "[D] Debug: Card1 # String",
            "[D] Debug: 3 # Int",
            "[D] Debug: Card2 # String",
            "[D] Debug: 123 # Int"
        ).inOrder()
    }
}