package io.github.micfabian.snappo

import io.github.micfabian.snappo.comparison.CsvComparison
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class CsvComparisonSpec extends Specification {
  def 'csv comparison parses records from text'() {
    given:
    def comparison = new CsvComparison()

    when:
    def rows = comparison.beforeComparison("name,age\r\nAlice,42")

    then:
    rows == [['name', 'age'], ['Alice', '42']]
  }

  def 'csv comparison stores canonical csv'() {
    given:
    def comparison = new CsvComparison()

    when:
    def csv = new String(comparison.beforeStore('"name","age"\r\n"Alice","42"'), StandardCharsets.UTF_8)

    then:
    csv == 'name,age\nAlice,42'
  }

  def 'csv comparison handles quoted values without external libraries'() {
    given:
    def comparison = new CsvComparison()

    when:
    def rows = comparison.beforeComparison('"last, first","He said ""hi"""')

    then:
    rows == [['last, first', 'He said "hi"']]
  }
}
