load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

def repositories():
<#list archives as archive>
    http_archive(
        name = "${archive.name}",<#if archive.prefix != "">
        strip_prefix = "${archive.prefix}",</#if>
        sha256 = "${archive.sha256}",
        urls = [
        <#list archive.urls as url>
            "${url}",
        </#list>
        ],
    )

<#else>
    print("no project repositories specified!")
</#list>
