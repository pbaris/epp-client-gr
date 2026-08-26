# EppClient Domain Command Split Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Split `EppClient`'s 20 flattened per-command methods into four small per-object command
classes (`EppDomainCommands`, `EppHostCommands`, `EppContactCommands`, `EppRegistrarCommands`)
reached via `eppClient.domains()`/`.hosts()`/`.contacts()`/`.registrar()`, resolving the "Monster
Class" coupling warning (25 dependencies, limit 20) without changing `EppClient`'s bean identity or
session/retry behavior.

**Architecture:** `EppClient` keeps its session lifecycle (`login`/`logout`/`hello`) and all
retry/reconnect internals unchanged, and implements a new package-private `EppCommandSender`
interface (`send(EppRequest)`) that adapts to its existing private `sendCommandRequest`. Four new
public classes hold one field (`EppCommandSender`) and delegate every method to `sender.send(...)`.
This is an intentional breaking change to the public API — no deprecation shim.

**Tech Stack:** Plain Java (no new dependencies). Existing sandbox integration test suite
(`EppClientTestSuite`) is the regression check — no new unit tests are needed for the four
delegate classes since they contain no independent logic (see spec Section 5).

**Spec:** `docs/superpowers/specs/2026-08-26-eppclient-domain-command-split-design.md`

## Global Constraints

- No behavior change: every command must still go through `EppClient`'s existing
  `ensureConnected → gateway.sendCommand → retry-once-on-2201` path.
- `login()`, `logout()`, `hello()` keep their exact current signatures on `EppClient`.
- No deprecation shim — the 20 flattened methods are removed outright, not kept as delegating
  wrappers.
- The four new command classes have package-private constructors — only `EppClient` constructs
  them (`new EppDomainCommands(this)`, etc.), never directly instantiable by host apps.
- `EppCommandSender` is package-private — not part of the public API.
- Every source file follows this project's existing Lombok/Checkstyle conventions: `final`
  parameters, no comments except where a non-obvious constraint justifies one.
- The full `EppClientTestSuite` must keep passing against the real ICS-FORTH sandbox after this
  change.
- `build.gradle`'s `test` task restricts execution to `**/EppClientTestSuite.class`. To run an
  individual test class while developing, temporarily edit that `include` line, run, then restore
  it — do not leave it changed.
- Never commit unless explicitly asked.

---

### Task 1: The four command classes + `EppCommandSender`

**Files:**
- Create: `src/main/java/gr/netmechanics/epp/client/EppCommandSender.java`
- Create: `src/main/java/gr/netmechanics/epp/client/EppDomainCommands.java`
- Create: `src/main/java/gr/netmechanics/epp/client/EppHostCommands.java`
- Create: `src/main/java/gr/netmechanics/epp/client/EppContactCommands.java`
- Create: `src/main/java/gr/netmechanics/epp/client/EppRegistrarCommands.java`

**Interfaces:**
- Consumes: `gr.netmechanics.epp.client.impl.EppRequest` (existing marker interface — every
  concrete request type, e.g. `DomainCheckRequest`, already implements it transitively via
  `CheckRequest`/`CreateRequest`/`InfoRequest`/`UpdateRequest`/`DeleteRequest`/`RenewRequest`/
  `TransferRequest`), `gr.netmechanics.epp.client.impl.EppCommandResponse` (existing).
- Produces: `EppCommandSender` — a package-private functional interface with one method,
  `EppCommandResponse send(EppRequest eppRequest)` — that Task 2 will make `EppClient` implement.
  `EppDomainCommands`/`EppHostCommands`/`EppContactCommands`/`EppRegistrarCommands` — public
  classes, each with a package-private `(EppCommandSender)` constructor, that Task 2 will
  instantiate from inside `EppClient`.

- [ ] **Step 1: Create `EppCommandSender.java`**

```java
package gr.netmechanics.epp.client;

import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.EppRequest;

interface EppCommandSender {

    EppCommandResponse send(EppRequest eppRequest);
}
```

- [ ] **Step 2: Create `EppDomainCommands.java`**

