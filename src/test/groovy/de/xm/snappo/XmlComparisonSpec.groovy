package de.xm.snappo

import de.xm.snappo.comparison.XmlComparison
import groovy.xml.XmlSlurper
import spock.lang.Specification

class XmlComparisonSpec extends Specification {
  def 'xml comparison accepts gpath results'() {
    given:
    def comparison = new XmlComparison()
    def parsed = new XmlSlurper().parseText('<root><a>1</a></root>')

    when:
    def result = comparison.beforeComparison(parsed)

    then:
    result.is(parsed)
  }

  def 'xml comparison rejects non-string input'() {
    given:
    def comparison = new XmlComparison()

    when:
    comparison.beforeComparison(123)

    then:
    thrown(RuntimeException)
  }
}
