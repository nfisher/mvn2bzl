package mvntobzl.graph

import com.google.common.graph.MutableValueGraph
import com.google.common.graph.ValueGraphBuilder

data class ClassNode(val name: String, val isPublic: Boolean) : ProjectNode(type = "class")
data class FileNode(val path: String, val mediaType: String) : ProjectNode(type = "file")
data class ModuleNode(val path: String, val groupId: String, val artifactId: String, val version: String) : ProjectNode(type = "module")
data class PackageNode(val name: String) : ProjectNode(type = "package")

data class RelationshipEdge(val rel: String)

typealias WorkspaceGraph = MutableValueGraph<ProjectNode, RelationshipEdge>

fun newWorkspace(): WorkspaceGraph {
    return ValueGraphBuilder
            .directed()
            .allowsSelfLoops(false)
            .build()
}

fun add(g: WorkspaceGraph, f: FileNode, c: ClassNode) {
    g.putEdgeValue(f, c, HAS_A)
    g.putEdgeValue(c, f, BELONGS_TO)
}

fun add(g: WorkspaceGraph, f: FileNode, m: ModuleNode) {
    g.putEdgeValue(m, f, HAS_A)
    g.putEdgeValue(f, m, BELONGS_TO)
}

fun add(g: WorkspaceGraph, p: PackageNode, m: ModuleNode) {
    g.putEdgeValue(m, p, HAS_A)
    g.putEdgeValue(p, m, BELONGS_TO)
}

fun add(g: WorkspaceGraph, c: ClassNode, p: PackageNode) {
    g.putEdgeValue(p, c, HAS_A)
    g.putEdgeValue(c, p, BELONGS_TO)
}

fun add(g: WorkspaceGraph, f: FileNode, p: PackageNode) {
    g.putEdgeValue(p, f, HAS_A)
    g.putEdgeValue(f, p, BELONGS_TO)
}

fun add(g: WorkspaceGraph, c: ClassNode, f: FileNode) {
    g.putEdgeValue(f, c, HAS_A)
    g.putEdgeValue(c, f, BELONGS_TO)
}

abstract class ProjectNode(protected val type: String)

val HAS_A = RelationshipEdge(rel = "hasA")
val BELONGS_TO = RelationshipEdge(rel = "belongsTo")