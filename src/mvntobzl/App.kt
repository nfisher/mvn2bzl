package mvntobzl

import com.google.gson.Gson
import mvntobzl.bazel.HttpArchive
import mvntobzl.bazel.MavenRepo
import mvntobzl.bazel.Workspace
import mvntobzl.bazel.newCfg
import org.apache.commons.cli.*
import org.eclipse.aether.util.artifact.JavaScopes.*
import java.io.File
import java.io.FileReader
import java.nio.file.Paths

class App {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val options = buildOpts()
            var opts: Args?

            try {
                opts = parseOptions(options, args)
            } catch(ex: ParseException) {
                val formatter = HelpFormatter()
                formatter.printHelp("mvn2bzl", options)
                println("${ex.message}")
                return
            }

            var config: Config? = null
            val gson = Gson()
            FileReader(opts.config).use {
                config = gson.fromJson(it, Config::class.java)
            }

            if (null == config) {
                println("Unable to read workspace config: ${opts.config}")
                return
            }

            val listOfPoms = pomsInWorkspace(opts.root, opts.depth)

            val modules = readAllPoms(listOfPoms)

            val repositories = config?.repositories ?: emptyList()

            val walker = resolve(modules, opts.m2home, repositories)

            val artifacts = walker.artifacts.toTypedArray()

            val cfg = newCfg()

            val mavenRepos = config
                    ?.repositories
                    ?.map { MavenRepo(url = it.url) }
                    ?: emptyList()

            genWorkspace(cfg, highlander(artifacts, walker), opts.root, Workspace(name = opts.name, repositories = mavenRepos))

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
                    genBuild(opts.root, File(pomFilename).parent, depList, artifactId.toString(), modules.idsToFilenames, cfg)
                }
            }
        }

    }
}

data class Config(val name: String, val archives: List<HttpArchive> = emptyList(), val repositories: List<Repository> = emptyList())
data class Args(val name: String, val config: String, val depth: Int, val root: String, val m2home: String)

fun buildOpts(): Options {
    val configOpt = Option.builder("c")
            .longOpt("config")
            .required(true)
            .desc("repository and archive configuration")
            .argName("config_file")
            .hasArg()
            .build()
    val depthOpt = Option.builder("d")
            .longOpt("depth")
            .required(true)
            .desc("pom file search depth")
            .argName("depth")
            .hasArg()
            .build()
    val rootOpt = Option.builder("r")
            .longOpt("root")
            .required(true)
            .desc("target workspace root")
            .argName("path")
            .hasArg()
            .build()
    val m2homeOpt = Option.builder("m")
            .longOpt("m2home")
            .desc("maven 2 repository path")
            .argName("m2_home")
            .hasArg()
            .build()
    val nameOpt = Option.builder("n")
            .longOpt("name")
            .required(true)
            .desc("bazel workspace name")
            .argName("workspace_name")
            .hasArg()
            .build()

    val options = Options()
    options.addOption(configOpt)
    options.addOption(rootOpt)
    options.addOption(depthOpt)
    options.addOption(nameOpt)
    options.addOption(m2homeOpt)

    return options
}

fun parseOptions(options: Options, args: Array<String>): Args {
    val parser = DefaultParser()
    val cmd = parser.parse(options, args)
    val config = cmd.getOptionValue("config")
    val depth = cmd.getOptionValue("depth").toInt()
    val name = cmd.getOptionValue("name")
    val root = cmd.getOptionValue("root")
    val m2home = cmd.getOptionValue("m2home") ?: Paths.get(System.getProperty("user.home"), ".m2", "repository").toString()

    return Args(
            config = config,
            depth = depth,
            m2home = m2home,
            name = name,
            root = root
    )
}
