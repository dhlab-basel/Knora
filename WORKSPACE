workspace(name = "io_dasch_dsp_api")

# load http_archive method
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_file")

# bazel-skylib 1.0.3 released 2020.08.27 (https://github.com/bazelbuild/bazel-skylib/releases/tag/1.0.3)
skylib_version = "1.0.3"
http_archive(
    name = "bazel_skylib",
    type = "tar.gz",
    url = "https://github.com/bazelbuild/bazel-skylib/releases/download/{}/bazel-skylib-{}.tar.gz".format (skylib_version, skylib_version),
    sha256 = "1c531376ac7e5a180e0237938a2536de0c54d93f5c278634818e0efc952dd56c",
)

# download rules_scala repository
rules_scala_version="de13a82f65af1e3c7047afd369e505bb3b2e1c64" # 12.02.2021
rules_scala_version_sha256="35db23be908d4a6fccdfaed8a6f100b7307fd0b6aa7b64dc119ca0ecff78027b"
http_archive(
    name = "io_bazel_rules_scala",
    strip_prefix = "rules_scala-%s" % rules_scala_version,
    type = "zip",
    url = "https://github.com/bazelbuild/rules_scala/archive/%s.zip" % rules_scala_version,
    sha256 = rules_scala_version_sha256,
)

# Stores Scala version and other configuration
# 2.12 is a default version, other versions can be use by passing them explicitly:
# scala_config(scala_version = "2.11.12")
load("@io_bazel_rules_scala//:scala_config.bzl", "scala_config")
scala_config(scala_version = "2.12.11")

# register default and our custom scala toolchain
load("@io_bazel_rules_scala//scala:toolchains.bzl", "scala_register_toolchains")
scala_register_toolchains()
register_toolchains("//toolchains:dsp_api_scala_toolchain")

# needed by rules_scala
load("@io_bazel_rules_scala//scala:scala.bzl", "scala_repositories")
scala_repositories()

# register the test toolchain for rules_scala
load("@io_bazel_rules_scala//testing:scalatest.bzl", "scalatest_repositories", "scalatest_toolchain")
scalatest_repositories()
scalatest_toolchain()

#
# Download the protobuf repository (needed by go and rules_scala_annex)
#
protobuf_tag = "3.12.3"
protobuf_sha256 = "e5265d552e12c1f39c72842fa91d84941726026fa056d914ea6a25cd58d7bbf8"
http_archive(
    name = "com_google_protobuf",
    strip_prefix = "protobuf-{}".format(protobuf_tag),
    type = "zip",
    url = "https://github.com/protocolbuffers/protobuf/archive/v{}.zip".format(protobuf_tag),
    sha256 = protobuf_sha256,
)

load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")
protobuf_deps()

#
# download rules_jvm_external used for maven dependency resolution
# defined in the third_party sub-folder
#
rules_jvm_external_version = "4.0" # 6.01.2021
rules_jvm_external_version_sha256 = "31701ad93dbfe544d597dbe62c9a1fdd76d81d8a9150c2bf1ecf928ecdf97169"

http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % rules_jvm_external_version,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % rules_jvm_external_version,
    sha256 = rules_jvm_external_version_sha256,
)

# load the dependencies defined in the third_party sub-folder
load("//third_party:dependencies.bzl", "dependencies")
dependencies()

# pin dependencies to the ones stored in maven_install.json in the third_party sub-folder
# to update: bazel run @maven//:pin
load("@maven//:defs.bzl", "pinned_maven_install")
pinned_maven_install()

#
# Load rules_scala_annex, required by rules_twirl
#
rules_scala_annex_version = "aed5cc8a433824e23dc82119de1b98904ab1bb9e" # 29.01.2021
rules_scala_annex_sha256 = "4f8c622123600b66d9f202c18953cebbeadf2e9e4a7dd11ebebffb2f600d73bc"
http_archive(
    name = "rules_scala_annex",
    strip_prefix = "rules_scala-{}".format(rules_scala_annex_version),
    url = "https://github.com/higherkindness/rules_scala/archive/{}.zip".format(rules_scala_annex_version),
    sha256 = rules_scala_annex_sha256,
)

load("@rules_scala_annex//rules/scala:workspace.bzl", "scala_register_toolchains", "scala_repositories")
scala_repositories()
load("@annex//:defs.bzl", annex_pinned_maven_install = "pinned_maven_install")
annex_pinned_maven_install()
scala_register_toolchains()

load("@rules_scala_annex//rules/scalafmt:workspace.bzl", "scalafmt_default_config", "scalafmt_repositories")
scalafmt_repositories()
load("@annex_scalafmt//:defs.bzl", annex_scalafmt_pinned_maven_install = "pinned_maven_install")
annex_scalafmt_pinned_maven_install()
scalafmt_default_config()

load("@rules_scala_annex//rules/scala_proto:workspace.bzl", "scala_proto_register_toolchains", "scala_proto_repositories",)
scala_proto_repositories()
load("@annex_proto//:defs.bzl", annex_proto_pinned_maven_install = "pinned_maven_install")
annex_proto_pinned_maven_install()
scala_proto_register_toolchains()

# Specify the scala compiler we wish to use; in this case, we'll use the default one specified in rules_scala_annex
bind(
    name = "default_scala",
    actual = "@rules_scala_annex//src/main/scala:zinc_2_12_10",
)

