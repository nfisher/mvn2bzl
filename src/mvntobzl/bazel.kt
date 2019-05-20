package mvntobzl

import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.util.artifact.JavaScopes.*
import java.io.File
import java.nio.file.Paths

fun genWorkspace(workspace: String, idsToArtifacts: MutableMap<String, Artifact>) {
    val ws = File(Paths.get(workspace, "WORKSPACE.tmp").toString())
    ws.printWriter().use {
        it.print("""

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# proto_library, cc_proto_library, and java_proto_library rules implicitly
# depend on @com_google_protobuf for protoc and proto runtimes.
# This statement defines the @com_google_protobuf repo.
http_archive(
    name = "com_google_protobuf",
    strip_prefix = "protobuf-3.8.0",
    urls = ["https://github.com/protocolbuffers/protobuf/archive/v3.8.0.tar.gz"],
    sha256 = "03d2e5ef101aee4c2f6ddcf145d2a04926b9c19e7086944df3842b1b8502b783",
    )

http_archive(
    name = "com_google_protobuf_java",
    strip_prefix = "protobuf-3.8.0",
    urls = ["https://github.com/protocolbuffers/protobuf/archive/v3.8.0.tar.gz"],
    sha256 = "03d2e5ef101aee4c2f6ddcf145d2a04926b9c19e7086944df3842b1b8502b783",
    )

load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")

protobuf_deps()

# skylib required by protobuf
http_archive(
    name = "bazel_skylib",
    urls = ["https://github.com/bazelbuild/bazel-skylib/releases/download/0.8.0/bazel-skylib.0.8.0.tar.gz"],
    sha256 = "2ef429f5d7ce7111263289644d233707dba35e39696377ebab8b0bc701f7818e",
    )

# download rules_jvm_external
RULES_JVM_EXTERNAL_TAG = "2.1"
RULES_JVM_EXTERNAL_SHA = "515ee5265387b88e4547b34a57393d2bcb1101314bcc5360ec7a482792556f42"
http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
    )
load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven")

maven_install(
    fetch_sources = False,
    fail_on_missing_checksum = False,
    repositories = [
        maven.repository("http://localhost:8081/repository/instana-private/"),
        maven.repository("http://localhost:8081/repository/maven-central/"),
        ],
    artifacts = [

            """.trimIndent())

        idsToArtifacts
                .forEach { (_, v) ->
                    it.println("        maven.artifact(\"${v.groupId}\", \"${v.artifactId}\", \"${v.version}\"),")
                    if (v.version == "RELEASE") {
                        println("WARNING: ${v.groupId}:${v.artifactId} is using a RELEASE, this will need to be changed to the artifacts latest version")
                    }
                }

        it.print("""

  ],
)

            """.trimIndent())
    }
    ws.renameTo(File(Paths.get(workspace, "WORKSPACE").toString()))
}

val bazelPathRegex = "[-.:]".toRegex()

fun toBazelPath(s: String): String {
    return bazelPathRegex.replace(s, "_")
}

fun genBuild(workspaceRoot: String, modulePath: String?, depList: MutableSet<Dependency>, artifactId: String, idsToFilenames: Map<String, String>) {
    val buildFile = File(modulePath, "BUILD.tmp")
    val destFile = File(modulePath, "BUILD.bazel")
    val libName = toBazelPath(artifactId)
    val counts = mutableMapOf<String, Int>()

    buildFile.printWriter().use { pw ->
        pw.println("""
            java_library(
                srcs = glob(["src/main/java/**/*.java"]),
                visibility = ["//visibility:public"],
                deps = [
        """.trimIndent())
        depList.sortedBy { dependency -> dependency.toString() }.forEach {
            val current = counts.getOrDefault(it.scope, 0)
            counts[it.scope] = current + 1

            if (it.scope == COMPILE || it.scope == PROVIDED || it.scope == RUNTIME) {
                val depId = artifactId(it.artifact)
                val depFilename = idsToFilenames[depId]
                if (depFilename != null) {
                    val relPath = pomPathToWorkspace(depFilename, workspaceRoot)
                    val depName = toBazelPath(it.artifact.artifactId)
                    pw.println("    \"//$relPath:$depName\",")
                } else {
                    pw.println("    \"@maven//:${artifactToPath(it.artifact)}\",")
                }
            }
        }
        pw.println("""
                ],
                name = "$libName",
            )
        """.trimIndent())
    }

    buildFile.renameTo(destFile)

    println(libName.padEnd(40, ' ') +
            paddedPrint(counts, COMPILE) +
            paddedPrint(counts, PROVIDED) +
            paddedPrint(counts, RUNTIME) +
            paddedPrint(counts, TEST))
}

fun paddedPrint(counts:MutableMap<String, Int>, key:String):String {
   return counts.getOrDefault(key, 0).toString().padStart(10, ' ')
}