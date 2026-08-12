# Spring Integration → RestClient Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the `spring-integration-http`-based EPP transport (message channels, `IntegrationFlow`, `@MessagingGateway`) with a plain Spring `RestClient`, fixing several real gaps found along the way: missing `Content-Type: application/epp+xml;charset=UTF-8`, no HTTP timeouts, HTTP/network failures not wrapped in `EppGatewayException`, `Set-Cookie` attributes copied verbatim into the outgoing `Cookie` header, and the login password appearing in plaintext in DEBUG logs.

**Architecture:** `EppClient` still depends on the `EppGateway` interface (unchanged). Its only implementation becomes `RestEppGateway`, a plain `@Component` that uses a `RestClient` bean to POST/receive XML strings. Marshalling/unmarshalling and DEBUG logging (with password redaction) move into a new `EppXmlCodec` component, replacing `ObjectToXMLTransformer`/`XMLToObjectTransformer`. Session-cookie propagation moves from two Spring Integration `ChannelInterceptor`s sharing a `Map` bean to a single `ClientHttpRequestInterceptor` (`EppCookieInterceptor`) backed by a small `EppSessionCookieStore`, which correctly extracts only the `name=value` pair from `Set-Cookie` instead of the whole attribute string. `EppRequestFlowManager` is deleted outright: with `RestClient`, the registry URL is read fresh on every call (via the current `EppPropertiesProvider` bean, fetched from `ApplicationContext` the same way `EppRefreshListener` already does), so there is no "flow" to tear down and rebuild when properties change.

