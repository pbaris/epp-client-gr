# EppClient Domain Command Split Design

**Date:** 2026-08-26
**Project:** epp-client-gr
**Approach:** Extract per-object command classes, reached via accessor methods on `EppClient`

## Summary

`EppClient` currently declares one flattened method per EPP command (`checkDomains`,
`createHost`, `updateContact`, ...), directly importing all 20 request types plus their shared
response/session types. A static-analysis coupling rule ("Monster Class" / Sonar S1200) flags this
as 25 distinct type dependencies against a configured limit of 20. Splitting the flattened methods
into four small per-object command classes — reached through accessor methods on `EppClient` — cuts
`EppClient`'s own direct dependency count while keeping `EppClient` as the single Spring bean host
apps inject. This is a breaking change to the public API surface (call shape changes), accepted
explicitly for this refactor.

**Non-goals:** no change to session/retry behavior (`sendCommandRequest`, `ensureConnected`,
`reconnect`, `connect`, the lock, the atomics), no change to `EppCommandResponse`'s shape, no change
to `login()`/`logout()`/`hello()` signatures, no new Spring beans (the four command classes are
plain objects owned by `EppClient`, not `@Component`s).

---

## Section 1: Architecture

`EppClient` keeps its `@Component(BEAN_EPP_CLIENT)` bean identity, its session lifecycle methods
(`login`, `logout`, `hello`), and every private field/method it has today
(`connectionLock`, `eppProps`, `greeting`, `sessionGeneration`, `connected`, `sendCommandRequest`,
`ensureConnected`, `reconnect`, `connect`, `getGreeting`, `isAuthorizationError`, `clear`,
`setEppProps`) untouched.

What moves out is the 20-method flattened command surface. In its place, `EppClient` exposes four
accessor methods, each returning a command object built once (in the constructor) and reused:

```java
public EppDomainCommands domains() { return domainCommands; }
public EppHostCommands hosts() { return hostCommands; }
public EppContactCommands contacts() { return contactCommands; }
public EppRegistrarCommands registrar() { return registrarCommands; }
```

To let these four classes trigger the existing session/retry logic without making it public,
`EppClient` implements a new package-private functional interface:

```java
package gr.netmechanics.epp.client;

interface EppCommandSender {
    EppCommandResponse send(EppRequest request);
}
```

`EppClient`'s existing private `sendCommandRequest(EppCommandRequest)` already does the
ensure-connected + send + retry-on-2201 work; `EppCommandSender.send(EppRequest)` is a one-line
adapter over it:

```java
@Override
public EppCommandResponse send(final EppRequest request) {
    return sendCommandRequest(request(request, eppProps.get().getClTrId()));
}
```

(`request(...)` here is the existing static import `EppCommandRequest.request`.) Each of the four
command classes is constructed with `this` (the `EppCommandSender`) and holds nothing else.

## Section 2: The Four Command Classes

New public top-level classes in `gr.netmechanics.epp.client`, each with a package-private
constructor (only `EppClient` can build them — host apps only ever obtain an instance via
`eppClient.domains()` etc., never `new EppDomainCommands(...)` directly) and one line per method
delegating to the sender. Javadoc moves verbatim from the corresponding current `EppClient` method
(reworded from "Checks the availability of specified domain names..." style to drop the
now-redundant "domain"/"host"/"contact" noun repeated in both the class name and prose, but keeping
the substantive content — e.g. RFC references, parameter/return descriptions).

### `EppDomainCommands` (RFC3731)

| New method | Old `EppClient` method | Request type |
|---|---|---|
| `info(DomainInfoRequest)` | `getDomainInfo` | `DomainInfoRequest` |
| `check(DomainCheckRequest)` | `checkDomains` | `DomainCheckRequest` |
| `create(DomainCreateRequest)` | `createDomain` | `DomainCreateRequest` |
| `update(DomainUpdateRequest)` | `updateDomain` | `DomainUpdateRequest` |
| `renew(DomainRenewRequest)` | `renewDomain` | `DomainRenewRequest` |
| `transfer(DomainTransferRequest)` | `transferDomain` | `DomainTransferRequest` |
| `delete(DomainDeleteRequest)` | `deleteDomain` | `DomainDeleteRequest` |

### `EppHostCommands` (RFC3732)

| New method | Old `EppClient` method | Request type |
|---|---|---|
| `info(HostInfoRequest)` | `getHostInfo` | `HostInfoRequest` |
| `check(HostCheckRequest)` | `checkHosts` | `HostCheckRequest` |
| `create(HostCreateRequest)` | `createHost` | `HostCreateRequest` |
| `update(HostUpdateRequest)` | `updateHost` | `HostUpdateRequest` |
| `delete(HostDeleteRequest)` | `deleteHost` | `HostDeleteRequest` |

### `EppContactCommands` (RFC3733)

| New method | Old `EppClient` method | Request type |
|---|---|---|
| `info(ContactInfoRequest)` | `getContactInfo` | `ContactInfoRequest` |
| `check(ContactCheckRequest)` | `checkContacts` | `ContactCheckRequest` |
| `create(ContactCreateRequest)` | `createContact` | `ContactCreateRequest` |
| `update(ContactUpdateRequest)` | `updateContact` | `ContactUpdateRequest` |

