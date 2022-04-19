package dev.bitspittle.racketeer.console.trie

import java.util.*

/** A trie-tree for storing words that can be searched and iterated quickly and efficiently */
interface TextTree {
    fun contains(word: String): Boolean

    /**
     * Return a double-direction iterator that lets you search for all strings in this word tree which start with the
     * passed in prefix.
     */
    fun cursor(prefix: String = ""): TextTreeCursor
}

interface TextTreeCursor {
    val prefix: String
    val curr: String?
    fun next(): Boolean
    fun prev(): Boolean
}

class MutableTextTree() : TextTree {
    constructor(words: Iterable<String>) : this() {
        words.forEach { add(it) }
    }

    private class Node(
        val parent: Node? = null,
        val edges: SortedMap<Char, Node> = sortedMapOf(),
        var word: String? = null,
    ) {
        /** The path of ancestors, starting with the current node and going up to the oldest ancestor */
        fun ancestors(): Sequence<Node> = sequence {
            var currNode: Node? = this@Node
            do {
                yield(currNode!!)
                currNode = currNode.parent
            } while (currNode != null)
        }

        override fun toString(): String {
            return "Node(word = $word)"
        }
    }

    private val root = Node()

    private fun nodeFor(prefix: String): Node? {
        var currNode = root
        for (c in prefix.lowercase()) {
            currNode = currNode.edges[c] ?: return null
        }
        return currNode
    }

    fun add(word: String) {
        require(word.isNotEmpty())

        var currNode = root
        for (c in word.lowercase()) {
            currNode = currNode.edges.getOrPut(c) { Node(currNode) }
        }

        currNode.word = word
    }

    fun clear() {
        root.edges.clear()
    }

    override fun contains(word: String): Boolean {
        return nodeFor(word)?.word == word
    }


    override fun cursor(prefix: String): TextTreeCursor = nodeFor(prefix)?.let { TextTreeCursorImpl(prefix, it) } ?: EmptyTextTreeCursor(prefix)

    private class EmptyTextTreeCursor(override val prefix: String) : TextTreeCursor {
        override val curr = null
        override fun next() = false
        override fun prev() = false
    }

    private class TextTreeCursorImpl(override val prefix: String, private val rootNode: Node) : TextTreeCursor {
        private data class SearchState(val node: Node, var index: Int)
        private var currNode: Node? = searchForwardFrom(rootNode, canMatchSelf = true)

        override val curr get() = currNode?.word

        override fun next(): Boolean {
            val currNode = currNode ?: return false
            return searchForwardFrom(currNode)?.let { newNode ->
                this.currNode = newNode
                true
            } ?: false
        }

        override fun prev(): Boolean {
            val currNode = currNode ?: return false
            return searchBackwardFrom(currNode)?.let { newNode ->
                this.currNode = newNode
                true
            } ?: false
        }

        private fun searchForwardFrom(startingPoint: Node, canMatchSelf: Boolean = false): Node? {
            val parentChildStack = startingPoint.ancestors().mapNotNull { c -> c.parent?.let { p -> p to c } }
            val searchStateStack =
                parentChildStack.map { (parent, child) -> SearchState(parent, parent.edges.values.indexOf(child)) }
                    .toMutableList().apply {
                        reverse()
                        add(SearchState(startingPoint, -1))
                    }

            while (true) { // Searching upward
                while (true) { // Searching downward
                    val searchState = searchStateStack.last()
                    if (searchState.index == -1 && searchState.node.word != null && (canMatchSelf || searchState.node != startingPoint)) {
                        return searchState.node
                    }

                    val nextNode: Node = searchState.node.edges.values.toList().getOrNull(++searchState.index) ?: break
                    searchStateStack.add(SearchState(nextNode, -1))
                }

                // If here, we ran out of children. Go up instead!
                // If we got back to the original search state, that means we're all out of words!
                if (searchStateStack.last().node != rootNode) {
                    searchStateStack.removeLast()
                } else {
                    return null
                }
            }
        }

        private fun searchBackwardFrom(startingPoint: Node, canMatchSelf: Boolean = false): Node? {
            val parentChildStack = startingPoint.ancestors().mapNotNull { c -> c.parent?.let { p -> p to c } }
            val searchStateStack =
                parentChildStack.map { (parent, child) -> SearchState(parent, parent.edges.values.indexOf(child)) }
                    .toMutableList().apply {
                        reverse()
                        add(SearchState(startingPoint, -1))
                    }

            while (true) { // Searching upward
                while (true) { // Searching downward
                    val searchState = searchStateStack.last()
                    if (searchState.index == -1) {
                        if (searchState.node.word != null && (canMatchSelf || startingPoint != searchState.node)) {
                            return searchState.node
                        }
                        break
                    }

                    searchState.node.edges.values.toList().getOrNull(--searchState.index)?.let { nextNode ->
                        searchStateStack.add(SearchState(nextNode, nextNode.edges.size))
                    }
                }

                // If we got back to the original search state, that means we're all out of words!
                if (searchStateStack.last().node != rootNode) {
                    searchStateStack.removeLast()
                } else {
                    return null
                }
            }
        }
    }
}

fun Iterable<String>.intoWordTree() = MutableTextTree(this)