**Tech Stack:** Spring Framework 6.2.x `RestClient`/`ClientHttpRequestInterceptor` (from `spring-web`, transitively available via Spring Boot 3.5.3's dependency management — no version needs pinning), Jackson `XmlMapper` (unchanged), JUnit 5 + AssertJ + Mockito (already test dependencies via `spring-boot-starter-test`).

## Global Constraints

- Every source file follows this project's existing Lombok/Checkstyle conventions: `final` parameters, `@RequiredArgsConstructor`/`@Getter`/`@Slf4j` where the rest of the codebase uses them, no comments except where a non-obvious constraint justifies one.
- No behavior change to the public `EppClient`/`EppGateway` API surface — this is a transport-internals swap only. The second, separate task (session/concurrency rework in `EppClient`) is explicitly out of scope for this plan.
- Every task must leave `./gradlew build` compiling and the full `EppClientTestSuite` passing against the real ICS-FORTH sandbox (`src/test/resources/application.properties` already has working sandbox credentials in this environment).
- `build.gradle`'s `test` task restricts execution to `**/EppClientTestSuite.class`. To run an individual test class while developing, temporarily edit that `include` line, run the tests, then restore it — do not leave it changed.
- Never commit unless explicitly asked.

---

### Task 1: `EppSessionCookieStore` + `EppCookieInterceptor`

**Files:**
- Create: `src/main/java/gr/netmechanics/epp/client/EppSessionCookieStore.java`
- Create: `src/main/java/gr/netmechanics/epp/client/EppCookieInterceptor.java`
- Test: `src/test/java/gr/netmechanics/epp/client/EppCookieInterceptorTest.java`

**Interfaces:**
- Produces: `EppSessionCookieStore` with `String get()`, `void set(String cookie)`, `void clear()` — a plain (not `@Component`-scanned as a request-interceptor itself, but registered as a `@Component` bean so it can be injected into `EppClient` in Task 3) holder for the current session cookie string.
- Produces: `EppCookieInterceptor implements org.springframework.http.client.ClientHttpRequestInterceptor`, constructed with `new EppCookieInterceptor(EppSessionCookieStore)`. On each request: if the store holds a cookie, attaches it as the `Cookie` header; after the response, if a `Set-Cookie` header is present, stores only the `name=value` pair(s) (stripping `Path`/`HttpOnly`/etc.) back into the store.

- [ ] **Step 1: Write the failing tests**

Create `src/test/java/gr/netmechanics/epp/client/EppCookieInterceptorTest.java`:

```java
package gr.netmechanics.epp.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

class EppCookieInterceptorTest {

    @Test
    void strips_set_cookie_attributes_and_stores_only_name_value_pair() throws Exception {
        EppSessionCookieStore cookieStore = new EppSessionCookieStore();
        EppCookieInterceptor interceptor = new EppCookieInterceptor(cookieStore);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.put(HttpHeaders.SET_COOKIE, List.of("JSESSIONID=abc123; Path=/; HttpOnly; SameSite=Lax"));

        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getHeaders()).thenReturn(responseHeaders);

        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        when(execution.execute(any(), any())).thenReturn(response);

        HttpRequest request = mock(HttpRequest.class);
        when(request.getHeaders()).thenReturn(new HttpHeaders());

        interceptor.intercept(request, new byte[0], execution);

        assertThat(cookieStore.get()).isEqualTo("JSESSIONID=abc123");
    }

    @Test
    void sends_stored_cookie_as_cookie_header_on_next_request() throws Exception {
        EppSessionCookieStore cookieStore = new EppSessionCookieStore();
        cookieStore.set("JSESSIONID=abc123");
        EppCookieInterceptor interceptor = new EppCookieInterceptor(cookieStore);

        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getHeaders()).thenReturn(new HttpHeaders());

        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        when(execution.execute(any(), any())).thenReturn(response);

        HttpHeaders requestHeaders = new HttpHeaders();
        HttpRequest request = mock(HttpRequest.class);
        when(request.getHeaders()).thenReturn(requestHeaders);

        interceptor.intercept(request, new byte[0], execution);

        assertThat(requestHeaders.get(HttpHeaders.COOKIE)).containsExactly("JSESSIONID=abc123");
    }

    @Test
    void does_not_add_cookie_header_when_store_is_empty() throws Exception {
        EppSessionCookieStore cookieStore = new EppSessionCookieStore();
        EppCookieInterceptor interceptor = new EppCookieInterceptor(cookieStore);

        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getHeaders()).thenReturn(new HttpHeaders());

        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        when(execution.execute(any(), any())).thenReturn(response);

        HttpHeaders requestHeaders = new HttpHeaders();
        HttpRequest request = mock(HttpRequest.class);
        when(request.getHeaders()).thenReturn(requestHeaders);

        interceptor.intercept(request, new byte[0], execution);

        assertThat(requestHeaders.get(HttpHeaders.COOKIE)).isNull();
    }
}
```

This file will not compile yet because `EppSessionCookieStore` and `EppCookieInterceptor` don't exist. That is expected for this step.

- [ ] **Step 2: Run the test to verify it fails to compile**

Temporarily change `build.gradle`'s test include to `include '**/EppCookieInterceptorTest.class'`, then run:

`./gradlew test --tests "gr.netmechanics.epp.client.EppCookieInterceptorTest" -Dcheckstyle.skip=true --console=plain`

Expected: compilation failure — `cannot find symbol EppSessionCookieStore` / `EppCookieInterceptor`.

- [ ] **Step 3: Implement `EppSessionCookieStore`**

Create `src/main/java/gr/netmechanics/epp/client/EppSessionCookieStore.java`:

```java
package gr.netmechanics.epp.client;

import org.springframework.stereotype.Component;

@Component
public class EppSessionCookieStore {

    private volatile String cookie;

    public String get() {
        return cookie;
    }

    public void set(final String cookie) {
        this.cookie = cookie;
    }

    public void clear() {
        this.cookie = null;
    }
}
```

- [ ] **Step 4: Implement `EppCookieInterceptor`**

Create `src/main/java/gr/netmechanics/epp/client/EppCookieInterceptor.java`:

```java
package gr.netmechanics.epp.client;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;

@RequiredArgsConstructor
public class EppCookieInterceptor implements ClientHttpRequestInterceptor {

    private final EppSessionCookieStore cookieStore;

    @Override
    @NonNull
    public ClientHttpResponse intercept(
        @NonNull final HttpRequest request,
        @NonNull final byte[] body,
        @NonNull final ClientHttpRequestExecution execution) throws IOException {

        String cookie = cookieStore.get();
        if (cookie != null) {
            request.getHeaders().add(HttpHeaders.COOKIE, cookie);
        }

        ClientHttpResponse response = execution.execute(request, body);

        List<String> setCookieHeaders = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (setCookieHeaders != null && !setCookieHeaders.isEmpty()) {
            cookieStore.set(toCookieHeader(setCookieHeaders));
        }

        return response;
    }

    private String toCookieHeader(final List<String> setCookieHeaders) {
        return setCookieHeaders.stream()
            .map(header -> header.split(";", 2)[0].trim())
            .collect(Collectors.joining("; "));
    }
}
```

- [ ] **Step 5: Run the test to verify it passes**

`./gradlew test --tests "gr.netmechanics.epp.client.EppCookieInterceptorTest" -Dcheckstyle.skip=true --console=plain`

Expected: `BUILD SUCCESSFUL`, 3 tests passed.

- [ ] **Step 6: Restore `build.gradle`'s test include to `**/EppClientTestSuite.class`**

- [ ] **Step 7: Commit**

```bash
git add src/main/java/gr/netmechanics/epp/client/EppSessionCookieStore.java \
        src/main/java/gr/netmechanics/epp/client/EppCookieInterceptor.java \
        src/test/java/gr/netmechanics/epp/client/EppCookieInterceptorTest.java
git commit -m "feat: add EppSessionCookieStore and EppCookieInterceptor for RestClient migration"
```

---

### Task 2: `EppXmlCodec`

**Files:**
- Create: `src/main/java/gr/netmechanics/epp/client/xml/EppXmlCodec.java`
- Test: `src/test/java/gr/netmechanics/epp/client/xml/EppXmlCodecTest.java`
- Reference (not modified in this task, deleted in Task 3): `src/main/java/gr/netmechanics/epp/client/xml/ObjectToXMLTransformer.java`, `src/main/java/gr/netmechanics/epp/client/xml/XMLToObjectTransformer.java`

**Interfaces:**
- Produces: `EppXmlCodec`, constructed with `new EppXmlCodec(XmlMapper)`, with:
  - `String marshal(Object payload)` — serializes to XML, throws `EppGatewayException` on failure (previously `ObjectToXMLTransformer` silently returned `payload.toString()` on failure, which would have POSTed garbage to the registry — this task fixes that).
  - `<T> T unmarshal(String payload, Class<T> type)` — deserializes to the given type, throws `EppGatewayException` on failure. Unlike the old `XMLToObjectTransformer`, the target type is passed explicitly by the caller instead of being guessed by sniffing the XML string for `</greeting>` vs `</response>`.
  - Both log the request/response XML at DEBUG (matching current behavior), and `marshal` redacts `<pw>`/`<newPW>` element content before logging so the EPP account password and any new password being set never appear in logs.
  - Package-visible static `String redact(String xml)` (not `private`) so it is directly unit-testable.

- [ ] **Step 1: Write the failing tests**

Create `src/test/java/gr/netmechanics/epp/client/xml/EppXmlCodecTest.java`:

```java
package gr.netmechanics.epp.client.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import gr.netmechanics.epp.client.error.EppGatewayException;
import gr.netmechanics.epp.client.impl.EppCommandResponse;
import org.junit.jupiter.api.Test;

class EppXmlCodecTest {

    @Test
    void unmarshal_wraps_parse_failures_in_epp_gateway_exception() {
        EppXmlCodec codec = new EppXmlCodec(new XmlMapper());

        assertThatThrownBy(() -> codec.unmarshal("not xml at all", EppCommandResponse.class))
            .isInstanceOf(EppGatewayException.class);
    }

    @Test
    void redact_masks_password_field_but_preserves_everything_else() {
        String xml = "<command><login><clID>user</clID><pw><![CDATA[secret123]]></pw></login></command>";

        String redacted = EppXmlCodec.redact(xml);

        assertThat(redacted)
            .contains("<clID>user</clID>")
            .contains("<pw>***REDACTED***</pw>")
            .doesNotContain("secret123");
    }

    @Test
    void redact_masks_new_password_field_too() {
        String xml = "<login><newPW>brandNewSecret</newPW></login>";

        String redacted = EppXmlCodec.redact(xml);

        assertThat(redacted)
            .contains("<newPW>***REDACTED***</newPW>")
            .doesNotContain("brandNewSecret");
    }

    @Test
    void redact_is_a_no_op_when_there_is_no_password_field() {
        String xml = "<hello/>";

        assertThat(EppXmlCodec.redact(xml)).isEqualTo("<hello/>");
    }
}
```

This will not compile yet — `EppXmlCodec` doesn't exist.

- [ ] **Step 2: Run the test to verify it fails to compile**

Temporarily set `build.gradle`'s test include to `include '**/EppXmlCodecTest.class'`, then:

`./gradlew test --tests "gr.netmechanics.epp.client.xml.EppXmlCodecTest" -Dcheckstyle.skip=true --console=plain`

Expected: compilation failure — `cannot find symbol EppXmlCodec`.

- [ ] **Step 3: Implement `EppXmlCodec`**

Create `src/main/java/gr/netmechanics/epp/client/xml/EppXmlCodec.java`:

```java
package gr.netmechanics.epp.client.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.underscore.U;
import com.github.underscore.Xml;
import gr.netmechanics.epp.client.error.EppGatewayException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EppXmlCodec {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?s)(<(?:pw|newPW)>).*?(</(?:pw|newPW)>)");

    private final XmlMapper xmlMapper;

    public String marshal(final Object payload) {
        try {
            String xml = xmlMapper.writeValueAsString(payload);
            if (log.isDebugEnabled()) {
                log.debug("Sending message:\n{}\n", redact(xml));
            }
            return xml;

        } catch (Exception e) {
            throw new EppGatewayException("Failed to marshal XML", e);
        }
    }

    public <T> T unmarshal(final String payload, final Class<T> type) {
        if (log.isDebugEnabled()) {
            log.debug("Received message:\n{}\n", minifyXml(payload));
        }

        try {
            return xmlMapper.readValue(payload, type);

        } catch (Exception e) {
            throw new EppGatewayException("Failed to unmarshal XML", e);
        }
    }

    static String redact(final String xml) {
        return PASSWORD_PATTERN.matcher(xml).replaceAll("$1***REDACTED***$2");
    }

    @SneakyThrows
    private static String minifyXml(final String xml) {
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        StreamResult result = new StreamResult(new StringWriter());

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.transform(new StreamSource(inputStream), result);

        return U.formatXml(result.getWriter().toString(), Xml.XmlStringBuilder.Step.COMPACT);
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

`./gradlew test --tests "gr.netmechanics.epp.client.xml.EppXmlCodecTest" -Dcheckstyle.skip=true --console=plain`

Expected: `BUILD SUCCESSFUL`, 4 tests passed.

- [ ] **Step 5: Restore `build.gradle`'s test include to `**/EppClientTestSuite.class`**

- [ ] **Step 6: Commit**

```bash
git add src/main/java/gr/netmechanics/epp/client/xml/EppXmlCodec.java \
        src/test/java/gr/netmechanics/epp/client/xml/EppXmlCodecTest.java
git commit -m "feat: add EppXmlCodec with password-redacted logging for RestClient migration"
```

---

### Task 3: `RestEppGateway` + `EppHttpClientConfiguration` — full swap

This is the task that actually replaces the transport. It cannot be split further without leaving the codebase in a non-compiling state, because the old (`EppGatewayConfiguration`, `EppRequestFlowManager`, `@MessagingGateway`, `@IntegrationComponentScan`) and new (`RestEppGateway`, `EppHttpClientConfiguration`) systems cannot coexist — both would try to be *the* `EppGateway` implementation, and removing `spring-integration-http` from `build.gradle` makes the old classes uncompilable anyway.

**Files:**
- Create: `src/main/java/gr/netmechanics/epp/client/RestEppGateway.java`
- Create: `src/main/java/gr/netmechanics/epp/client/config/EppHttpClientConfiguration.java`
- Delete: `src/main/java/gr/netmechanics/epp/client/EppRequestFlowManager.java`
- Delete: `src/main/java/gr/netmechanics/epp/client/config/EppGatewayConfiguration.java`
- Delete: `src/main/java/gr/netmechanics/epp/client/xml/ObjectToXMLTransformer.java`
- Delete: `src/main/java/gr/netmechanics/epp/client/xml/XMLToObjectTransformer.java`
- Modify: `src/main/java/gr/netmechanics/epp/client/EppGateway.java` (drop `@MessagingGateway`)
- Modify: `src/main/java/gr/netmechanics/epp/client/config/EppClientAutoConfiguration.java` (drop `@IntegrationComponentScan`)
- Modify: `src/main/java/gr/netmechanics/epp/client/EppClient.java` (swap shared-headers `Map` for `EppSessionCookieStore`)
- Modify: `src/main/java/gr/netmechanics/epp/client/EppRefreshListener.java` (drop the flow-restart call)
- Modify: `src/main/java/gr/netmechanics/epp/client/EppConstants.java` (remove channel/flow/shared-headers bean-name constants)
- Modify: `src/main/java/gr/netmechanics/epp/client/EppProperties.java`, `EppPropertiesProvider.java`, `EppPropertiesDefaultProvider.java` (add connect/read timeout properties)
- Modify: `build.gradle` (swap `spring-integration-http` for `spring-web`)

**Interfaces:**
- Consumes: `EppSessionCookieStore`/`EppCookieInterceptor` from Task 1, `EppXmlCodec` from Task 2.
- Produces: `RestEppGateway implements EppGateway` (unchanged interface, same two methods `hello(Hello)`/`sendCommand(EppCommandRequest)`), wired as the sole `EppGateway` bean.

- [ ] **Step 1: Add HTTP timeout properties to `EppProperties`**

Edit `src/main/java/gr/netmechanics/epp/client/EppProperties.java` — replace the whole file:

```java
package gr.netmechanics.epp.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Setter
@ConfigurationProperties(prefix = "epp")
public class EppProperties {

    @Getter private String clientId;
    @Getter private String password;
    @Getter private String language;
    @Getter private boolean useSandbox;
    @Getter private long connectTimeoutMillis;
    @Getter private long readTimeoutMillis;

    // should be changed for test and debug
    private Long clTrId;

    public EppProperties(
        final String clientId,
        final String password,
        @DefaultValue("el") final String language,
        @DefaultValue("true") final boolean useSandbox,
        @DefaultValue("10000") final long connectTimeoutMillis,
        @DefaultValue("30000") final long readTimeoutMillis,
        final Long clTrId) {

        this.clientId = clientId;
        this.password = password;
        this.language = language;
        this.useSandbox = useSandbox;
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.readTimeoutMillis = readTimeoutMillis;
        this.clTrId = clTrId;
    }

    public Long getClTrId() {
        return clTrId != null ? clTrId : System.currentTimeMillis();
    }

    public String getUrl() {
        return useSandbox ? EppConstants.URL_SANDBOX : EppConstants.URL_PRODUCTION;
    }
}
```

Edit `src/main/java/gr/netmechanics/epp/client/EppPropertiesProvider.java` — replace the whole file. The two new methods are `default`, not abstract, so any host application with an existing custom `EppPropertiesProvider` implementation (this is a published library — adding abstract interface methods would be a breaking change for them) keeps compiling unchanged and simply gets the fallback timeouts unless it chooses to override them:

```java
package gr.netmechanics.epp.client;

public interface EppPropertiesProvider {
    boolean isUseSandbox();

    String getClientId();

    String getPassword();

    String getLanguage();

    Long getClTrId();

    String getUrl();

    default long getConnectTimeoutMillis() {
        return 10_000;
    }

    default long getReadTimeoutMillis() {
        return 30_000;
    }
}
```

Edit `src/main/java/gr/netmechanics/epp/client/EppPropertiesDefaultProvider.java` — replace the whole file:

```java
package gr.netmechanics.epp.client;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EppPropertiesDefaultProvider implements EppPropertiesProvider {

    private final EppProperties properties;

    @Override
    public boolean isUseSandbox() {
        return properties.isUseSandbox();
    }

    @Override
    public String getClientId() {
        return properties.getClientId();
    }

    @Override
    public String getPassword() {
        return properties.getPassword();
    }

    @Override
    public String getLanguage() {
        return properties.getLanguage();
    }

    @Override
    public Long getClTrId() {
        return properties.getClTrId();
    }

    @Override
    public String getUrl() {
        return properties.getUrl();
    }

    @Override
    public long getConnectTimeoutMillis() {
        return properties.getConnectTimeoutMillis();
    }

    @Override
    public long getReadTimeoutMillis() {
        return properties.getReadTimeoutMillis();
    }
}
```

- [ ] **Step 2: Update `build.gradle` dependencies**

In `build.gradle`, replace:

```gradle
    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'org.springframework.integration:spring-integration-http'
```

with:

```gradle
    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'org.springframework:spring-web'
```

- [ ] **Step 3: Drop the Spring Integration annotations from `EppGateway` and `EppClientAutoConfiguration`**

Edit `src/main/java/gr/netmechanics/epp/client/EppGateway.java` — replace the whole file:

```java
package gr.netmechanics.epp.client;

import gr.netmechanics.epp.client.error.EppGatewayException;
import gr.netmechanics.epp.client.impl.EppCommandRequest;
import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.commands.Hello;
import gr.netmechanics.epp.client.impl.elements.Greeting;

public interface EppGateway {

    /**
     * Sends a Hello message to the EPP server and receives a server greeting.
     *
     * @param hello the Hello message to be sent to the server
     * @return the server's greeting response
     * @throws EppGatewayException if there is an issue during communication with the EPP server
     */
    Greeting hello(Hello hello) throws EppGatewayException;

    /**
     * Sends an EPP command to the server and retrieves the corresponding response.
     *
     * @param command the EPP command to be sent
     * @return the server's response to the provided command
     * @throws EppGatewayException if there is an issue during communication or processing of the command
     */
    EppCommandResponse sendCommand(EppCommandRequest command) throws EppGatewayException;
}
```

Edit `src/main/java/gr/netmechanics/epp/client/config/EppClientAutoConfiguration.java` — replace the whole file:

```java
package gr.netmechanics.epp.client.config;

import static gr.netmechanics.epp.client.EppConstants.BASE_PACKAGE;

import gr.netmechanics.epp.client.EppProperties;
import gr.netmechanics.epp.client.EppPropertiesDefaultProvider;
import gr.netmechanics.epp.client.EppPropertiesProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan(BASE_PACKAGE)
@ConfigurationPropertiesScan(BASE_PACKAGE)
public class EppClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(EppPropertiesProvider.class)
    public EppPropertiesProvider eppPropertiesProvider(final EppProperties properties) {
        return new EppPropertiesDefaultProvider(properties);
    }
}
```

- [ ] **Step 4: Delete the old transport files**

```bash
rm src/main/java/gr/netmechanics/epp/client/EppRequestFlowManager.java
rm src/main/java/gr/netmechanics/epp/client/config/EppGatewayConfiguration.java
rm src/main/java/gr/netmechanics/epp/client/xml/ObjectToXMLTransformer.java
rm src/main/java/gr/netmechanics/epp/client/xml/XMLToObjectTransformer.java
```

- [ ] **Step 5: Update `EppConstants`**

Edit `src/main/java/gr/netmechanics/epp/client/EppConstants.java` — replace the whole file:

```java
package gr.netmechanics.epp.client;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EppConstants {

    public static final String BASE_PACKAGE = "gr.netmechanics.epp.client";

    public static final String BEAN_EPP_CLIENT = "eppClient";

    public static final String URL_SANDBOX = "https://uat-regepp.ics.forth.gr:700/epp/proxy";
    public static final String URL_PRODUCTION = "https://regepp.ics.forth.gr:700/epp/proxy";

}
```

- [ ] **Step 6: Create `RestEppGateway`**

Create `src/main/java/gr/netmechanics/epp/client/RestEppGateway.java`:

```java
package gr.netmechanics.epp.client;

