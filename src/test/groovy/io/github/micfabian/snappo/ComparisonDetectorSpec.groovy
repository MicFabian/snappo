package io.github.micfabian.snappo

import io.github.micfabian.snappo.comparison.ArrayComparison
import io.github.micfabian.snappo.comparison.BinaryComparison
import io.github.micfabian.snappo.comparison.JsonComparison
import io.github.micfabian.snappo.comparison.PngComparison
import io.github.micfabian.snappo.comparison.TextComparison
import io.github.micfabian.snappo.comparison.XmlComparison
import spock.lang.Specification

class ComparisonDetectorSpec extends Specification {
  def 'detects json xml and text'() {
    given:
    def detector = new ComparisonDetector()

    expect:
    detector.detect('{"a":1}') instanceof JsonComparison
    detector.detect('<root><a>1</a></root>') instanceof XmlComparison
    detector.detect('plain text') instanceof TextComparison
  }

  def 'detects arrays png and binary'() {
    given:
    def detector = new ComparisonDetector()
    byte[] png = [
      (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47,
      (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A,
      (byte) 0x00
    ]

    expect:
    detector.detect([1, 2, 3]) instanceof ArrayComparison
    detector.detect(png) instanceof PngComparison
    detector.detect([0x01, 0x02] as byte[]) instanceof BinaryComparison
  }

  def 'comparisons detect delegates to the detector'() {
    expect:
    Comparisons.detect('{"a":1}') instanceof JsonComparison
  }
}
