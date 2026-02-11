package de.xm.snappo

import de.xm.snappo.comparison.BinaryComparison
import de.xm.snappo.comparison.JsonComparison
import de.xm.snappo.comparison.PngComparison
import de.xm.snappo.comparison.TextComparison
import de.xm.snappo.comparison.XmlComparison

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