import java.nio.charset.StandardCharsets;

import gr.netmechanics.epp.client.error.EppGatewayException;
import gr.netmechanics.epp.client.impl.EppCommandRequest;
import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.commands.Hello;
import gr.netmechanics.epp.client.impl.elements.Greeting;
import gr.netmechanics.epp.client.xml.EppXmlCodec;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class RestEppGateway implements EppGateway {

    private static final MediaType EPP_XML = new MediaType("application", "epp+xml", StandardCharsets.UTF_8);

    private final RestClient restClient;
    private final EppXmlCodec codec;
    private final ApplicationContext context;

    @Override
    public Greeting hello(final Hello hello) throws EppGatewayException {
        return exchange(hello, Greeting.class);
    }

    @Override
    public EppCommandResponse sendCommand(final EppCommandRequest command) throws EppGatewayException {
        return exchange(command, EppCommandResponse.class);
    }

    private <T> T exchange(final Object payload, final Class<T> responseType) {
        String requestXml = codec.marshal(payload);
        String url = context.getBean(EppPropertiesProvider.class).getUrl();

        try {
            String responseXml = restClient.post()
                .uri(url)
                .contentType(EPP_XML)
                .accept(EPP_XML)
                .body(requestXml)
                .retrieve()
                .body(String.class);

            return codec.unmarshal(responseXml, responseType);

        } catch (RestClientException e) {
            throw new EppGatewayException("Failed to communicate with the EPP server", e);
        }
    }
}
```

`context.getBean(EppPropertiesProvider.class)` is looked up per-call (not injected once) so that a host application publishing `EppRefreshEvent` after replacing the `EppPropertiesProvider` bean is picked up immediately — this replaces what `EppRequestFlowManager.restartFlow(...)` used to do, without needing to rebuild anything.

- [ ] **Step 7: Create `EppHttpClientConfiguration`**

Create `src/main/java/gr/netmechanics/epp/client/config/EppHttpClientConfiguration.java`:

```java
package gr.netmechanics.epp.client.config;

