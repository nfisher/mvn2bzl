load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven")

def maven_deps():
<#list maven.repositories as repository>
    native.maven_server(
        name = "default",
        url = "${repository.url}"
    )

</#list>
<#list maven.artifacts as artifact>
    native.maven_jar(
        name = "${artifact.bazelPath}",
        artifact = "${artifact.groupId}:${artifact.artifactId}:${artifact.version}",
        server = "default",
    )

<#else>
    print("no maven dependencies in project")
</#list>
