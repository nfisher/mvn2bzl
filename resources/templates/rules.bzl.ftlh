# modified Selenium java_test_suite, thanks @shs96c!
def java_test_suite(
    name,
    srcs,
    resources=None,
    deps=None,
    visibility=None,
    size = None,
    tags = []):

  pkg = native.package_name()

  tests = []

  actual_tags = []
  actual_tags.extend(tags)
  if "small" != size:
    actual_tags.append("no-sandbox")

  for src in srcs:
    if src.endswith('Test.java'):
      test_name = src[:-len('.java')]
      tests += [test_name]
      test_class = None
      if pkg != None:
        test_class = pkg + "." + test_name
      native.java_test(
          name = test_name,
          srcs = [src],
          size = size,
          test_class = test_class,
          resources = resources,
	  tags = actual_tags,
          deps = deps,
          visibility = ["//visibility:private"])

  native.test_suite(
      name = name,
      tests = tests,
      tags = ["manual"] + tags,
      visibility = visibility)