import java.time.Duration;

import gr.netmechanics.epp.client.EppCookieInterceptor;
import gr.netmechanics.epp.client.EppPropertiesProvider;
import gr.netmechanics.epp.client.EppSessionCookieStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class EppHttpClientConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RestClient eppRestClient(final EppPropertiesProvider eppProps, final EppSessionCookieStore cookieStore) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(eppProps.getConnectTimeoutMillis()));
        requestFactory.setReadTimeout(Duration.ofMillis(eppProps.getReadTimeoutMillis()));

        return RestClient.builder()
            .requestFactory(requestFactory)
            .requestInterceptor(new EppCookieInterceptor(cookieStore))
            .build();
    }
}
```

The `RestClient` bean is `@ConditionalOnMissingBean` so a host application can supply its own fully-customized instance (corporate proxy, mTLS, extra interceptors) and override this default, matching the existing extensibility pattern already used for `EppPropertiesProvider` in `EppClientAutoConfiguration`.

- [ ] **Step 8: Update `EppClient` to use `EppSessionCookieStore`**

In `src/main/java/gr/netmechanics/epp/client/EppClient.java`:

Replace:
```java
import static gr.netmechanics.epp.client.EppConstants.BEAN_EPP_CLIENT;
import static gr.netmechanics.epp.client.EppConstants.BEAN_SHARED_HEADERS;
import static gr.netmechanics.epp.client.impl.EppCommandRequest.request;

