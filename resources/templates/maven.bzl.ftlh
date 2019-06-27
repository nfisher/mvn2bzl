load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven")

def maven_deps():
    maven_install(
        artifacts = [
<#list maven.artifacts as artifact>
            maven.artifact("${artifact.groupId}", "${artifact.artifactId}", "${artifact.version}"),
</#list>
        ],
        fail_on_missing_checksum = True,
        fetch_sources = True,
        repositories = [
<#list maven.repositories as repository>
            maven.repository("${repository.url}"),
</#list>
        ],
    )
