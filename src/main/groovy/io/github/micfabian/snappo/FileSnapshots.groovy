package io.github.micfabian.snappo

import io.github.micfabian.snappo.environment.ContinousIntegration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FileSnapshots {
  private static final Logger LOG = LoggerFactory.getLogger(FileSnapshots)

  private static final ComparisonDetector DETECTOR = new ComparisonDetector()
  private static ThreadLocal<String> lastWrittenFeatureName = new ThreadLocal<>()
  private static ThreadLocal<Integer> featuresWritten = new ThreadLocal<>()
  private static ThreadLocal<Boolean> updateOverride = new ThreadLocal<>()
  private static ThreadLocal<String> featureNameOverride = new ThreadLocal<>()

  static Path snapshotsRoot = Paths.get(System.getProperty('spock.snapshot.dir', 'src/test/resources/snapshots'))

  static Closure<String> packageNameProvider = { FeatureNameExtension.packageName ?: '' }
  static Closure<String> classNameProvider = { FeatureNameExtension.className ?: '' }
  static Closure<String> featureName = { FeatureNameExtension.featureName ?: '' }
  static Closure<Boolean> updating = {
    def env = System.getenv('SNAPPO_UPDATE') ?: System.getenv('SPOCK_UPDATE')
    if (env && env.equalsIgnoreCase('true')) {
      return true
    }
    def prop = System.getProperty('snappo.snapshot.update') ?: System.getProperty('spock.snapshot.update')
    prop && prop.equalsIgnoreCase('true')
  }

  static boolean isUpdating() {
    Boolean override = updateOverride.get()
    if (override != null) {
      return override
    }
    updating.call()
  }

  static void assertSnapshot(Object actual) {
    assertSnapshot(actual, DETECTOR.detect(actual))
  }

  static void assertSnapshot(Object actual, Comparison comparison) {
    Path resource = detectResource(comparison)
    Object expected = readResource(resource, comparison)
    Object current = current(actual, comparison)

    if (expected == current) {
      return
    }

    if (isUpdating()) {
      expected = upsertResource(actual, resource, comparison)
    }

    if (expected != current) {
      throw new AssertionError("Snapshot mismatch for ${resource}\nExpected: ${expected}\nActual: ${current}")
    }
  }

  static void assertSnapshotNamed(String name, Object actual) {
    assertSnapshotNamed(name, actual, new ComparisonDetector().detect(actual))
  }

  static void assertSnapshotNamed(String name, Object actual, Comparison comparison) {
    withFeatureName(name) {
      assertSnapshot(actual, comparison)
    }
  }

  static Object snapshot(Object actual, Comparison comparison) {
    Path resource = detectResource(comparison)
    Object current = current(actual, comparison)
    Object expected = readResource(resource, comparison)

    if (expected == current) {
      return expected
    }

    return upsertResource(actual, resource, comparison)
  }

  static Object snapshotNamed(String name, Object actual) {
    snapshotNamed(name, actual, DETECTOR.detect(actual))
  }

  static Object snapshotNamed(String name, Object actual, Comparison comparison) {
    withFeatureName(name) {
      snapshot(actual, comparison)
    }
  }

  static Object updateSnapshot(Object actual) {
    updateSnapshot(actual, DETECTOR.detect(actual))
  }

  static Object updateSnapshot(Object actual, Comparison comparison) {
    withUpdate(true) {
      snapshot(actual, comparison)
    }
  }

  static Object updateSnapshotNamed(String name, Object actual) {
    updateSnapshotNamed(name, actual, DETECTOR.detect(actual))
  }

  static Object updateSnapshotNamed(String name, Object actual, Comparison comparison) {
    withFeatureName(name) {
      updateSnapshot(actual, comparison)
    }
  }

  static <T> T withUpdate(Closure<T> block) {
    withUpdate(true, block)
  }

  static <T> T withUpdate(boolean enabled, Closure<T> block) {
    Boolean previous = updateOverride.get()
    updateOverride.set(enabled)
    try {
      return block.call()
    } finally {
      if (previous == null) {
        updateOverride.remove()
      } else {
        updateOverride.set(previous)
      }
    }
  }

  static <T> T withFeatureName(String name, Closure<T> block) {
    String previous = featureNameOverride.get()
    featureNameOverride.set(name)
    try {
      return block.call()
    } finally {
      if (previous == null) {
        featureNameOverride.remove()
      } else {
        featureNameOverride.set(previous)
      }
    }
  }

  static Path packageDir() {
    String pkg = packageNameProvider.call()
    String className = classNameProvider.call()

    Path root = snapshotsRoot
    if (pkg) {
      root = root.resolve(pkg.replace('.', File.separator))
    }
    String classDir = toKebabCase(className)
    root.resolve(classDir)
  }

  static Object current(Object actual, Comparison comparison) {
    comparison.beforeComparison(actual)
  }

  private static Object upsertResource(Object actual, Path resource, Comparison comparison) {
    File file = resource.toFile()

    if (isUpdating()) {
      if (!file.exists()) {
        file.parentFile?.mkdirs()
        file.createNewFile()
      }
      byte[] bytes = comparison.beforeStore(actual)
      file.bytes = bytes
      return readResource(resource, comparison)
    }

    if (!file.exists() || file.length() == 0) {
      if (ContinousIntegration.isCi()) {
        throw new FileNotFoundException(file.path)
      }
      LOG.debug('Creating {}', file.path)
      file.parentFile?.mkdirs()
      file.createNewFile()
      byte[] bytes = comparison.beforeStore(actual)
      file.bytes = bytes
      return readResource(resource, comparison)
    }

    return readResource(resource, comparison)
  }

  private static Path detectResource(Comparison comparison) {
    Path dir = packageDir()
    if (!Files.exists(dir)) {
      LOG.debug('Creating package dir {}', dir)
      Files.createDirectories(dir)
    }

    String feature = sanitizeFeatureName(featureNameOverride.get() ?: featureName.call())
    if (!feature) {
      feature = 'snapshot'
    }

    String lastFeature = lastWrittenFeatureName.get()
    Integer count = featuresWritten.get()
    if (lastFeature && lastFeature == feature) {
      count = (count ?: 0) + 1
    } else {
      lastFeature = feature
      count = 0
    }
    lastWrittenFeatureName.set(lastFeature)
    featuresWritten.set(count)

    String suffix = count > 0 ? "-${count}" : ''
    String fileName = "${feature}${suffix}.${comparison.fileExtension()}"
    dir.resolve(fileName)
  }

  private static Object readResource(Path resource, Comparison comparison) {
    File file = resource.toFile()
    if (!file.canRead()) {
      return ''
    }

    FileCleanupExtension.addPackageFile(file.name)
    LOG.debug('Restoring resources from {}', file.path)

    byte[] bytes = file.bytes
    Object restored = comparison.afterRestore(bytes)
    comparison.beforeComparison(restored)
  }

  private static String toKebabCase(String className) {
    if (!className) {
      return 'spec'
    }
    String withDashes = className.replaceAll(/(?<upper>[A-Z])/, '-$1')
    String lower = withDashes.toLowerCase(Locale.ENGLISH)
    lower.startsWith('-') ? lower.substring(1) : lower
  }

  private static String sanitizeFeatureName(String name) {
    if (!name) {
      return ''
    }
    String lower = name.toLowerCase(Locale.ENGLISH)
    String cleaned = lower.replaceAll(/[^a-z0-9]+/, '-')
    cleaned.replaceAll(/^-+|-+$/, '')
  }
}
