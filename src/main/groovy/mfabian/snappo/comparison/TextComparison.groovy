package mfabian.snappo.comparison

import mfabian.snappo.Comparison

import java.nio.charset.StandardCharsets

class TextComparison implements Comparison {
  boolean ignoreWhitespace = true
  boolean ignoreCase = false

  @Override
  String fileExtension() {
    'txt'
  }

  @Override
  Object beforeComparison(Object input) {
    String text = String.valueOf(input)
    if (ignoreWhitespace) {
      text = text.replaceAll(/\s+/, '')
    }
    if (ignoreCase) {
      text = text.toLowerCase(Locale.ROOT)
    }
    text
  }

  @Override
  byte[] beforeStore(Object input) {
    String.valueOf(input).getBytes(StandardCharsets.UTF_8)
  }

  @Override
  Object afterRestore(byte... bytes) {
    new String(bytes, StandardCharsets.UTF_8)
  }
}
