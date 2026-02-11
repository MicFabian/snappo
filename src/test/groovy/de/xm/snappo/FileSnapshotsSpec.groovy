package de.xm.snappo

import spock.lang.Specification
import spock.lang.TempDir

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class FileSnapshotsSpec extends Specification {
  @TempDir Path tempDir

  private String currentFeatureName

  def setup() {
    FileSnapshots.snapshotsRoot = tempDir
    FileSnapshots.packageNameProvider = { 'com.example' }
    FileSnapshots.classNameProvider = { 'FileSnapshotsSpec' }
    FileSnapshots.featureName = { currentFeatureName ?: '' }
    System.clearProperty('snappo.snapshot.update')
    System.clearProperty('spock.snapshot.update')
  }

  def 'reads existing snapshot'() {
    given:
    currentFeatureName = 'reads existing snapshot'
    Path file = FileSnapshots.packageDir().resolve('reads-existing-snapshot.txt')
    Files.createDirectories(file.parent)
    Files.write(file, 'hello'.getBytes(StandardCharsets.UTF_8))

    when:
    FileSnapshots.assertSnapshot('hello', Comparisons.TXT)

    then:
    noExceptionThrown()
  }

  def 'updateSnapshot overwrites existing snapshot'() {
    given:
    currentFeatureName = 'update snapshot overwrites existing snapshot'
    Path file = FileSnapshots.packageDir().resolve('update-snapshot-overwrites-existing-snapshot.txt')
    Files.createDirectories(file.parent)
    Files.write(file, 'old'.getBytes(StandardCharsets.UTF_8))

    when:
    FileSnapshots.updateSnapshot('new', Comparisons.TXT)

    then:
    new String(Files.readAllBytes(file), StandardCharsets.UTF_8) == 'new'
  }

  def 'assertSnapshotNamed uses explicit name'() {
    given:
    currentFeatureName = ''

    when:
    FileSnapshots.withUpdate(true) {
      FileSnapshots.assertSnapshotNamed('Explicit Name', 'value', Comparisons.TXT)
    }

    then:
    Path file = FileSnapshots.packageDir().resolve('explicit-name.txt')
    Files.exists(file)
  }

  def 'withUpdate is scoped to the block'() {
    expect:
    !FileSnapshots.isUpdating()

    when:
    def inside = FileSnapshots.withUpdate(true) {
      FileSnapshots.isUpdating()
    }

    then:
    inside
    !FileSnapshots.isUpdating()
  }
}
