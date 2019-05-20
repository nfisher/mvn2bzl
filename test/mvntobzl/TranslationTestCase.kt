package mvntobzl

import org.eclipse.aether.artifact.DefaultArtifact
import org.junit.Test
import kotlin.test.assertEquals

class TranslationTestCase {
    @Test
    fun `should map sub-module pom into workspace correctly`() {
        val rel = pomPathToWorkspace("/Users/nfisher/workspace/project/sub/pom.xml", "/Users/nfisher/workspace/project")
        assertEquals("sub", rel)
    }

    @Test
    fun `should map root pom into workspace correctly`() {
        val rel = pomPathToWorkspace("/Users/nfisher/workspace/project/pom.xml", "/Users/nfisher/workspace/project")
        assertEquals("", rel)
    }

    @Test
    fun `should map artifact to valid bazel path`() {
        val artifact = DefaultArtifact("junit", "junit", "jar", "4.12")
        val actual = artifactToPath(artifact)
        assertEquals("junit_junit", actual)
    }
}
