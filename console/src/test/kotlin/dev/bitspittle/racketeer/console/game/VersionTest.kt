package dev.bitspittle.racketeer.console.game

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import org.junit.Test

class VersionTest {
    @Test
    fun compareVersions() {
        assertThat(Version("4.0.1") > Version("3.9.9")).isTrue()
        assertThat(Version("3.4.5") > Version("3.2.9")).isTrue()
        assertThat(Version("2.4.9") > Version("2.4.8")).isTrue()

        assertThat(Version("3.9.9") < Version("4.0.1")).isTrue()
        assertThat(Version("3.2.9") < Version("3.4.5")).isTrue()
        assertThat(Version("2.4.8") < Version("2.4.9")).isTrue()

        assertThat(Version("4.0.1") == Version("4.0.1")).isTrue()
        assertThat(Version("3.4.5") == Version("3.4.5")).isTrue()
        assertThat(Version("2.4.9") == Version("2.4.9")).isTrue()
        assertThat(Version("3.9.9") == Version("3.9.9")).isTrue()
        assertThat(Version("3.2.9") == Version("3.2.9")).isTrue()
        assertThat(Version("2.4.8") == Version("2.4.8")).isTrue()
    }

    @Test
    fun rejectInvalidVersions() {
        assertThrows<IllegalArgumentException> { Version("1.2") }
        assertThrows<IllegalArgumentException> { Version("1.2.3.4") }
        assertThrows<IllegalArgumentException> { Version("one.two.three") }
        assertThrows<IllegalArgumentException> { Version("") }
    }
}