#
# download the rules_twirl repository (needed to compile twirl templates)
#
rules_twirl_version = "7482a70aa5c3b9eb9cb67e2b263e3c95b6503a0c" # 14.01.2021
rules_twirl_version_sha256 = "49e4f53d8bb754be45ffa7a0ae337e70a0ebcb4cb10701cd9e9a14955502485e"
http_archive(
  name = "io_bazel_rules_twirl",
  strip_prefix = "rules_twirl-%s" % rules_twirl_version,
  type = "zip",
  url = "https://github.com/lucidsoftware/rules_twirl/archive/%s.zip" % rules_twirl_version,
  sha256 = rules_twirl_version_sha256,
)

load("@io_bazel_rules_twirl//:workspace.bzl", "twirl_repositories")
twirl_repositories()

load("@twirl//:defs.bzl", twirl_pinned_maven_install = "pinned_maven_install")
twirl_pinned_maven_install()

#
# Download the rules_go repository
#
http_archive(
    name = "io_bazel_rules_go",
    urls = [
        "https://storage.googleapis.com/bazel-mirror/github.com/bazelbuild/rules_go/releases/download/v0.20.1/rules_go-v0.20.1.tar.gz",
        "https://github.com/bazelbuild/rules_go/releases/download/v0.20.1/rules_go-v0.20.1.tar.gz",
    ],
    sha256 = "842ec0e6b4fbfdd3de6150b61af92901eeb73681fd4d185746644c338f51d4c0",
)

load("@io_bazel_rules_go//go:deps.bzl", "go_rules_dependencies", "go_register_toolchains")

go_rules_dependencies()

go_register_toolchains()

# legacy variant used by rules_docker. Remove after rules_docker was updated to
# newest rules_python
load("@rules_python//python:pip.bzl", "pip_import", "pip_repositories")
pip_repositories()

#
# Download the rules_docker repository at release v0.14.4
#
rules_docker_version="0.14.4"
rules_docker_version_sha256="4521794f0fba2e20f3bf15846ab5e01d5332e587e9ce81629c7f96c793bb7036"
http_archive(
    name = "io_bazel_rules_docker",
    sha256 = rules_docker_version_sha256,
    strip_prefix = "rules_docker-%s" % rules_docker_version,
    url = "https://github.com/bazelbuild/rules_docker/releases/download/v%s/rules_docker-v%s.tar.gz" % (rules_docker_version, rules_docker_version),
)

load(
    "@io_bazel_rules_docker//repositories:repositories.bzl",
    container_repositories = "repositories",
)
container_repositories()

load("@io_bazel_rules_docker//repositories:deps.bzl", container_deps = "deps")

container_deps()

load("@io_bazel_rules_docker//repositories:pip_repositories.bzl", "pip_deps")

pip_deps()

# load container_pull method
load(
    "@io_bazel_rules_docker//container:container.bzl",
    "container_pull",
)

# get distroless java
container_pull(
  name = "java_base",
  registry = "gcr.io",
  repository = "distroless/java",
  # 'tag' is also supported, but digest is encouraged for reproducibility.
  digest = "sha256:deadbeef",
)

# get openjdk
container_pull(
    name = "openjdk11",
    registry = "docker.io",
    repository = "adoptopenjdk",
    tag = "11-jre-hotspot-bionic",
    digest = "sha256:0e51b455654bd162c485a6a6b5b120cc82db453d9265cc90f0c4fb5d14e2f62e",
)

# get sipi
load("//third_party:versions.bzl", "SIPI_REPOSITORY", "SIPI_VERSION", "SIPI_IMAGE_DIGEST")
container_pull(
    name = "sipi",
    registry = "docker.io",
    repository = SIPI_REPOSITORY,
    tag = SIPI_VERSION,
    digest = SIPI_IMAGE_DIGEST,
)

# get fuseki
load("//third_party:versions.bzl", "FUSEKI_REPOSITORY", "FUSEKI_VERSION", "FUSEKI_IMAGE_DIGEST")
container_pull(
    name = "jenafuseki",
    registry = "docker.io",
    repository = FUSEKI_REPOSITORY,
    tag = FUSEKI_VERSION,
    digest = FUSEKI_IMAGE_DIGEST,
)

#
# download rules_pkg - basic packaging rules
#
rules_package_version="0.2.4"
rules_package_version_sha256="4ba8f4ab0ff85f2484287ab06c0d871dcb31cc54d439457d28fd4ae14b18450a"
http_archive(
    name = "rules_pkg",
    url = "https://github.com/bazelbuild/rules_pkg/releases/download/%s/rules_pkg-%s.tar.gz" % (rules_package_version, rules_package_version),
    sha256 = rules_package_version_sha256
)

# load further dependencies of this rule
load("@rules_pkg//:deps.bzl", "rules_pkg_dependencies")
rules_pkg_dependencies()

#
# download rules_stamp - stamping helper
#
http_archive(
    name = "ecosia_rules_stamp",
    url = "https://github.com/ecosia/rules_stamp/archive/48d5ef2bc0d93bd65fddddbe02f3ae410e25169d.tar.gz",
    strip_prefix = "rules_stamp-48d5ef2bc0d93bd65fddddbe02f3ae410e25169d",
    sha256 = "36d7ea381bfb2520f9353299b162434b25c77365d3c9e9459195c536da5e837d",
)
