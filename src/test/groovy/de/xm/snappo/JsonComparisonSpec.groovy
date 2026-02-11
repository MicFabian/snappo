package de.xm.snappo

import de.xm.snappo.comparison.JsonComparison
import spock.lang.Specification

import java.time.Instant

class JsonComparisonSpec extends Specification {
  static class Sample {
    String id
    String name
    Instant createdAt
  }

  def 'json comparison excludes properties'() {
    given:
    def comparison = new JsonComparison(excludedProperties: ['id'] as String[])
    def sample = new Sample(id: '1', name: 'demo', createdAt: Instant.parse('2024-01-01T00:00:00Z'))

    when:
    def result = comparison.beforeComparison(sample) as Map

    then:
    !result.containsKey('id')
    result.name == 'demo'
  }

  def 'json comparison uses converters for time types'() {
    given:
    def comparison = new JsonComparison()
    def sample = new Sample(id: '1', name: 'demo', createdAt: Instant.parse('2024-01-01T00:00:00Z'))

    when:
    def result = comparison.beforeComparison(sample) as Map

    then:
    result.createdAt == '2024-01-01T00:00:00Z'
  }

  def 'json comparison excludes types'() {
    given:
    def comparison = new JsonComparison(excludedTypes: [Instant] as Class[])
    def sample = new Sample(id: '1', name: 'demo', createdAt: Instant.parse('2024-01-01T00:00:00Z'))

    when:
    def result = comparison.beforeComparison(sample) as Map

    then:
    !result.containsKey('createdAt')
  }
}
