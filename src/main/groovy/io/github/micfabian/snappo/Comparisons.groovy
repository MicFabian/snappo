package io.github.micfabian.snappo

import io.github.micfabian.snappo.comparison.BinaryComparison
import io.github.micfabian.snappo.comparison.JsonComparison
import io.github.micfabian.snappo.comparison.PngComparison
import io.github.micfabian.snappo.comparison.TextComparison
import io.github.micfabian.snappo.comparison.XmlComparison

class Comparisons {
  private static final ComparisonDetector DETECTOR = new ComparisonDetector()

  static final Comparison PNG = new PngComparison()
  static final Comparison OBJECT_AS_JSON = new JsonComparison()
  static final Comparison JSON = new JsonComparison()
  static final Comparison API_RESPONSE = new JsonComparison(excludedProperties: ['id', 'createdAt', 'lastModified'] as String[])
  static final Comparison BINARY = new BinaryComparison('bin')
  static final Comparison XML = new XmlComparison()
  static final Comparison TXT = new TextComparison()

  static Comparison png(PngComparison.MODE mode) {
    new PngComparison(mode)
  }

  static Comparison jsonExcludingProperties(String... props) {
    new JsonComparison(excludedProperties: props)
  }

  static Comparison jsonExcludingTypes(Class... types) {
    new JsonComparison(excludedTypes: types)
  }

  static Comparison detect(Object input) {
    DETECTOR.detect(input)
  }
}
