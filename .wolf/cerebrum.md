# Cerebrum

> OpenWolf's learning memory. Updated automatically as the AI learns from interactions.
> Do not edit manually unless correcting an error.
> Last updated: 2026-08-12

## User Preferences

<!-- How the user likes things done. Code style, tools, patterns, communication. -->

## Key Learnings

- **Project:** epp-client-gr
- [2026-08-13] `EppClient` uses a `ReentrantLock`-guarded connect sequence (no session-expiry
  timer). Reconnect-on-2201 dedup pattern: a `volatile long sessionGeneration` bumped only inside
  `login()` (always called under `connectionLock`, safe via reentrancy) lets a thread that loses
  the race into `reconnect()` detect another thread already fixed the session and skip
  tearing it down again — just compare `sessionGeneration` before/after acquiring the lock.
- [2026-08-13] To isolate one test class under this project's Gradle setup (see CLAUDE.md), the
  only way is to temporarily edit `build.gradle`'s `test { include '**/EppClientTestSuite.class' }`
  line to point at the target class, since `--tests` filters intersect with (don't replace) that
  include. Always restore it to `**/EppClientTestSuite.class` before finishing — verify via
  `git diff build.gradle`.
- [2026-08-13] `.superpowers/sdd/**` task/report files are gitignored (see
  `.superpowers/sdd/.gitignore`), so edits there never show in `git status`/`git diff` — that's
  expected, not a sign the edit failed.
- [2026-08-27] Response POJOs / nested XML classes MUST be Jackson-deserializable: give every
  class a no-arg constructor (`@NoArgsConstructor(access = AccessLevel.PRIVATE)`) and non-final
  fields. The `XmlMapper` (see `config/XmlMapperConfiguration`) sets `FIELD` visibility `ANY`, so
  no setters are needed, but a class whose only constructor takes args (esp. an arg whose type
  differs from the field, like an enum) leaves Jackson with no usable creator ->
  `InvalidDefinitionException` at unmarshal. `@JacksonXmlText` makes this stricter (text = property
  name `""`, must bind via creator if a parameterized creator is the sole creator). Working
  reference pattern: `SecDnsExtension.DsData`, `Bundle.BundleName`. A public arg constructor for
  request-building can coexist with the private no-arg one (as in `DomainExtension`).

## Do-Not-Repeat

<!-- Mistakes made and corrected. Each entry prevents the same mistake recurring. -->
<!-- Format: [YYYY-MM-DD] Description of what went wrong and what to do instead. -->

## Decision Log

<!-- Significant technical decisions with rationale. Why X was chosen over Y. -->
