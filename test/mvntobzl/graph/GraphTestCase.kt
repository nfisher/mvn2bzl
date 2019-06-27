package mvntobzl.graph

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.jgrapht.Graphs.addEdge
import org.jgrapht.graph.SimpleDirectedGraph


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

Effects

    {
      path: metrics/pom.xml,
      nodes: [
          File(path: metrics/pom.xml, mediaType: xml, sha256: "123ABC..."),
          Module(path: metrics, groupId: com.instana.com, artifactId: metrics, version: 1.0.0-SNAPSHOT),
      ]
      relationships: [
          { a: Module, b: File },
      ]
    }

    {
        path: metrics/src/main/java/com/instana/metrics/Formatter.java,
        nodes: [
            File(path: metrics/src/main/java/com/instana/metrics/Formatter.java, mediaType: javaSrc, sha256: "ABC123..."),
            Module(path: metrics, groupId: com.instana.com, artifactId: metrics, version: 1.0.0-SNAPSHOT),
            Package(name: com.instana.metrics),
            Class(name: com.instana.metrics.Formatter, source: java),
        ],
        relationships: [
            { a: Module, b: Package },
            { a: Package, b: Class },
            { a: File, b: Class },
            { a: Class, b: Package },
        ]
    }

Events (maybe?)

    { event: "discovered_file", mimetype:String, path:String }
    { event: "added_file", path:String, node:FileNode }
    { event: "added_module", path:String, node:ModuleNode }
    { event: "added_package", path:String, node:PackageNode }
    { event: "added_class", path:String, node:ClassNode }

EventHandler(val q:BlockingQueue) {

    // add a receiver to the dispatcher
    addReceiver(receiver:NodeReceiver)

    // start the event loop (non-blocking)
    start()

    // put poison pill into the queue and drain.
    drain()

    // stop the event loop (blocking)
    stop()

    applyEffects()
}

interface NodeReceiver {
    receive(NodeList): NodeList
}

*/
class GraphTestCase {

    @Test
    fun `build workspace jgrapht`() {
        val g: Graph<String, DefaultEdge> = SimpleDirectedGraph(DefaultEdge::class.java)
        val v1 = "v1"
        val v2 = "v2"
        val v3 = "v3"
        val v4 = "v4"

        // add the vertices
        g.addVertex(v1)
        g.addVertex(v2)
        g.addVertex(v3)
        g.addVertex(v4)

        // add edges to create a circuit
        g.addEdge(v1, v2)
        g.addEdge(v2, v3)
        g.addEdge(v3, v4)
        g.addEdge(v4, v1)

        assertNotNull(g)
    }

    @Test
    fun `build workspace guava graph`() {
        val graph = newWorkspace()

        val metrics = ModuleNode(path = "metrics", groupId = "com.instana", artifactId = "metrics", version = "1.0.0-SNAPSHOT")
        val metricsPom = FileNode(path = "metrics/pom.xml", mediaType = "xml")
        val metricsPackage = PackageNode(name = "com.instana.metrics")

        val metricsFormatter = FileNode(path = "metrics/src/main/java/com/instana/metrics/Formatter.java", mediaType = "java")
        val metricsFormatterClass = ClassNode(name = "com.instana.metrics.Formatter", isPublic = true)

        val catalogPackage = PackageNode(name = "com.instana.metrics.catalog")
        val metricDescription = FileNode(path = "metrics/src/main/java/com/instana/metrics/catalog/MetricDescription.java", mediaType = "java")
        val metricDescriptionClass = ClassNode(name = "com.instana.metrics.catalog.MetricDescription", isPublic = true)

        add(graph, metricsPom, metrics)

        add(graph, metricsPackage, metrics)
        add(graph, metricsFormatter, metricsPackage)
        add(graph, metricsFormatterClass, metricsPackage)
        add(graph, metricsFormatterClass, metricsFormatter)

        add(graph, catalogPackage, metrics)

        add(graph, metricDescription, catalogPackage)
        add(graph, metricDescriptionClass, catalogPackage)
        add(graph, metricDescriptionClass, metricDescription)

        add(graph, metricDescription, metricsFormatterClass)

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