```java
package gr.netmechanics.epp.client;

import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.commands.check.domain.DomainCheckRequest;
import gr.netmechanics.epp.client.impl.commands.create.domain.DomainCreateRequest;
import gr.netmechanics.epp.client.impl.commands.delete.domain.DomainDeleteRequest;
import gr.netmechanics.epp.client.impl.commands.info.domain.DomainInfoRequest;
import gr.netmechanics.epp.client.impl.commands.renew.domain.DomainRenewRequest;
import gr.netmechanics.epp.client.impl.commands.transfer.domain.DomainTransferRequest;
import gr.netmechanics.epp.client.impl.commands.update.domain.DomainUpdateRequest;
import org.springframework.lang.NonNull;

/**
 * Domain commands (RFC3731), reached via {@link EppClient#domains()}.
 */
public class EppDomainCommands {

    private final EppCommandSender sender;

    EppDomainCommands(final EppCommandSender sender) {
        this.sender = sender;
    }

    /**
     * Retrieves detailed information about a specific domain.
     *
     * @param infoRequest the request containing details of the domain for which information is being queried
     * @return the response from the EPP server containing the domain information
     */
    public EppCommandResponse info(@NonNull final DomainInfoRequest infoRequest) {
        return sender.send(infoRequest);
    }

    /**
     * Checks the availability of specified domain names against the EPP server.
     *
     * @param checkRequest the request containing the domain names to be checked for availability
     * @return the response from the EPP server indicating the availability of the specified domain names
     */
    public EppCommandResponse check(@NonNull final DomainCheckRequest checkRequest) {
        return sender.send(checkRequest);
    }

    /**
     * Creates a new domain.
     *
     * @param createRequest the request object containing the details of the domain to be created
     * @return the response from the EPP server indicating the result of the domain creation process
     */
    public EppCommandResponse create(@NonNull final DomainCreateRequest createRequest) {
        return sender.send(createRequest);
    }

    /**
     * Updates an existing domain.
     *
     * @param updateRequest the request containing the details of the changes to be made to the domain,
     *                      such as contact modifications, name server adjustments, or status updates
     * @return the response from the EPP server indicating the result of the domain update process
     */
    public EppCommandResponse update(@NonNull final DomainUpdateRequest updateRequest) {
        return sender.send(updateRequest);
    }

    /**
     * Renews an existing domain.
     *
     * @param renewRequest the request containing the details of the domain to be renewed,
     *                     including its name, current expiration date, and renewal period
     * @return the response from the EPP server indicating the result of the domain renewal process
     */
    public EppCommandResponse renew(@NonNull final DomainRenewRequest renewRequest) {
        return sender.send(renewRequest);
    }

    /**
     * Transfers a domain.
     *
     * @param transferRequest the request containing the details of the domain transfer,
     *                        including the domain name, transfer operation type, and authentication code
     * @return the response from the EPP server indicating the result of the domain transfer process
     */
    public EppCommandResponse transfer(@NonNull final DomainTransferRequest transferRequest) {
        return sender.send(transferRequest);
    }

    /**
     * Deletes a domain.
     *
     * @param deleteRequest the request containing the details of the domain to be deleted, including its name
     * @return the response from the EPP server indicating the result of the domain deletion process
     */
    public EppCommandResponse delete(@NonNull final DomainDeleteRequest deleteRequest) {
        return sender.send(deleteRequest);
    }
}
```

- [ ] **Step 3: Create `EppHostCommands.java`**

