# mvn2bzl

This project was a spike to generate a Bazel workspace from a maven
multi-module project. This is *very* rough around the edges and likely requires
changes to adapt to other projects.

This will generate a `java_library` per sub-module.

The plan is to generate a BUILD file per package which includes:

 - [ ] `proto_library` and `java_proto_library` targets.
 - [ ] `java_binary` targets.
 - [ ] `java_library` per package.
 - [ ] `java_library` per test package.
 - [ ] `java_test` for all test classes.
 - [ ] Inspection of Java AST to add/remove deps as imports are changed.
 - [ ] File watcher to auto-update the BUILD files.

