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
        root.insert(listOf("com", "instana", "metrics"), 10)
        assertTrue(root.insert(listOf("com", "instana"), 5), "parent insertion")
        assertEquals(5, root.find(listOf("com", "instana")), "parent value")
        assertEquals(10, root.find(listOf("com", "instana", "metrics")), "child value")
    }

    @Test
    fun `duplicate insert`() {
        val root = intRoot()
        root.insert(listOf("com", "instana", "metrics"), 10)
        assertFalse(root.insert(listOf("com", "instana", "metrics"), 9), "second insert should fail")
        assertEquals(1, root.size())
        assertEquals(10, root.find(listOf("com", "instana", "metrics")), "key value")
    }

    @Test
    fun `find match`() {
        val root = intRoot()
        assertTrue(root.insert(listOf("com", "instana", "metrics"), 10))
        assertEquals(10, root.find(listOf("com", "instana", "metrics")))
    }

    @Test
    fun `find fail`() {
        val root = intRoot()
        assertNull(root.find(listOf("com", "instana", "metrics")))
    }

    @Test
    fun `longest match against parent`() {
        val root = intRoot()
        root.insert(listOf("com", "instana"), 9)
        assertEquals(9, root.longestMatch(listOf("com", "instana", "metrics")))
    }

    @Test
    fun `module paths`() {
        // module paths should exclude the pom.xml
        val root = intRoot()

        root.insert(listOf("eum"), 1)
        root.insert(listOf("eum", "eum-processor"), 2)
        root.insert(listOf("eum", "eum-acceptor"), 3)
        root.insert(listOf("eum", "js-stack-trace-translator"), 4)

        assertEquals(2, root.longestMatch(toList("eum/eum-processor/src/main/java/com/instana/eum/config/EumProcessorConfig.java")))
        assertEquals(4, root.longestMatch(toList("eum/js-stack-trace-translator/src/main/java/com/instana/eum/config/StackTraceConfig.java")))
    }

    @Test
    fun `longest match exact`() {
        val root = intRoot()
        root.insert(listOf("com", "instana"), 9)
        assertEquals(9, root.longestMatch(listOf("com", "instana")))
    }

    @Test
    fun `no match`() {
        val root = intRoot()
        root.insert(listOf("org", "apache"), 9)
        assertNull(root.longestMatch(listOf("com", "instana", "metrics")))
    }

    @Test
    fun `longest typed match`() {
        val root = Trie<Number>()
        root.insert(listOf("eum"), 1.0)
        root.insert(listOf("eum", "pom.xml"), 2)

        assertEquals(1.0, root.longestTypedMatch(toList("eum/eum-processor/src/main/java/com/instana/eum/config/EumProcessorConfig.java"), Double::class))
        assertEquals(null, root.longestTypedMatch(toList("eum/eum-processor/src/main/java/com/instana/eum/config/EumProcessorConfig.java"), Int::class))
        assertEquals(2, root.longestTypedMatch(toList("eum/pom.xml"), Int::class))
    }
}

fun toList(s: String): List<String> {
    return s.split("/")
}

fun intRoot(): Trie<Int> {
    return Trie()
}