import java.time.Instant;
import java.util.Map;
```
with:
```java
import static gr.netmechanics.epp.client.EppConstants.BEAN_EPP_CLIENT;
import static gr.netmechanics.epp.client.impl.EppCommandRequest.request;

import java.time.Instant;
```

Replace:
```java
    private final EppGateway eppGateway;

    @Qualifier(BEAN_SHARED_HEADERS)
    private final Map<String, Object> sharedHeaders;
```
with:
```java
    private final EppGateway eppGateway;
    private final EppSessionCookieStore cookieStore;
```

Remove the now-unused `import org.springframework.beans.factory.annotation.Qualifier;` line.

Replace:
```java
    private void clear() {
        sessionExpiresAt = null;
        greeting = null;
        sharedHeaders.clear();
    }
```
with:
```java
    private void clear() {
        sessionExpiresAt = null;
        greeting = null;
        cookieStore.clear();
    }
```

- [ ] **Step 9: Simplify `EppRefreshListener`**

Edit `src/main/java/gr/netmechanics/epp/client/EppRefreshListener.java` — replace the whole file:

```java
package gr.netmechanics.epp.client;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EppRefreshListener {

    @Lazy
    private final EppClient eppClient;

    @Lazy
    private final ConfigurableApplicationContext context;

    @EventListener
    public void onRefresh(final EppRefreshEvent event) {
        eppClient.setEppProps(context.getBean(EppPropertiesProvider.class));
    }
}
```

- [ ] **Step 10: Compile**

`./gradlew compileJava compileTestJava -Dcheckstyle.skip=true`

Expected: `BUILD SUCCESSFUL`. If it fails, the error will point at a leftover reference to one of the removed classes/constants — fix it before continuing.

- [ ] **Step 11: Run the full test suite against the real sandbox**

`./gradlew test -Dcheckstyle.skip=true --console=plain`

Expected: `BUILD SUCCESSFUL`, all tests in `EppClientTestSuite` pass (`SessionTests`, `ContactTests`, `HostTests`, `DomainTests`, `RegistrarTests`, `RefreshEppTest`) — this is the real verification for this task, since it exercises login, session-cookie reuse across multiple commands, and the refresh-event flow end-to-end against the live sandbox.

- [ ] **Step 12: Run checkstyle**

`./gradlew checkstyleMain checkstyleTest`

Expected: `BUILD SUCCESSFUL`. Fix any violations (e.g. import order, line length) before continuing.

- [ ] **Step 13: Commit**

```bash
git add build.gradle \
        src/main/java/gr/netmechanics/epp/client/EppGateway.java \
        src/main/java/gr/netmechanics/epp/client/RestEppGateway.java \
        src/main/java/gr/netmechanics/epp/client/EppClient.java \
        src/main/java/gr/netmechanics/epp/client/EppRefreshListener.java \
        src/main/java/gr/netmechanics/epp/client/EppConstants.java \
        src/main/java/gr/netmechanics/epp/client/EppProperties.java \
        src/main/java/gr/netmechanics/epp/client/EppPropertiesProvider.java \
        src/main/java/gr/netmechanics/epp/client/EppPropertiesDefaultProvider.java \
        src/main/java/gr/netmechanics/epp/client/config/EppClientAutoConfiguration.java \
        src/main/java/gr/netmechanics/epp/client/config/EppHttpClientConfiguration.java
