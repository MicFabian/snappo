package io.github.micfabian.snappo

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.SpecInfo

import java.nio.file.Files
import java.nio.file.Path

class FileCleanupExtension implements IGlobalExtension {
  private static final Logger LOG = LoggerFactory.getLogger(FileCleanupExtension)

  private static ThreadLocal<List<String>> classSpecifications = new ThreadLocal<>()

  @Override
  void start() {
  }

  static void addPackageFile(String fileName) {
    List<String> specs = classSpecifications.get()
    if (!specs) {
      specs = []
    }
    specs = specs + fileName
    classSpecifications.set(specs)
  }

  @Override
  void visitSpec(SpecInfo spec) {
    spec.addCleanupSpecInterceptor(new CleanupMethodInterceptor())
  }

  @Override
  void stop() {
  }

  static class CleanupMethodInterceptor implements IMethodInterceptor {
    @Override
    void intercept(IMethodInvocation invocation) throws Throwable {
      List<String> specs = classSpecifications.get() ?: []
      Path packageDir = FileSnapshots.packageDir()
      boolean update = FileSnapshots.isUpdating()
      cleanupFiles(specs, packageDir, update)
      classSpecifications.remove()
    }

    protected void cleanupFiles(List<String> keep, Path packageDir, boolean update) {
      if (!update) {
        LOG.debug('Not in UPDATE mode, no files are deleted')
        return
      }
      File dir = packageDir?.toFile()
      if (!dir || !dir.exists()) {
        return
      }
      Set<String> keepNames = new HashSet<>(keep)
      List<Path> files = (dir.listFiles() ?: [])
        .findAll { File f -> f.isFile() }
        .collect { File f -> f.toPath() }

      List<Path> deletions = files.findAll { Path path -> !keepNames.contains(path.fileName.toString()) }
      deletions.each { Path path ->
        Files.deleteIfExists(path)
      }
      LOG.warn('Deleted {}', deletions)
    }
  }
}