### `EppRegistrarCommands` (RFC3733)

| New method | Old `EppClient` method | Request type |
|---|---|---|
| `info(RegistrarInfoRequest)` | `getRegistrarInfo` | `RegistrarInfoRequest` |

Each method body is exactly `return sender.send(request);` — all four classes are a handful of
lines each plus Javadoc, no other logic. Each takes `@NonNull final XxxRequest` parameters,
matching the `@NonNull` convention already used on every `EppClient` command method today.

## Section 3: Behavior & Error Handling

No behavior changes anywhere. Every call still funnels through the same
`ensureConnected → gateway.sendCommand → retry-once-on-2201` path that lives in `EppClient` today.
`EppCommandSender` is a pure pass-through adapter — it does not add validation, error handling, or
any branch that didn't already exist in `EppClient.sendCommandRequest`.

## Section 4: Public API Breakage

This removes 20 public methods from `EppClient`
(`getDomainInfo`, `checkDomains`, `createDomain`, `updateDomain`, `renewDomain`, `transferDomain`,
`deleteDomain`, `getHostInfo`, `checkHosts`, `createHost`, `updateHost`, `deleteHost`,
`getContactInfo`, `getRegistrarInfo`, `checkContacts`, `createContact`, `updateContact`) and adds 4
(`domains`, `hosts`, `contacts`, `registrar`) plus 4 new public classes. `login`, `logout`, `hello`
are unaffected. Any host application calling the old flattened methods directly will fail to
compile against the new version — this is an intentional breaking change, to be called out in the
next release notes (no automated migration/deprecation shim is in scope; see Out of Scope).

## Section 5: Testing

All existing test classes that call the flattened methods must be updated to the new call shape.
Based on the current test suite (`EppClientTestSuite`'s `@SelectClasses` list):

- `DomainTests`, `HostTests`, `ContactTests`, `RegistrarTests` — update every call site
  (`eppClient.checkDomains(...)` → `eppClient.domains().check(...)`, etc.) per the mapping tables
  in Section 2.
- `SessionTests`, `RefreshEppTest` — exercise `login`/`logout`/`hello`/`setEppProps` only, which are
  unaffected; expected to need no changes beyond a compile check.
- `EppClientSessionTest` — its three `client.checkDomains(DomainCheckRequest...)` call sites (used
  as a generic "any business command" stand-in for the concurrency/reconnect tests, not because the
  tests care about domains specifically) become `client.domains().check(...)`. No change to the
  `FakeEppGateway` test double or its assertions (`loginAttempts`, `businessAttempts`) — those
  operate at the `EppGateway`/wire level, below where this refactor operates.

No new test classes are needed for `EppCommandSender` or the four command classes themselves, since
they contain no independent logic beyond delegation — their correctness is exercised end-to-end by
the existing per-domain test classes continuing to pass against the real sandbox.

## Section 6: Documentation

- `CLAUDE.md`'s Architecture section: update the `EppClient` bullet to describe the
  `domains()`/`hosts()`/`contacts()`/`registrar()` accessor shape instead of "one method per EPP
  command"; add a short bullet introducing the four new command classes and `EppCommandSender`.
- `README.adoc`: update the usage/installation snippet and any inline call examples to the new
  `eppClient.domains().check(...)` style.

---

## Files Changed

| File | Action |
|---|---|
| `src/main/java/gr/netmechanics/epp/client/EppClient.java` | Remove 20 flattened command methods; add `domains()`/`hosts()`/`contacts()`/`registrar()` accessors, 4 command-object fields built in the constructor, and `EppCommandSender` implementation |
| `src/main/java/gr/netmechanics/epp/client/EppCommandSender.java` | Create — package-private functional interface |
| `src/main/java/gr/netmechanics/epp/client/EppDomainCommands.java` | Create — 7 methods |
| `src/main/java/gr/netmechanics/epp/client/EppHostCommands.java` | Create — 5 methods |
| `src/main/java/gr/netmechanics/epp/client/EppContactCommands.java` | Create — 4 methods |
| `src/main/java/gr/netmechanics/epp/client/EppRegistrarCommands.java` | Create — 1 method |
| `src/test/java/gr/netmechanics/epp/client/DomainTests.java` | Update call sites to new shape |
| `src/test/java/gr/netmechanics/epp/client/HostTests.java` | Update call sites to new shape |
| `src/test/java/gr/netmechanics/epp/client/ContactTests.java` | Update call sites to new shape |
| `src/test/java/gr/netmechanics/epp/client/RegistrarTests.java` | Update call sites to new shape |
| `src/test/java/gr/netmechanics/epp/client/EppClientSessionTest.java` | Update 3 `checkDomains` call sites to `domains().check(...)` |
| `CLAUDE.md` | Update Architecture section |
| `README.adoc` | Update usage examples |

## Out of Scope

- Any deprecation shim / transitional flattened methods that delegate to the new classes — this is
  a clean breaking change, not a soft migration.
- Changing `EppCommandResponse` to a typed-per-command response shape.
- Turning the four command classes into independent Spring beans.
- Any change to `EppGateway`, `EppCommandRequest`, `EppCommandResponse`, `EppXmlCodec`, or the XML
  mapping layer.
- Re-evaluating or fixing any other Sonar/IDE-only advisory warning not related to this coupling
  rule.