git rm src/main/java/gr/netmechanics/epp/client/EppRequestFlowManager.java \
       src/main/java/gr/netmechanics/epp/client/config/EppGatewayConfiguration.java \
       src/main/java/gr/netmechanics/epp/client/xml/ObjectToXMLTransformer.java \
       src/main/java/gr/netmechanics/epp/client/xml/XMLToObjectTransformer.java
git commit -m "feat: migrate EPP transport from Spring Integration HTTP to RestClient"
```

---

### Task 4: Cleanup verification

**Files:** none created; verification only.

- [ ] **Step 1: Confirm no Spring Integration references remain**

```bash
grep -rn "springframework.integration\|MessagingGateway\|IntegrationFlow\|ChannelInterceptor\|MessageChannel\|IntegrationComponentScan" src/
```

Expected: no output. If anything remains, it was missed in Task 3 — fix it.

- [ ] **Step 2: Confirm the dependency is actually gone from the resolved classpath**

```bash
./gradlew dependencies --configuration runtimeClasspath | grep -i "spring-integration"
```

Expected: no output.

- [ ] **Step 3: Full clean build**

```bash
./gradlew clean build -Dcheckstyle.skip=false
```

Expected: `BUILD SUCCESSFUL` — this runs compilation, checkstyle, the full sandbox-backed test suite, and jacoco in one pass.

- [ ] **Step 4: Manually verify the Content-Type fix and password redaction**

Temporarily set `build.gradle`'s test include to `include '**/SessionTests.class'`, then:

`./gradlew test --tests "gr.netmechanics.epp.client.SessionTests.test_login" -Dcheckstyle.skip=true --console=plain`

Inspect `build/test-results/test/TEST-gr.netmechanics.epp.client.SessionTests.xml` for the `<system-out>` DEBUG log of the login request: confirm the `<pw>` content shows `***REDACTED***` instead of the real password. Then restore `build.gradle`'s test include to `**/EppClientTestSuite.class`.

- [ ] **Step 5: Report to the user**

Summarize what changed, what was fixed along the way (Content-Type, timeouts, exception wrapping, cookie parsing, password redaction), and confirm the full suite is green. Do not commit this step (nothing to commit — verification only).
