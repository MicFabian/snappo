# Snappo (Groovy/Spock Snapshots)

A snapshot testing helper for Spock specs, updated for Groovy 5 + the latest Spock and Gradle. It is designed to be simple in tests and easy to update snapshots when needed.

## Highlights
- Global Spock extension captures spec/class/feature names automatically.
- Snapshots are stored in `src/test/resources/snapshots/<package>/<class-kebab>/`.
- Update mode via `SNAPPO_UPDATE=true` or `-Dsnappo.snapshot.update=true` (also supports the legacy `SPOCK_UPDATE` and `-Dspock.snapshot.update`).
- A tiny facade (`Snappo`) with `expect`, `snapshot`, `updateSnapshot`, and `withUpdate` helpers.
- JSON/XML/TXT/PNG/BINARY/array comparisons with JSON helpers for exclusions.

## Install (Gradle)
For releases published to Maven Central:

```groovy
repositories {
  mavenCentral()
}

dependencies {
  testImplementation 'io.github.micfabian:snappo:1.0.1'
  testImplementation 'org.spockframework:spock-core:2.4-groovy-5.0'
}
```

For snapshots from Central Portal:

```groovy
repositories {
  mavenCentral()
  maven { url = uri('https://central.sonatype.com/repository/maven-snapshots/') }
}

dependencies {
  testImplementation 'io.github.micfabian:snappo:1.0.1-SNAPSHOT'
}
```

## Usage
In your Spock spec:

```groovy
import io.github.micfabian.snappo.Snappo
import io.github.micfabian.snappo.Comparisons

class ApiSpec extends Specification {
  def 'response matches snapshot'() {
    when:
    def result = service.fetch()

    then:
    Snappo.expect(result, Comparisons.JSON)
  }
}
```

### Updating snapshots
Run your tests with update mode enabled:

```
SNAPPO_UPDATE=true ./gradlew test
```

or

```
./gradlew test -Dsnappo.snapshot.update=true
```

You can also update a single snapshot in code:

```groovy
Snappo.updateSnapshot(result, Comparisons.JSON)
```

Or scope update mode for a block:

```groovy
Snappo.withUpdate {
  Snappo.expect(result, Comparisons.JSON)
}
```

### Named snapshots (non-Spock or multiple snapshots)
```groovy
Snappo.snapshotNamed('users list', result, Comparisons.JSON)
```

## Publish To Maven Central
1. Create a namespace and user token in Sonatype Central Portal.
2. Generate an ASCII-armored OpenPGP keypair and publish the public key.
3. Put credentials in `~/.gradle/gradle.properties`:

```properties
# Sonatype Central Portal token
sonatypeUsername=YOUR_CENTRAL_USERNAME
sonatypePassword=YOUR_CENTRAL_PASSWORD

# PGP signing (ASCII armored private key)
signingKeyId=YOUR_KEY_ID
signingKey=-----BEGIN PGP PRIVATE KEY BLOCK-----\n...\n-----END PGP PRIVATE KEY BLOCK-----
signingPassword=YOUR_KEY_PASSPHRASE

# POM metadata (replace placeholders)
POM_NAME=snappo
POM_DESCRIPTION=Spock snapshot testing helpers for Groovy/Java projects
POM_URL=https://github.com/MicFabian/snappo
POM_LICENSE_NAME=The Apache License, Version 2.0
POM_LICENSE_URL=https://www.apache.org/licenses/LICENSE-2.0.txt
POM_DEVELOPER_ID=your-id
POM_DEVELOPER_NAME=Your Name
POM_DEVELOPER_EMAIL=you@example.com
POM_SCM_CONNECTION=scm:git:https://github.com/MicFabian/snappo.git
POM_SCM_DEV_CONNECTION=scm:git:ssh://git@github.com:MicFabian/snappo.git
POM_SCM_URL=https://github.com/MicFabian/snappo
```

4. Publish:

```bash
# Snapshot version (e.g. 1.0.1-SNAPSHOT)
./gradlew publishToMavenCentral

# Release version (e.g. 1.0.1)
./gradlew publishToMavenCentral
```

Release versions are uploaded, staged, and released automatically through Sonatype (`publishToSonatype` + `closeAndReleaseSonatypeStagingRepository`).

### Snapshot folder
By default snapshots are written to:

```
src/test/resources/snapshots/<package>/<class-kebab>/<feature>.ext
```

You can override the root directory in tests or builds:

```groovy
System.setProperty('spock.snapshot.dir', 'build/test-snapshots')
```

### Comparison helpers
Available comparisons:

- `Comparisons.JSON`
- `Comparisons.OBJECT_AS_JSON`
- `Comparisons.API_RESPONSE` (excludes `id`, `createdAt`, `lastModified`)
- `Comparisons.XML`
- `Comparisons.TXT`
- `Comparisons.PNG`
- `Comparisons.BINARY`

Customizable helpers:

```groovy
Snappo.expect(data, Comparisons.png(PngComparison.MODE.PIXEL))
Snappo.expect(data, Comparisons.jsonExcludingProperties('id', 'createdAt'))
Snappo.expect(data, Comparisons.jsonExcludingTypes(Instant))
Snappo.expect(data, Comparisons.detect(data))
```

## Notes
- The global Spock extension is registered via `META-INF/services` and runs automatically.
- Missing snapshots are created automatically on first run.
- This library targets Groovy 5 + Spock 2.4. If you need different versions, update the versions in `build.gradle`.
