package mfabian.snappo

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.SpecInfo

class FeatureNameExtension implements IGlobalExtension {
  private static final Logger LOG = LoggerFactory.getLogger(FeatureNameExtension)

  private static ThreadLocal<String> featureNameContext = new ThreadLocal<>()
  private static ThreadLocal<String> packageNameContext = new ThreadLocal<>()
  private static ThreadLocal<String> classNameContext = new ThreadLocal<>()

  static String getFeatureName() {
    featureNameContext.get()
  }

  static String getPackageName() {
    packageNameContext.get()
  }

  static String getClassName() {
    classNameContext.get()
  }

  @Override
  void start() {
  }

  @Override
  void visitSpec(SpecInfo spec) {
    spec.allFeatures.each { feature ->
      feature.featureMethod.addInterceptor(new MethodNameInterceptor())
    }
    spec.addSetupSpecInterceptor(new SetupSpecInterceptor())
  }

  @Override
  void stop() {
  }

  static class MethodNameInterceptor implements IMethodInterceptor {
    @Override
    void intercept(IMethodInvocation invocation) {
      String name = invocation?.feature?.name ?: invocation?.method?.name
      if (!name) {
        name = invocation?.method?.feature?.name
      }
      featureNameContext.set(name)
      invocation.proceed()
    }
  }

  static class SetupSpecInterceptor implements IMethodInterceptor {
    @Override
    void intercept(IMethodInvocation invocation) {
      Class<?> specClass = invocation?.spec?.reflection
      if (!specClass) {
        specClass = invocation?.method?.feature?.spec?.reflection
      }
      if (!specClass) {
        LOG.debug('Unable to resolve spec class for snapshot naming')
      } else {
        packageNameContext.set(specClass.package?.name ?: '')
        classNameContext.set(specClass.simpleName)
      }
      invocation.proceed()
    }
  }
}
