load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

def repositories():
    RULES_KOTLIN_TAG = "990fcc53689c8b58b3229c7f628f843a60cb9f5c"
    RULES_KOTLIN_SHA = "51f86a66c0affd7a9a63a44d061a154da37c8771f3b8daa8f51b150903b4d797"
    RULES_JVM_EXTERNAL_TAG = "2.2"
    RULES_JVM_EXTERNAL_SHA = "f1203ce04e232ab6fdd81897cf0ff76f2c04c0741424d192f28e65ae752ce2d6"

    http_archive(
        name = "io_bazel_rules_kotlin",
        sha256 = RULES_KOTLIN_SHA,
        strip_prefix = "rules_kotlin-%s" % RULES_KOTLIN_TAG,
        type = "zip",
        urls = ["https://github.com/bazelbuild/rules_kotlin/archive/%s.zip" % RULES_KOTLIN_TAG],
    )

    http_archive(
        name = "rules_jvm_external",
        sha256 = RULES_JVM_EXTERNAL_SHA,
        strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
        url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
    )
