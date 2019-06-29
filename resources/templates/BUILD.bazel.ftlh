java_library(
    name = "${build.name}",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
<#list build.libDeps as dep>
        "${dep.workspace}${dep.target}",
</#list>
    ],
)
<#if build.hasTestDeps >

java_library(
    name = "testing",
    exports = [
<#list build.testDeps as dep>
        "${dep.workspace}${dep.target}",
</#list>
    ],
)
</#if>
<#if build.hasMainClass >

java_binary(
    name = "${build.mainName}",
    main_class = "${build.mainClass}",
    deps = [ ":${build.name}" ],
)
</#if>
