package dev.bitspittle.racketeer.model.random

import com.varabyte.truthish.assertThat
import kotlin.random.Random
import kotlin.test.Test

class CloneableRandomTest {
    @Test
    fun cloneableRandomBasedOnNormalRandom() {
        repeat(100) {
            val cloneableRandom = CloneableRandom()
            val normalRandom = Random(cloneableRandom.seed)
            val int1 = cloneableRandom.nextInt()
            normalRandom.nextLong() // Implementation detail, but cloneableRandom generates a new seed each time
            val int2 = normalRandom.nextInt()

            assertThat(int1).isEqualTo(int2)
        }
    }

    @Test
    fun checkCanCopyConsistentRandom() {
        val cloneableRandom = CloneableRandom()

        repeat(10) {
            val copy1 = cloneableRandom.copy()
            val copy2 = cloneableRandom.copy()

            val nextInt = cloneableRandom.nextInt()
            assertThat(copy1.nextInt()).isEqualTo(nextInt)
            assertThat(copy2.nextInt()).isEqualTo(nextInt)
        }
    }
}