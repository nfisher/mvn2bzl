workspace(name = "${workspace.name}")

load("//:repositories.bzl", "repositories")
repositories()

load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")
protobuf_deps()

load("//:maven.bzl", "maven_deps")
maven_deps()
