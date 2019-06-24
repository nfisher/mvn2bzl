package mvntobzl

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.FileVisitResult.SKIP_SUBTREE
import java.nio.file.Files.walkFileTree
import java.nio.file.attribute.BasicFileAttributes
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
    val WITH_DOT_FILES = SIMPLE_PROJECT + listOf(
            ".git/config",
            "target/Formatter.class",
            "bazel-out/Formatter.class"
            )

    var workspacePath: Path? = null

    @Test
    fun `ignore dotfiles while traversing`() {
        val q:BlockingQueue<String> = ArrayBlockingQueue(1000)
        genWorkspace(workspacePath!!, WITH_DOT_FILES)

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
            val path:Path = workspacePath!!
            Files.walk(path)
                    .asSequence()
                    .sortedDescending()
                    .map(Path::toFile)
                    .forEach { it.delete() }
        }
    }

}

val IGNORE_FILE_PREFIXES = listOf(
        "target",
        "bazel-",
        "."
)

fun walkWorkspace(start: Path, q: BlockingQueue<String>, ignoreFilePrefixes: List<String>) {
    walkFileTree(start, WalkerTexasRanger(start, q, ignoreFilePrefixes))
}

class WalkerTexasRanger(private val start: Path, private val q: BlockingQueue<String>, val ignorePrefixes: List<String>) : FileVisitor<Path> {

    override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
        q.put(file.toString().substring(start.toString().length + 1))
        return CONTINUE
    }

    override fun visitFileFailed(file: Path, exc: IOException?): FileVisitResult {
        if (null != exc) {
           throw exc
        }
        return CONTINUE
    }

    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes?): FileVisitResult {
        val filename = dir.fileName
        for (ignorePrefix in ignorePrefixes) {
            if (filename.toString().startsWith(ignorePrefix)) {
                println("dotfile")
                return SKIP_SUBTREE
            }
        }

        return CONTINUE
    }

    override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
        if (null != exc) {
            throw exc
        }
        return CONTINUE
    }
}

internal fun genWorkspace(workspacePath:Path, filenames:List<String>) {
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
}
