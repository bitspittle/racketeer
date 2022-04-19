package dev.bitspittle.racketeer.console.trie

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import org.junit.Test

class TextTreeTest {
    @Test
    fun createBasicTextTree() {
        val tree = MutableTextTree()
        tree.add("cat")
        tree.add("dog")
        tree.add("bird")
        tree.add("#blessed")
        tree.add("hello world")
        assertThat(tree.contains("cat"))
        assertThat(tree.contains("dog"))
        assertThat(tree.contains("bird"))
        assertThat(tree.contains("#blessed"))
        assertThat(tree.contains("hello world"))
        assertThat(!tree.contains("ca"))
        assertThat(!tree.contains("car"))
        assertThat(!tree.contains("catt"))
        assertThat(!tree.contains("hello"))
        assertThat(!tree.contains("world"))

        tree.clear()
        assertThat(!tree.contains("cat"))
        assertThat(!tree.contains("dog"))
        assertThat(!tree.contains("bird"))
        assertThat(!tree.contains("#blessed"))
        assertThat(!tree.contains("hello world"))
    }

    @Test
    fun emptyStringNotAllowed() {
        val tree = MutableTextTree()
        assertThrows<java.lang.IllegalArgumentException> {
            tree.add("")
        }
    }

    @Test
    fun textTreeIsCaseInsensitive() {
        val tree = MutableTextTree()
        tree.add("cat")
        assertThat(tree.contains("cat"))
        assertThat(tree.contains("CAT"))
        assertThat(tree.contains("cAt"))
    }

    private fun TextTreeCursor.iterateForward() = sequence {
        if (curr == null) return@sequence
        do {
            yield(curr!!)
        } while (next())
    }

    private fun TextTreeCursor.iterateBackwards() = sequence {
        if (curr == null) return@sequence
        do {
            yield(curr!!)
        } while (prev())
    }

    @Test
    fun canIterateTextTreeWithCursor() {
        val tree = MutableTextTree()
        tree.add("carthage")
        tree.add("dog")
        tree.add("car")
        tree.add("cart")
        tree.add("apple")
        tree.add("door")
        tree.add("dark")

        with(tree.cursor()) {
            assertThat(this.prefix).isEmpty()
            assertThat(this.iterateForward())
                .containsExactly("apple", "car", "cart", "carthage", "dark", "dog", "door")
                .inOrder()

            assertThat(this.curr).isEqualTo("door")

            assertThat(this.iterateBackwards())
                .containsExactly("door", "dog", "dark", "carthage", "cart", "car", "apple")
                .inOrder()

            // And forward again
            assertThat(this.iterateForward())
                .containsExactly("apple", "car", "cart", "carthage", "dark", "dog", "door")
                .inOrder()
        }

        with(tree.cursor()) {
            // How the user will experience the API
            assertThat(this.curr).isEqualTo("apple")
            assertThat(this.next()).isTrue()
            assertThat(this.curr).isEqualTo("car")
            assertThat(this.prev()).isTrue()
            assertThat(this.curr).isEqualTo("apple")
            assertThat(this.prev()).isFalse()
            assertThat(this.curr).isEqualTo("apple")
        }

        with(tree.cursor("cart")) {
            assertThat(this.prefix).isEqualTo("cart")
            assertThat(this.curr).isEqualTo("cart")
            assertThat(this.next()).isTrue()
            assertThat(this.curr).isEqualTo("carthage")
            assertThat(this.next()).isFalse()
            assertThat(this.prev()).isTrue()
            assertThat(this.curr).isEqualTo("cart")
            assertThat(this.prev()).isFalse()
        }
    }

    @Test
    fun cursorIsCaseInsensitive() {
        val tree = MutableTextTree()
        tree.add("car")
        tree.add("cart")
        tree.add("Carthage")

        with(tree.cursor("CaR")) {
            assertThat(this.prefix).isEqualTo("CaR")
            assertThat(this.iterateForward())
                .containsExactly("car", "cart", "Carthage")
                .inOrder()

        }
    }

    @Test
    fun emptyTreeProducesEmptyIterator() {
        val tree = MutableTextTree()
        with(tree.cursor()) {
            assertThat(this.prefix).isEmpty()
            assertThat(this.curr).isNull()
            assertThat(this.next()).isFalse()
            assertThat(this.prev()).isFalse()
        }
    }
}