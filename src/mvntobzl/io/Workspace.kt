package mvntobzl.io

import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.FileVisitResult.SKIP_SUBTREE
import java.nio.file.FileVisitor
import java.nio.file.Files.walkFileTree
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.BlockingQueue

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

val IGNORE_FILE_PREFIXES = listOf(
        "target",
        "bazel-",
        "."
)
