# Build Setup Improvement Design

**Date:** 2026-04-27
**Project:** epp-client-gr
**Approach:** Incremental cleanup + version catalog (Groovy DSL retained)

## Summary

Modernize `build.gradle` by fixing structural issues, introducing a Gradle version catalog for
unmanaged third-party dependencies, adding a javadoc jar, and enriching the Maven publication
with standard POM metadata required for a publicly-published open-source library.

---

## Section 1: Structural Fixes to `build.gradle`

### 1.1 Merge duplicate `java {}` blocks

The current file declares `java {}` twice (lines 19–23 and 55–57). They must be merged into one:

```groovy
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
    withJavadocJar()
}
```

### 1.2 Remove redundant `artifacts` block

```groovy
// REMOVE:
artifacts {
    archives sourcesJar
}
```

This is a leftover from the legacy `maven` plugin. With `maven-publish` + `from components.java`,
sources are included automatically once `withSourcesJar()` is declared. The block is dead code.

### 1.3 Remove `GenerateModuleMetadata` workaround

```groovy
// REMOVE:
// remove after https://youtrack.jetbrains.com/issue/IDEA-227215 is fixed
tasks.withType(GenerateModuleMetadata).configureEach {
    enabled = false
}
```

IDEA-227215 was resolved in IntelliJ IDEA 2020.1. Re-enabling module metadata improves
dependency resolution for Gradle consumers of this library.

---

## Section 2: Version Catalog

Create `gradle/libs.versions.toml` to manage the three third-party dependency versions that are
not covered by the Spring Boot BOM.

**File: `gradle/libs.versions.toml`**

```toml
[versions]
woodstox = "7.1.0"
underscore = "1.68"
commons-validator = "1.9.0"

[libraries]
woodstox-core = { module = "com.fasterxml.woodstox:woodstox-core", version.ref = "woodstox" }
underscore = { module = "com.github.javadev:underscore21", version.ref = "underscore" }
commons-validator = { module = "commons-validator:commons-validator", version.ref = "commons-validator" }
```

BOM-managed dependencies (spring-boot-starter, spring-integration-http, jackson-*, lombok, etc.)
are intentionally excluded from the catalog — their versions are controlled by the Spring Boot BOM
and must not be pinned independently.

In `build.gradle`, replace the three pinned declarations:

```groovy
// BEFORE:
implementation 'com.fasterxml.woodstox:woodstox-core:7.1.0'
implementation 'com.github.javadev:underscore21:1.68'
implementation 'commons-validator:commons-validator:1.9.0'

// AFTER:
implementation libs.woodstox.core
implementation libs.underscore
implementation libs.commons.validator
```

---

## Section 3: POM Metadata

Add a `pom {}` block inside the `javaMaven` publication:

```groovy
pom {
    name = 'epp-client-gr'
    description = 'EPP client library for the .gr domain registry'
    url = 'https://github.com/pbaris/epp-client-gr'
    licenses {
        license {
            name = 'Apache License, Version 2.0'
            url = 'https://www.apache.org/licenses/LICENSE-2.0'
        }
    }
    scm {
        connection = 'scm:git:git://github.com/pbaris/epp-client-gr.git'
        developerConnection = 'scm:git:ssh://github.com/pbaris/epp-client-gr.git'
        url = 'https://github.com/pbaris/epp-client-gr'
    }
    developers {
        developer {
            id = 'pbaris'
            name = 'Panos Bariamis'
        }
    }
}
```

**Source of truth:**
- License: Apache 2.0 (confirmed from `LICENSE` file)
- GitHub URL: `git@github.com:pbaris/epp-client-gr.git` (confirmed from git remote)
- Developer: `pbaris / Panos Bariamis` (from `@author` in `EppClientAutoConfiguration.java`)

---

## Files Changed

| File | Action |
|------|--------|
| `build.gradle` | Merge java blocks, remove artifacts + GenerateModuleMetadata, add withJavadocJar, update 3 deps to catalog refs, add pom metadata |
| `gradle/libs.versions.toml` | Create new — version catalog for 3 unmanaged dependencies |

## Out of Scope

- Kotlin DSL migration (`build.gradle.kts`)
- Moving BOM-managed dependency versions into the catalog
- Any changes to source code or test configuration
