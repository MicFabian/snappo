package io.github.micfabian.snappo

import org.spockframework.runtime.extension.IMethodInvocation
import spock.lang.Specification

class FeatureNameExtensionSpec extends Specification {
  def 'method interceptor resolves class context from invocation instance fallback'() {
    given:
    def interceptor = new FeatureNameExtension.MethodNameInterceptor()
    def invocation = Mock(IMethodInvocation)
    invocation.spec >> null
    invocation.feature >> null
    invocation.method >> null
    invocation.instance >> this

    when:
    interceptor.intercept(invocation)

    then:
    FeatureNameExtension.className == 'FeatureNameExtensionSpec'
    FeatureNameExtension.packageName == 'io.github.micfabian.snappo'
    1 * invocation.proceed()
  }
}
