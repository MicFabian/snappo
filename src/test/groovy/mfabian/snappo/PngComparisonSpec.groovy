package mfabian.snappo

import mfabian.snappo.comparison.PngComparison
import spock.lang.Specification

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

class PngComparisonSpec extends Specification {
  def 'png comparison size mode returns dimensions'() {
    given:
    byte[] png = createPng(2, 3)
    def comparison = new PngComparison(PngComparison.MODE.SIZE)

    when:
    def result = comparison.beforeComparison(png) as PngComparison.Result

    then:
    result.width == 2
    result.height == 3
  }

  def 'png comparison pixel mode returns data'() {
    given:
    byte[] png = createPng(1, 1)
    def comparison = new PngComparison(PngComparison.MODE.PIXEL)

    when:
    def result = comparison.beforeComparison(png)

    then:
    result instanceof String
    result.length() > 0
  }

  private static byte[] createPng(int width, int height) {
    def image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
    def out = new ByteArrayOutputStream()
    ImageIO.write(image, 'png', out)
    out.toByteArray()
  }
}
