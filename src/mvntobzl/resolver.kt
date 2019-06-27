package mvntobzl

import org.apache.maven.model.Model
import org.apache.maven.model.io.DefaultModelReader
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.DefaultRepositorySystemSession
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.repository.RepositoryPolicy.UPDATE_POLICY_NEVER
import org.eclipse.aether.resolution.ArtifactDescriptorRequest
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import java.io.File
import java.io.FileReader
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

fun newRepoSystem(): RepositorySystem {
    val locator = MavenRepositorySystemUtils.newServiceLocator()
    locator.addService(RepositoryConnectorFactory::class.java, BasicRepositoryConnectorFactory::class.java)
    locator.addService(TransporterFactory::class.java, FileTransporterFactory::class.java)
    locator.addService(TransporterFactory::class.java, HttpTransporterFactory::class.java)

    return locator.getService(RepositorySystem::class.java)
}


fun newSession(repoSystem: RepositorySystem, repoBase: String): DefaultRepositorySystemSession {
    val localRepo = LocalRepository(repoBase)
    val session = MavenRepositorySystemUtils.newSession()
    session.localRepositoryManager = repoSystem.newLocalRepositoryManager(session, localRepo)
    session.updatePolicy = UPDATE_POLICY_NEVER
    return session
}

fun effectiveId(model: Model): String {
    if (null == model.groupId) {
        return "${model.parent.groupId}:${model.artifactId}"
    }

    return "${model.groupId}:${model.artifactId}"
}

fun pomsInWorkspace(workspace: String, searchDepth: Int): MutableList<String> {
    // TODO: if it's actually multi-module use root pom to identify sub-modules rather than scanning.
    val results: MutableList<String> = mutableListOf()
    val queue: Queue<PathNode> = ConcurrentLinkedQueue<PathNode>()

    queue.add(PathNode(workspace, 0))

    var pathNode: PathNode = queue.remove()
    while (true) {
        val path = pathNode.path
        val nodeDepth = pathNode.depth
        val dir = File(path)
        val pom = File(dir, "pom.xml")

        if (pom.exists()) {
            // println("found $pom at $nodeDepth level")
            results.add(pom.absolutePath)
        }

        val nextDepth = nodeDepth + 1
        if (nextDepth > searchDepth) {
            break
        }

        for (file in dir.listFiles()) {
            if (file.isDirectory) {
                queue.add(PathNode(file.absolutePath, nextDepth))
            }
        }
        pathNode = queue.remove()
    }

    results.sort()
    return results
}

fun readAllPoms(listOfPoms: List<String>): Modules {
    val reader = DefaultModelReader()
    val idsToPoms = mutableMapOf<String, Model>()
    val idsToFilenames = mutableMapOf<String, String>()
    listOfPoms.forEach { filename ->
        val file = FileReader(filename)
        file.use {
            val model = reader.read(file, null)
            val id = effectiveId(model)
            idsToPoms[id] = model
            idsToFilenames[id] = filename
        }
    }
    return Modules(idsToPoms.toMap(), idsToFilenames.toMap())
}

data class Modules(val idsToPoms: Map<String, Model>, val idsToFilenames: Map<String, String>)

data class PathNode(val path: String, val depth: Int)

// highlander because there can only be one (dependency for the same groupId+artifactId tuple).
// This function takes the version with the most references.
fun highlander(artifacts: Array<Artifact>, walker: GraphWalker): MutableMap<String, Artifact> {
    val m = mutableMapOf<String, Artifact>()
    val a = artifacts.sortedBy { artifactId(it) }
    a.forEach {
        run {
            if (null == walker.moduleDeps[artifactId(it)]) {
                val itUsedBy = walker.moduleRefsByDep[it]
                val key = artifactId(it)
                val v = m[key] ?: it
                val curUsedBy = walker.moduleRefsByDep[v]
                if (itUsedBy?.size ?: 0 >= curUsedBy?.size ?: 0) {
                    m[key] = it
                }
            }
        }
    }
    return m
}

fun artifactId(it: Artifact) = "${it.groupId}:${it.artifactId}"

data class Repository(val id: String, val type: String, val url: String)

fun resolve(modules: Modules, repoBase: String, repos: List<Repository>): GraphWalker {
    val walker = GraphWalker()
    val repoSystem = newRepoSystem()
    val session = newSession(repoSystem, repoBase)
    val repositories = mutableListOf<RemoteRepository>()
    for (r in repos) {
        repositories.add(RemoteRepository.Builder(r.id, r.type, r.url).build())
    }

    print("resolving ${modules.idsToPoms.size} modules ")
    val queue = LinkedBlockingQueue<String>()
    val threads = mutableListOf<Thread>()

    for (i in 1..1) {
        val t = thread(start = true) {
            while (true) {
                val id = queue.take()
                if (id.isNullOrBlank()) {
                    break
                }

                val artifact = DefaultArtifact("$id:1.0.0-SNAPSHOT")

                val descriptorRequest = ArtifactDescriptorRequest()
                descriptorRequest.artifact = artifact
                descriptorRequest.repositories = repositories

                val descriptorResult = repoSystem.readArtifactDescriptor(session, descriptorRequest)

                val collectRequest = CollectRequest()

                collectRequest.rootArtifact = descriptorResult.artifact
                collectRequest.dependencies = descriptorResult.dependencies
                collectRequest.managedDependencies = descriptorResult.managedDependencies
                collectRequest.repositories = repositories

                val collectResult = repoSystem.collectDependencies(session, collectRequest)
                walker.currentArtifact = id
                collectResult.root.accept(walker)

                print(".")
            }
            queue.put("") // ensure other threads receive signal
        }
        threads.add(t)
    }

    modules.idsToPoms.forEach { (id, _) -> queue.put(id) }
    queue.put("") // signal end of queue

    for (t in threads) {
        t.join()
    }

    println("\u001B[32m DONE\u001B[0m")
    return walker
}