package io.github.micfabian.snappo

import io.github.micfabian.snappo.comparison.ArrayComparison
import io.github.micfabian.snappo.comparison.BinaryComparison
import io.github.micfabian.snappo.comparison.CsvComparison
import io.github.micfabian.snappo.comparison.JsonComparison
import io.github.micfabian.snappo.comparison.PngComparison
import io.github.micfabian.snappo.comparison.TextComparison
import io.github.micfabian.snappo.comparison.XmlComparison
import groovy.json.JsonException
import groovy.json.JsonSlurper
import groovy.xml.XmlSlurper
import org.xml.sax.SAXException

import java.nio.charset.StandardCharsets

class ComparisonDetector {
  CanCompare[] compares = [
    new CanComparePng(),
    new CanCompareArray(),
    new CanCompareJson(),
    new CanCompareXml(this),
    new CanCompareCsv(),
    new CanCompareText(this)
  ]

  Comparison detect(Object input) {
    Comparison match = compares.collect { it.detect(input) }.find { it }
    if (match) {
      return match
    }
    if (input instanceof byte[]) {
      return new BinaryComparison('bin')
    }
    return new JsonComparison()
  }

  static class CanComparePng implements CanCompare {
    @Override
    Comparison detect(Object input) {
      if (!(input instanceof byte[])) {
        return null
      }
      byte[] bytes = (byte[]) input
      if (bytes.length >= 8) {
        byte[] signature = [
          (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47,
          (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A
        ]
        boolean matches = true
        for (int i = 0; i < signature.length; i++) {
          if (bytes[i] != signature[i]) {
            matches = false
            break
          }
        }
        if (matches) {
          return new PngComparison()
        }
      }
      null
    }
  }

  static class CanCompareArray implements CanCompare {
    @Override
    Comparison detect(Object input) {
      if (input instanceof byte[]) {
        return null
      }
      if (input instanceof Collection || input?.getClass()?.isArray()) {
        return new ArrayComparison()
      }
      null
    }
  }

  static class CanCompareJson implements CanCompare {
    private final JsonSlurper slurper = new JsonSlurper()

    @Override
    Comparison detect(Object input) {
      if (input instanceof String && isJson(input as String)) {
        return new JsonComparison()
      }
      null
    }

    boolean isJson(String text) {
      try {
        slurper.parseText(text)
        return true
      } catch (JsonException ignored) {
        return false
      }
    }
  }

  static class CanCompareXml implements CanCompare {
    private final XmlSlurper slurper = new XmlSlurper()

    CanCompareXml(ComparisonDetector owner) {
    }

    @Override
    Comparison detect(Object input) {
      if (input instanceof String && isXml(input as String)) {
        return new XmlComparison()
      }
      null
    }

    private boolean isXml(String text) {
      try {
        slurper.parseText(text)
        return true
      } catch (SAXException ignored) {
        return false
      }
    }
  }

  static class CanCompareCsv implements CanCompare {
    private final CsvComparison comparison = new CsvComparison()

    @Override
    Comparison detect(Object input) {
      if (!(input instanceof String)) {
        return null
      }

      String text = input as String
      try {
        List<List<String>> rows = comparison.beforeComparison(text) as List<List<String>>
        if (rows.isEmpty()) {
          return null
        }

        boolean multipleColumns = rows.any { List<String> row -> row.size() > 1 }
        boolean multipleRows = rows.size() > 1
        if (multipleColumns && (multipleRows || text.contains('\n') || text.contains('\r'))) {
          return new CsvComparison()
        }
      } catch (RuntimeException ignored) {
        return null
      }
      null
    }
  }

  static class CanCompareText implements CanCompare {
    CanCompareText(ComparisonDetector owner) {
    }

    @Override
    Comparison detect(Object input) {
      if (input instanceof String && isText((input as String).getBytes(StandardCharsets.UTF_8))) {
        return new TextComparison()
      }
      null
    }

    boolean isText(byte... bytes) {
      String text = new String(bytes, StandardCharsets.UTF_8)
      if (text.length() > 4) {
        return text.substring(0, 4).matches(/[A-Za-z0-9]+/)
      }
      false
    }
  }
}
