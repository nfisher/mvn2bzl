package mvntobzl

import freemarker.template.Configuration
import mvntobzl.bazel.*
import mvntobzl.bazel.renderWorkspace
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.util.artifact.JavaScopes.*
import java.io.File
import java.io.FileWriter
import java.nio.file.Paths

fun genWorkspace(cfg: Configuration, idsToArtifacts: MutableMap<String, Artifact>, root: String, workspace: Workspace) {
    val repoTmp = Paths.get(root, "repositories.bzl.tmp")
    FileWriter(repoTmp.toString()).use {
        renderRepositories(cfg, defaultRepos(), it)
    }

    val wsTmp = Paths.get(root, "WORKSPACE.tmp").toString()
    FileWriter(wsTmp).use {
        renderWorkspace(cfg, workspace, it)
    }

    val artifacts = idsToArtifacts
            .map { (_, a) -> MavenArtifact(
                    groupId = a.groupId,
                    artifactId = a.artifactId,
                    version = a.version
            )}
            .sortedBy { it.groupId + it.artifactId }
    val depsInput = MavenDependencies(
            repositories = workspace.repositories,
            artifacts = artifacts
    )
    val depsTmp = Paths.get(root, "maven.bzl.tmp").toString()
    FileWriter(depsTmp).use {
        renderMavenDependencies(cfg, depsInput, it)
    }

    renameTo(root, "WORKSPACE.tmp", "WORKSPACE")
    renameTo(root, "repositories.bzl.tmp", "repositories.bzl")
    renameTo(root, "maven.bzl.tmp", "maven.bzl")
}

fun renameTo(path: String, src: String, dest: String) {
    val from = File(path, src)
    from.renameTo(File(path, dest))
}

val bazelPathRegex = "[-.:]".toRegex()

fun toBazelPath(s: String): String {
    return bazelPathRegex.replace(s, "_")
}

fun mapToWd(d: Dependency, idsToFilenames: Map<String, String>, workspaceRoot: String) :WorkspaceDependency {
    val depId = artifactId(d.artifact)
    val depFilename = idsToFilenames[depId]

    when {
        null != depFilename -> {
            val relPath = pomPathToWorkspace(depFilename, workspaceRoot)
            val depName = toBazelPath(d.artifact.artifactId)
            return WorkspaceDependency("", "//$relPath:$depName")
        }
        else -> return WorkspaceDependency("@", "${artifactToPath(d.artifact)}//jar")
    }
}

val libScopes = arrayOf(COMPILE, PROVIDED, RUNTIME)

fun genBuild(workspaceRoot: String, modulePath: String?, depList: MutableSet<Dependency>, artifactId: String, idsToFilenames: Map<String, String>, cfg: Configuration) {
    val buildFile = File(modulePath, "BUILD.tmp")
    val destFile = File(modulePath, "BUILD.bazel")
    val libName = toBazelPath(artifactId)
    val counts = mutableMapOf<String, Int>()
    val seen = mutableMapOf<String, Boolean>()
    val libDeps = depList
            .filter { libScopes.contains(it.scope) }
            .filter {
                var keep = true
                val key = it.artifact.groupId + it.artifact.artifactId
                if (seen[key] == true) {
                    keep = false
                }
                seen[key] = true
                keep
            }
            .map { mapToWd(it, idsToFilenames, workspaceRoot) }
            .sortedBy { it.workspace + it.target }
    val seenTest = mutableMapOf<String, Boolean>()
    val testLibs = depList
            .filterNot { libScopes.contains(it.scope) }
            .filter {
                var keep = true
                val key = it.artifact.groupId + it.artifact.artifactId
                if (seenTest[key] == true) {
                    keep = false
                }
                seenTest[key] = true
                keep
            }
            .map { mapToWd(it, idsToFilenames, workspaceRoot) }
            .sortedBy { it.workspace + it.target }

    val testDeps = testLibs + listOf(WorkspaceDependency("", ":$libName"))

    val input = Build(
            name = libName,
            libDeps = libDeps,
            testDeps = testDeps
    )

    FileWriter(buildFile).use {
        renderBuild(cfg, input, it)
    }

    counts[COMPILE] = libDeps.size
    counts[TEST] = testLibs.size

    buildFile.renameTo(destFile)

    println(libName.padEnd(40, ' ') +
            paddedPrint(counts, COMPILE) +
            paddedPrint(counts, PROVIDED) +
            paddedPrint(counts, RUNTIME) +
            paddedPrint(counts, TEST))
}

fun paddedPrint(counts: MutableMap<String, Int>, key: String): String {
    return counts.getOrDefault(key, 0).toString().padStart(10, ' ')
}