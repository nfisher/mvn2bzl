package mvntobzl.bazel

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import java.io.Writer
import java.util.concurrent.ConcurrentHashMap

fun renderBuild(cfg: Configuration, input: Build, out: Writer) {
    val tpl = cfg.getTemplate("BUILD.bazel.ftlh")
    val root = ConcurrentHashMap<String, Any>()
    root["build"] = input as Any
    tpl.process(root, out)
}

fun renderMavenDependencies(cfg: Configuration, input: MavenDependencies, out: Writer) {
    val tpl = cfg.getTemplate("maven.bzl.ftlh")
    val root = ConcurrentHashMap<String, Any>()
    root["maven"] = input as Any
    tpl.process(root, out)
}

fun renderRepositories(cfg: Configuration, repositories: List<HttpArchive>, out: Writer) {
    val tpl = cfg.getTemplate("repositories.bzl.ftlh")
    val root = ConcurrentHashMap<String, Any>()
    root["archives"] = repositories as Any
    tpl.process(root, out)
}

fun renderWorkspace(cfg: Configuration, workspace: Workspace, out: Writer) {
    val tpl = cfg.getTemplate("WORKSPACE.ftlh")
    val root = ConcurrentHashMap<String, Any>()
    root["workspace"] = workspace as Any
    tpl.process(root, out)
}

fun newCfg(): Configuration {
    val cfg = Configuration(Configuration.VERSION_2_3_28)
    cfg.defaultEncoding = "UTF-8"
    cfg.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    cfg.logTemplateExceptions = false
    cfg.wrapUncheckedExceptions = true
    cfg.setClassForTemplateLoading(cfg::class.java, "/resources/templates/")

    return cfg
}

data class Build(val name: String, val libDeps: List<WorkspaceDependency>, val testDeps: List<WorkspaceDependency>) {
    fun getHasTestDeps(): Boolean {
        return testDeps.isNotEmpty()
    }
}
data class HttpArchive(val name: String, val prefix: String = "", val sha256: String, val urls: List<String>)
data class MavenArtifact(val groupId: String, val artifactId: String, val version: String)
data class MavenDependencies(val repositories: List<MavenRepo>, val artifacts: List<MavenArtifact>)
data class MavenRepo(val url: String)
data class Workspace(val name: String, val archives: List<HttpArchive> = emptyList(), val repositories: List<MavenRepo> = emptyList())
data class WorkspaceDependency(val workspace: String, val target: String)

fun defaultRepos(): List<HttpArchive> {
    return listOf(
            HttpArchive(
                    name = "com_google_protobuf",
                    prefix = "protobuf-3.8.0",
                    sha256 = "03d2e5ef101aee4c2f6ddcf145d2a04926b9c19e7086944df3842b1b8502b783",
                    urls = listOf(
                            "https://github.com/protocolbuffers/protobuf/archive/v3.8.0.tar.gz"
                    )
            ),
            HttpArchive(
                    name = "com_google_protobuf_java",
                    prefix = "protobuf-3.8.0",
                    sha256 = "03d2e5ef101aee4c2f6ddcf145d2a04926b9c19e7086944df3842b1b8502b783",
                    urls = listOf(
                            "https://github.com/protocolbuffers/protobuf/archive/v3.8.0.tar.gz"
                    )
            ),
            HttpArchive(
                    name = "bazel_skylib",
                    sha256 = "2ef429f5d7ce7111263289644d233707dba35e39696377ebab8b0bc701f7818e",
                    urls = listOf(
                            "https://github.com/bazelbuild/bazel-skylib/releases/download/0.8.0/bazel-skylib.0.8.0.tar.gz"
                    )
            ),
            HttpArchive(
                    name = "rules_jvm_external",
                    sha256 = "f1203ce04e232ab6fdd81897cf0ff76f2c04c0741424d192f28e65ae752ce2d6",
                    prefix = "rules_jvm_external-2.2",
                    urls = listOf(
                            "https://github.com/bazelbuild/rules_jvm_external/archive/2.2.zip"
                    )
            )
    )
}
