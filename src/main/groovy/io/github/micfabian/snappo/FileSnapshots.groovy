package io.github.micfabian.snappo

import java.nio.file.Path

@Deprecated
class FileSnapshots {
  static Path getSnapshotsRoot() {
    Snappo.snapshotsRoot
  }

  static void setSnapshotsRoot(Path snapshotsRoot) {
    Snappo.snapshotsRoot = snapshotsRoot
  }

  static Closure<String> getPackageNameProvider() {
    Snappo.packageNameProvider
  }

  static void setPackageNameProvider(Closure<String> packageNameProvider) {
    Snappo.packageNameProvider = packageNameProvider
  }

  static Closure<String> getClassNameProvider() {
    Snappo.classNameProvider
  }

  static void setClassNameProvider(Closure<String> classNameProvider) {
    Snappo.classNameProvider = classNameProvider
  }

  static Closure<String> getFeatureName() {
    Snappo.featureName
  }

  static void setFeatureName(Closure<String> featureName) {
    Snappo.featureName = featureName
  }

  static Closure<Boolean> getUpdating() {
    Snappo.updating
  }

  static void setUpdating(Closure<Boolean> updating) {
    Snappo.updating = updating
  }

  static boolean isUpdating() {
    Snappo.isUpdating()
  }

  static void assertSnapshot(Object actual) {
    Snappo.assertSnapshot(actual)
  }

  static void assertSnapshot(Object actual, Comparison comparison) {
    Snappo.assertSnapshot(actual, comparison)
  }

  static void assertSnapshotNamed(String name, Object actual) {
    Snappo.assertSnapshotNamed(name, actual)
  }

  static void assertSnapshotNamed(String name, Object actual, Comparison comparison) {
    Snappo.assertSnapshotNamed(name, actual, comparison)
  }

  static Object snapshot(Object actual, Comparison comparison) {
    Snappo.snapshot(actual, comparison)
  }

  static Object snapshotNamed(String name, Object actual) {
    Snappo.snapshotNamed(name, actual)
  }

  static Object snapshotNamed(String name, Object actual, Comparison comparison) {
    Snappo.snapshotNamed(name, actual, comparison)
  }

  static Object updateSnapshot(Object actual) {
    Snappo.updateSnapshot(actual)
  }

  static Object updateSnapshot(Object actual, Comparison comparison) {
    Snappo.updateSnapshot(actual, comparison)
  }

  static Object updateSnapshotNamed(String name, Object actual) {
    Snappo.updateSnapshotNamed(name, actual)
  }

  static Object updateSnapshotNamed(String name, Object actual, Comparison comparison) {
    Snappo.updateSnapshotNamed(name, actual, comparison)
  }

  static <T> T withUpdate(Closure<T> block) {
    Snappo.withUpdate(block)
  }

  static <T> T withUpdate(boolean enabled, Closure<T> block) {
    Snappo.withUpdate(enabled, block)
  }

  static <T> T withFeatureName(String name, Closure<T> block) {
    Snappo.withFeatureName(name, block)
  }

  static Path packageDir() {
    Snappo.packageDir()
  }

  static Object current(Object actual, Comparison comparison) {
    Snappo.current(actual, comparison)
  }

  static String inferClassNameFromStack() {
    Snappo.inferClassNameFromStack()
  }
}
