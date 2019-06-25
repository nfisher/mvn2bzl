package mvntobzl

import org.apache.maven.model.Model
import org.apache.maven.model.Parent
import org.junit.Test
import kotlin.test.assertEquals

class ResolverTestCase {
    @Test
    fun `model without groupID specified should use parent groupID`() {
        val parent = Parent()
        parent.groupId = "junit"
        parent.artifactId = "bom"
        val model = Model()
        model.parent = parent
        model.artifactId = "junit"

        val actual = effectiveId(model)
        assertEquals("junit:junit", actual)
    }
}