```java
package gr.netmechanics.epp.client;

import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.commands.check.host.HostCheckRequest;
import gr.netmechanics.epp.client.impl.commands.create.host.HostCreateRequest;
import gr.netmechanics.epp.client.impl.commands.delete.host.HostDeleteRequest;
import gr.netmechanics.epp.client.impl.commands.info.host.HostInfoRequest;
import gr.netmechanics.epp.client.impl.commands.update.host.HostUpdateRequest;
import org.springframework.lang.NonNull;

/**
 * Host commands (RFC3732), reached via {@link EppClient#hosts()}.
 */
public class EppHostCommands {

    private final EppCommandSender sender;

    EppHostCommands(final EppCommandSender sender) {
        this.sender = sender;
    }

    /**
     * Retrieves detailed information about a specific host.
     *
     * @param infoRequest the request containing details of the host for which information is being queried
     * @return the response from the EPP server containing the host information
     */
    public EppCommandResponse info(@NonNull final HostInfoRequest infoRequest) {
        return sender.send(infoRequest);
    }

    /**
     * Checks the availability of specified hostnames against the EPP server.
     *
     * @param checkRequest the request containing the hostnames to be checked for availability
     * @return the response from the EPP server indicating the availability of the specified hostnames
     */
    public EppCommandResponse check(@NonNull final HostCheckRequest checkRequest) {
        return sender.send(checkRequest);
    }

    /**
     * Creates a new host.
     *
     * @param createRequest the request object containing the details of the host to be created,
     *                      including its name and optional IP addresses
     * @return the response from the EPP server indicating the result of the host creation process
     */
    public EppCommandResponse create(@NonNull final HostCreateRequest createRequest) {
        return sender.send(createRequest);
    }

    /**
     * Updates an existing host.
     *
     * @param updateRequest the request containing the details of the changes to be made to the host,
     *                      such as name modifications and IP address adjustment, or status updates
     * @return the response from the EPP server indicating the result of the host update process
     */
    public EppCommandResponse update(@NonNull final HostUpdateRequest updateRequest) {
        return sender.send(updateRequest);
    }

    /**
     * Deletes a host.
     *
     * @param deleteRequest the request containing the details of the host to be deleted, including its name
     * @return the response from the EPP server indicating the result of the host deletion process
     */
    public EppCommandResponse delete(@NonNull final HostDeleteRequest deleteRequest) {
        return sender.send(deleteRequest);
    }
}
```

- [ ] **Step 4: Create `EppContactCommands.java`**

```java
package gr.netmechanics.epp.client;

import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.commands.check.contact.ContactCheckRequest;
import gr.netmechanics.epp.client.impl.commands.create.contact.ContactCreateRequest;
import gr.netmechanics.epp.client.impl.commands.info.contact.ContactInfoRequest;
import gr.netmechanics.epp.client.impl.commands.update.contact.ContactUpdateRequest;
import org.springframework.lang.NonNull;

/**
 * Contact commands (RFC3733), reached via {@link EppClient#contacts()}.
 */
public class EppContactCommands {

    private final EppCommandSender sender;

    EppContactCommands(final EppCommandSender sender) {
        this.sender = sender;
    }

    /**
     * Retrieves detailed information about a specific contact.
     *
     * @param infoRequest the request containing details of the contact for which information is being queried
     * @return the response from the EPP server containing the contact information
     */
    public EppCommandResponse info(@NonNull final ContactInfoRequest infoRequest) {
        return sender.send(infoRequest);
    }

    /**
     * Checks the availability of specified contact IDs against the EPP server.
     *
     * @param checkRequest the request containing the contact IDs to be checked for availability
     * @return the response from the EPP server indicating the availability of the specified contact IDs
     */
    public EppCommandResponse check(@NonNull final ContactCheckRequest checkRequest) {
        return sender.send(checkRequest);
    }

    /**
     * Creates a new contact.
     *
     * @param createRequest the request object containing the details of the contact to be created
     * @return the response from the EPP server indicating the result of the contact creation process
     */
    public EppCommandResponse create(@NonNull final ContactCreateRequest createRequest) {
        return sender.send(createRequest);
    }

    /**
     * Updates an existing contact.
     *
     * @param updateRequest the request containing the details of the changes to be made to the contact,
     *                      such as contact information modifications or status updates
     * @return the response from the EPP server indicating the result of the contact update process
     */
    public EppCommandResponse update(@NonNull final ContactUpdateRequest updateRequest) {
        return sender.send(updateRequest);
    }
}
```

- [ ] **Step 5: Create `EppRegistrarCommands.java`**

