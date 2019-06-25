package mvntobzl.io

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.FileSystemException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.stream.Collectors
import kotlin.streams.asSequence
import kotlin.test.assertEquals

class WorkspaceTestCase {
    val SIMPLE_PROJECT = listOf(
            "pom.xml",
            "metrics/pom.xml",
            "metrics/src/main/java/com/instana/metrics/Formatter.java",
            "metrics/src/main/java/com/instana/metrics/catalog/MetricDescription.java"
    ).sorted()
    val WITH_EXCLUDED_FILES = SIMPLE_PROJECT + listOf(
            ".git/config",
            "target/Formatter.class",
            "bazel-out/Formatter.class"
    ).sorted()

    var workspacePath: Path? = null

    // timeout of 1s in case the queue put blocks indefinitely in walkWorkspace.
    @Test(timeout = 1000L)
    fun `ignore dotfiles while traversing`() {
        val q = genWorkspace(workspacePath!!, WITH_EXCLUDED_FILES)

        walkWorkspace(workspacePath!!, q, IGNORE_FILE_PREFIXES)

        val result = q.stream().sorted().collect(Collectors.toList())
        assertEquals(SIMPLE_PROJECT, result)
    }

    @Before
    fun setUp() {
        workspacePath = Files.createTempDirectory("junit_")
    }

    @After
    fun tearDown() {
        if (null == workspacePath) {
            val path: Path = workspacePath!!
            Files.walk(path)
                    .asSequence()
                    .sortedDescending()
                    .map(Path::toFile)
                    .forEach { it.delete() }
        }
    }
}

internal fun genWorkspace(workspacePath: Path, filenames: List<String>): BlockingQueue<String> {
    for (filename in filenames) {
        val f = File(workspacePath.toFile(), filename)
        val dir = f.parent

        if (null != dir) {
            File(dir).mkdirs()
        }

        if (!f.createNewFile()) {
            throw FileSystemException("unable to create file $filename")
        }
    }
    return ArrayBlockingQueue(filenames.size + 1)
}
