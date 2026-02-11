package mfabian.snappo

import mfabian.snappo.comparison.JsonComparison
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant

class JsonSnapshotSpec extends Specification {
  @TempDir Path tempDir

  private String currentFeatureName

  def setup() {
    FileSnapshots.snapshotsRoot = tempDir
    FileSnapshots.packageNameProvider = { 'com.example' }
    FileSnapshots.classNameProvider = { 'JsonSnapshotSpec' }
    FileSnapshots.featureName = { currentFeatureName ?: '' }
    System.clearProperty('snappo.snapshot.update')
    System.clearProperty('spock.snapshot.update')
  }

  def 'json snapshot ignores excluded properties'() {
    given:
    currentFeatureName = 'json snapshot ignores excluded properties'
    writeSnapshot('{"id":"1","name":"Alice"}')
    def comparison = new JsonComparison(excludedProperties: ['id'] as String[])

    when:
    FileSnapshots.assertSnapshot([id: '2', name: 'Alice'], comparison)

    then:
    noExceptionThrown()
  }

  def 'json snapshot fails when non-excluded property differs'() {
    given:
    currentFeatureName = 'json snapshot fails when non-excluded property differs'
    writeSnapshot('{"id":"1","name":"Alice"}')

    when:
    FileSnapshots.assertSnapshot([id: '1', name: 'Bob'], Comparisons.JSON)

    then:
    thrown(AssertionError)
  }

  def 'json snapshot ignores excluded types'() {
    given:
    currentFeatureName = 'json snapshot ignores excluded types'
    writeSnapshot('{"name":"Alice"}')
    def comparison = new JsonComparison(excludedTypes: [Instant] as Class[])

    when:
    FileSnapshots.assertSnapshot([name: 'Alice', createdAt: Instant.parse('2026-01-01T00:00:00Z')], comparison)

    then:
    noExceptionThrown()
  }

  def 'json snapshot compares real-world API payloads while excluding volatile fields'() {
    given:
    currentFeatureName = 'json snapshot compares real-world API payloads while excluding volatile fields'
    writeSnapshot('''
      {
        "id": "order-1001",
        "createdAt": "2026-01-01T10:00:00Z",
        "lastModified": "2026-01-01T10:05:00Z",
        "status": "PAID",
        "customer": {
          "id": "customer-1",
          "name": "Alice"
        },
        "items": [
          { "id": "line-1", "sku": "A-1", "quantity": 2 },
          { "id": "line-2", "sku": "B-2", "quantity": 1 }
        ],
        "totals": {
          "net": 29.90,
          "tax": 5.68,
          "gross": 35.58
        }
      }
      '''.stripIndent())

    when:
    FileSnapshots.assertSnapshot('''
      {
        "id": "order-2002",
        "createdAt": "2026-02-10T08:00:00Z",
        "lastModified": "2026-02-10T08:04:00Z",
        "status": "PAID",
        "customer": {
          "id": "customer-9",
          "name": "Alice"
        },
        "items": [
          { "id": "line-9", "sku": "A-1", "quantity": 2 },
          { "id": "line-8", "sku": "B-2", "quantity": 1 }
        ],
        "totals": {
          "net": 29.90,
          "tax": 5.68,
          "gross": 35.58
        }
      }
      '''.stripIndent(), Comparisons.API_RESPONSE)

    then:
    noExceptionThrown()
  }

  private void writeSnapshot(String json) {
    Path file = FileSnapshots.packageDir().resolve("${sanitize(currentFeatureName)}.json")
    Files.createDirectories(file.parent)
    Files.write(file, json.getBytes(StandardCharsets.UTF_8))
  }

  private static String sanitize(String name) {
    name.toLowerCase(Locale.ENGLISH)
      .replaceAll(/[^a-z0-9]+/, '-')
      .replaceAll(/^-+|-+$/, '')
  }
}
