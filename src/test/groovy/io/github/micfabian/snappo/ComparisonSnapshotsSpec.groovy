package io.github.micfabian.snappo

import io.github.micfabian.snappo.comparison.ArrayComparison
import io.github.micfabian.snappo.comparison.BinaryComparison
import io.github.micfabian.snappo.comparison.HtmlComparison
import io.github.micfabian.snappo.comparison.JsonComparison
import io.github.micfabian.snappo.comparison.PngComparison
import io.github.micfabian.snappo.comparison.TextComparison
import io.github.micfabian.snappo.comparison.XmlComparison
import spock.lang.Specification
import spock.lang.TempDir

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path

class ComparisonSnapshotsSpec extends Specification {
  @TempDir Path tempDir

  private String currentFeatureName

  def setup() {
    FileSnapshots.snapshotsRoot = tempDir
    FileSnapshots.packageNameProvider = { 'com.example' }
    FileSnapshots.classNameProvider = { 'ComparisonSnapshotsSpec' }
    FileSnapshots.featureName = { currentFeatureName ?: '' }
    System.clearProperty('snappo.snapshot.update')
    System.clearProperty('spock.snapshot.update')
  }

  def 'text comparison matches snapshot'() {
    given:
    def comparison = new TextComparison()
    writeSnapshot('text comparison matches snapshot', comparison, 'hello world')

    when:
    FileSnapshots.assertSnapshot('hello   world', comparison)

    then:
    noExceptionThrown()
  }

  def 'html comparison preserves whitespace'() {
    given:
    def comparison = new HtmlComparison()
    writeSnapshot('html comparison preserves whitespace', comparison, '<div>a b</div>')

    when:
    FileSnapshots.assertSnapshot('<div>a b</div>', comparison)

    then:
    noExceptionThrown()
  }

  def 'xml comparison matches snapshot'() {
    given:
    def comparison = new XmlComparison()
    writeSnapshot('xml comparison matches snapshot', comparison, '<root><a>1</a></root>')

    when:
    FileSnapshots.assertSnapshot('<root><a>1</a></root>', comparison)

    then:
    noExceptionThrown()
  }

  def 'json comparison excludes properties in real snapshot'() {
    given:
    def comparison = new JsonComparison(excludedProperties: ['id'] as String[])
    writeSnapshot('json comparison excludes properties in real snapshot', comparison, [id: '1', name: 'Alice'])

    when:
    FileSnapshots.assertSnapshot([id: '2', name: 'Alice'], comparison)

    then:
    noExceptionThrown()
  }

  def 'array comparison matches snapshot'() {
    given:
    def comparison = new ArrayComparison(rounding: 2)
    writeSnapshot('array comparison matches snapshot', comparison, [[1.234, 2.345], [3.456, 4.567]])

    when:
    FileSnapshots.assertSnapshot([[1.2344, 2.3454], [3.4564, 4.5674]], comparison)

    then:
    noExceptionThrown()
  }

  def 'binary comparison matches snapshot'() {
    given:
    def comparison = new BinaryComparison('bin')
    byte[] bytes = [0x01, 0x02, 0x03] as byte[]
    writeSnapshot('binary comparison matches snapshot', comparison, bytes)

    when:
    FileSnapshots.assertSnapshot(bytes, comparison)

    then:
    noExceptionThrown()
  }

  def 'png comparison matches snapshot in size mode'() {
    given:
    def comparison = new PngComparison(PngComparison.MODE.SIZE)
    byte[] png = createPng(3, 4)
    writeSnapshot('png comparison matches snapshot in size mode', comparison, png)

    when:
    FileSnapshots.assertSnapshot(png, comparison)

    then:
    noExceptionThrown()
  }

  private void writeSnapshot(String featureName, def comparison, Object value) {
    currentFeatureName = featureName
    Path file = FileSnapshots.packageDir().resolve("${sanitize(featureName)}.${comparison.fileExtension()}")
    Files.createDirectories(file.parent)
    Files.write(file, comparison.beforeStore(value))
  }

  private static String sanitize(String name) {
    name.toLowerCase(Locale.ENGLISH)
      .replaceAll(/[^a-z0-9]+/, '-')
      .replaceAll(/^-+|-+$/, '')
  }

  private static byte[] createPng(int width, int height) {
    def image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
    def out = new ByteArrayOutputStream()
    ImageIO.write(image, 'png', out)
    out.toByteArray()
  }
}