```java
package gr.netmechanics.epp.client;

import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.commands.info.registrar.RegistrarInfoRequest;
import org.springframework.lang.NonNull;

/**
 * Registrar commands (RFC3733), reached via {@link EppClient#registrar()}.
 */
public class EppRegistrarCommands {

    private final EppCommandSender sender;

    EppRegistrarCommands(final EppCommandSender sender) {
        this.sender = sender;
    }

    /**
     * Retrieves detailed information about the registrar account.
     *
     * @param infoRequest the request for which registrar information is being queried
     * @return the response from the EPP server containing the registrar information
     */
    public EppCommandResponse info(@NonNull final RegistrarInfoRequest infoRequest) {
        return sender.send(infoRequest);
    }
}
```

- [ ] **Step 6: Compile to verify the new files are syntactically and referentially correct**

Run: `./gradlew compileJava -Dcheckstyle.skip=true --console=plain`

Expected: `BUILD SUCCESSFUL`. `EppClient` doesn't reference these new classes yet, so this only
proves the four new files compile standalone against the existing `impl` request types.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/gr/netmechanics/epp/client/EppCommandSender.java \
        src/main/java/gr/netmechanics/epp/client/EppDomainCommands.java \
        src/main/java/gr/netmechanics/epp/client/EppHostCommands.java \
        src/main/java/gr/netmechanics/epp/client/EppContactCommands.java \
        src/main/java/gr/netmechanics/epp/client/EppRegistrarCommands.java
git commit -m "feat: add per-object EPP command classes

Add EppDomainCommands/EppHostCommands/EppContactCommands/EppRegistrarCommands
and the package-private EppCommandSender interface they delegate through.
Not yet wired into EppClient (next commit)."
```

---

### Task 2: Wire `EppClient` to the new command classes and update all callers

**Files:**
- Modify: `src/main/java/gr/netmechanics/epp/client/EppClient.java`
- Modify: `src/test/java/gr/netmechanics/epp/client/DomainTests.java`
- Modify: `src/test/java/gr/netmechanics/epp/client/HostTests.java`
- Modify: `src/test/java/gr/netmechanics/epp/client/ContactTests.java`
- Modify: `src/test/java/gr/netmechanics/epp/client/RegistrarTests.java`
- Modify: `src/test/java/gr/netmechanics/epp/client/EppClientSessionTest.java`

**Interfaces:**
- Consumes: `EppCommandSender`, `EppDomainCommands`, `EppHostCommands`, `EppContactCommands`,
  `EppRegistrarCommands` from Task 1.
- Produces: `EppClient.domains(): EppDomainCommands`, `.hosts(): EppHostCommands`,
  `.contacts(): EppContactCommands`, `.registrar(): EppRegistrarCommands` — the new public call
  shape every test file in this task (and any future caller) uses instead of the removed flattened
  methods.

This task removes the 20 flattened methods and updates every existing caller in the same step, so
the module compiles (and the sandbox suite passes) only once the whole task is done — do not stop
partway.

- [ ] **Step 1: Replace `EppClient.java` in full**

```java
package gr.netmechanics.epp.client;

