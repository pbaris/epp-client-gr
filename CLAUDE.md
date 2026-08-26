# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## OpenWolf

@.wolf/OPENWOLF.md

This project uses OpenWolf for context management. Read and follow .wolf/OPENWOLF.md every session. Check .wolf/cerebrum.md before generating code. Check .wolf/anatomy.md before reading files.

## Git Conventions
- NEVER run `git commit` or `git push` without an explicit instruction from the user.
- NEVER add "Co-Authored-By" lines or attribution text to commits.
- Keep commit message lines under 90 characters

## Project overview

`epp-client-gr` (Maven coordinates `gr.netmechanics:epp-client-gr`) is a Spring Boot **auto-configuration library**, not a runnable app. It implements an EPP (Extensible Provisioning Protocol) client for the ICS-FORTH registry that runs the `.gr` / `.ελ` ccTLDs, covering domain, host, contact and registrar management per RFC3730-RFC3735. Host applications add it as a dependency and get an `eppClient` bean auto-wired via `EppClientAutoConfiguration`. See `README.adoc` for the installation snippet and the full per-command implementation-status table (RFC coverage).

## Commands

```bash
./gradlew build                    # compile, checkstyle, test, jacoco — full verification
./gradlew test                     # run the test suite only
./gradlew checkstyleMain checkstyleTest
./gradlew jacocoTestReport
./gradlew publishToMavenLocal      # publish to local Maven repo
./gradlew publish                  # publish to Netmechanics Nexus (needs -Pnexus_username/-Pnexus_password)
```

- Checkstyle (`checkstyle.xml`, `ignoreFailures = false`) runs as part of `build`/`check` and **fails the build** on any violation — 150-char line length, LF-only line endings, no tabs, single trailing newline.
- Tests require real sandbox credentials in `src/test/resources/application.properties` (gitignored, not present by default):
  ```properties
  epp.use-sandbox=true
  epp.client-id=<your-client-id>
  epp.password=<your-password>
  epp.cl-tr-id=211119810900
  ```
  Without this file the test context fails to start. To build without it, skip tests: `./gradlew build -PskipTests=true` (or `-DskipTests=true`).
- **Running a single test class is not straightforward.** The `test` task is restricted to `include '**/EppClientTestSuite.class'` (see `build.gradle`), and that suite (`EppClientTestSuite`) explicitly registers member classes via `@SelectClasses`. A bare `./gradlew test --tests gr.netmechanics.epp.client.DomainTests` will not run because that class doesn't match the include pattern. To isolate one class, either run it directly from the IDE, or temporarily trim `@SelectClasses` in `EppClientTestSuite.java`, or use the (currently commented-out) `@IncludeTags("run-this")` mechanism with JUnit `@Tag` on the target tests.
- `tools/build-test-scan.sh` is a Maven/SonarQube scan script (`./mvnw ... sonar:sonar`) left over from elsewhere — this project has no `mvnw`/`pom.xml`, so it does not currently work as-is against this Gradle build.

## Architecture

The library is one linear pipeline: **`EppClient` (public façade) → `EppGateway` (interface) → `RestEppGateway` (its one implementation, using a `RestClient` bean) → ICS-FORTH EPP server**, with symmetric XML (de)serialization on the way in and out via `EppXmlCodec`.

