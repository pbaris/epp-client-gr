# EppClient Session Concurrency & Reactive Reconnect Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace `EppClient`'s arbitrary 13-minute client-side session-expiry guess with (a) a `ReentrantLock`-guarded connect sequence so concurrent callers on the same `EppClient` bean serialize into a single login instead of racing into duplicate/corrupting logins, and (b) reactive detection: any command that comes back with EPP result code 2201 (`AUTHORIZATION_ERROR`, i.e. "you're not authenticated for this") triggers exactly one automatic reconnect-and-retry, so the client adapts to however long the registry's real session actually lasts instead of guessing.

**Architecture:** All session-mutating entry points (`login`, `logout`, the internal `connect`/`reconnect`) go through one `ReentrantLock`. Normal business calls (`checkDomains`, `createContact`, etc.) take a fast, lock-free path once connected (`ensureConnected()` checks a `volatile boolean connected` before ever touching the lock), so concurrent business traffic isn't serialized — only the connect/reconnect sequence is. On a command response carrying result code 2201, `sendCommandRequest` acquires the lock, clears local session state, reconnects, and retries the same command exactly once; a second failure of any kind is returned to the caller as-is (no infinite retry, no retry on other error codes like bad credentials or object-not-found).

**Tech Stack:** `java.util.concurrent.locks.ReentrantLock` (JDK, no new dependency). Tests use a hand-written `EppGateway` test double (not Mockito) so the concurrency test can precisely control when a simulated login "arrives" — this mirrors the pattern established by `RestEppGatewayTest` in the prior migration (real `EppXmlCodec`/`XmlMapper` deserializing literal XML fixtures, since `Greeting`/`EppCommandResponse` have Jackson-only private constructors and aren't hand-buildable).

## Global Constraints

- No change to `EppClient`'s public method signatures — every `checkX`/`createX`/`updateX`/... method keeps its exact current signature and return type.
- Every source file follows this project's existing Lombok/Checkstyle conventions: `final` parameters, no comments except where a non-obvious constraint justifies one.
- The full `EppClientTestSuite` must keep passing against the real ICS-FORTH sandbox after this change — this is the regression check that the real login/logout/refresh flows still work end-to-end.
- `build.gradle`'s `test` task restricts execution to `**/EppClientTestSuite.class`. To run an individual test class while developing, temporarily edit that `include` line, run, then restore it — do not leave it changed.
- Never commit unless explicitly asked.

---

### Task 1: Reactive reconnect + concurrency-safe `EppClient`

**Files:**
- Modify: `src/main/java/gr/netmechanics/epp/client/EppClient.java`
- Test: `src/test/java/gr/netmechanics/epp/client/EppClientSessionTest.java` (new)

**Interfaces:**
- Consumes: `EppGateway` (unchanged interface), `EppSessionCookieStore` (unchanged, from the prior migration), `EppResultCodes.AUTHORIZATION_ERROR` (existing constant, `= 2201`), `EppCommandResponse.getResults(): List<EppResponseResult>` and `EppResponseResult.getCode(): int` / `.isSuccess(): boolean` (existing, unchanged).
- Produces: no new public API. `EppClient`'s existing public constructor (Lombok `@RequiredArgsConstructor` over `EppGateway eppGateway, EppSessionCookieStore cookieStore`) is unchanged, so it stays directly constructible in tests exactly as `RestEppGatewayTest` already relies on for `RestEppGateway`.

- [ ] **Step 1: Write the failing tests**

Create `src/test/java/gr/netmechanics/epp/client/EppClientSessionTest.java`:

```java
package gr.netmechanics.epp.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import gr.netmechanics.epp.client.impl.EppCommandRequest;
import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.EppResultCodes;
import gr.netmechanics.epp.client.impl.commands.Hello;
import gr.netmechanics.epp.client.impl.commands.check.domain.DomainCheckRequest;
import gr.netmechanics.epp.client.impl.elements.Greeting;
import gr.netmechanics.epp.client.xml.EppXmlCodec;
import org.junit.jupiter.api.Test;

class EppClientSessionTest {

    @Test
    void concurrent_calls_before_any_login_trigger_exactly_one_login() throws Exception {
        int threadCount = 20;
        CountDownLatch loginEntered = new CountDownLatch(1);
        CountDownLatch loginRelease = new CountDownLatch(1);

        FakeEppGateway gateway = new FakeEppGateway();
        gateway.blockFirstLoginUntilReleased(loginEntered, loginRelease);

        EppClient client = new EppClient(gateway, new EppSessionCookieStore());
        client.setEppProps(fixedProvider());

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<EppCommandResponse>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            Callable<EppCommandResponse> task = () -> {
                go.await();
                return client.checkDomains(DomainCheckRequest.builder().domainNames("example.gr").build());
            };
            futures.add(pool.submit(task));
        }

        go.countDown();

        assertThat(loginEntered.await(5, TimeUnit.SECONDS))
            .as("at least one thread must have entered the login critical section")
            .isTrue();
        loginRelease.countDown();

        for (Future<EppCommandResponse> future : futures) {
            assertThat(future.get(10, TimeUnit.SECONDS).isSuccess()).isTrue();
        }
        pool.shutdown();

        assertThat(gateway.loginAttempts.get()).isEqualTo(1);
        assertThat(gateway.businessAttempts.get()).isEqualTo(threadCount);
    }

    @Test
    void authorization_error_triggers_exactly_one_reconnect_and_retry() {
        FakeEppGateway gateway = new FakeEppGateway();
        gateway.failNthBusinessCallWith(1, EppResultCodes.AUTHORIZATION_ERROR);

        EppClient client = new EppClient(gateway, new EppSessionCookieStore());
        client.setEppProps(fixedProvider());

        EppCommandResponse response = client.checkDomains(
            DomainCheckRequest.builder().domainNames("example.gr").build());

        assertThat(response.isSuccess()).isTrue();
        assertThat(gateway.loginAttempts.get()).isEqualTo(2);
        assertThat(gateway.businessAttempts.get()).isEqualTo(2);
    }

    @Test
    void non_authorization_error_is_returned_without_retry() {
        FakeEppGateway gateway = new FakeEppGateway();
        gateway.failNthBusinessCallWith(1, EppResultCodes.OBJECT_DOES_NOT_EXIST);

        EppClient client = new EppClient(gateway, new EppSessionCookieStore());
        client.setEppProps(fixedProvider());

        EppCommandResponse response = client.checkDomains(
            DomainCheckRequest.builder().domainNames("example.gr").build());

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getResults().getFirst().getCode()).isEqualTo(EppResultCodes.OBJECT_DOES_NOT_EXIST);
        assertThat(gateway.loginAttempts.get()).isEqualTo(1);
        assertThat(gateway.businessAttempts.get()).isEqualTo(1);
    }

    @Test
    void hello_failure_during_connect_does_not_throw() {
        EppGateway gateway = new EppGateway() {
            @Override
            public Greeting hello(final Hello hello) {
                throw new gr.netmechanics.epp.client.error.EppGatewayException("sandbox unreachable");
            }

            @Override
            public EppCommandResponse sendCommand(final EppCommandRequest command) {
                throw new AssertionError("should never reach sendCommand if hello failed");
            }
        };

        EppClient client = new EppClient(gateway, new EppSessionCookieStore());
        client.setEppProps(fixedProvider());

        assertThat(client.checkDomains(DomainCheckRequest.builder().domainNames("example.gr").build())).isNull();
    }

    private static EppPropertiesProvider fixedProvider() {
        return new EppPropertiesProvider() {
            @Override
            public boolean isUseSandbox() {
                return true;
            }

            @Override
            public String getClientId() {
                return "client";
            }

            @Override
            public String getPassword() {
                return "password";
            }

            @Override
            public String getLanguage() {
                return "en";
            }

            @Override
            public Long getClTrId() {
                return 1L;
            }

            @Override
            public String getUrl() {
                return "http://localhost/epp/proxy";
            }
        };
    }

    private static final class FakeEppGateway implements EppGateway {

        private static final String GREETING_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
            + "<epp xmlns=\"urn:ietf:params:xml:ns:epp-1.0\">"
            + "<greeting>"
            + "<svID>.gr and .ελ ccTLD EPP Service</svID>"
            + "<svDate>2025-04-25T09:41:55.429Z</svDate>"
            + "<svcMenu>"
            + "<version>1.0</version>"
            + "<lang>en</lang>"
            + "<lang>el</lang>"
            + "<objURI>urn:ietf:params:xml:ns:host-1.0</objURI>"
            + "<objURI>urn:ietf:params:xml:ns:contact-1.0</objURI>"
            + "<objURI>urn:ietf:params:xml:ns:domain-1.0</objURI>"
            + "</svcMenu>"
            + "</greeting>"
            + "</epp>";

        private final EppXmlCodec codec = buildCodec();

        final AtomicInteger loginAttempts = new AtomicInteger();
        final AtomicInteger businessAttempts = new AtomicInteger();

        private volatile int failBusinessAttemptNumber = -1;
        private volatile int failBusinessAttemptCode;

        private volatile CountDownLatch loginEntered;
        private volatile CountDownLatch loginRelease;

        void failNthBusinessCallWith(final int attemptNumber, final int code) {
            this.failBusinessAttemptNumber = attemptNumber;
            this.failBusinessAttemptCode = code;
        }

        void blockFirstLoginUntilReleased(final CountDownLatch entered, final CountDownLatch release) {
            this.loginEntered = entered;
            this.loginRelease = release;
        }

        @Override
        public Greeting hello(final Hello hello) {
            return codec.unmarshal(GREETING_XML, Greeting.class);
        }

        @Override
        public EppCommandResponse sendCommand(final EppCommandRequest command) {
            if (command.getCommand().getLoginRequest() != null) {
                int attempt = loginAttempts.incrementAndGet();
                if (attempt == 1 && loginEntered != null) {
                    loginEntered.countDown();
                    await(loginRelease);
                }
                return codec.unmarshal(responseXml(EppResultCodes.COMMAND_COMPLETED_SUCCESSFULLY, "OK"), EppCommandResponse.class);
            }

            int attempt = businessAttempts.incrementAndGet();
            if (attempt == failBusinessAttemptNumber) {
                return codec.unmarshal(responseXml(failBusinessAttemptCode, "failed"), EppCommandResponse.class);
            }
            return codec.unmarshal(responseXml(EppResultCodes.COMMAND_COMPLETED_SUCCESSFULLY, "OK"), EppCommandResponse.class);
        }

        private static void await(final CountDownLatch latch) {
            try {
                latch.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private static String responseXml(final int code, final String message) {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
                + "<epp xmlns=\"urn:ietf:params:xml:ns:epp-1.0\">"
                + "<response>"
                + "<result code=\"" + code + "\"><msg>" + message + "</msg></result>"
                + "<trID><clTRID>test-cltrid</clTRID><svTRID>test-svtrid</svTRID></trID>"
                + "</response>"
                + "</epp>";
        }

        private static EppXmlCodec buildCodec() {
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.registerModule(new JavaTimeModule());
            return new EppXmlCodec(xmlMapper);
        }
    }
}
```

Note on the 4th test (`hello_failure_during_connect_does_not_throw`): this proves a real pre-existing latent bug fix. The current `connect()` calls `greet.getLanguages()` without checking whether `getGreeting()` returned `null` (it returns `null` when `hello()` throws, since `getGreeting()` catches `EppGatewayException` and logs instead of rethrowing) — so a `hello()` failure during connect currently causes a `NullPointerException` instead of a clean `false`/failed response. This test will fail with an NPE against the current code and must pass (return `null` from `checkDomains`, matching `sendCommandRequest`'s existing behavior of still sending the command even when the pre-connect step didn't succeed, which itself is pre-existing behavior this task is not changing — only the NPE-vs-clean-failure distinction is being fixed).

- [ ] **Step 2: Run the tests to verify they fail**

Temporarily change `build.gradle`'s test include to `include '**/EppClientSessionTest.class'`, then:

`./gradlew test --tests "gr.netmechanics.epp.client.EppClientSessionTest" -Dcheckstyle.skip=true --console=plain`

Expected: compilation succeeds (all referenced methods/constants already exist on the current `EppClient`/`EppGateway`/`EppResultCodes`), but test execution fails — the concurrency test will very likely show `loginAttempts` greater than 1 (or hang/timeout depending on scheduling), the reconnect test will show `loginAttempts == 1` instead of the expected `2` (current code has no reconnect-on-2201 logic at all, so it just returns the failed response), and the `hello_failure_during_connect_does_not_throw` test will fail with a `NullPointerException` inside `connect()`.

- [ ] **Step 3: Rewrite `EppClient.java`**

Replace the whole file with:

```java
package gr.netmechanics.epp.client;

import static gr.netmechanics.epp.client.EppConstants.BEAN_EPP_CLIENT;
import static gr.netmechanics.epp.client.impl.EppCommandRequest.request;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import gr.netmechanics.epp.client.error.EppGatewayException;
import gr.netmechanics.epp.client.impl.EppCommandRequest;
import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.EppResponseResult;
import gr.netmechanics.epp.client.impl.EppResultCodes;
import gr.netmechanics.epp.client.impl.commands.Hello;
import gr.netmechanics.epp.client.impl.commands.LoginRequest;
import gr.netmechanics.epp.client.impl.commands.LogoutRequest;
import gr.netmechanics.epp.client.impl.commands.check.contact.ContactCheckRequest;
import gr.netmechanics.epp.client.impl.commands.check.domain.DomainCheckRequest;
import gr.netmechanics.epp.client.impl.commands.check.host.HostCheckRequest;
import gr.netmechanics.epp.client.impl.commands.create.contact.ContactCreateRequest;
import gr.netmechanics.epp.client.impl.commands.create.domain.DomainCreateRequest;
import gr.netmechanics.epp.client.impl.commands.create.host.HostCreateRequest;
import gr.netmechanics.epp.client.impl.commands.delete.domain.DomainDeleteRequest;
import gr.netmechanics.epp.client.impl.commands.delete.host.HostDeleteRequest;
import gr.netmechanics.epp.client.impl.commands.info.contact.ContactInfoRequest;
import gr.netmechanics.epp.client.impl.commands.info.domain.DomainInfoRequest;
import gr.netmechanics.epp.client.impl.commands.info.host.HostInfoRequest;
import gr.netmechanics.epp.client.impl.commands.info.registrar.RegistrarInfoRequest;
import gr.netmechanics.epp.client.impl.commands.renew.domain.DomainRenewRequest;
import gr.netmechanics.epp.client.impl.commands.transfer.domain.DomainTransferRequest;
import gr.netmechanics.epp.client.impl.commands.update.contact.ContactUpdateRequest;
import gr.netmechanics.epp.client.impl.commands.update.domain.DomainUpdateRequest;
import gr.netmechanics.epp.client.impl.commands.update.host.HostUpdateRequest;
import gr.netmechanics.epp.client.impl.elements.Greeting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component(BEAN_EPP_CLIENT)
@RequiredArgsConstructor
public class EppClient {

    private final EppGateway eppGateway;
    private final EppSessionCookieStore cookieStore;

    private final ReentrantLock connectionLock = new ReentrantLock();

    private volatile EppPropertiesProvider eppProps;
    private volatile boolean connected;
    private volatile Greeting greeting;

    //region Session (RFC3730)

    /**
     * Logs in to the EPP server using the provided login request details.
     *
     * @param loginRequest the login credentials and session options required to authenticate with the server
     * @return the response from the EPP server containing the result of the login attempt
     */
    public EppCommandResponse login(@NonNull final LoginRequest loginRequest) {
        connectionLock.lock();
        try {
            if (connected) {
                logout();
            }

            EppCommandResponse response = eppGateway.sendCommand(request(loginRequest, eppProps.getClTrId()));
            connected = response.isSuccess();
            return response;
        } finally {
            connectionLock.unlock();
        }
    }

    /**
     * Logs out from the EPP server by sending a logout command request.
     *
     * @return the response from the EPP server, indicating the result of the logout attempt
     */
    @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
    public EppCommandResponse logout() {
        connectionLock.lock();
        try {
            EppCommandResponse cmd = eppGateway.sendCommand(request(new LogoutRequest(), eppProps.getClTrId()));
            clear();
            return cmd;
        } finally {
            connectionLock.unlock();
        }
    }

    /**
     * Sends a Hello message to the EPP server and retrieves the server's greeting response.
     *
     * @return the server's greeting response
     * @throws EppGatewayException if an error occurs during communication with the EPP server
     */
    public Greeting hello() {
        return eppGateway.hello(new Hello());
    }
    //endregion

    //region Domains (RFC3731)

    /**
     * Retrieves detailed information about a specific domain by sending a Domain Info request to the EPP server.
     *
     * @param infoRequest the request containing details of the domain for which information is being queried
     * @return the response from the EPP server containing the domain information
     */
    public EppCommandResponse getDomainInfo(@NonNull final DomainInfoRequest infoRequest) {
        return sendCommandRequest(request(infoRequest, eppProps.getClTrId()));
    }

    /**
     * Checks the availability of specified domain names against the EPP server.
     * This method sends a Domain Check request and retrieves the corresponding response.
     *
     * @param checkRequest the request containing the domain names to be checked for availability
     * @return the response from the EPP server indicating the availability of the specified domain names
     */
    public EppCommandResponse checkDomains(@NonNull final DomainCheckRequest checkRequest) {
        return sendCommandRequest(request(checkRequest, eppProps.getClTrId()));
    }

    /**
     * Creates a new domain by sending a Domain Create request to the EPP server.
     *
     * @param createRequest the request object containing the details of the domain to be created
     * @return the response from the EPP server indicating the result of the domain creation process
     */
    public EppCommandResponse createDomain(@NonNull final DomainCreateRequest createRequest) {
        return sendCommandRequest(request(createRequest, eppProps.getClTrId()));
    }

    /**
     * Updates an existing domain by sending a Domain Update request to the EPP server.
     *
     * @param updateRequest the request containing the details of the changes to be made to the domain,
     *                      such as contact modifications, name server adjustments, or status updates
     * @return the response from the EPP server indicating the result of the domain update process
     */
    public EppCommandResponse updateDomain(@NonNull final DomainUpdateRequest updateRequest) {
        return sendCommandRequest(request(updateRequest, eppProps.getClTrId()));
    }

    /**
     * Renews an existing domain by sending a Domain Renew request to the EPP server.
     *
     * @param renewRequest the request containing the details of the domain to be renewed,
     *                     including its name, current expiration date, and renewal period
     * @return the response from the EPP server indicating the result of the domain renewal process
     */
    public EppCommandResponse renewDomain(@NonNull final DomainRenewRequest renewRequest) {
        return sendCommandRequest(request(renewRequest, eppProps.getClTrId()));
    }

    /**
     * Transfers a domain by sending a Domain Transfer request to the EPP server.
     *
     * @param transferRequest the request containing the details of the domain transfer,
     *                        including the domain name, transfer operation type, and authentication code
     * @return the response from the EPP server indicating the result of the domain transfer process
     */
    public EppCommandResponse transferDomain(@NonNull final DomainTransferRequest transferRequest) {
        return sendCommandRequest(request(transferRequest, eppProps.getClTrId()));
    }

    /**
     * Deletes a domain by sending a Domain Delete request to the EPP server.
     *
     * @param deleteRequest the request containing the details of the domain to be deleted, including its name
     * @return the response from the EPP server indicating the result of the domain deletion process
     */
    public EppCommandResponse deleteDomain(@NonNull final DomainDeleteRequest deleteRequest) {
        return sendCommandRequest(request(deleteRequest, eppProps.getClTrId()));
    }
    //endregion

    //region Hosts (RFC3732)

    /**
     * Retrieves detailed information about a specific host by sending a Host Info request to the EPP server.
     *
     * @param infoRequest the request containing details of the host for which information is being queried
     * @return the response from the EPP server containing the host information
     */
    public EppCommandResponse getHostInfo(@NonNull final HostInfoRequest infoRequest) {
        return sendCommandRequest(request(infoRequest, eppProps.getClTrId()));
    }

    /**
     * Checks the availability of specified hostnames against the EPP server.
     * This method sends a Host Check request and retrieves the corresponding response.
     *
     * @param checkRequest the request containing the hostnames to be checked for availability
     * @return the response from the EPP server indicating the availability of the specified hostnames
     */
    public EppCommandResponse checkHosts(@NonNull final HostCheckRequest checkRequest) {
        return sendCommandRequest(request(checkRequest, eppProps.getClTrId()));
    }

    /**
     * Creates a new host by sending a Host Create request to the EPP server.
     *
     * @param createRequest the request object containing the details of the host to be created, including its name and optional IP addresses
     * @return the response from the EPP server indicating the result of the host creation process
     */
    public EppCommandResponse createHost(@NonNull final HostCreateRequest createRequest) {
        return sendCommandRequest(request(createRequest, eppProps.getClTrId()));
    }

    /**
     * Updates an existing host by sending a Host Update request to the EPP server.
     *
     * @param updateRequest the request containing the details of the changes to be made to the host,
     *                      such as name modifications and IP address adjustment, or status updates
     * @return the response from the EPP server indicating the result of the host update process
     */
    public EppCommandResponse updateHost(@NonNull final HostUpdateRequest updateRequest) {
        return sendCommandRequest(request(updateRequest, eppProps.getClTrId()));
    }

    /**
     * Deletes a host by sending a Host Delete request to the EPP server.
     *
     * @param deleteRequest the request containing the details of the host to be deleted, including its name
     * @return the response from the EPP server indicating the result of the host deletion process
     */
    public EppCommandResponse deleteHost(@NonNull final HostDeleteRequest deleteRequest) {
        return sendCommandRequest(request(deleteRequest, eppProps.getClTrId()));
    }
    //endregion

    //region Contacts (RFC3733)

    /**
     * Retrieves detailed information about a specific contact by sending a Contact Info request to the EPP server.
     *
     * @param infoRequest the request containing details of the contact for which information is being queried
     * @return the response from the EPP server containing the contact information
     */
    public EppCommandResponse getContactInfo(@NonNull final ContactInfoRequest infoRequest) {
        return sendCommandRequest(request(infoRequest, eppProps.getClTrId()));
    }

    /**
     * Retrieves detailed information about a registrar account by sending an Info request to the EPP server.
     *
     * @param infoRequest the request for which registrar information is being queried
     * @return the response from the EPP server containing the registrar information
     */
    public EppCommandResponse getRegistrarInfo(@NonNull final RegistrarInfoRequest infoRequest) {
        return sendCommandRequest(request(infoRequest, eppProps.getClTrId()));
    }

    /**
     * Checks the availability of specified contact IDs against the EPP server.
     * This method sends a Contact Check request and retrieves the corresponding response.
     *
     * @param checkRequest the request containing the contact IDs to be checked for availability
     * @return the response from the EPP server indicating the availability of the specified contact IDs
     */
    public EppCommandResponse checkContacts(@NonNull final ContactCheckRequest checkRequest) {
        return sendCommandRequest(request(checkRequest, eppProps.getClTrId()));
    }

    /**
     * Creates a new contact by sending a Contact Create request to the EPP server.
     *
     * @param createRequest the request object containing the details of the contact to be created
     * @return the response from the EPP server indicating the result of the contact creation process
     */
    public EppCommandResponse createContact(@NonNull final ContactCreateRequest createRequest) {
        return sendCommandRequest(request(createRequest, eppProps.getClTrId()));
    }

    /**
     * Updates an existing contact by sending a Contact Update request to the EPP server.
     *
     * @param updateRequest the request containing the details of the changes to be made to the contact,
     *                      such as contact information modifications or status updates
     * @return the response from the EPP server indicating the result of the contact update process
     */
    public EppCommandResponse updateContact(@NonNull final ContactUpdateRequest updateRequest) {
        return sendCommandRequest(request(updateRequest, eppProps.getClTrId()));
    }
    //endregion

    private EppCommandResponse sendCommandRequest(@NonNull final EppCommandRequest command) {
        ensureConnected();

        EppCommandResponse response = eppGateway.sendCommand(command);
        if (isAuthorizationError(response) && reconnect()) {
            response = eppGateway.sendCommand(command);
        }

        return response;
    }

    private void ensureConnected() {
        if (connected) {
            return;
        }

        connectionLock.lock();
        try {
            if (!connected) {
                connect();
            }
        } finally {
            connectionLock.unlock();
        }
    }

    private boolean reconnect() {
        connectionLock.lock();
        try {
            log.debug("Session appears to have expired, reconnecting");
            clear();
            return connect();
        } finally {
            connectionLock.unlock();
        }
    }

    private boolean connect() {
        Greeting greet = getGreeting();
        if (greet == null) {
            return false;
        }

        String language = eppProps.getLanguage();
        if (!greet.getLanguages().contains(language)) {
            language = greet.getDefaultLanguage();
        }

        LoginRequest loginRequest = LoginRequest.builder()
            .clientId(eppProps.getClientId())
            .password(eppProps.getPassword())
            .language(language)
            .version(greet.getVersion())
            .objectUris(greet.getObjectUris())
            .build();

        return login(loginRequest).isSuccess();
    }

    private Greeting getGreeting() {
        if (greeting == null) {
            try {
                greeting = hello();

            } catch (EppGatewayException e) {
                log.error("Checking with hello failed, we are not connected, returning false!");
            }
        }

        return greeting;
    }

    private boolean isAuthorizationError(final EppCommandResponse response) {
        if (response.isSuccess()) {
            return false;
        }

        List<EppResponseResult> results = response.getResults();
        return results != null && !results.isEmpty()
            && results.getFirst().getCode() == EppResultCodes.AUTHORIZATION_ERROR;
    }

    private void clear() {
        connected = false;
        greeting = null;
        cookieStore.clear();
    }

    @Autowired
    void setEppProps(final EppPropertiesProvider eppProps) {
        connectionLock.lock();
        try {
            boolean hadSession = connected;
            this.eppProps = eppProps;
            if (hadSession) {
                logout();
            }
        } finally {
            connectionLock.unlock();
        }
    }
}
```

Design notes for whoever implements this:
- `connectionLock` is a `ReentrantLock`, not `synchronized`, specifically because `connect()` (called while the lock is held by `ensureConnected()`/`reconnect()`) calls `login()`, which itself acquires the same lock — `ReentrantLock` allows the owning thread to re-acquire without deadlocking; a plain `synchronized` method calling another `synchronized` method on the same instance would also work for the same reason (intrinsic locks are reentrant too), but `ReentrantLock` was chosen so the lock/unlock pairing is visible at each call site via explicit `try`/`finally`, matching how the two-phase acquire-in-`ensureConnected`-then-in-`connect`-via-`login` flow needs to be traced.
- `sessionExpiresAt`/`Instant` is gone entirely — replaced by the `volatile boolean connected` flag, set on successful/failed login (`login()`) and cleared in `clear()`. There is no more time-based guessing of when the server's session will expire; the reactive retry-on-2201 in `sendCommandRequest` is what actually detects real expiry now, whenever it happens.
- `eppProps` and `greeting` are `volatile` because they're read from `sendCommandRequest`'s business-method call sites (e.g. `getDomainInfo`) without holding `connectionLock` — those reads need cross-thread visibility of whatever the lock-holding thread most recently wrote.
- `connect()` now null-checks `getGreeting()`'s result before touching it — the original code would `NullPointerException` here if `hello()` failed, instead of cleanly returning `false`. This is a real fix, not a refactor-only change (see the 4th test).

- [ ] **Step 4: Run the tests to verify they pass**

`./gradlew test --tests "gr.netmechanics.epp.client.EppClientSessionTest" -Dcheckstyle.skip=true --console=plain`

Expected: `BUILD SUCCESSFUL`, all 4 tests pass.

- [ ] **Step 5: Restore `build.gradle`'s test include to `**/EppClientTestSuite.class`**

- [ ] **Step 6: Add the new test class to the suite**

Edit `src/test/java/gr/netmechanics/epp/client/EppClientTestSuite.java`: add `import gr.netmechanics.epp.client.EppClientSessionTest;` (alphabetically ordered with the other same-package imports already there — check current file for exact placement) and add `EppClientSessionTest.class` to the `@SelectClasses` list (this project's checkstyle enforces import order, and the prior migration's Task 2 fix round already established this exact wiring pattern for `EppCookieInterceptorTest`/`EppXmlCodecTest` — follow the same shape).

- [ ] **Step 7: Run the full suite against the real sandbox**

`./gradlew test -Dcheckstyle.skip=true --console=plain`

Expected: `BUILD SUCCESSFUL` — every test in `EppClientTestSuite` passes, including `SessionTests` (real login/logout/password-change against the sandbox), `RefreshEppTest` (the `EppRefreshEvent`/`setEppProps` flow this task touched), and the new `EppClientSessionTest`. This is the real regression check that login/logout/reconnect still behave correctly end-to-end, not just in the isolated fake-gateway tests.

- [ ] **Step 8: Run checkstyle**

`./gradlew checkstyleMain checkstyleTest --console=plain`

Expected: `BUILD SUCCESSFUL`. Pre-existing unrelated `FinalParameters` warnings in `EppBuilder.java`/`XmlUtil.java` are not yours to fix. Fix anything checkstyle reports in `EppClient.java` or `EppClientSessionTest.java`.

- [ ] **Step 9: Commit**

```bash
git add src/main/java/gr/netmechanics/epp/client/EppClient.java \
        src/test/java/gr/netmechanics/epp/client/EppClientSessionTest.java \
        src/test/java/gr/netmechanics/epp/client/EppClientTestSuite.java
git commit -m "feat: make EppClient session handling concurrency-safe with reactive reconnect

Replace the 13-minute client-side session-expiry guess with a
ReentrantLock-guarded connect sequence (concurrent callers serialize into
a single login instead of racing) plus reactive detection: any command
response carrying EPP result code 2201 (AUTHORIZATION_ERROR) triggers one
automatic reconnect-and-retry. Also fixes a latent NullPointerException
in connect() when hello() fails."
```
