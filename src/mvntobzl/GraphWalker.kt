package mvntobzl

import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.graph.DependencyNode
import org.eclipse.aether.graph.DependencyVisitor

class GraphWalker : DependencyVisitor {
    val artifacts = mutableSetOf<Artifact>()
    val moduleRefsByDep = mutableMapOf<Artifact, MutableSet<String>>()
    val moduleDeps = mutableMapOf<String, MutableSet<Dependency>>()
    var currentArtifact: String = ""

    override fun visitLeave(node: DependencyNode?): Boolean {
        return true
    }

    override fun visitEnter(node: DependencyNode?): Boolean {
        if (null == node) {
            return true
        }

        val dep = node.dependency
        val artifact = node.artifact

        val l = moduleRefsByDep[artifact] ?: mutableSetOf()
        l.add(currentArtifact)
        moduleRefsByDep[artifact] = l
        artifacts.add(artifact)

        // the workspaces sub-modules have null dependencies
        if (null != dep) {
            val deps = moduleDeps[currentArtifact] ?: mutableSetOf()
            deps.add(dep)
            moduleDeps[currentArtifact] = deps
        }

        return true
    }
}