- **`EppClientAutoConfiguration`** is the Spring Boot auto-configuration entry point: it component-scans `gr.netmechanics.epp.client` and registers a default `EppPropertiesProvider` (backed by `@ConfigurationProperties(prefix = "epp")` class `EppProperties`) unless the host app supplies its own bean — the intended extension point for dynamic credential loading.
- **`EppClient`** is the single public API bean (`BEAN_EPP_CLIENT = "eppClient"`), with one method per EPP command (`checkDomains`, `createDomain`, `getHostInfo`, `updateContact`, ...). Every method wraps its typed request in `EppCommandRequest.request(request, clTrId)` and delegates to `EppGateway.sendCommand`. It also owns session management: a `ReentrantLock`-guarded connect sequence tracked by a `volatile boolean connected` flag, a cached `Greeting` from `hello()`, and reactive reconnect-and-retry via `LoginRequest` whenever an EPP command comes back with result code 2201 (`AUTHORIZATION_ERROR`) — callers never call `login()`/`logout()` directly in normal use. Session-cookie persistence is delegated to `EppSessionCookieStore` rather than a shared header map.
- **`EppGateway`** is now a plain interface with one implementation, `RestEppGateway` (`@Component`), which uses a `RestClient` bean to POST the marshaled XML and read back the raw response body. It looks up the current `EppPropertiesProvider` via `ApplicationContext.getBean(...)` on every call (not injected once) specifically so a host app publishing `EppRefreshEvent` after replacing that bean is picked up immediately, without needing to rebuild anything. `RestClientException`s from the HTTP call are wrapped in `EppGatewayException`.
- **`EppXmlCodec`** (in the `xml` package) replaces the old `ObjectToXMLTransformer`/`XMLToObjectTransformer` — marshals/unmarshals via the shared `XmlMapper`, logs at DEBUG with password redaction (namespace-prefixed and unprefixed `pw`/`newPW` tags, both directions), and throws `EppGatewayException` on failure in both directions.
- **`EppHttpClientConfiguration`** builds the `RestClient` bean (`@ConditionalOnMissingBean`, so a host app can override it) with configurable connect/read timeouts (`epp.connect-timeout-millis`/`epp.read-timeout-millis`, defaulting to 10s/30s) and registers `EppCookieInterceptor` (backed by `EppSessionCookieStore`) so the EPP session cookie survives across requests — the interceptor strips `Set-Cookie` response attributes down to `name=value` pairs before storing, rather than copying the whole header verbatim.
- **XML mapping**: request/response objects are Jackson `XmlMapper` POJOs (`@JacksonXmlProperty`/`@JacksonXmlElementWrapper`, often Java records for nested/immutable nodes). `XmlMapperConfiguration` builds the mapper on Woodstox with a custom `NamespaceXmlFactory` that pre-registers every EPP/ICS-FORTH XML namespace (`domain-1.0`, `contact-1.0`, `host-1.0`, `secDNS-1.1`, plus ICS-FORTH `ext*` namespaces) so vanilla-EPP and .gr-registry-specific extensions serialize correctly together.
- **Commands** live under `impl/commands/<verb>/<object>` (e.g. `impl/commands/create/domain/DomainCreateRequest`), one package per RFC verb (`check`, `create`, `delete`, `info`, `renew`, `transfer`, `update`) plus session commands (`LoginRequest`, `LogoutRequest`, `Hello`) at the `impl/commands` root. Each concrete request implements a verb marker interface (`CheckRequest`, `CreateRequest`, ...) and exposes a static `builder()` returning a private `XxxRequestBuilder implements EppBuilder`. `EppBuilder` centralizes validation (`requireNonEmpty`, `requireYears`, `mergeContacts`, ...) so builders fail fast with `IllegalArgumentException` before anything hits the wire.
- **`EppCommand`** is the node serialized as `<command>`: its constructor pattern-matches the concrete `EppRequest` (`instanceof CheckRequest r`, `instanceof CreateRequest r`, ...) to populate the right field, and separately pulls extensions off any `HasExtension` request into `<extension>` nodes. **`EppCommandResponse`** mirrors this on the way back — a private record `ResponseNode` maps `<response>` (`results`/`resData`/`extension`/`trID`), and typed getters (`getInfoResponse()`, `getCheckResponse()`, ...) just downcast whichever `resData` sub-object is populated. `isSuccess()` requires exactly one successful `<result>`.
- **Extensions** (`impl/elements/ext/*`) implement `HasExtension` to attach registry-specific data (DNSSEC/`secDNS`, ICS-FORTH common/domain/contact/host extensions, domain transfer/delete/issue-token extensions) onto the base RFC objects. Their (de)serialization is centralized in `xml/ExtensionSerializer`/`xml/ExtensionDeserializer`, so adding a new extension type doesn't need bespoke Jackson wiring elsewhere.

### Testing

- Tests are real integration tests: `EppClientTestBase` boots a `@SpringBootTest` with `EppClientAutoConfiguration`, and tests hit the actual ICS-FORTH **sandbox** EPP server over the network (`epp.use-sandbox=true`) — nothing is mocked. This is why a valid sandbox `application.properties` is required to run them at all (see Commands above).
- Expected request/response XML fixtures live in `src/test/resources/xml/*.xml`, loaded through the test-only `XmlUtil` helper (wired via `TestHelperConfiguration`).
- Test classes are wired together explicitly in `EppClientTestSuite` via `@SelectClasses` (`SessionTests`, `ContactTests`, `HostTests`, `DomainTests`, `RegistrarTests`, `RefreshEppTest`) — new test classes must be added there to run under Gradle.
