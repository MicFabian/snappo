package io.github.micfabian.snappo.comparison

import io.github.micfabian.snappo.Comparison
import groovy.xml.XmlSlurper
import groovy.xml.XmlUtil
import groovy.xml.slurpersupport.GPathResult

import java.nio.charset.StandardCharsets

class XmlComparison implements Comparison {
  @Override
  String fileExtension() {
    'xml'
  }

  @Override
  Object beforeComparison(Object input) {
    if (input instanceof GPathResult) {
      return input
    }
    if (!(input instanceof String)) {
      throw new RuntimeException("XmlComparison input must provide XML as String, input was ${input?.class}")
    }
    new XmlSlurper().parseText(input as String)
  }

  @Override
  byte[] beforeStore(Object input) {
    def parsed = new XmlSlurper().parseText(String.valueOf(input))
    XmlUtil.serialize(parsed).getBytes(StandardCharsets.UTF_8)
  }

  @Override
  Object afterRestore(byte... bytes) {
    new XmlSlurper().parseText(new String(bytes, StandardCharsets.UTF_8))
  }
}
