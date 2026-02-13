package io.github.micfabian.snappo.comparison

import io.github.micfabian.snappo.Comparison
import groovy.json.JsonGenerator
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year

class JsonComparison implements Comparison {
  private JsonGenerator jsonGenerator

  String[] excludedProperties = []
  Class[] excludedTypes = []

  Map<Class, Closure> converter = [
    (Instant): { Instant value -> value.toString() },
    (Year): { Year value -> value.toString() },
    (LocalDate): { LocalDate value -> value.toString() },
    (LocalDateTime): { LocalDateTime value -> value.toString() }
  ]

  protected Object ConfigureableResponseComparison(String... excluded) {
    excludedProperties = excluded
    excluded
  }

  @Override
  String fileExtension() {
    'json'
  }

  @Override
  Object beforeComparison(Object input) {
    String json
    if (input instanceof CharSequence) {
      json = toJson(new JsonSlurper().parseText(input.toString()))
    } else {
      json = toJson(input)
    }
    new JsonSlurper().parseText(json)
  }

  @Override
  byte[] beforeStore(Object input) {
    String json
    if (input instanceof String) {
      json = input
    } else {
      json = toJson(input)
    }
    JsonOutput.prettyPrint(json).getBytes(StandardCharsets.UTF_8)
  }

  @Override
  Object afterRestore(byte... bytes) {
    new JsonSlurper().parseText(new String(bytes, StandardCharsets.UTF_8))
  }

  private String toJson(Object value) {
    if (jsonGenerator == null) {
      JsonGenerator.Options options = new JsonGenerator.Options()
      if (excludedProperties) {
        options = options.excludeFieldsByName(excludedProperties as List)
      }
      if (excludedTypes) {
        options = options.excludeFieldsByType(excludedTypes as List)
      }
      converter.each { Class type, Closure closure ->
        options = options.addConverter(type, closure)
      }
      jsonGenerator = options.build()
    }
    jsonGenerator.toJson(value)
  }
}
