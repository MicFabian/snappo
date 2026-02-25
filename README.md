# Snappo

Snapshot testing for Spock specs in Groovy/Java projects.

Snappo keeps snapshots in files, compares current test output against those files, and gives you controlled update workflows for intentional changes.

## What You Can Do
- Snapshot plain text, JSON, XML, PNG, binary content, arrays, lists, and arbitrary objects.
- Auto-detect comparison type from the actual value.
- Use named snapshots when you need multiple snapshots in one test.
- Update snapshots globally (CLI flag) or locally (single call/block).
- Exclude volatile JSON fields or JSON value types from comparisons.

## Install

Use Maven Central for stable releases:

```groovy
repositories {
  mavenCentral()
}

dependencies {
  testImplementation 'io.github.micfabian:snappo:1.0.4' // replace with latest
  testImplementation 'org.spockframework:spock-core:2.4-groovy-5.0'
}
```

Optional snapshot repository:

```groovy
repositories {
  mavenCentral()
  maven { url = uri('https://central.sonatype.com/repository/maven-snapshots/') }
}
```

## Quick Start

```groovy
import io.github.micfabian.snappo.Comparisons
import io.github.micfabian.snappo.Snappo
import spock.lang.Specification

class ApiSpec extends Specification {
  def 'response matches snapshot'() {
    when:
    def result = service.fetch()

    then:
    Snappo.expect(result, Comparisons.JSON)
  }
}
```

Behavior:
- First run: missing snapshot file is created.
- Next runs: content is compared against the stored snapshot.
- Mismatch: test fails unless update mode is enabled.

## Update Snapshots

Enable update mode for the full test run:

```bash
SNAPPO_UPDATE=true ./gradlew test
```

or

```bash
./gradlew test -Dsnappo.snapshot.update=true
```

Legacy compatibility flags are also supported:
- `SPOCK_UPDATE=true`
- `-Dspock.snapshot.update=true`

Update only one snapshot in code:

```groovy
Snappo.updateSnapshot(result, Comparisons.JSON)
```

Update only within a block:

```groovy
Snappo.withUpdate {
  Snappo.expect(result, Comparisons.JSON)
}
```

## Named Snapshots

Use named snapshots when you are outside Spock feature methods, or when you need multiple snapshots in one feature.

```groovy
Snappo.expectNamed('users list', users, Comparisons.JSON)
Snappo.snapshotNamed('raw payload', payload, Comparisons.TXT)
Snappo.updateSnapshotNamed('normalized payload', payload, Comparisons.TXT)
```

## Comparison Modes

### Auto-detection

```groovy
Snappo.expect(anyValue)
```

Detection behavior:
- JSON string -> JSON comparison
- XML string -> XML comparison
- `byte[]` with PNG signature -> PNG comparison
- collection/array -> array comparison
- other `byte[]` -> binary comparison
- fallback -> object-as-JSON comparison

### Built-in comparisons

- `Comparisons.JSON`
- `Comparisons.OBJECT_AS_JSON`
- `Comparisons.API_RESPONSE` (ignores `id`, `createdAt`, `lastModified`)
- `Comparisons.XML`
- `Comparisons.TXT`
- `Comparisons.PNG`
- `Comparisons.BINARY`

### JSON helpers

```groovy
Snappo.expect(data, Comparisons.jsonExcludingProperties('id', 'createdAt'))
Snappo.expect(data, Comparisons.jsonExcludingTypes(java.time.Instant))
```

You can also call the detector directly:

```groovy
Snappo.expect(data, Comparisons.detect(data))
```

### PNG comparison mode

```groovy
import io.github.micfabian.snappo.comparison.PngComparison

Snappo.expect(imageBytes, Comparisons.png(PngComparison.MODE.SIZE))
Snappo.expect(imageBytes, Comparisons.png(PngComparison.MODE.PIXEL))
```

### Array normalization

```groovy
import io.github.micfabian.snappo.comparison.ArrayComparison

Snappo.expect(values, new ArrayComparison(rounding: 2))
```

### Custom comparison classes

```groovy
import io.github.micfabian.snappo.comparison.HtmlComparison
import io.github.micfabian.snappo.comparison.TextComparison

Snappo.expect(html, new HtmlComparison())                                // *.html snapshot
Snappo.expect(message, new TextComparison(ignoreWhitespace: false))      // strict whitespace
```

