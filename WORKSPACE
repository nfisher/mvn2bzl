# vim: set syntax=bzl :

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

rules_kotlin_version = "990fcc53689c8b58b3229c7f628f843a60cb9f5c"

http_archive(
    name = "io_bazel_rules_kotlin",
    sha256 = "51f86a66c0affd7a9a63a44d061a154da37c8771f3b8daa8f51b150903b4d797",
    strip_prefix = "rules_kotlin-%s" % rules_kotlin_version,
    type = "zip",
    urls = ["https://github.com/bazelbuild/rules_kotlin/archive/%s.zip" % rules_kotlin_version],
)

load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kotlin_repositories", "kt_register_toolchains")

kotlin_repositories()

kt_register_toolchains()

# download rules_jvm_external
RULES_JVM_EXTERNAL_TAG = "2.2"
RULES_JVM_EXTERNAL_SHA = "f1203ce04e232ab6fdd81897cf0ff76f2c04c0741424d192f28e65ae752ce2d6"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven")

MAVEN_VERSION = "3.6.1"
MAVEN_RESOLVER_VERSION = "1.3.3"
SLF4J_VERSION = "1.7.25"

maven_install(
    artifacts = [
        maven.artifact("org.neo4j", "neo4j", "3.5.6"),
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
        maven.artifact("org.jetbrains.kotlin", "kotlin-test-junit", "1.2.71"),
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
