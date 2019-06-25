package mvntobzl.graph

import com.google.common.graph.MutableValueGraph
import com.google.common.graph.ValueGraphBuilder
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

/*
Assumption:
    - For modules the specific will always be preferred over the general
      (e.g. com.instana is more general than com.instana.metrics).
    - Events will *always* be emitted in the following total order:

        1. file.
        2. module.
        3. package.
        4. class.

      This implies a later node can always assume an earlier node in this ordering
      is present in the graph.

    - Relationship Ordering

        file
         └─ module ──┐
             ├─ file │
             │   └─ package
             │       └─ class
             └─ file
                 └─ package (cached)
                     └─ class

NodeList

    {
      path: metrics/pom.xml,
      nodes: [
          File(path: metrics/pom.xml, mediaType: xml, sha: "123ABC..."),
          Module(path: metrics, groupId: com.instana.com, artifactId: metrics, version: 1.0.0-SNAPSHOT),
      ]
      relationships: [
          { from: Module, rel: HAS_FILE, to: File },
          { from: File, rel: BELONGS_TO_MODULE, to: Module },
      ]
    }

    {
        path: metrics/src/main/java/com/instana/metrics/Formatter.java,
        nodes: [
            File(path: metrics/src/main/java/com/instana/metrics/Formatter.java, mediaType: javaSrc, sha: "ABC123..."),
            Module(path: metrics, groupId: com.instana.com, artifactId: metrics, version: 1.0.0-SNAPSHOT),
            Package(name: com.instana.metrics),
            Class(name: com.instana.metrics.Formatter, source: java),
        ],
        relationships: [
            { from: Module, rel: HAS_A, to: Package },
            { from: Package, rel: BELONGS_TO, to: Module },
            { from: Package, rel: HAS_A, to: Class },
            { from: Class, rel: BELONGS_TO, to: Package },
        ]
    }

Events (maybe?)

    { event: "discovered_file", mimetype:String, path:String }
    { event: "added_file", path:String, node:FileNode }
    { event: "added_module", path:String, node:ModuleNode }
    { event: "added_package", path:String, node:PackageNode }
    { event: "added_class", path:String, node:ClassNode }

GraphDispatcher(val q:BlockingQueue) {

    // add a receiver to the dispatcher
    addReceiver(receiver:NodeReceiver)

    // start the event loop (non-blocking)
    start()

    // put poison pill into the queue and drain.
    drain()

    // stop the event loop (blocking)
    stop()
}

interface NodeReceiver {
    receive(NodeList): NodeList
}

*/
class GraphTestCase {

    @Test
    fun `build workspace graph`() {
        val graph: MutableValueGraph<ProjectNode, String> = ValueGraphBuilder
                .directed()
                .allowsSelfLoops(false)
                .build()

        val metrics = ModuleNode(path = "metrics", groupId = "com.instana", artifactId = "metrics", version = "1.0.0-SNAPSHOT")
        val metricsPom = FileNode(path = "metrics/pom.xml", mediaType = "xml")
        val metricsPackage = PackageNode(name = "com.instana.metrics")

        val metricsFormatter = FileNode(path = "metrics/src/main/java/com/instana/metrics/Formatter.java", mediaType = "java")
        val metricsFormatterClass = ClassNode(name = "com.instana.metrics.Formatter", isPublic = true)

        val catalogPackage = PackageNode(name = "com.instana.metrics.catalog")
        val metricDescription = FileNode(path = "metrics/src/main/java/com/instana/metrics/catalog/MetricDescription.java", mediaType = "java")
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

data class ClassNode(val name: String, val isPublic: Boolean, override val type: String = "class") : ProjectNode
data class FileNode(val path: String, val mediaType: String, override val type: String = "file") : ProjectNode
data class ModuleNode(val path: String, val groupId: String, val artifactId: String, val version: String, override val type: String = "module") : ProjectNode
data class PackageNode(val name: String, override val type: String = "package") : ProjectNode
