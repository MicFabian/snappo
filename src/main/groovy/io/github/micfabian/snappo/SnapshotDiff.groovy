package io.github.micfabian.snappo

import java.lang.reflect.Array

class SnapshotDiff {
  private static final int MAX_DIFFERENCES = Integer.getInteger('snappo.diff.max', 25)
  private static final int MAX_VALUE_LENGTH = Integer.getInteger('snappo.diff.value.maxLength', 160)

  static String describe(Object expected, Object actual) {
    List<String> differences = []
    boolean[] truncated = [false] as boolean[]

    collectDifferences('$', normalize(expected), normalize(actual), differences, truncated)
    if (differences.isEmpty()) {
      return ''
    }

    StringBuilder builder = new StringBuilder()
    if (truncated[0]) {
      builder.append("Differences (showing first ${MAX_DIFFERENCES}):")
    } else {
      builder.append("Differences (${differences.size()}):")
    }

    differences.each { String difference ->
      builder.append(System.lineSeparator())
      builder.append(' - ')
      builder.append(difference)
    }
    builder.toString()
  }

  private static void collectDifferences(String path, Object expected, Object actual, List<String> differences, boolean[] truncated) {
    if (differences.size() >= MAX_DIFFERENCES) {
      truncated[0] = true
      return
    }

    if (expected == actual) {
      return
    }

    if (expected instanceof Number && actual instanceof Number && equalNumbers(expected as Number, actual as Number)) {
      return
    }

    if (expected instanceof Map && actual instanceof Map) {
      compareMaps(path, expected as Map, actual as Map, differences, truncated)
      return
    }

    if (expected instanceof List && actual instanceof List) {
      compareLists(path, expected as List, actual as List, differences, truncated)
      return
    }

    if (expected?.getClass() != actual?.getClass()) {
      addDifference(
        differences,
        truncated,
        "${path} type mismatch: expected ${formatValue(expected)} (${typeName(expected)}), but was ${formatValue(actual)} (${typeName(actual)})"
      )
      return
    }

    addDifference(differences, truncated, "${path} expected ${formatValue(expected)}, but was ${formatValue(actual)}")
  }

  private static void compareMaps(String path, Map expected, Map actual, List<String> differences, boolean[] truncated) {
    List<Object> keys = new ArrayList<>(expected.keySet())
    actual.keySet().each { Object key ->
      if (!keys.contains(key)) {
        keys.add(key)
      }
    }
    keys.sort { a, b -> String.valueOf(a) <=> String.valueOf(b) }

    for (Object key : keys) {
      if (differences.size() >= MAX_DIFFERENCES) {
        truncated[0] = true
        return
      }

      String keyPath = pathForKey(path, key)
      boolean hasExpected = expected.containsKey(key)
      boolean hasActual = actual.containsKey(key)

      if (hasExpected && !hasActual) {
        addDifference(differences, truncated, "${keyPath} missing in actual (expected ${formatValue(expected[key])})")
        continue
      }
      if (!hasExpected && hasActual) {
        addDifference(differences, truncated, "${keyPath} unexpected in actual (actual ${formatValue(actual[key])})")
        continue
      }

      collectDifferences(keyPath, normalize(expected[key]), normalize(actual[key]), differences, truncated)
    }
  }

  private static void compareLists(String path, List expected, List actual, List<String> differences, boolean[] truncated) {
    if (expected.size() != actual.size()) {
      addDifference(differences, truncated, "${path} size mismatch: expected ${expected.size()}, but was ${actual.size()}")
    }

    int maxIndex = Math.min(expected.size(), actual.size())
    for (int index = 0; index < maxIndex; index++) {
      if (differences.size() >= MAX_DIFFERENCES) {
        truncated[0] = true
        return
      }
      collectDifferences("${path}[${index}]", normalize(expected[index]), normalize(actual[index]), differences, truncated)
    }
  }

  private static void addDifference(List<String> differences, boolean[] truncated, String difference) {
    if (differences.size() >= MAX_DIFFERENCES) {
      truncated[0] = true
      return
    }
    differences.add(difference)
  }

  private static Object normalize(Object value) {
    if (value == null) {
      return null
    }
    if (value instanceof Map) {
      Map normalized = [:]
      (value as Map).each { Object key, Object item ->
        normalized[key] = normalize(item)
      }
      return normalized
    }
    if (value instanceof List) {
      return (value as List).collect { Object item -> normalize(item) }
    }
    if (value.getClass().isArray()) {
      int size = Array.getLength(value)
      List normalized = []
      for (int index = 0; index < size; index++) {
        normalized.add(normalize(Array.get(value, index)))
      }
      return normalized
    }
    value
  }

  private static String pathForKey(String path, Object key) {
    String name = String.valueOf(key)
    if (name ==~ /[A-Za-z_][A-Za-z0-9_]*/) {
      return "${path}.${name}"
    }
    String escaped = name.replace("\\", "\\\\").replace("'", "\\'")
    "${path}['${escaped}']"
  }

  private static String formatValue(Object value) {
    if (value == null) {
      return 'null'
    }

    String rendered
    if (value instanceof CharSequence) {
      String escaped = value.toString().replace("\\", "\\\\").replace("'", "\\'")
      rendered = "'${escaped}'"
    } else {
      rendered = String.valueOf(value)
    }

    if (rendered.length() > MAX_VALUE_LENGTH) {
      return rendered.substring(0, MAX_VALUE_LENGTH - 3) + '...'
    }
    rendered
  }

  private static String typeName(Object value) {
    value == null ? 'null' : value.getClass().simpleName
  }

  private static boolean equalNumbers(Number expected, Number actual) {
    new BigDecimal(expected.toString()).compareTo(new BigDecimal(actual.toString())) == 0
  }
}
