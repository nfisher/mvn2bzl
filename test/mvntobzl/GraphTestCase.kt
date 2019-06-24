package mvntobzl

import com.google.common.graph.*
import org.junit.Test
import kotlin.test.assertEquals

const val BELONGS_TO_MODULE: String = "belongsToModule"
const val BELONGS_TO_FILE: String = "belongsToFile"
const val BELONGS_TO_PACKAGE: String = "belongsToPackage"
const val HAS_FILE: String = "hasFile"
const val HAS_PACKAGE: String = "hasPackage"
const val HAS_CLASS: String = "hasClass"
const val HAS_DEPENDENCY: String = "hasDependency"
const val BELONGS_TO_DEPENDENT: String = "belongsToDependent"

class GraphTestCase {
    @Test
    fun `neo4j graph the things`() {
        
    }

    @Test
    fun `should build dep graph`() {
        val graph: MutableValueGraph<ProjectNode, String> = ValueGraphBuilder
                .directed()
                .allowsSelfLoops(false)
                .build()

        val metrics = ModuleNode(path = "metrics", groupId = "com.instana", artifactId = "metrics", version = "1.0.0-SNAPSHOT")
        val metricsPom = FileNode(path = "metrics/pom.xml", ext = "xml")
        val metricsPackage = PackageNode(name = "com.instana.metrics")

        val metricsFormatter = FileNode(path = "metrics/src/main/java/com/instana/metrics/Formatter.java", ext = "java")
        val metricsFormatterClass = ClassNode(name = "com.instana.metrics.Formatter", isPublic = true)

        val catalogPackage = PackageNode(name = "com.instana.metrics.catalog")
        val metricDescription = FileNode(path = "metrics/src/main/java/com/instana/metrics/catalog/MetricDescription.java", ext = "java")
        val metricDescriptionClass = ClassNode(name = "com.instana.metrics.catalog.MetricDescription", isPublic = true)

        addFileToModule(graph, metricsPom, metrics)

        addPackageToModule(graph, metricsPackage, metrics)
        addFileToPackage(graph, metricsFormatter, metricsPackage)
        addClassToPackage(graph, metricsFormatterClass, metricsPackage)
        addClassToFile(graph, metricsFormatterClass, metricsFormatter)

        addPackageToModule(graph, catalogPackage, metrics)

        addFileToPackage(graph, metricDescription, catalogPackage)
        addClassToPackage(graph, metricDescriptionClass, catalogPackage)
        addClassToFile(graph, metricDescriptionClass, metricDescription)

        addClassImport(graph, metricDescription, metricsFormatterClass)

        assertEquals(6, graph.degree(metrics), "modules degree")
        assertEquals(2, graph.degree(metricsPom), "pom degree")

        assertEquals(6, graph.degree(metricsPackage), "metrics package degree")
        assertEquals(4, graph.degree(metricsFormatter), "formatter file degree")
        assertEquals(6, graph.degree(metricsFormatterClass), "formatter class degree")

        assertEquals(6, graph.degree(catalogPackage), "catalog package degree")
        assertEquals(6, graph.degree(metricDescription), "description file degree")
        assertEquals(4, graph.degree(metricDescriptionClass), "description class degree")
    }

}

/*
 (m:Module {name: "metrics"}) -[:HAS_PACKAGE]-> () -[:HAS_CLASS]-> () -[:HAS_IMPORT]-> () -[:BELONGS_TO_PACKAGE]-> (dep)
 RETURN m, dep
*/

fun addClassImport(g: MutableValueGraph<ProjectNode, String>, f: FileNode, c: ClassNode) {
    g.putEdgeValue(f, c, HAS_DEPENDENCY)
    g.putEdgeValue(c, f, BELONGS_TO_DEPENDENT)
}

fun addFileToModule(g: MutableValueGraph<ProjectNode, String>, f: FileNode, m: ModuleNode) {
    g.putEdgeValue(m, f, HAS_FILE)
    g.putEdgeValue(f, m, BELONGS_TO_MODULE)
}

fun addPackageToModule(g: MutableValueGraph<ProjectNode, String>, p: PackageNode, m: ModuleNode) {
    g.putEdgeValue(m, p, HAS_PACKAGE)
    g.putEdgeValue(p, m, BELONGS_TO_MODULE)
}

fun addClassToPackage(g: MutableValueGraph<ProjectNode, String>, c: ClassNode, p: PackageNode) {
    g.putEdgeValue(p, c, HAS_CLASS)
    g.putEdgeValue(c, p, BELONGS_TO_PACKAGE)
}

fun addFileToPackage(g: MutableValueGraph<ProjectNode, String>, f: FileNode, p: PackageNode) {
    g.putEdgeValue(p, f, HAS_FILE)
    g.putEdgeValue(f, p, BELONGS_TO_PACKAGE)
}

fun addClassToFile(g: MutableValueGraph<ProjectNode, String>, c: ClassNode, f: FileNode) {
    g.putEdgeValue(f, c, HAS_CLASS)
    g.putEdgeValue(c, f, BELONGS_TO_FILE)
}

interface ProjectNode {
    val type: String
}

data class ClassNode(val name: String, val isPublic: Boolean,  override val type: String = "class") : ProjectNode
data class FileNode(val path: String, val ext: String, override val type: String = "file") : ProjectNode
data class ModuleNode(val path: String, val groupId: String, val artifactId: String, val version: String, override val type: String = "module") : ProjectNode
data class PackageNode(val name: String, override val type: String = "package") : ProjectNode
