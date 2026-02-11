package de.xm.snappo

import de.xm.snappo.comparison.JsonComparison
import spock.lang.Specification

class ComparisonsSpec extends Specification {
  def 'json exclusions are wired into comparisons helper'() {
    when:
    JsonComparison comparison = Comparisons.jsonExcludingProperties('id', 'createdAt') as JsonComparison

    then:
    comparison.excludedProperties.toList() == ['id', 'createdAt']
  }

  def 'json exclusions by type are wired into comparisons helper'() {
    when:
    JsonComparison comparison = Comparisons.jsonExcludingTypes(UUID) as JsonComparison

    then:
    comparison.excludedTypes.toList() == [UUID]
  }

  def 'api response comparison excludes expected fields'() {
    expect:
    (Comparisons.API_RESPONSE as JsonComparison).excludedProperties.toList() == ['id', 'createdAt', 'lastModified']
  }
}
