# vim: set syntax=bzl :
workspace(name = "mvn2bzl")

load("//:repositories.bzl", "repositories")
repositories()

load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kotlin_repositories", "kt_register_toolchains")
kotlin_repositories()
kt_register_toolchains()

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven")

MAVEN_VERSION = "3.6.1"
MAVEN_RESOLVER_VERSION = "1.3.3"
SLF4J_VERSION = "1.7.25"

maven_install(
    artifacts = [
        maven.artifact("com.google.guava", "guava", "27.1-jre"),
        maven.artifact("javax.inject", "javax.inject", "1"),
        maven.artifact("junit", "junit", "4.12"),
        maven.artifact("org.apache.maven", "maven-core", MAVEN_VERSION),
        maven.artifact("org.apache.maven", "maven-model", MAVEN_VERSION),
        maven.artifact("org.apache.maven", "maven-model-builder", MAVEN_VERSION),
        maven.artifact("org.apache.maven","maven-resolver-provider", MAVEN_VERSION),
        maven.artifact("org.apache.maven.resolver", "maven-resolver-api", MAVEN_RESOLVER_VERSION),
        maven.artifact("org.apache.maven.resolver", "maven-resolver-connector-basic", MAVEN_RESOLVER_VERSION),
        maven.artifact("org.apache.maven.resolver", "maven-resolver-impl", MAVEN_RESOLVER_VERSION),
        maven.artifact("org.apache.maven.resolver", "maven-resolver-spi", MAVEN_RESOLVER_VERSION),
        maven.artifact("org.apache.maven.resolver", "maven-resolver-transport-file", MAVEN_RESOLVER_VERSION),
        maven.artifact("org.apache.maven.resolver", "maven-resolver-transport-http", MAVEN_RESOLVER_VERSION),
        maven.artifact("org.apache.maven.resolver", "maven-resolver-util", MAVEN_RESOLVER_VERSION),
        maven.artifact("org.eclipse.sisu", "org.eclipse.sisu.inject", "0.3.3"),
        maven.artifact("org.freemarker", "freemarker", "2.3.28"),
        maven.artifact("org.jgrapht", "jgrapht-core", "1.3.0"),
        maven.artifact("org.slf4j", "slf4j-api", SLF4J_VERSION),
        maven.artifact("org.slf4j", "slf4j-simple", SLF4J_VERSION),
        maven.artifact("org.sonatype.sisu", "sisu-guice", "3.2.6"),
    ],
    fail_on_missing_checksum = True,
    fetch_sources = True,
    repositories = [
        maven.repository("http://localhost:8081/repository/maven-central/"),
    ],
)
