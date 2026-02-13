package io.github.micfabian.snappo.comparison

class HtmlComparison extends TextComparison {
  HtmlComparison() {
    ignoreCase = false
    ignoreWhitespace = false
  }

  @Override
  String fileExtension() {
    'html'
  }
}
