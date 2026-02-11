package mfabian.snappo.comparison

import javax.imageio.ImageIO
import java.awt.image.DataBufferByte
import java.util.Base64
import java.io.ByteArrayInputStream

class PngComparison extends BinaryComparison {
  enum MODE { PIXEL, SIZE }

  static final Object PIXEL = MODE.PIXEL
  static final Object SIZE = MODE.SIZE

  MODE comparisonMode = MODE.SIZE

  PngComparison() {
    super('png')
  }

  PngComparison(MODE mode) {
    super('png')
    comparisonMode = mode
  }

  static PngComparison withMode(MODE mode) {
    new PngComparison(mode)
  }

  @Override
  Object beforeComparison(Object input) {
    def image = ImageIO.read(new ByteArrayInputStream((byte[]) input))
    if (comparisonMode == MODE.PIXEL) {
      def buffer = (DataBufferByte) image.raster.dataBuffer
      return Base64.encoder.encodeToString(buffer.data)
    }
    if (comparisonMode == MODE.SIZE) {
      return new Result(width: image.width, height: image.height)
    }
    throw new RuntimeException("Unable to compare image in mode ${comparisonMode}")
  }

  static class Result {
    int width
    int height

    @Override
    boolean equals(Object other) {
      if (this.is(other)) {
        return true
      }
      if (!(other instanceof Result)) {
        return false
      }
      Result result = (Result) other
      width == result.width && height == result.height
    }

    @Override
    int hashCode() {
      31 * width + height
    }

    String toString() {
      "Result(width=${width}, height=${height})"
    }
  }
}
