package mvntobzl.bazel

import org.junit.Test
import java.io.StringWriter
import kotlin.test.assertEquals

class ViewTestCase {

    @Test
    fun `should render empty repositories bzl`() {
        val out = StringWriter()
        val cfg = newCfg()
        val input: List<HttpArchive> = listOf()

        renderRepositories(cfg, input, out)
        assertEquals(REPO_EMPTY, out.toString())
    }

    @Test
    fun `should render repositories bzl`() {
        val out = StringWriter()
        val cfg = newCfg()
        val input: List<HttpArchive> = defaultRepos()

        renderRepositories(cfg, input, out)
        assertEquals(REPO_LIST, out.toString())
    }

    @Test
    fun `should render WORKSPACE`() {
        val out = StringWriter()
        val cfg = newCfg()
        val input = Workspace(name = "mvn2bzl")

        renderWorkspace(cfg, input, out)
        assertEquals(WORKSPACE, out.toString())
    }

    @Test
    fun `should render empty maven bzl`() {
        val out = StringWriter()
        val cfg = newCfg()
        val input = MavenDependencies(repositories = emptyList(), artifacts = emptyList())

        renderMavenDependencies(cfg, input, out)
        assertEquals(EMPTY_MAVEN, out.toString())
    }

    @Test
    fun `should render junit maven bzl`() {
        val out = StringWriter()
        val cfg = newCfg()
        val input = MavenDependencies(
                repositories = listOf(MavenRepo(url = "http://localhost:8081/repository/maven-central/")),
                artifacts = listOf(MavenArtifact(groupId = "junit", artifactId = "junit", version = "4.12") )
        )

        renderMavenDependencies(cfg, input, out)
        assertEquals(JUNIT_MAVEN, out.toString())
    }

    @Test
    fun `should render BUILD with no deps`() {
        val out = StringWriter()
        val cfg = newCfg()
        val input = Build(
                name = "metrics",
                libDeps = emptyList(),
                testDeps = emptyList()
        )

        renderBuild(cfg, input, out)
        assertEquals(LIB_BUILD_NO_DEPS, out.toString())
    }

    @Test
    fun `should render BUILD with deps`() {
        val out = StringWriter()
        val cfg = newCfg()
        val input = Build(
                name = "metrics",
                libDeps = listOf(
                        WorkspaceDependency("@maven", "//:com_google_guava_guava")
                ),
                testDeps = emptyList()
        )

        renderBuild(cfg, input, out)
        assertEquals(LIB_BUILD_WITH_DEP, out.toString())
    }

    @Test
    fun `should render BUILD with testing deps`() {
        val out = StringWriter()
        val cfg = newCfg()
        val input = Build(
                name = "metrics",
                libDeps = listOf(
                        WorkspaceDependency("@maven", "//:com_google_guava_guava")
                ),
                testDeps = listOf(
                        WorkspaceDependency("@maven", "//:junit_junit")

                )
        )

        renderBuild(cfg, input, out)
        assertEquals(LIB_BUILD_WITH_TESTING_DEPS, out.toString())
    }
}

const val REPO_EMPTY = """load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

def repositories():
    print("no project repositories specified!")
"""

const val REPO_LIST = """load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

def repositories():
    http_archive(
        name = "com_google_protobuf",
        strip_prefix = "protobuf-3.8.0",
        sha256 = "03d2e5ef101aee4c2f6ddcf145d2a04926b9c19e7086944df3842b1b8502b783",
        urls = [
            "https://github.com/protocolbuffers/protobuf/archive/v3.8.0.tar.gz",
        ],
    )

    http_archive(
        name = "com_google_protobuf_java",
        strip_prefix = "protobuf-3.8.0",
        sha256 = "03d2e5ef101aee4c2f6ddcf145d2a04926b9c19e7086944df3842b1b8502b783",
        urls = [
            "https://github.com/protocolbuffers/protobuf/archive/v3.8.0.tar.gz",
        ],
    )

    http_archive(
        name = "bazel_skylib",
        sha256 = "2ef429f5d7ce7111263289644d233707dba35e39696377ebab8b0bc701f7818e",
        urls = [
            "https://github.com/bazelbuild/bazel-skylib/releases/download/0.8.0/bazel-skylib.0.8.0.tar.gz",
        ],
    )

    http_archive(
        name = "rules_jvm_external",
        strip_prefix = "rules_jvm_external-2.2",
        sha256 = "f1203ce04e232ab6fdd81897cf0ff76f2c04c0741424d192f28e65ae752ce2d6",
        urls = [
            "https://github.com/bazelbuild/rules_jvm_external/archive/2.2.zip",
        ],
    )

"""

const val WORKSPACE = """workspace(name = "mvn2bzl")

load("//:repositories.bzl", "repositories")
repositories()

load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")
protobuf_deps()

load("//:maven.bzl", "maven_deps")
maven_deps()
"""

const val EMPTY_MAVEN = """load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven")

def maven_deps():
    maven_install(
        artifacts = [
        ],
        fail_on_missing_checksum = True,
        fetch_sources = True,
        repositories = [
        ],
    )
"""

const val JUNIT_MAVEN = """load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven")

def maven_deps():
    maven_install(
        artifacts = [
            maven.artifact("junit", "junit", "4.12"),
        ],
        fail_on_missing_checksum = True,
        fetch_sources = True,
        repositories = [
            maven.repository("http://localhost:8081/repository/maven-central/"),
        ],
    )
"""

const val LIB_BUILD_NO_DEPS = """java_library(
    name = "metrics",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
    ],
)
"""

const val LIB_BUILD_WITH_DEP = """java_library(
    name = "metrics",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_google_guava_guava",
    ],
)
"""

const val LIB_BUILD_WITH_TESTING_DEPS = """java_library(
    name = "metrics",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_google_guava_guava",
    ],
)

java_library(
    name = "testing",
    exports = [
        "@maven//:junit_junit",
    ],
)
"""