## Snapshot File Layout

Default root:

```text
src/test/resources/snapshots
```

Default path format:

```text
src/test/resources/snapshots/<package>/<class-kebab>/<feature>.ext
```

Naming behavior:
- Feature names are sanitized to kebab-case.
- Multiple snapshots with the same feature name get `-1`, `-2`, ... suffixes.
- Class folder is derived from the spec/test class name (`MyGreatSpec` -> `my-great-spec`).

## Configuration

Override snapshot root directory:

```bash
./gradlew test -Dspock.snapshot.dir=build/test-snapshots
```

or in code:

```groovy
System.setProperty('spock.snapshot.dir', 'build/test-snapshots')
```

## API Reference

Primary API is `Snappo`:
- `expect(actual)`
- `expect(actual, comparison)`
- `expectNamed(name, actual)`
- `expectNamed(name, actual, comparison)`
- `snapshot(actual)`
- `snapshot(actual, comparison)`
- `snapshotNamed(name, actual)`
- `snapshotNamed(name, actual, comparison)`
- `updateSnapshot(actual)`
- `updateSnapshot(actual, comparison)`
- `updateSnapshotNamed(name, actual)`
- `updateSnapshotNamed(name, actual, comparison)`
- `withUpdate { ... }`
- `withUpdate(enabled) { ... }`

`FileSnapshots` still exists as a deprecated compatibility alias for older code.

## Publish To Maven Central (Maintainers)

### Local publish prerequisites
- Sonatype Central Portal token
- OpenPGP keypair
- Public key uploaded to at least one public key server supported by Sonatype
- Gradle properties for Sonatype + signing credentials

Example `~/.gradle/gradle.properties`:

```properties
sonatypeUsername=YOUR_CENTRAL_USERNAME
sonatypePassword=YOUR_CENTRAL_PASSWORD
signingKey=-----BEGIN PGP PRIVATE KEY BLOCK-----\n...\n-----END PGP PRIVATE KEY BLOCK-----
signingPassword=YOUR_KEY_PASSPHRASE

POM_NAME=snappo
POM_DESCRIPTION=Spock snapshot testing helpers for Groovy/Java projects
POM_URL=https://github.com/MicFabian/snappo
POM_LICENSE_NAME=The Apache License, Version 2.0
POM_LICENSE_URL=https://www.apache.org/licenses/LICENSE-2.0.txt
POM_DEVELOPER_ID=micfabian
POM_DEVELOPER_NAME=Michael Fabian
POM_DEVELOPER_EMAIL=mvirks@web.de
POM_SCM_CONNECTION=scm:git:https://github.com/MicFabian/snappo.git
POM_SCM_DEV_CONNECTION=scm:git:ssh://git@github.com:MicFabian/snappo.git
POM_SCM_URL=https://github.com/MicFabian/snappo
```

Publish release:

```bash
./gradlew publishToMavenCentral -PreleaseVersion=1.0.5
```

Publish snapshot:

```bash
./gradlew publishToMavenCentral -PreleaseVersion=1.0.6-SNAPSHOT
```

If Sonatype fails closing the staging repository with "Could not find a public key by the key fingerprint", upload your public key to supported key servers and wait for propagation before retrying.

## GitHub Actions (CI + Release)

### CI workflow
`.github/workflows/ci.yml`:
- runs on push to `main`
- runs on pull requests
- validates Gradle wrapper
- runs `./gradlew --no-daemon test`

### Release workflow
`.github/workflows/release.yml`:
- runs on tag push `v*` (example: `v1.0.5`)
- can also run manually (`workflow_dispatch`) with `version`
- validates version format
- runs `./gradlew --no-daemon clean test -PreleaseVersion=<version>`
- publishes with `./gradlew --no-daemon publishToMavenCentral -PreleaseVersion=<version>`

Required GitHub repository secrets:
- `SONATYPE_USERNAME`
- `SONATYPE_PASSWORD`
- `SIGNING_KEY`
- `SIGNING_PASSWORD`

`SIGNING_KEY_ID` is optional.

### Triggering a release

```bash
git tag -a v1.0.5 -m "Release 1.0.5"
git push origin v1.0.5
```

or manual dispatch:

```bash
gh workflow run Release --ref main -f version=1.0.5
```

## Notes
- Spock global extensions are auto-registered via `META-INF/services`.
- This project targets Groovy 5 and Spock 2.4.
