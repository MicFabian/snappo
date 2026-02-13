package io.github.micfabian.snappo

import spock.lang.Specification
import spock.lang.TempDir

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class SnappoSpec extends Specification {
  @TempDir Path tempDir

  private String currentFeatureName

  def setup() {
    FileSnapshots.snapshotsRoot = tempDir
    FileSnapshots.packageNameProvider = { 'com.example' }
    FileSnapshots.classNameProvider = { 'SnappoSpec' }
    FileSnapshots.featureName = { currentFeatureName ?: '' }
    System.clearProperty('snappo.snapshot.update')
    System.clearProperty('spock.snapshot.update')
  }

  def 'snappo expect reads snapshot'() {
    given:
    currentFeatureName = 'snappo expect reads snapshot'
    Path file = FileSnapshots.packageDir().resolve('snappo-expect-reads-snapshot.txt')
    Files.createDirectories(file.parent)
    Files.write(file, 'ok'.getBytes(StandardCharsets.UTF_8))

    when:
    Snappo.expect('ok', Comparisons.TXT)

    then:
    noExceptionThrown()
  }

  def 'snappo updateSnapshot writes file'() {
    given:
    currentFeatureName = 'snappo updateSnapshot writes file'

    when:
    Snappo.updateSnapshot('fresh', Comparisons.TXT)

    then:
    Path file = FileSnapshots.packageDir().resolve('snappo-updatesnapshot-writes-file.txt')
    new String(Files.readAllBytes(file), StandardCharsets.UTF_8) == 'fresh'
  }

  def 'snappo expectNamed uses explicit name'() {
    when:
    Snappo.withUpdate(true) {
      Snappo.expectNamed('Named Snapshot', 'value', Comparisons.TXT)
    }

    then:
    Path file = FileSnapshots.packageDir().resolve('named-snapshot.txt')
    Files.exists(file)
  }
}
