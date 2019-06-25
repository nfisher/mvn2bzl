package mvntobzl.graph

import kotlin.reflect.KClass

class Trie<T>(var value: T? = null, private val children: MutableMap<String, Trie<T>> = mutableMapOf()) {
    // insert adds the provided value into the trie with keys. If it already exists it will return false.
    fun insert(keys: List<String>, value: T): Boolean {
        var parent: Trie<T> = this
        for (k in keys) {
            if (null == parent.children[k]) {
                parent.children[k] = Trie()
            }

            parent = parent.children[k]!!
        }

        if (null != parent.value) {
            return false
        }

        parent.value = value
        return true
    }

    fun find(keys: List<String>): T? {
        var parent: Trie<T> = this

        for (k in keys) {
            if (null == parent.children[k]) {
                return null
            }
            parent = parent.children[k]!!
        }

        return parent.value
    }

    // longestMatch will find the longest matching non-null value or null if none.
    fun longestMatch(keys: List<String>): T? {
        var parent: Trie<T> = this
        var value: T? = parent.value

        for (k in keys) {
            if (null == parent.children[k]) {
                break
            }

            parent = parent.children[k]!!
            if (null != parent.value) {
                value = parent.value
            }
        }

        return value
    }

    fun longestTypedMatch(keys: List<String>, clazz: KClass<*>): T? {
        var parent: Trie<T> = this
        var value: T? = null

        for (k in keys) {
            if (null == parent.children[k]) {
                break
            }

            parent = parent.children[k]!!
            if (null != parent.value && clazz.isInstance(parent.value)) {
                value = parent.value
            }
        }

        return value
    }

    fun size(): Int {
        return children.size
    }
}
