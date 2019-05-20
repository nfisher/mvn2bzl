package mvntobzl

import org.eclipse.aether.artifact.Artifact

fun pomPathToWorkspace(depFilename: String, workspaceRoot: String):String {
    if (depFilename == "$workspaceRoot/pom.xml") {
        return ""
    }

    return depFilename.substring(workspaceRoot.length + 1, depFilename.length - "/pom.xml".length)
}

fun artifactToPath(a: Artifact): String {
    return toBazelPath(artifactId(a))
}