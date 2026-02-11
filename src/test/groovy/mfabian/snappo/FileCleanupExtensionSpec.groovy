package mfabian.snappo

import spock.lang.Specification
import spock.lang.TempDir

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class FileCleanupExtensionSpec extends Specification {
  @TempDir Path tempDir

  def setup() {
    FileSnapshots.snapshotsRoot = tempDir
    FileSnapshots.packageNameProvider = { 'com.example' }
    FileSnapshots.classNameProvider = { 'FileCleanupExtensionSpec' }
  }

  def 'cleanup deletes files not referenced when updating'() {
    given:
    Path dir = FileSnapshots.packageDir()
    Files.createDirectories(dir)
    Path keep = dir.resolve('keep.txt')
    Path remove = dir.resolve('remove.txt')
    Files.write(keep, 'keep'.getBytes(StandardCharsets.UTF_8))
    Files.write(remove, 'remove'.getBytes(StandardCharsets.UTF_8))
    def interceptor = new FileCleanupExtension.CleanupMethodInterceptor()

    when:
    interceptor.cleanupFiles(['keep.txt'], dir, true)

    then:
    Files.exists(keep)
    !Files.exists(remove)
  }

  def 'cleanup does nothing when not updating'() {
    given:
    Path dir = FileSnapshots.packageDir()
    Files.createDirectories(dir)
    Path keep = dir.resolve('keep.txt')
    Path remove = dir.resolve('remove.txt')
    Files.write(keep, 'keep'.getBytes(StandardCharsets.UTF_8))
    Files.write(remove, 'remove'.getBytes(StandardCharsets.UTF_8))
    def interceptor = new FileCleanupExtension.CleanupMethodInterceptor()

    when:
    interceptor.cleanupFiles(['keep.txt'], dir, false)

    then:
    Files.exists(keep)
    Files.exists(remove)
  }
}
