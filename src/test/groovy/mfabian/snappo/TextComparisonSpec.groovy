package mfabian.snappo

import mfabian.snappo.comparison.HtmlComparison
import mfabian.snappo.comparison.TextComparison
import spock.lang.Specification

class TextComparisonSpec extends Specification {
  def 'text comparison ignores whitespace by default'() {
    given:
    def comparison = new TextComparison()

    expect:
    comparison.beforeComparison('Hello World') == comparison.beforeComparison('Hello\n  World')
  }

  def 'text comparison can ignore case'() {
    given:
    def comparison = new TextComparison(ignoreCase: true)

    expect:
    comparison.beforeComparison('Hello') == comparison.beforeComparison('hello')
  }

  def 'html comparison preserves whitespace'() {
    given:
    def comparison = new HtmlComparison()

    expect:
    comparison.beforeComparison('a b') != comparison.beforeComparison('ab')
  }
}
