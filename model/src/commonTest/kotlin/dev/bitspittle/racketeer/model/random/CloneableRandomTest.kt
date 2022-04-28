package dev.bitspittle.racketeer.model.random

import com.varabyte.truthish.assertThat
import net.mamoe.yamlkt.Yaml
import kotlin.random.Random
import kotlin.test.Test

class CopyableRandomTest {
    @Test
    fun copyableRandomBasedOnNormalRandom() {
        repeat(100) {
            val copyableRandom = CopyableRandom()
            val normalRandom = Random(copyableRandom.seed)
            val int1 = copyableRandom.nextInt()
            normalRandom.nextLong() // Implementation detail, but copyableRandom generates a new seed each time
            val int2 = normalRandom.nextInt()

            assertThat(int1).isEqualTo(int2)
        }
    }

    @Test
    fun checkCanCopyConsistentRandom() {
        val copyableRandom = CopyableRandom()

        repeat(10) {
            val copy1 = copyableRandom.copy()
            val copy2 = copyableRandom.copy()

            val nextInt = copyableRandom.nextInt()
            assertThat(copy1.nextInt()).isEqualTo(nextInt)
            assertThat(copy2.nextInt()).isEqualTo(nextInt)
        }
    }

    @Test
    fun canSerializeCopyableRandom() {
        repeat(100) {
            val copyableRandom = CopyableRandom()
            val copyrableRandomCopy = Yaml.decodeFromString(CopyableRandom.serializer(), Yaml.encodeToString(copyableRandom))

            assertThat(copyableRandom.nextInt()).isEqualTo(copyrableRandomCopy.nextInt())
            assertThat(copyableRandom.nextInt(123)).isEqualTo(copyrableRandomCopy.nextInt(123))
            assertThat(copyableRandom.nextInt(123, 456)).isEqualTo(copyrableRandomCopy.nextInt(123, 456))
        }
    }
}