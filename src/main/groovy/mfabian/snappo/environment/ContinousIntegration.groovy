package mfabian.snappo.environment

class ContinousIntegration {
  static boolean isCi() {
    Map<String, String> env = System.getenv()
    return env.containsKey('CI') ||
      env.containsKey('GITHUB_ACTIONS') ||
      env.containsKey('GITLAB_CI') ||
      env.containsKey('JENKINS_URL') ||
      env.containsKey('BUILD_NUMBER') ||
      env.containsKey('TF_BUILD') ||
      env.containsKey('BUILDKITE')
  }
}
