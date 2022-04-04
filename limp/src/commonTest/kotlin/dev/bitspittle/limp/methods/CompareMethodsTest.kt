package dev.bitspittle.limp.methods

import com.varabyte.truthish.assertThat
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.methods.compare.*
import dev.bitspittle.limp.methods.math.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class CompareMethodsTest {
    @Test
    fun testComparisonMethods() = runTest {
        val env = Environment()

        val lt = LessThanMethod()
        val lte = LessThanEqualsMethod()
        val eq = EqualsMethod()
        val neq = NotEqualsMethod()
        val gt = GreaterThanMethod()
        val gte = GreaterThanEqualsMethod()

        listOf(Value(1), Value(2)).let { nums ->
            assertThat(lt.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(lte.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(eq.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(neq.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(gte.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(gt.invoke(env, nums).wrapped).isEqualTo(false)
        }

        listOf(Value(2), Value(2)).let { nums ->
            assertThat(lt.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(lte.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(eq.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(neq.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(gte.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(gt.invoke(env, nums).wrapped).isEqualTo(false)
        }

        listOf(Value(3), Value(2)).let { nums ->
            assertThat(lt.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(lte.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(eq.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(neq.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(gte.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(gt.invoke(env, nums).wrapped).isEqualTo(true)
        }

        listOf(Value('a'), Value('b')).let { nums ->
            assertThat(lt.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(lte.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(eq.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(neq.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(gte.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(gt.invoke(env, nums).wrapped).isEqualTo(false)
        }

        listOf(Value('b'), Value('b')).let { nums ->
            assertThat(lt.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(lte.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(eq.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(neq.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(gte.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(gt.invoke(env, nums).wrapped).isEqualTo(false)
        }

        listOf(Value('c'), Value('b')).let { nums ->
            assertThat(lt.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(lte.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(eq.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(neq.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(gte.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(gt.invoke(env, nums).wrapped).isEqualTo(true)
        }

        listOf(Value("A"), Value("B")).let { nums ->
            assertThat(lt.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(lte.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(eq.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(neq.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(gte.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(gt.invoke(env, nums).wrapped).isEqualTo(false)
        }

        listOf(Value("B"), Value("B")).let { nums ->
            assertThat(lt.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(lte.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(eq.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(neq.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(gte.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(gt.invoke(env, nums).wrapped).isEqualTo(false)
        }

        listOf(Value("C"), Value("B")).let { nums ->
            assertThat(lt.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(lte.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(eq.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(neq.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(gte.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(gt.invoke(env, nums).wrapped).isEqualTo(true)
        }
    }

    @Test
    fun testCompareMethod() = runTest {
        val env = Environment()
        val compareMethod = CompareMethod()

        assertThat(compareMethod.invoke(env, listOf(Value(1), Value(2))).wrapped as Int).isLessThan(0)
        assertThat(compareMethod.invoke(env, listOf(Value(2), Value(2))).wrapped as Int).isEqualTo(0)
        assertThat(compareMethod.invoke(env, listOf(Value(3), Value(2))).wrapped as Int).isGreaterThan(0)
    }
}