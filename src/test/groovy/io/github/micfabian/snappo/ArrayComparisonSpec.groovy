package io.github.micfabian.snappo

import io.github.micfabian.snappo.comparison.ArrayComparison
import spock.lang.Specification

class ArrayComparisonSpec extends Specification {
  def 'array comparison normalizes numbers and ignores values'() {
    given:
    def comparison = new ArrayComparison(
      rounding: 2,
      minValue: -10,
      maxValue: 10,
      ignoreValues: [0]
    )

    when:
    def csv = comparison.toCsv([[1.2345, 0, 100], [null, -100]])

    then:
    csv == '1.23,,10\n,-10'
  }

  def 'array comparison drops empty rows after normalization'() {
    given:
    def comparison = new ArrayComparison(ignoreValues: [0])

    when:
    def csv = comparison.toCsv([[0, 0], [1, 2]])

    then:
    csv == '1,2'
  }
}
