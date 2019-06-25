package mvntobzl.graph

class Trie<T>(var value: T? = null, private val children: MutableMap<String, Trie<T>> = mutableMapOf()) {
    // insert adds the provided value into the trie with keys. If it already exists it will return false.
    fun insert(keys: Array<String>, value: T): Boolean {
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

    fun find(keys: Array<String>): T? {
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
    fun longestMatch(keys: Array<String>): T? {
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
}
