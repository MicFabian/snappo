package mfabian.snappo

import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

class FileSnapshotsNamingSpec extends Specification {
  @TempDir Path tempDir

  private String currentFeatureName

  def setup() {
    FileSnapshots.snapshotsRoot = tempDir
    FileSnapshots.packageNameProvider = { 'com.acme.snap' }
    FileSnapshots.classNameProvider = { 'FancySpec' }
    FileSnapshots.featureName = { currentFeatureName ?: '' }
    System.clearProperty('snappo.snapshot.update')
    System.clearProperty('spock.snapshot.update')
  }

  def 'package dir uses package and class names'() {
    expect:
    FileSnapshots.packageDir() == tempDir.resolve('com/acme/snap/fancy-spec')
  }

  def 'feature name sanitized and suffixed'() {
    given:
    currentFeatureName = 'User list / v2'

    when:
    FileSnapshots.withUpdate(true) {
      FileSnapshots.assertSnapshot('one', Comparisons.TXT)
      FileSnapshots.assertSnapshot('two', Comparisons.TXT)
    }

    then:
    Path dir = FileSnapshots.packageDir()
    Files.exists(dir.resolve('user-list-v2.txt'))
    Files.exists(dir.resolve('user-list-v2-1.txt'))
  }
}
