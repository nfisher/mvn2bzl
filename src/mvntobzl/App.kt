package mvntobzl

import org.eclipse.aether.util.artifact.JavaScopes.*
import java.io.File
import java.util.*

class App {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size != 2) {
                println("path and depth must be provided")
                return
            }
            val workspaceRoot = args[0]
            val depth = args[1].toInt()

            val listOfPoms = pomsInWorkspace(workspaceRoot, depth)

            val modules = readAllPoms(listOfPoms)

            val repositories = Arrays.asList(
                    Repository("private", "default", "http://localhost:8081/repository/instana-private/"),
                    Repository("central", "default", "http://localhost:8081/repository/maven-central/")
            )

            val walker = resolve(modules,"/Users/nfisher/.m2/repository", repositories)

            val artifacts = walker.artifacts.toTypedArray()

            genWorkspace(workspaceRoot, highlander(artifacts, walker))

            println("")
            println(" ".repeat(40) +
                COMPILE.padStart(10, ' ') +
                PROVIDED.padStart(10, ' ') +
                RUNTIME.padStart(10, ' ') +
                TEST.padStart(10, ' '))
            println("~".repeat(80))
            walker.moduleDeps.toSortedMap().forEach { (id, depList) ->
                val pomFilename = modules.idsToFilenames[id]
                val artifactId = modules.idsToPoms[id]?.artifactId
                if (!artifactId.isNullOrEmpty() && depList.size > 1) {
                    genBuild(workspaceRoot, File(pomFilename).parent, depList, artifactId.toString(), modules.idsToFilenames)
                }
            }
        }
    }
}
