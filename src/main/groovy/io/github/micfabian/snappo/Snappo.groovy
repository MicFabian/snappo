package io.github.micfabian.snappo

class Snappo {
  static void expect(Object actual) {
    FileSnapshots.assertSnapshot(actual)
  }

  static void expect(Object actual, Comparison comparison) {
    FileSnapshots.assertSnapshot(actual, comparison)
  }

  static void expectNamed(String name, Object actual) {
    FileSnapshots.assertSnapshotNamed(name, actual)
  }

  static void expectNamed(String name, Object actual, Comparison comparison) {
    FileSnapshots.assertSnapshotNamed(name, actual, comparison)
  }

  static Object snapshot(Object actual) {
    FileSnapshots.snapshot(actual, new ComparisonDetector().detect(actual))
  }

  static Object snapshot(Object actual, Comparison comparison) {
    FileSnapshots.snapshot(actual, comparison)
  }

  static Object snapshotNamed(String name, Object actual) {
    FileSnapshots.snapshotNamed(name, actual)
  }

  static Object snapshotNamed(String name, Object actual, Comparison comparison) {
    FileSnapshots.snapshotNamed(name, actual, comparison)
  }

  static Object updateSnapshot(Object actual) {
    FileSnapshots.updateSnapshot(actual)
  }

  static Object updateSnapshot(Object actual, Comparison comparison) {
    FileSnapshots.updateSnapshot(actual, comparison)
  }

  static Object updateSnapshotNamed(String name, Object actual) {
    FileSnapshots.updateSnapshotNamed(name, actual)
  }

  static Object updateSnapshotNamed(String name, Object actual, Comparison comparison) {
    FileSnapshots.updateSnapshotNamed(name, actual, comparison)
  }

  static <T> T withUpdate(Closure<T> block) {
    FileSnapshots.withUpdate(block)
  }

  static <T> T withUpdate(boolean enabled, Closure<T> block) {
    FileSnapshots.withUpdate(enabled, block)
  }
}
