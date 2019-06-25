package mvntobzl.graph

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TrieTestCase {

    @Test
    fun `parent insert after child`() {
        val root = intRoot()
        root.insert(arrayOf("com", "instana", "metrics"), 10)
        assertTrue(root.insert(arrayOf("com", "instana"), 5), "parent insertion")
        assertEquals(5, root.find(arrayOf("com", "instana")), "parent value")
        assertEquals(10, root.find(arrayOf("com", "instana", "metrics")), "child value")
    }

    @Test
    fun `duplicate insert`() {
        val root = intRoot()
        root.insert(arrayOf("com", "instana", "metrics"), 10)
        assertFalse(root.insert(arrayOf("com", "instana", "metrics"), 9), "second insert should fail")
        assertEquals(10, root.find(arrayOf("com", "instana", "metrics")), "key value")
    }

    @Test
    fun `find match`() {
        val root = intRoot()
        assertTrue(root.insert(arrayOf("com", "instana", "metrics"), 10))
        assertEquals(10, root.find(arrayOf("com", "instana", "metrics")))
    }

    @Test
    fun `find fail`() {
        val root = intRoot()
        assertNull(root.find(arrayOf("com", "instana", "metrics")))
    }

    @Test
    fun `longest match against parent`() {
        val root = intRoot()
        root.insert(arrayOf("com", "instana"), 9)
        assertEquals(9, root.longestMatch(arrayOf("com", "instana", "metrics")))
    }

    @Test
    fun `longest match exact`() {
        val root = intRoot()
        root.insert(arrayOf("com", "instana"), 9)
        assertEquals(9, root.longestMatch(arrayOf("com", "instana")))
    }

    @Test
    fun `no match`() {
        val root = intRoot()
        root.insert(arrayOf("org", "apache"), 9)
        assertNull(root.longestMatch(arrayOf("com", "instana", "metrics")))
    }
}

fun intRoot(): Trie<Int> {
    return Trie()
}