import static gr.netmechanics.epp.client.EppConstants.BEAN_EPP_CLIENT;
import static gr.netmechanics.epp.client.impl.EppCommandRequest.request;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import gr.netmechanics.epp.client.error.EppGatewayException;
import gr.netmechanics.epp.client.impl.EppCommandRequest;
import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.EppRequest;
import gr.netmechanics.epp.client.impl.EppResponseResult;
import gr.netmechanics.epp.client.impl.EppResultCodes;
import gr.netmechanics.epp.client.impl.commands.Hello;
import gr.netmechanics.epp.client.impl.commands.LoginRequest;
import gr.netmechanics.epp.client.impl.commands.LogoutRequest;
import gr.netmechanics.epp.client.impl.elements.Greeting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Command methods are grouped by object type and reached via {@link #domains()}, {@link #hosts()},
 * {@link #contacts()} and {@link #registrar()}. Every command returns {@code null} if no EPP
 * session could be established with the server.
 */
@Slf4j
@Component(BEAN_EPP_CLIENT)
@RequiredArgsConstructor
public class EppClient implements EppCommandSender {

    private final EppGateway eppGateway;
    private final EppSessionCookieStore cookieStore;

    private final ReentrantLock connectionLock = new ReentrantLock();

    private final AtomicReference<EppPropertiesProvider> eppProps = new AtomicReference<>();
    private final AtomicReference<Greeting> greeting = new AtomicReference<>();
    private final AtomicLong sessionGeneration = new AtomicLong();

    private final EppDomainCommands domainCommands = new EppDomainCommands(this);
    private final EppHostCommands hostCommands = new EppHostCommands(this);
    private final EppContactCommands contactCommands = new EppContactCommands(this);
    private final EppRegistrarCommands registrarCommands = new EppRegistrarCommands(this);

    private volatile boolean connected;

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

            EppCommandResponse response = eppGateway.sendCommand(request(loginRequest, eppProps.get().getClTrId()));
            connected = response.isSuccess();
            if (connected) {
                sessionGeneration.incrementAndGet();
            }
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
    public EppCommandResponse logout() {
        connectionLock.lock();
        try {
            EppCommandResponse cmd = eppGateway.sendCommand(request(new LogoutRequest(), eppProps.get().getClTrId()));
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

    /**
     * Domain commands (RFC3731).
     *
     * @return the domain command group
     */
    public EppDomainCommands domains() {
        return domainCommands;
    }

    /**
     * Host commands (RFC3732).
     *
     * @return the host command group
     */
    public EppHostCommands hosts() {
        return hostCommands;
    }

    /**
     * Contact commands (RFC3733).
     *
     * @return the contact command group
     */
    public EppContactCommands contacts() {
        return contactCommands;
    }

    /**
     * Registrar commands (RFC3733).
     *
     * @return the registrar command group
     */
    public EppRegistrarCommands registrar() {
        return registrarCommands;
    }

    @Override
    public EppCommandResponse send(final EppRequest eppRequest) {
        return sendCommandRequest(request(eppRequest, eppProps.get().getClTrId()));
    }

    private EppCommandResponse sendCommandRequest(@NonNull final EppCommandRequest command) {
        ensureConnected();

        // Prevent gateway invocation if connect attempt failed, matching EppClientSessionTest#hello_failure_during_connect_does_not_throw
        if (!connected) {
            log.error("Not connected to the EPP server, command not sent: {}", command);
            return null;
        }

        long seenGeneration = sessionGeneration.get();
        EppCommandResponse response = eppGateway.sendCommand(command);
        if (isAuthorizationError(response) && reconnect(seenGeneration)) {
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

    private boolean reconnect(final long seenGeneration) {
        connectionLock.lock();
        try {
            if (connected && sessionGeneration.get() != seenGeneration) {
                log.debug("Session was already reconnected by another thread, retrying without reconnecting again");
                return true;
            }

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

        EppPropertiesProvider props = eppProps.get();
        String language = props.getLanguage();
        if (!greet.getLanguages().contains(language)) {
            language = greet.getDefaultLanguage();
        }

        LoginRequest loginRequest = LoginRequest.builder()
            .clientId(props.getClientId())
            .password(props.getPassword())
            .language(language)
            .version(greet.getVersion())
            .objectUris(greet.getObjectUris())
            .build();

        return login(loginRequest).isSuccess();
    }

    private Greeting getGreeting() {
        if (greeting.get() == null) {
            try {
                greeting.set(hello());

            } catch (EppGatewayException e) {
                log.error("Checking with hello failed, we are not connected, returning false!");
            }
        }

        return greeting.get();
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
        greeting.set(null);
        cookieStore.clear();
    }

    @Autowired
    void setEppProps(final EppPropertiesProvider eppProps) {
        connectionLock.lock();
        try {
            boolean hadSession = connected;
            this.eppProps.set(eppProps);
            if (hadSession) {
                logout();
            } else {
                clear();
            }
        } finally {
            connectionLock.unlock();
        }
    }
}
```

- [ ] **Step 2: Update `DomainTests.java` call sites**

Replace every occurrence (10 call sites) per this mapping — same request variable, only the
receiver expression changes:

| Before | After |
|---|---|
| `eppClient.createDomain(createRequest)` | `eppClient.domains().create(createRequest)` |
| `eppClient.getDomainInfo(infoRequest)` | `eppClient.domains().info(infoRequest)` |
| `eppClient.checkDomains(checkRequest)` | `eppClient.domains().check(checkRequest)` |
| `eppClient.renewDomain(renewRequest)` | `eppClient.domains().renew(renewRequest)` |
| `eppClient.updateDomain(updateRequest)` | `eppClient.domains().update(updateRequest)` |

These appear at (line numbers from the pre-Task-2 file, for locating them — re-check after Step 1
since nothing in this file shifts): `createDomain` (line 149), `getDomainInfo` (lines 164, 257),
`checkDomains` (lines 206, 229), `renewDomain` (line 269), `updateDomain` (lines 290, 300, 311,
322).

- [ ] **Step 3: Update `HostTests.java` call sites**

| Before | After |
|---|---|
| `eppClient.createHost(createRequest)` | `eppClient.hosts().create(createRequest)` |
| `eppClient.getHostInfo(infoRequest)` | `eppClient.hosts().info(infoRequest)` |
| `eppClient.checkHosts(checkRequest)` | `eppClient.hosts().check(checkRequest)` |
| `eppClient.updateHost(updateRequest)` | `eppClient.hosts().update(updateRequest)` |
| `eppClient.deleteHost(...)` | `eppClient.hosts().delete(...)` |

These appear at: `createHost` (lines 95, 103, 115), `updateHost` (lines 129, 136),
`getHostInfo` (line 146), `checkHosts` (lines 161, 186), `deleteHost` (lines 210, 214).

- [ ] **Step 4: Update `ContactTests.java` call sites**

| Before | After |
|---|---|
| `eppClient.getContactInfo(infoRequest)` | `eppClient.contacts().info(infoRequest)` |
| `eppClient.checkContacts(checkRequest)` | `eppClient.contacts().check(checkRequest)` |
| `eppClient.updateContact(updateRequest)` | `eppClient.contacts().update(updateRequest)` |

These appear at: `getContactInfo` (line 93), `checkContacts` (lines 113, 138),
`updateContact` (line 175).

- [ ] **Step 5: Update `RegistrarTests.java` call site**

| Before | After |
|---|---|
| `eppClient.getRegistrarInfo(infoRequest)` | `eppClient.registrar().info(infoRequest)` |

This appears at line 30.

- [ ] **Step 6: Update `EppClientSessionTest.java` call sites**

| Before | After |
|---|---|
| `client.checkDomains(DomainCheckRequest.builder().domainNames("example.gr").build())` | `client.domains().check(DomainCheckRequest.builder().domainNames("example.gr").build())` |

This exact expression appears 5 times (lines 50, 79, 95, 117, 158) — one per test method. No other
change needed in this file: the `FakeEppGateway` test double and all its assertions
(`loginAttempts`, `businessAttempts`) operate at the `EppGateway` level, below this refactor.

- [ ] **Step 7: Compile everything**

Run: `./gradlew compileJava compileTestJava -Dcheckstyle.skip=true --console=plain`

Expected: `BUILD SUCCESSFUL`. If it fails, grep the 5 files above for any remaining old-style call
(`eppClient.checkDomains(`, `eppClient.createDomain(`, `eppClient.getDomainInfo(`,
`eppClient.updateDomain(`, `eppClient.renewDomain(`, `eppClient.transferDomain(`,
`eppClient.deleteDomain(`, `eppClient.checkHosts(`, `eppClient.createHost(`,
`eppClient.getHostInfo(`, `eppClient.updateHost(`, `eppClient.deleteHost(`,
`eppClient.checkContacts(`, `eppClient.createContact(`, `eppClient.getContactInfo(`,
`eppClient.updateContact(`, `eppClient.getRegistrarInfo(`, `client.checkDomains(`) you missed one.

- [ ] **Step 8: Run checkstyle**

Run: `./gradlew checkstyleMain checkstyleTest --console=plain`

Expected: `BUILD SUCCESSFUL`. The only acceptable pre-existing violation is the unrelated
`FinalParameters` warning in `XmlUtil.java` — not yours to fix. Fix anything else checkstyle
reports in any file this task touched.

- [ ] **Step 9: Run the full suite against the real sandbox**

Run: `./gradlew test -Dcheckstyle.skip=true --console=plain`

Expected: `BUILD SUCCESSFUL` — every class in `EppClientTestSuite` passes, including
`DomainTests`, `HostTests`, `ContactTests`, `RegistrarTests` (now exercising the new accessor call
shape), `SessionTests`/`RefreshEppTest` (unaffected, still exercising `login`/`logout`/`hello`/
`setEppProps`), and `EppClientSessionTest` (now exercising `client.domains().check(...)`).

- [ ] **Step 10: Commit**

```bash
git add src/main/java/gr/netmechanics/epp/client/EppClient.java \
        src/test/java/gr/netmechanics/epp/client/DomainTests.java \
        src/test/java/gr/netmechanics/epp/client/HostTests.java \
        src/test/java/gr/netmechanics/epp/client/ContactTests.java \
        src/test/java/gr/netmechanics/epp/client/RegistrarTests.java \
        src/test/java/gr/netmechanics/epp/client/EppClientSessionTest.java
git commit -m "feat: split EppClient's flattened commands into per-object classes

Remove the 20 flattened checkX/createX/... methods from EppClient in favor
of eppClient.domains()/.hosts()/.contacts()/.registrar() accessors backed
by EppDomainCommands/EppHostCommands/EppContactCommands/EppRegistrarCommands.
Resolves the Monster Class coupling warning (25 deps, limit 20). Breaking
change to the public API; no deprecation shim. Session/retry behavior in
EppClient is otherwise unchanged."
```

---

### Task 3: Update documentation

**Files:**
- Modify: `CLAUDE.md`
- Modify: `README.adoc`

- [ ] **Step 1: Update `CLAUDE.md`'s Architecture section**

Find the `EppClient` bullet (currently starts "**`EppClient`** is the single public API bean...")
and replace its first two sentences with:

```
- **`EppClient`** is the single public API bean (`BEAN_EPP_CLIENT = "eppClient"`), reached via
  `eppClient.domains()`/`.hosts()`/`.contacts()`/`.registrar()`, each returning a small per-object
  command class (`EppDomainCommands`, `EppHostCommands`, `EppContactCommands`,
  `EppRegistrarCommands`) whose methods (`check`, `create`, `update`, `info`, ...) delegate through
  the package-private `EppCommandSender` interface back to `EppClient`'s session/retry logic.
```

Keep the rest of that bullet (session management, cached `Greeting`, reactive reconnect,
`EppSessionCookieStore`) unchanged — none of that behavior changed.

- [ ] **Step 2: Update the Testing section's suite description if it lists method names**

Check the "Testing" section's mention of test classes — it lists class names only
(`SessionTests`, `ContactTests`, ...), not method call shapes, so no change is expected there.
Confirm by re-reading that section after Step 1; if it does mention the old flattened method
names anywhere, update to the new accessor shape.

- [ ] **Step 3: Update `README.adoc`'s EppClient section**

Replace:

```adoc
=== EppClient

Inject/Autowire the EppClient to your code.
```

with:

```adoc
=== EppClient

Inject/Autowire the EppClient to your code. Commands are grouped by object type:

[source,java]
----
eppClient.domains().check(DomainCheckRequest.builder().domainNames("example.gr").build());
eppClient.hosts().info(HostInfoRequest.builder().hostName("ns1.example.gr").build());
eppClient.contacts().create(ContactCreateRequest.builder()...build());
eppClient.registrar().info(new RegistrarInfoRequest());
----

Session management (`login`, `logout`, `hello`) stays directly on `EppClient` and is handled
automatically — you don't need to call it yourself in normal use.
```

- [ ] **Step 4: Commit**

```bash
git add CLAUDE.md README.adoc
git commit -m "docs: describe EppClient's per-object command accessors

Update the architecture description and usage example for the
domains()/hosts()/contacts()/registrar() split."
```
