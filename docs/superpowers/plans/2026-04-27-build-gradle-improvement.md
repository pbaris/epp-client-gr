# Build Setup Improvement Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Modernize `build.gradle` by introducing a version catalog, fixing structural issues, adding a javadoc jar, and enriching the Maven POM with metadata required for a publicly-published library.

**Architecture:** Three independent, atomic changes to Gradle build configuration: (1) create a version catalog and migrate unmanaged dependency versions into it, (2) fix structural issues in `build.gradle` (duplicate block, dead code), (3) add standard POM metadata to the Maven publication.

**Tech Stack:** Gradle (Groovy DSL), `maven-publish` plugin, Spring Boot BOM, Gradle Version Catalog (TOML)

---

## Files

| File | Action |
|------|--------|
| `gradle/libs.versions.toml` | **Create** — version catalog for the 3 unmanaged third-party dependencies |
| `build.gradle` | **Modify** — migrate 3 deps to catalog refs, merge java blocks + add javadoc jar, remove redundant artifacts block, remove stale GenerateModuleMetadata workaround, add pom metadata |

---

## Task 1: Create the version catalog

**Files:**
- Create: `gradle/libs.versions.toml`

- [ ] **Step 1: Create `gradle/libs.versions.toml`**

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

Note: BOM-managed dependencies (spring-boot-*, jackson-*, lombok, etc.) are intentionally excluded — their versions are controlled by the Spring Boot BOM.

- [ ] **Step 2: Verify Gradle recognizes the catalog**

Run:
```bash
./gradlew dependencies --configuration compileClasspath 2>&1 | head -30
```

Expected: Gradle resolves dependencies without errors. The catalog file is auto-discovered from `gradle/libs.versions.toml` by Gradle 7.4+.

- [ ] **Step 3: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "chore: introduce Gradle version catalog for unmanaged dependencies"
```

---

## Task 2: Migrate hardcoded dep versions to catalog refs in `build.gradle`

**Files:**
- Modify: `build.gradle` (dependencies block, lines 36–53)

- [ ] **Step 1: Replace the 3 hardcoded dep declarations**

In `build.gradle`, find this block:
```groovy
implementation 'com.fasterxml.woodstox:woodstox-core:7.1.0'
implementation 'com.github.javadev:underscore21:1.68'
implementation 'commons-validator:commons-validator:1.9.0'
```

Replace with:
```groovy
implementation libs.woodstox.core
implementation libs.underscore
implementation libs.commons.validator
```

The surrounding dependencies block should look like:
```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'org.springframework.integration:spring-integration-http'

    implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-xml'
    implementation 'com.fasterxml.jackson.datatype:jackson-datatype-jsr310'
    implementation libs.woodstox.core
    implementation libs.underscore
    implementation libs.commons.validator

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.junit.platform:junit-platform-suite'
}
```

- [ ] **Step 2: Verify the build compiles**

Run:
```bash
./gradlew compileJava
```

Expected output ends with:
```
BUILD SUCCESSFUL
```

- [ ] **Step 3: Commit**

```bash
git add build.gradle
git commit -m "chore: migrate unmanaged dependency versions to version catalog"
```

---

## Task 3: Fix structural issues in `build.gradle`

**Files:**
- Modify: `build.gradle`

Three changes in one commit: merge java blocks, remove `artifacts` block, remove stale JetBrains workaround.

- [ ] **Step 1: Merge the two `java {}` blocks and add `withJavadocJar()`**

Find the first `java {}` block (around line 19):
```groovy
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
```

Replace it with:
```groovy
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
    withJavadocJar()
}
```

Then delete the second `java {}` block that appears after the dependencies section:
```groovy
java {
    withSourcesJar()
}
```

- [ ] **Step 2: Remove the `artifacts` block**

Find and delete:
```groovy
artifacts {
    archives sourcesJar
}
```

This is dead code. With `maven-publish` + `from components.java`, the sources jar is included automatically via `withSourcesJar()` declared above.

- [ ] **Step 3: Remove the stale `GenerateModuleMetadata` workaround**

Find and delete:
```groovy
// remove after https://youtrack.jetbrains.com/issue/IDEA-227215 is fixed
tasks.withType(GenerateModuleMetadata).configureEach {
    enabled = false
}
```

IDEA-227215 was fixed in IntelliJ IDEA 2020.1. Removing this re-enables Gradle module metadata, which improves dependency resolution for Gradle consumers of this library.

- [ ] **Step 4: Verify the build and tests pass**

Run:
```bash
./gradlew build
```

Expected output ends with:
```
BUILD SUCCESSFUL
```

If tests need to be skipped (no live EPP server available), run:
```bash
./gradlew build -PskipTests=true
```

- [ ] **Step 5: Commit**

```bash
git add build.gradle
git commit -m "chore: fix structural issues - merge java blocks, remove dead code, re-enable module metadata"
```

---

## Task 4: Add POM metadata to the Maven publication

**Files:**
- Modify: `build.gradle` (publishing block)

- [ ] **Step 1: Add `pom {}` block inside the `javaMaven` publication**

Find the `publications` block:
```groovy
publications {
    create("javaMaven", MavenPublication) {
        groupId = project.group
        artifactId = 'epp-client-gr'
        version = project.version
        from components.java
    }
}
```

Replace with:
```groovy
publications {
    create("javaMaven", MavenPublication) {
        groupId = project.group
        artifactId = 'epp-client-gr'
        version = project.version
        from components.java
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
    }
}
```

- [ ] **Step 2: Publish to local Maven repo and inspect generated POM**

Run:
```bash
./gradlew publishToMavenLocal -PskipTests=true
```

Expected output ends with:
```
BUILD SUCCESSFUL
```

Then inspect the generated POM:
```bash
cat ~/.m2/repository/gr/netmechanics/epp-client-gr/1.1.0/epp-client-gr-1.1.0.pom
```

Verify the POM contains:
- `<description>EPP client library for the .gr domain registry</description>`
- `<url>https://github.com/pbaris/epp-client-gr</url>`
- `<licenses>` block with Apache 2.0
- `<scm>` block with the GitHub URLs
- `<developers>` block with `pbaris`

Also verify a `-sources.jar` and `-javadoc.jar` were published alongside the main jar:
```bash
ls ~/.m2/repository/gr/netmechanics/epp-client-gr/1.1.0/
```

Expected files: `epp-client-gr-1.1.0.jar`, `epp-client-gr-1.1.0-sources.jar`, `epp-client-gr-1.1.0-javadoc.jar`, `epp-client-gr-1.1.0.pom`, `epp-client-gr-1.1.0.module`

- [ ] **Step 3: Commit**

```bash
git add build.gradle
git commit -m "chore: add POM metadata for public Maven publication (license, SCM, developer)"
```
